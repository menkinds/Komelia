package snd.komelia.ui.platform

import androidx.compose.runtime.Composable
import snd.komelia.adult.AdultLockState

actual val adultLockSupported: Boolean = false

@Composable
actual fun rememberAdultUnlockRequest(lockState: AdultLockState, onUnlocked: () -> Unit): () -> Unit = {}

@Composable
actual fun AdultRelockEffect(lockState: AdultLockState) = Unit
