package snd.komelia.adult

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import snd.komga.client.library.KomgaLibraryId

/**
 * Single source of truth for the lock of the restricted ("19+") library.
 * Feature is inactive while [libraryId] is null. State is never persisted: it is locked on every cold start.
 */
class AdultLockState(
    /** persisted id of the restricted library (null = feature off) */
    val libraryId: StateFlow<String?>,
) {
    private val mutableUnlocked = MutableStateFlow(false)
    val unlocked: StateFlow<Boolean> = mutableUnlocked.asStateFlow()

    /** id of the library that must be hidden right now, null when the feature is off or unlocked */
    val hiddenLibraryId: Flow<String?> = combine(libraryId, unlocked) { id, isUnlocked ->
        if (isUnlocked) null else id
    }.distinctUntilChanged()

    fun currentHiddenLibraryId(): String? = if (unlocked.value) null else libraryId.value

    fun isHidden(libraryId: KomgaLibraryId): Boolean {
        val hidden = currentHiddenLibraryId() ?: return false
        return hidden == libraryId.value
    }

    fun unlock() {
        mutableUnlocked.value = true
    }

    fun lock() {
        mutableUnlocked.value = false
    }

}
