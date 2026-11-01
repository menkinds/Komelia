package snd.komelia.pages

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TextAwareAutoTurnTest {
    private val s = TextAwareAutoTurnSettings(enabled = true)

    @Test
    fun unknownCountUsesBase() = assertEquals(10_000, TextAwareDelay.delayMs(10, null, s))

    @Test
    fun basePlusPerChar() = assertEquals(15_000, TextAwareDelay.delayMs(10, 50, s))

    @Test
    fun clampsToMax() = assertEquals(30_000, TextAwareDelay.delayMs(10, 1000, s))

    @Test
    fun clampsToMin() = assertEquals(2_000, TextAwareDelay.delayMs(1, 0, s.copy(perCharMs = 0)))

    @Test
    fun maxBelowMinUsesMin() =
        assertEquals(5_000, TextAwareDelay.delayMs(3, 100, s.copy(minSeconds = 5, maxSeconds = 4)))

    @Test
    fun negativeCountTreatedAsZero() = assertEquals(10_000, TextAwareDelay.delayMs(10, -5, s))

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
}
