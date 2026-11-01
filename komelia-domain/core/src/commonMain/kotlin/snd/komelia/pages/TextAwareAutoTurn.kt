package snd.komelia.pages

import kotlinx.serialization.Serializable

enum class OcrLanguage { JAPANESE, KOREAN, CHINESE, LATIN }

/** Auto page turn mode where the delay depends on the amount of text on the page (PAGES fork). */
@Serializable
data class TextAwareAutoTurnSettings(
    val enabled: Boolean = false,
    val perCharMs: Int = DEFAULT_PER_CHAR_MS,
    val minSeconds: Int = DEFAULT_MIN_SECONDS,
    val maxSeconds: Int = DEFAULT_MAX_SECONDS,
    val ocrLanguage: OcrLanguage = OcrLanguage.JAPANESE,
) {
    companion object {
        const val DEFAULT_PER_CHAR_MS = 100
        const val DEFAULT_MIN_SECONDS = 2
        const val DEFAULT_MAX_SECONDS = 30
    }
}

object TextAwareDelay {
    /**
     * delay = clamp(base + perChar * charCount, min, max), in milliseconds.
     * Returns the plain base delay when [charCount] is unknown (OCR unavailable or failed).
     */
    fun delayMs(baseSeconds: Int, charCount: Int?, settings: TextAwareAutoTurnSettings): Long {
        val baseMs = baseSeconds * 1000L
        if (charCount == null) return baseMs
        val minMs = settings.minSeconds * 1000L
        val maxMs = maxOf(minMs, settings.maxSeconds * 1000L)
        val raw = baseMs + settings.perCharMs.toLong() * charCount.coerceAtLeast(0)
        return raw.coerceIn(minMs, maxMs)
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
