package snd.komelia.pages

import kotlinx.serialization.Serializable

enum class OcrLanguage { JAPANESE, KOREAN, CHINESE, LATIN }

/** What the page delay is measured by: recognized characters (OCR) or the share of dark pixels. */
enum class AutoTurnMeasure { CHARS, INK }

/** Auto page turn mode where the delay depends on the amount of text on the page (PAGES fork). */
@Serializable
data class TextAwareAutoTurnSettings(
    val enabled: Boolean = false,
    val perCharMs: Int = DEFAULT_PER_CHAR_MS,
    val minSeconds: Int = DEFAULT_MIN_SECONDS,
    val maxSeconds: Int = DEFAULT_MAX_SECONDS,
    val ocrLanguage: OcrLanguage = OcrLanguage.JAPANESE,
    val measure: AutoTurnMeasure = AutoTurnMeasure.CHARS,
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
     * delay = clamp(base + perUnit * amount, min, max), in milliseconds.
     * [amount] is the character count ([AutoTurnMeasure.CHARS]) or the dark pixel percent ([AutoTurnMeasure.INK]).
     * Returns the plain base delay when [amount] is unknown (OCR unavailable or failed).
     */
    fun delayMs(baseSeconds: Int, amount: Int?, settings: TextAwareAutoTurnSettings): Long {
        val baseMs = baseSeconds * 1000L
        if (amount == null) return baseMs
        val minMs = settings.minSeconds * 1000L
        val maxMs = maxOf(minMs, settings.maxSeconds * 1000L)
        val perUnit = when (settings.measure) {
            AutoTurnMeasure.CHARS -> settings.perCharMs
            AutoTurnMeasure.INK -> settings.perInkPercentMs
        }
        val raw = baseMs + perUnit.toLong() * amount.coerceAtLeast(0)
        return raw.coerceIn(minMs, maxMs)
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
            val o = i * bands
            val lum = if (bands >= 3) {
                ((pixels[o].toInt() and 0xFF) + (pixels[o + 1].toInt() and 0xFF) + (pixels[o + 2].toInt() and 0xFF)) / 3
            } else pixels[o].toInt() and 0xFF
            if (lum < DARK_THRESHOLD) dark++
        }
        return (dark * 100 + total / 2) / total
    }
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
