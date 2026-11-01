package snd.komelia.ui.pages

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.moduleinstall.InstallStatusListener
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate.InstallState
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import snd.komelia.image.ImageFormat
import snd.komelia.image.KomeliaImage
import snd.komelia.pages.OcrLanguage
import snd.komelia.pages.OcrLine
import snd.komelia.pages.OcrTextCounter
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max
import kotlin.math.roundToInt

private val logger = KotlinLogging.logger {}
private const val MAX_LONG_SIDE = 2048

/**
 * ML Kit text recognition v2 through the unbundled Google Play services artifacts.
 * Models are downloaded by Play services on demand (ModuleInstallClient); nothing is bundled in the APK
 * and no image data leaves the device.
 */
class AndroidPageTextRecognizer private constructor(context: Context) : PageTextRecognizer {
    private val appContext = context.applicationContext
    private val recognizers = ConcurrentHashMap<OcrLanguage, TextRecognizer>()

    override val supported = true
    override val servicesAvailable = MutableStateFlow(checkServices())
    override val modelStates = MutableStateFlow(
        OcrLanguage.entries.associateWith { OcrModelStatus(OcrModelState.UNKNOWN) }
    )

    private fun checkServices() = runCatching {
        GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(appContext) == ConnectionResult.SUCCESS
    }.getOrDefault(false)

    private fun recognizer(language: OcrLanguage): TextRecognizer = recognizers.getOrPut(language) {
        when (language) {
            OcrLanguage.JAPANESE -> TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
            OcrLanguage.KOREAN -> TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
            OcrLanguage.CHINESE -> TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
            OcrLanguage.LATIN -> TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        }
    }

    private fun setStatus(language: OcrLanguage, status: OcrModelStatus) {
        modelStates.update { it + (language to status) }
    }

    override fun refresh() {
        servicesAvailable.value = checkServices()
        if (!servicesAvailable.value) {
            OcrLanguage.entries.forEach { setStatus(it, OcrModelStatus(OcrModelState.UNAVAILABLE)) }
            return
        }
        val client = ModuleInstall.getClient(appContext)
        OcrLanguage.entries.forEach { language ->
            // keep an ongoing download state
            if (modelStates.value[language]?.state == OcrModelState.DOWNLOADING) return@forEach
            client.areModulesAvailable(recognizer(language))
                .addOnSuccessListener {
                    if (modelStates.value[language]?.state != OcrModelState.DOWNLOADING) {
                        setStatus(
                            language,
                            OcrModelStatus(if (it.areModulesAvailable()) OcrModelState.INSTALLED else OcrModelState.MISSING)
                        )
                    }
                }
                .addOnFailureListener {
                    logger.warn(it) { "OCR module availability check failed for $language" }
                    setStatus(language, OcrModelStatus(OcrModelState.UNAVAILABLE))
                }
        }
    }

    override fun download(language: OcrLanguage) {
        if (!checkServices()) {
            refresh()
            return
        }
        if (modelStates.value[language]?.state == OcrModelState.DOWNLOADING) return
        setStatus(language, OcrModelStatus(OcrModelState.DOWNLOADING, 0))
        val client = ModuleInstall.getClient(appContext)
        val listener = object : InstallStatusListener {
            override fun onInstallStatusUpdated(update: ModuleInstallStatusUpdate) {
                val info = update.progressInfo
                when (update.installState) {
                    InstallState.STATE_COMPLETED -> {
                        setStatus(language, OcrModelStatus(OcrModelState.INSTALLED))
                        client.unregisterListener(this)
                    }

                    InstallState.STATE_FAILED, InstallState.STATE_CANCELED -> {
                        setStatus(language, OcrModelStatus(OcrModelState.MISSING))
                        client.unregisterListener(this)
                    }

                    else -> {
                        val percent = info?.takeIf { it.totalBytesToDownload > 0 }
                            ?.let { (it.bytesDownloaded * 100 / it.totalBytesToDownload).toInt() }
                        setStatus(language, OcrModelStatus(OcrModelState.DOWNLOADING, percent))
                    }
                }
            }
        }
        val request = ModuleInstallRequest.newBuilder()
            .addApi(recognizer(language))
            .setListener(listener)
            .build()
        client.installModules(request)
            .addOnSuccessListener {
                if (it.areModulesAlreadyInstalled()) setStatus(language, OcrModelStatus(OcrModelState.INSTALLED))
            }
            .addOnFailureListener {
                logger.warn(it) { "OCR module install failed for $language" }
                setStatus(language, OcrModelStatus(OcrModelState.MISSING))
            }
    }

    override suspend fun countChars(language: OcrLanguage, image: KomeliaImage): Int? {
        if (!isReady(language)) return null
        val bitmap = withContext(Dispatchers.Default) { toSoftwareBitmap(image) }
        try {
            val text = recognize(recognizer(language), bitmap)
            val blocks = text.textBlocks.map { block -> block.lines.map { OcrLine(it.text, it.confidence) } }
            return OcrTextCounter.count(blocks)
        } finally {
            bitmap.recycle()
        }
    }

    private suspend fun recognize(recognizer: TextRecognizer, bitmap: Bitmap): Text =
        suspendCancellableCoroutine { cont ->
            recognizer.process(InputImage.fromBitmap(bitmap, 0))
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }

    /** Decoded page downscaled so the long side is at most [MAX_LONG_SIDE] px, as an ARGB_8888 bitmap. */
    private suspend fun toSoftwareBitmap(image: KomeliaImage): Bitmap {
        val longSide = max(image.width, image.height)
        val scaled = if (longSide > MAX_LONG_SIDE) {
            val factor = MAX_LONG_SIDE.toDouble() / longSide
            image.resize(
                max(1, (image.width * factor).roundToInt()),
                max(1, (image.height * factor).roundToInt()),
            )
        } else image
        try {
            val bytes = scaled.getBytes()
            val width = scaled.width
            val height = scaled.height
            val pixels = IntArray(width * height)
            when (scaled.type) {
                ImageFormat.RGBA_8888 -> for (i in pixels.indices) {
                    val o = i * 4
                    pixels[i] = (0xFF shl 24) or
                            ((bytes[o].toInt() and 0xFF) shl 16) or
                            ((bytes[o + 1].toInt() and 0xFF) shl 8) or
                            (bytes[o + 2].toInt() and 0xFF)
                }

                else -> for (i in pixels.indices) {
                    val g = bytes[i].toInt() and 0xFF
                    pixels[i] = (0xFF shl 24) or (g shl 16) or (g shl 8) or g
                }
            }
            return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
        } finally {
            if (scaled !== image) scaled.close()
        }
    }

    companion object {
        @Volatile
        private var instance: AndroidPageTextRecognizer? = null

        fun get(context: Context): AndroidPageTextRecognizer =
            instance ?: synchronized(this) {
                instance ?: AndroidPageTextRecognizer(context).also { instance = it }
            }
    }
}

@Composable
actual fun rememberPageTextRecognizer(): PageTextRecognizer {
    val context = LocalContext.current.applicationContext
    return remember(context) { AndroidPageTextRecognizer.get(context) }
}
