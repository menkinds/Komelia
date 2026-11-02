package snd.komelia.ui.platform

import androidx.compose.runtime.Composable
import snd.komelia.adult.AdultLockState

/** False on platforms that cannot unlock. There the lock selection is not offered. */
expect val adultLockSupported: Boolean

/**
 * Returns an action that asks the user to authenticate and, on success, unlocks [lockState]
 * and calls [onUnlocked].
 * Failure or cancel keeps the lock without any message. On platforms without support the action does nothing.
 */
@Composable
expect fun rememberAdultUnlockRequest(lockState: AdultLockState, onUnlocked: () -> Unit): () -> Unit

/** Locks [lockState] when the whole app goes to background. Does nothing on platforms without support. */
@Composable
expect fun AdultRelockEffect(lockState: AdultLockState)

/**
 * Opaque overlay with an auth prompt while [AdultLockState.resumePending]. Success removes it and keeps the
 * screens below as they were, cancel/failure leaves the restricted screens. Does nothing without support.
 */
@Composable
expect fun AdultResumeOverlay(lockState: AdultLockState)

/**
 * While a screen with restricted content is open ([AdultLockState.viewingRestrictedContent]) the app switcher
 * (recents) preview must be blank. Restores the normal preview otherwise. Does nothing without support.
 */
@Composable
expect fun AdultRecentsPreviewEffect(lockState: AdultLockState)
