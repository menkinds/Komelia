package snd.komelia.ui.platform

import androidx.compose.runtime.Composable

/**
 * Current local time as text (device 12/24h format), refreshed on each minute boundary.
 * Stops updating while the app is not visible. Empty string on platforms without support.
 */
@Composable
expect fun rememberClockText(): String
