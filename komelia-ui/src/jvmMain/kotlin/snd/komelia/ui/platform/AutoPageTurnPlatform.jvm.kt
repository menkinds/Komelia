package snd.komelia.ui.platform

import androidx.compose.runtime.Composable

@Composable
actual fun KeepScreenOnEffect(enabled: Boolean) = Unit

@Composable
actual fun AppBackgroundEffect(onBackground: () -> Unit) = Unit
