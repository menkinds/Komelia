package snd.komelia.ui.pages

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import snd.komelia.image.BookImageLoader
import snd.komelia.image.KomeliaImage
import snd.komelia.pages.AutoTurnMeasure
import snd.komelia.pages.EdgeRatio
import snd.komelia.pages.InkRatio
import snd.komelia.pages.OcrLanguage
import snd.komelia.pages.SimpleLru
import snd.komelia.pages.TextAwareAutoTurnSettings
import snd.komelia.pages.TextAwareDelay
import snd.komelia.settings.ImageReaderSettingsRepository
import snd.komelia.ui.reader.image.PageMetadata
import kotlin.time.TimeSource

/** Measured amounts of the shown spread and the resulting delay; null values were not measured in time. */
data class AutoTurnMeasurement(
    val linePercent: Int?,
    val darkPercent: Int?,
    val charCount: Int?,
    val delayMs: Long,
)

/** Settings and per-page character count cache for the text-aware auto page turn. */
class TextAwareAutoTurnState(
    private val repository: ImageReaderSettingsRepository,
    private val imageLoader: BookImageLoader,
    private val scope: CoroutineScope,
) {
    val settings = MutableStateFlow(TextAwareAutoTurnSettings())

    /** Shows [lastMeasurement] on the reader to tune the per-unit times. Not saved. */
    val showMeasurements = MutableStateFlow(false)
    val lastMeasurement = MutableStateFlow<AutoTurnMeasurement?>(null)

    private val ocrScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val ocrMutex = Mutex()

    // only accessed from the UI coroutine that starts the auto turn timer
    private val cache = SimpleLru<String, Deferred<IntArray?>>(50)

    suspend fun load() {
        settings.value = repository.getTextAwareAutoTurn().first().let {
            it.copy(
                perCharMs = it.perCharMs.coerceIn(0, 2000),
                perInkPercentMs = it.perInkPercentMs.coerceIn(0, 2000),
                minSeconds = it.minSeconds.coerceIn(1, 60),
                maxSeconds = it.maxSeconds.coerceIn(1, 120),
            )
        }
    }

    fun update(transform: (TextAwareAutoTurnSettings) -> TextAwareAutoTurnSettings) {
        val new = transform(settings.value)
        settings.value = new
        scope.launch { repository.putTextAwareAutoTurn(new) }
    }

    /**
     * Waits for the delay of the page(s) shown in [pages].
     * The timer starts with the base interval. The drawing (dark pixel) amount is measured first, then the
     * character count when OCR is ready; the longer of the two times wins. Measures that do not arrive
     * before the base interval ends are skipped, and with none the base interval is used.
     * [nextPages] are measured in the background for the following turn.
     */
    suspend fun awaitTurnDelay(
        recognizer: PageTextRecognizer,
        baseSeconds: Int,
        pages: List<PageMetadata>,
        nextPages: List<PageMetadata>?,
    ) {
        val s = settings.value
        val baseMs = baseSeconds * 1000L
        if (!s.enabled || pages.isEmpty()) {
            delay(baseMs)
            return
        }
        val ocr = recognizer.isReady(s.ocrLanguage)
        val ink = spreadAmount(recognizer, s, AutoTurnMeasure.INK, pages)
        val chars = if (ocr) spreadAmount(recognizer, s, AutoTurnMeasure.CHARS, pages) else null
        if (!nextPages.isNullOrEmpty()) {
            spreadAmount(recognizer, s, AutoTurnMeasure.INK, nextPages)
            if (ocr) spreadAmount(recognizer, s, AutoTurnMeasure.CHARS, nextPages)
        }

        val mark = TimeSource.Monotonic.markNow()
        fun left() = (baseMs - mark.elapsedNow().inWholeMilliseconds).coerceAtLeast(0)
        lastMeasurement.value = null
        val drawing = withTimeoutOrNull(left()) { ink.await() }
        val charCount = chars?.let { c -> withTimeoutOrNull(left()) { c.await() } }?.get(0)
        val linePercent = drawing?.get(0)
        val targetMs = TextAwareDelay.delayMs(baseSeconds, charCount, linePercent, s)
        lastMeasurement.value = AutoTurnMeasurement(linePercent, drawing?.get(1), charCount, targetMs)
        val remaining = targetMs - mark.elapsedNow().inWholeMilliseconds
        if (remaining > 0) delay(remaining)
    }

    private fun spreadAmount(
        recognizer: PageTextRecognizer,
        settings: TextAwareAutoTurnSettings,
        measure: AutoTurnMeasure,
        pages: List<PageMetadata>,
    ): Deferred<IntArray?> {
        val perPage = pages.map { pageAmount(recognizer, settings.ocrLanguage, measure, it) }
        return ocrScope.async {
            val counts = perPage.awaitAll()
            if (counts.any { it == null }) null
            else IntArray(counts.first()!!.size) { i -> counts.sumOf { it!![i] } }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun pageAmount(
        recognizer: PageTextRecognizer,
        language: OcrLanguage,
        measure: AutoTurnMeasure,
        page: PageMetadata,
    ): Deferred<IntArray?> {
        val unit = if (measure == AutoTurnMeasure.INK) "DRAWING" else language.name
        val key = "${page.bookId.value}:${page.pageNumber}:$unit"
        cache[key]?.let { existing ->
            val failed = existing.isCancelled || (existing.isCompleted && existing.getCompleted() == null)
            if (!failed) return existing
        }
        val deferred = ocrScope.async {
            try {
                ocrMutex.withLock {
                    val image = imageLoader.loadImage(page.bookId, page.pageNumber).image ?: return@withLock null
                    image.use {
                        when (measure) {
                            AutoTurnMeasure.CHARS -> recognizer.countChars(language, it)?.let { n -> intArrayOf(n) }
                            AutoTurnMeasure.INK -> drawingAmount(it)
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                null
            }
        }
        cache[key] = deferred
        return deferred
    }

    /** [line percent, dark percent] of the page shrunk to [INK_SAMPLE_WIDTH] pixels wide. */
    private suspend fun drawingAmount(image: KomeliaImage): IntArray {
        val width = minOf(INK_SAMPLE_WIDTH, image.width).coerceAtLeast(1)
        val height = (image.height.toLong() * width / image.width.coerceAtLeast(1)).toInt().coerceAtLeast(1)
        return image.resize(width, height).use { small ->
            val bytes = small.getBytes()
            intArrayOf(EdgeRatio.percent(bytes, small.bands, small.width), InkRatio.percent(bytes, small.bands))
        }
    }

    private companion object {
        const val INK_SAMPLE_WIDTH = 400
    }
}
