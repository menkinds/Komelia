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

    @Test
    fun mixedCollectionWithLockedItemIsLocked() {
        state.registerVisibleScreen("collection", listOf("public", "adult", "public"))
        assertTrue(state.isViewingRestrictedContent())
        state.lockOnBackground()
        assertTrue(state.resumePending.value)
    }

    @Test
    fun readListWithOnlyNormalItemsIsPlainLock() {
        state.registerVisibleScreen("readList", listOf("public", "other"))
        assertFalse(state.isViewingRestrictedContent())
        state.lockOnBackground()
        assertFalse(state.resumePending.value)
    }

    @Test
    fun readListWithLockedItemIsLocked() {
        state.registerVisibleScreen("readList", listOf("public", "adult"))
        state.lockOnBackground()
        assertTrue(state.resumePending.value)
        assertTrue(state.consumeKeepScreens())
    }

    @Test
    fun decisionIsPerScreenNotPerCollection() {
        // book screen of a normal library opened from a mixed collection: collection screen is no longer composed
        state.registerVisibleScreen("book", listOf("public"))
        assertFalse(state.isViewingRestrictedContent())
        state.unregisterVisibleScreen("book")
        state.registerVisibleScreen("book", listOf("adult"))
        assertTrue(state.isViewingRestrictedContent())
    }

    @Test
    fun bookLoadingWithLibraryHintIsLocked() {
        // reader opened from a collection: library known from the opener while the book is loading
        val hint: String? = "adult"
        state.registerVisibleScreen("reader", hint)
        state.lockOnBackground()
        assertTrue(state.resumePending.value)
        state.unlockAfterResume()
        assertTrue(state.unlocked.value)
        assertFalse(state.resumePending.value)
    }

    @Test
    fun bookLoadingWithoutHintIsPlainLock() {
        state.registerVisibleScreen("reader", null as String?)
        state.lockOnBackground()
        assertFalse(state.resumePending.value)
    }

    @Test
    fun emptyListClearsRegistration() {
        state.registerVisibleScreen("collection", listOf("adult"))
        state.registerVisibleScreen("collection", emptyList<String>())
        assertFalse(state.isViewingRestrictedContent())
    }

    @Test
    fun lockOnBackgroundTwiceIsIdempotent() {
        // activity stop and process stop both call it
        state.registerVisibleScreen("reader", "adult")
        state.lockOnBackground()
        state.lockOnBackground()
        assertTrue(state.resumePending.value)
        assertFalse(state.unlocked.value)
        state.unlockAfterResume()
        assertTrue(state.unlocked.value)
        assertFalse(state.resumePending.value)
    }

    @Test
    fun pendingStaysUntilUnlockOrCancel() {
        // prompt errors do not touch the state: overlay stays, a later success unlocks
        state.registerVisibleScreen("reader", "adult")
        state.lockOnBackground()
        assertTrue(state.resumePending.value)
        assertTrue(state.consumeKeepScreens())
        assertTrue(state.resumePending.value)
        state.unlockAfterResume()
        assertTrue(state.unlocked.value)
    }
}
