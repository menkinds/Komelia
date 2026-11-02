package snd.komelia.adult

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import snd.komga.client.library.KomgaLibraryId

/**
 * Single source of truth for the lock of the restricted ("19+") libraries.
 * Feature is inactive while [libraryIds] is empty. State is never persisted: it is locked on every cold start.
 */
class AdultLockState(
    /** persisted ids of the restricted libraries (empty = feature off) */
    val libraryIds: StateFlow<Set<String>>,
) {
    private val mutableUnlocked = MutableStateFlow(false)
    val unlocked: StateFlow<Boolean> = mutableUnlocked.asStateFlow()

    /** ids of the libraries that must be hidden right now, empty when the feature is off or unlocked */
    val hiddenLibraryIds: Flow<Set<String>> = combine(libraryIds, unlocked) { ids, isUnlocked ->
        if (isUnlocked) emptySet() else ids
    }.distinctUntilChanged()

    fun currentHiddenLibraryIds(): Set<String> = if (unlocked.value) emptySet() else libraryIds.value

    fun isHidden(libraryId: KomgaLibraryId): Boolean = libraryId.value in currentHiddenLibraryIds()

    // --- resume overlay: screens of a restricted library stay in place while the app is locked ---

    private val mutableResumePending = MutableStateFlow(false)

    /** true while the auth overlay covers a restricted screen that was kept after the app went to background */
    val resumePending: StateFlow<Boolean> = mutableResumePending.asStateFlow()

    private var restoredWithoutReset = false

    // screen token -> library ids of the content it shows (only screens that are composed right now)
    private val visibleLibraries = LinkedHashMap<Any, Set<String>>()

    private val visibleSnapshot = MutableStateFlow<List<Set<String>>>(emptyList())

    /** same rule as [isViewingRestrictedContent], observable (used to hide the app switcher preview) */
    val viewingRestrictedContent: Flow<Boolean> = combine(libraryIds, visibleSnapshot) { restricted, screens ->
        screens.any { ids -> ids.any { it in restricted } }
    }.distinctUntilChanged()

    fun registerVisibleScreen(token: Any, libraryId: String?) =
        registerVisibleScreen(token, listOfNotNull(libraryId))

    /** a screen can show content of several libraries (collection, read list); empty = nothing known yet */
    fun registerVisibleScreen(token: Any, libraryIds: Collection<String>) {
        if (libraryIds.isEmpty()) visibleLibraries.remove(token) else visibleLibraries[token] = libraryIds.toSet()
        visibleSnapshot.value = visibleLibraries.values.toList()
    }

    fun unregisterVisibleScreen(token: Any) {
        visibleLibraries.remove(token)
        visibleSnapshot.value = visibleLibraries.values.toList()
    }

    /** true if a screen that shows content of a restricted library is open */
    fun isViewingRestrictedContent(): Boolean {
        val restricted = libraryIds.value
        return visibleLibraries.values.any { ids -> ids.any { it in restricted } }
    }

    /**
     * App went to background. If a restricted screen is open the screens are kept and [resumePending] is set,
     * otherwise this is a plain [lock]. Never leaves a grace period: the API filter applies immediately.
     */
    fun lockOnBackground() {
        if (!unlocked.value) return
        if (isViewingRestrictedContent()) {
            mutableResumePending.value = true
            restoredWithoutReset = false
        }
        mutableUnlocked.value = false
    }

    /** auth succeeded for the overlay: unlock and keep the screens as they are */
    fun unlockAfterResume() {
        if (!mutableResumePending.value) {
            unlock()
            return
        }
        restoredWithoutReset = true
        mutableUnlocked.value = true
        mutableResumePending.value = false
    }

    /** auth cancelled or failed for the overlay: stay locked, the caller leaves the restricted screens */
    fun cancelResume() {
        mutableResumePending.value = false
        restoredWithoutReset = false
    }

    /** true once if the last visibility change must not reset the screens */
    fun consumeKeepScreens(): Boolean {
        if (mutableResumePending.value) return true
        val keep = restoredWithoutReset
        restoredWithoutReset = false
        return keep
    }

    fun unlock() {
        mutableUnlocked.value = true
    }

    fun lock() {
        mutableResumePending.value = false
        mutableUnlocked.value = false
    }

}
