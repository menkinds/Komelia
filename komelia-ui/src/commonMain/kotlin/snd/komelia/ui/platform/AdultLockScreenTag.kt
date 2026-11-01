package snd.komelia.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import snd.komelia.adult.AdultLockState
import snd.komga.client.library.KomgaLibraryId

val LocalAdultLockState = compositionLocalOf<AdultLockState?> { null }

/**
 * Tells the lock that this screen currently shows content of [libraryId] (null = not known yet).
 * When the app goes to background while such a screen is open it is kept under an auth overlay.
 */
@Composable
fun AdultLockScreenTag(libraryId: KomgaLibraryId?) {
    val lockState = LocalAdultLockState.current ?: return
    val token = remember { Any() }
    val id = libraryId?.value
    DisposableEffect(lockState, token, id) {
        lockState.registerVisibleScreen(token, id)
        onDispose { lockState.unregisterVisibleScreen(token) }
    }
}
