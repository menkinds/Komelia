package snd.komelia.ui.pages

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import snd.komelia.image.KomeliaImage
import snd.komelia.pages.OcrLanguage

enum class OcrModelState { UNKNOWN, MISSING, DOWNLOADING, INSTALLED, UNAVAILABLE }

data class OcrModelStatus(val state: OcrModelState, val progressPercent: Int? = null)

/** On-device page text recognition. Images never leave the device. */
interface PageTextRecognizer {
    /** false on platforms without an OCR implementation; the auto turn delay is then the base interval */
    val supported: Boolean

    /** false if Google Play services is missing (Android) */
    val servicesAvailable: StateFlow<Boolean>
    val modelStates: StateFlow<Map<OcrLanguage, OcrModelStatus>>

    fun refresh()
    fun download(language: OcrLanguage)

    /** Count of text characters on the page, or null if OCR is not possible or failed. [image] is not closed. */
    suspend fun countChars(language: OcrLanguage, image: KomeliaImage): Int?
}

fun PageTextRecognizer.isReady(language: OcrLanguage): Boolean =
    supported && servicesAvailable.value && modelStates.value[language]?.state == OcrModelState.INSTALLED

object NoPageTextRecognizer : PageTextRecognizer {
    override val supported = false
    override val servicesAvailable: StateFlow<Boolean> = MutableStateFlow(false)
    override val modelStates: StateFlow<Map<OcrLanguage, OcrModelStatus>> = MutableStateFlow(emptyMap())
    override fun refresh() = Unit
    override fun download(language: OcrLanguage) = Unit
    override suspend fun countChars(language: OcrLanguage, image: KomeliaImage): Int? = null
}

@Composable
expect fun rememberPageTextRecognizer(): PageTextRecognizer
