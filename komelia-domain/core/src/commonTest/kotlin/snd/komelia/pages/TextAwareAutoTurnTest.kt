package snd.komelia.pages

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TextAwareAutoTurnTest {
    private val s = TextAwareAutoTurnSettings(enabled = true)

    @Test
    fun unknownCountUsesBase() = assertEquals(10_000, TextAwareDelay.delayMs(10, null, null, s))

    @Test
    fun basePlusPerChar() = assertEquals(15_000, TextAwareDelay.delayMs(10, 50, null, s))

    @Test
    fun clampsToMax() = assertEquals(30_000, TextAwareDelay.delayMs(10, 1000, null, s))

    @Test
    fun clampsToMin() = assertEquals(2_000, TextAwareDelay.delayMs(1, 0, null, s.copy(perCharMs = 0)))

    @Test
    fun maxBelowMinUsesMin() =
        assertEquals(5_000, TextAwareDelay.delayMs(3, 100, null, s.copy(minSeconds = 5, maxSeconds = 4)))

    @Test
    fun negativeCountTreatedAsZero() = assertEquals(10_000, TextAwareDelay.delayMs(10, -5, null, s))

    @Test
    fun countIgnoresWhitespaceSymbolsAndShortBlocks() {
        val blocks = listOf(
            listOf(OcrLine("こんにちは", 0.9f), OcrLine("世 界!", 0.8f)), // 5 + 2
            listOf(OcrLine("！", 0.9f)),                                // symbol only
            listOf(OcrLine("ドン", 0f)),                                 // short sound effect
            listOf(OcrLine("ノイズです", 0.1f)),                          // low confidence
        )
        assertEquals(7, OcrTextCounter.count(blocks))
    }

    @Test
    fun lruEvictsOldest() {
        val lru = SimpleLru<Int, Int>(2)
        lru[1] = 1; lru[2] = 2
        lru[1]
        lru[3] = 3
        assertNull(lru[2])
        assertEquals(1, lru[1])
        assertEquals(2, lru.size)
    }

    @Test
    fun inkOnly() = assertEquals(16_000, TextAwareDelay.delayMs(10, null, 20, s.copy(perInkPercentMs = 300)))

    @Test
    fun inkLongerThanChars() = assertEquals(16_000, TextAwareDelay.delayMs(10, 10, 20, s.copy(perInkPercentMs = 300)))

    @Test
    fun charsLongerThanInk() = assertEquals(20_000, TextAwareDelay.delayMs(10, 100, 5, s.copy(perInkPercentMs = 300)))

    @Test
    fun inkPercentGrayscale() = assertEquals(
        50,
        InkRatio.percent(byteArrayOf(0, 10, 200.toByte(), 255.toByte()), 1)
    )

    @Test
    fun inkPercentRgbaIgnoresAlpha() = assertEquals(
        50,
        InkRatio.percent(byteArrayOf(0, 0, 0, 0, 255.toByte(), 255.toByte(), 255.toByte(), 0), 4)
    )

    @Test
    fun inkPercentEmpty() = assertEquals(0, InkRatio.percent(ByteArray(0), 1))
}
