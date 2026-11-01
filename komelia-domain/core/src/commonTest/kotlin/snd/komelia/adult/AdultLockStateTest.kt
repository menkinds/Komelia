package snd.komelia.adult

import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdultLockStateTest {
    private val ids = MutableStateFlow(setOf("adult"))
    private val state = AdultLockState(ids).also { it.unlock() }

    @Test
    fun backgroundOnRestrictedScreenKeepsScreensAndLocksApi() {
        state.registerVisibleScreen("reader", "adult")
        state.lockOnBackground()

        assertTrue(state.resumePending.value)
        assertFalse(state.unlocked.value)
        assertEquals(setOf("adult"), state.currentHiddenLibraryIds())
        assertTrue(state.consumeKeepScreens())
    }

    @Test
    fun backgroundOnOtherScreenIsPlainLock() {
        state.registerVisibleScreen("series", "public")
        state.lockOnBackground()

        assertFalse(state.resumePending.value)
        assertFalse(state.unlocked.value)
        assertFalse(state.consumeKeepScreens())
    }

    @Test
    fun noScreenRegisteredIsPlainLock() {
        state.lockOnBackground()
        assertFalse(state.resumePending.value)
        assertFalse(state.unlocked.value)
    }

    @Test
    fun unregisteredScreenIsNotCounted() {
        state.registerVisibleScreen("book", "adult")
        state.unregisterVisibleScreen("book")
        state.lockOnBackground()
        assertFalse(state.resumePending.value)
    }

    @Test
    fun unlockAfterResumeKeepsScreensOnce() {
        state.registerVisibleScreen("reader", "adult")
        state.lockOnBackground()
        state.unlockAfterResume()

        assertTrue(state.unlocked.value)
        assertFalse(state.resumePending.value)
        assertTrue(state.consumeKeepScreens())
        assertFalse(state.consumeKeepScreens())
    }

    @Test
    fun cancelResumeStaysLockedAndResetsScreens() {
        state.registerVisibleScreen("reader", "adult")
        state.lockOnBackground()
        state.cancelResume()

        assertFalse(state.unlocked.value)
        assertFalse(state.resumePending.value)
        assertFalse(state.consumeKeepScreens())
    }

    @Test
    fun lockOnBackgroundWhileAlreadyLockedChangesNothing() {
        state.lock()
        state.registerVisibleScreen("reader", "adult")
        state.lockOnBackground()
        assertFalse(state.resumePending.value)
    }

    @Test
    fun secondBackgroundWhilePendingKeepsPending() {
        state.registerVisibleScreen("reader", "adult")
        state.lockOnBackground()
        state.lockOnBackground()
        assertTrue(state.resumePending.value)
    }

    @Test
    fun plainLockClearsPending() {
        state.registerVisibleScreen("reader", "adult")
        state.lockOnBackground()
        state.lock()
        assertFalse(state.resumePending.value)
    }

    @Test
    fun featureOffNeverPending() {
        ids.value = emptySet()
        state.registerVisibleScreen("reader", "adult")
        state.lockOnBackground()
        assertFalse(state.resumePending.value)
    }
}
