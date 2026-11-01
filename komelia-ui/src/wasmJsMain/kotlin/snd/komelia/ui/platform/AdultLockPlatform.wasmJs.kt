package snd.komelia.ui.platform

import androidx.compose.runtime.Composable
import snd.komelia.adult.AdultLockState

@Composable
actual fun rememberAdultUnlockRequest(lockState: AdultLockState): () -> Unit = {}

@Composable
actual fun AdultRelockEffect(lockState: AdultLockState) = Unit
