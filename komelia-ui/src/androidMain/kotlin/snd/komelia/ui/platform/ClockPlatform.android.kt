package snd.komelia.ui.platform

import android.content.Context
import android.content.ContextWrapper
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
actual fun rememberClockText(): String {
    val context = LocalContext.current
    val is24Hour = DateFormat.is24HourFormat(context)
    val formatter = remember(is24Hour) {
        SimpleDateFormat(if (is24Hour) "HH:mm" else "h:mm a", Locale.getDefault())
    }

    var lifecycleContext: Context = context
    while (lifecycleContext is ContextWrapper && lifecycleContext !is LifecycleOwner) {
        lifecycleContext = lifecycleContext.baseContext
    }
    val lifecycle = (lifecycleContext as? LifecycleOwner)?.lifecycle

    var visible by remember { mutableStateOf(lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) ?: true) }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) visible = true
            if (event == Lifecycle.Event.ON_STOP) visible = false
        }
        lifecycle?.addObserver(observer)
        onDispose { lifecycle?.removeObserver(observer) }
    }

    var text by remember { mutableStateOf(formatter.format(Date())) }
    LaunchedEffect(visible, formatter) {
        if (!visible) return@LaunchedEffect
        while (true) {
            val now = System.currentTimeMillis()
            text = formatter.format(Date(now))
            delay(60_000L - now % 60_000L)
        }
    }
    return text
}
