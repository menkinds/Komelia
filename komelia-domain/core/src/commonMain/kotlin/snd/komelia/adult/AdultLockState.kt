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

    fun unlock() {
        mutableUnlocked.value = true
    }

    fun lock() {
        mutableUnlocked.value = false
    }

}
