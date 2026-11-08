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
import snd.komelia.pages.InkRatio
import snd.komelia.pages.SimpleLru
import snd.komelia.pages.TextAwareAutoTurnSettings
import snd.komelia.pages.TextAwareDelay
import snd.komelia.settings.ImageReaderSettingsRepository
import snd.komelia.ui.reader.image.PageMetadata
import kotlin.time.TimeSource

/** Settings and per-page character count cache for the text-aware auto page turn. */
class TextAwareAutoTurnState(
    private val repository: ImageReaderSettingsRepository,
    private val imageLoader: BookImageLoader,
    private val scope: CoroutineScope,
) {
    val settings = MutableStateFlow(TextAwareAutoTurnSettings())

    private val ocrScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val ocrMutex = Mutex()

    // only accessed from the UI coroutine that starts the auto turn timer
    private val cache = SimpleLru<String, Deferred<Int?>>(50)

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
     * The timer starts with the base interval; if the character count arrives before it ends,
     * the remaining time is adjusted. OCR failure or unavailability means the base interval.
     * [nextPages] are recognized in the background for the following turn.
     */
    suspend fun awaitTurnDelay(
        recognizer: PageTextRecognizer,
        baseSeconds: Int,
        pages: List<PageMetadata>,
        nextPages: List<PageMetadata>?,
    ) {
        val s = settings.value
        val baseMs = baseSeconds * 1000L
        val ready = s.measure == AutoTurnMeasure.INK || recognizer.isReady(s.ocrLanguage)
        if (!s.enabled || pages.isEmpty() || !ready) {
            delay(baseMs)
            return
        }
        val current = spreadCount(recognizer, s, pages)
        if (!nextPages.isNullOrEmpty()) spreadCount(recognizer, s, nextPages)

        val mark = TimeSource.Monotonic.markNow()
        val count = withTimeoutOrNull(baseMs) { current.await() }
        val targetMs = TextAwareDelay.delayMs(baseSeconds, count, s)
        val remaining = targetMs - mark.elapsedNow().inWholeMilliseconds
        if (remaining > 0) delay(remaining)
    }

    private fun spreadCount(
        recognizer: PageTextRecognizer,
        settings: TextAwareAutoTurnSettings,
        pages: List<PageMetadata>,
    ): Deferred<Int?> {
        val perPage = pages.map { pageCount(recognizer, settings, it) }
        return ocrScope.async {
            val counts = perPage.awaitAll()
            if (counts.any { it == null }) null else counts.sumOf { it!! }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun pageCount(
        recognizer: PageTextRecognizer,
        settings: TextAwareAutoTurnSettings,
        page: PageMetadata,
    ): Deferred<Int?> {
        val language = settings.ocrLanguage
        val measure = settings.measure
        val unit = if (measure == AutoTurnMeasure.INK) "INK" else language.name
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
                            AutoTurnMeasure.CHARS -> recognizer.countChars(language, it)
                            AutoTurnMeasure.INK -> inkPercent(it)
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

    private suspend fun inkPercent(image: KomeliaImage): Int {
        val width = minOf(INK_SAMPLE_WIDTH, image.width).coerceAtLeast(1)
        val height = (image.height.toLong() * width / image.width.coerceAtLeast(1)).toInt().coerceAtLeast(1)
        return image.resize(width, height).use { small -> InkRatio.percent(small.getBytes(), small.bands) }
    }

    private companion object {
        const val INK_SAMPLE_WIDTH = 300
    }
}
