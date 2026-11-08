package snd.komelia.pages

import kotlinx.serialization.Serializable

enum class OcrLanguage { JAPANESE, KOREAN, CHINESE, LATIN }

/** What a page is measured by: recognized characters (OCR) or the share of dark pixels. */
enum class AutoTurnMeasure { CHARS, INK }

/** Auto page turn mode where the delay depends on the amount of text on the page (PAGES fork). */
@Serializable
data class TextAwareAutoTurnSettings(
    val enabled: Boolean = false,
    val perCharMs: Int = DEFAULT_PER_CHAR_MS,
    val minSeconds: Int = DEFAULT_MIN_SECONDS,
    val maxSeconds: Int = DEFAULT_MAX_SECONDS,
    val ocrLanguage: OcrLanguage = OcrLanguage.JAPANESE,
    val perInkPercentMs: Int = DEFAULT_PER_INK_PERCENT_MS,
) {
    companion object {
        const val DEFAULT_PER_CHAR_MS = 100
        const val DEFAULT_PER_INK_PERCENT_MS = 300
        const val DEFAULT_MIN_SECONDS = 2
        const val DEFAULT_MAX_SECONDS = 30
    }
}

object TextAwareDelay {
    /**
     * delay = clamp(base + max(perChar * charCount, perInkPercent * inkPercent), min, max), in milliseconds.
     * [inkPercent] is the drawing amount ([EdgeRatio], percent of line pixels).
     * The drawing based time is used unless the character based time is longer.
     * An unknown measure (null) is skipped; with both unknown the plain base delay is returned.
     */
    fun delayMs(baseSeconds: Int, charCount: Int?, inkPercent: Int?, settings: TextAwareAutoTurnSettings): Long {
        val baseMs = baseSeconds * 1000L
        if (charCount == null && inkPercent == null) return baseMs
        val minMs = settings.minSeconds * 1000L
        val maxMs = maxOf(minMs, settings.maxSeconds * 1000L)
        val charMs = charCount?.let { settings.perCharMs.toLong() * it.coerceAtLeast(0) } ?: 0L
        val inkMs = inkPercent?.let { settings.perInkPercentMs.toLong() * it.coerceAtLeast(0) } ?: 0L
        return (baseMs + maxOf(charMs, inkMs)).coerceIn(minMs, maxMs)
    }
}

/**
 * Percent (0..100) of line pixels: pixels whose brightness differs from the right or lower neighbour by at
 * least [EDGE_THRESHOLD]. Line art counts, flat black fills and white areas do not, so the value follows the
 * amount of drawing rather than how dark the page is. [width] is the image width in pixels.
 */
object EdgeRatio {
    const val EDGE_THRESHOLD = 48

    fun percent(pixels: ByteArray, bands: Int, width: Int): Int {
        if (bands <= 0 || width <= 0) return 0
        val total = pixels.size / bands
        val height = total / width
        if (height <= 0) return 0
        val lum = IntArray(width * height) { InkRatio.luminance(pixels, it * bands, bands) }
        var edges = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                val i = y * width + x
                val l = lum[i]
                val right = x + 1 < width && kotlin.math.abs(l - lum[i + 1]) >= EDGE_THRESHOLD
                val down = y + 1 < height && kotlin.math.abs(l - lum[i + width]) >= EDGE_THRESHOLD
                if (right || down) edges++
            }
        }
        val n = width * height
        return (edges * 100 + n / 2) / n
    }
}

object InkRatio {
    private const val DARK_THRESHOLD = 128

    /**
     * Percent (0..100) of pixels darker than mid gray in packed 8-bit pixel data with [bands] channels.
     * For 3+ bands the first three are averaged as RGB; a fourth (alpha) band is ignored.
     */
    fun percent(pixels: ByteArray, bands: Int): Int {
        if (bands <= 0 || pixels.size < bands) return 0
        val total = pixels.size / bands
        var dark = 0
        for (i in 0 until total) {
            if (luminance(pixels, i * bands, bands) < DARK_THRESHOLD) dark++
        }
        return (dark * 100 + total / 2) / total
    }

    /** 0..255 brightness of the pixel at byte offset [o]; RGB is averaged, a fourth (alpha) band is ignored. */
    fun luminance(pixels: ByteArray, o: Int, bands: Int): Int =
        if (bands >= 3) {
            ((pixels[o].toInt() and 0xFF) + (pixels[o + 1].toInt() and 0xFF) + (pixels[o + 2].toInt() and 0xFF)) / 3
        } else pixels[o].toInt() and 0xFF
}

/** One recognized text line with the recognizer confidence (0 = unknown). */
data class OcrLine(val text: String, val confidence: Float)

object OcrTextCounter {
    private const val MIN_CONFIDENCE = 0.4f
    private const val MIN_BLOCK_CHARS = 3

    /**
     * Counts letters and digits (non-whitespace, no punctuation or symbols) in recognized blocks.
     * Lines with known low confidence are dropped, and blocks with fewer than [MIN_BLOCK_CHARS]
     * characters (isolated symbols, short sound effects such as "ドン") are ignored.
     */
    fun count(blocks: List<List<OcrLine>>): Int = blocks.sumOf { block ->
        val n = block.sumOf { line ->
            if (line.confidence > 0f && line.confidence < MIN_CONFIDENCE) 0
            else line.text.count { it.isLetterOrDigit() }
        }
        if (n < MIN_BLOCK_CHARS) 0 else n
    }
}

/** Minimal LRU map. Not thread safe. */
class SimpleLru<K, V>(private val maxSize: Int) {
    private val map = LinkedHashMap<K, V>()

    operator fun get(key: K): V? {
        val value = map.remove(key) ?: return null
        map[key] = value
        return value
    }

    operator fun set(key: K, value: V) {
        map.remove(key)
        map[key] = value
        while (map.size > maxSize) map.remove(map.keys.first())
    }

    fun remove(key: K) {
        map.remove(key)
    }

    val size get() = map.size
}
