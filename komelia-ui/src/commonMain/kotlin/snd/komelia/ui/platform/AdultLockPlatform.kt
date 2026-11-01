package snd.komelia.ui.platform

import androidx.compose.runtime.Composable
import snd.komelia.adult.AdultLockState

/**
 * Returns an action that asks the user to authenticate and, on success, unlocks [lockState].
 * Failure or cancel keeps the lock without any message. On platforms without support the action does nothing.
 */
@Composable
expect fun rememberAdultUnlockRequest(lockState: AdultLockState): () -> Unit

/** Locks [lockState] when the whole app goes to background. Does nothing on platforms without support. */
@Composable
expect fun AdultRelockEffect(lockState: AdultLockState)
