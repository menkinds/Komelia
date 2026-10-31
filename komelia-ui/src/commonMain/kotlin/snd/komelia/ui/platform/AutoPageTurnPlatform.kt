package snd.komelia.ui.platform

import androidx.compose.runtime.Composable

/** Keeps the screen on while [enabled] is true. No-op on platforms without the concept. */
@Composable
expect fun KeepScreenOnEffect(enabled: Boolean)

/** Calls [onBackground] when the app goes to background. */
@Composable
expect fun AppBackgroundEffect(onBackground: () -> Unit)
