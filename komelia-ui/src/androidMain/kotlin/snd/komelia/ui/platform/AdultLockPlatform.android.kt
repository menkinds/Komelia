package snd.komelia.ui.platform

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.view.WindowManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.Res
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.adult_lock_locked
import io.github.snd_r.komelia.ui.komelia_ui.generated.resources.adult_lock_unlock
import org.jetbrains.compose.resources.stringResource
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import snd.komelia.adult.AdultLockState

actual val adultLockSupported: Boolean = true

@Composable
actual fun rememberAdultUnlockRequest(lockState: AdultLockState, onUnlocked: () -> Unit): () -> Unit {
    val context = LocalContext.current
    val currentOnUnlocked by rememberUpdatedState(onUnlocked)
    return remember(context, lockState) {
        val activity = context.findFragmentActivity()
        val request: () -> Unit = {
            if (activity != null) authenticate(activity, lockState) { currentOnUnlocked() }
        }
        request
    }
}

@Composable
actual fun AdultRelockEffect(lockState: AdultLockState) {
    // process lifecycle changes to STOPPED only when no activity of the app is visible, and it comes late
    // (about 700 ms after the last activity stopped). The biometric dialog is a fragment of the same activity,
    // so it does not trigger it.
    val context = LocalContext.current
    DisposableEffect(lockState) {
        val lifecycle = ProcessLifecycleOwner.get().lifecycle
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) lockState.lockOnBackground()
        }
        lifecycle.addObserver(observer)

        // lock early on the activity stop too (idempotent), except for a configuration change (rotation)
        val activity = context.findFragmentActivity()
        val activityObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && activity?.isChangingConfigurations == false) {
                lockState.lockOnBackground()
            }
        }
        activity?.lifecycle?.addObserver(activityObserver)
        onDispose {
            lifecycle.removeObserver(observer)
            activity?.lifecycle?.removeObserver(activityObserver)
        }
    }
}

private fun authenticate(
    activity: FragmentActivity,
    lockState: AdultLockState,
    resume: Boolean = false,
    onFailed: (errorCode: Int) -> Unit = {},
    onUnlocked: () -> Unit,
): BiometricPrompt? {
    // BIOMETRIC_STRONG | DEVICE_CREDENTIAL is not supported by the library on API 28-29
    val authenticators =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        else BIOMETRIC_WEAK or DEVICE_CREDENTIAL

    // no negative button: it must not be set together with DEVICE_CREDENTIAL
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(activity.applicationInfo.loadLabel(activity.packageManager).toString())
        .setAllowedAuthenticators(authenticators)
        .build()

    val prompt = BiometricPrompt(
        activity,
        ContextCompat.getMainExecutor(activity),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                if (resume) lockState.unlockAfterResume() else lockState.unlock()
                onUnlocked()
            }

            // error or cancel: stay locked, no message
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) = onFailed(errorCode)
        }
    )

    return try {
        prompt.authenticate(promptInfo)
        prompt
    } catch (e: IllegalArgumentException) {
        // unsupported authenticator combination: stay locked
        onFailed(BiometricPrompt.ERROR_HW_UNAVAILABLE)
        null
    } catch (e: IllegalStateException) {
        // activity is not in a state that can show the prompt: stay locked
        onFailed(BiometricPrompt.ERROR_HW_UNAVAILABLE)
        null
    }
}

private fun Context.findFragmentActivity(): FragmentActivity? {
    var current: Context = this
    while (current is ContextWrapper) {
        if (current is FragmentActivity) return current
        current = current.baseContext
    }
    return null
}

@Composable
actual fun AdultResumeOverlay(lockState: AdultLockState) {
    val pending by lockState.resumePending.collectAsState()
    if (!pending) return

    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }

    // back press on the overlay counts as a cancel
    BackHandler { lockState.cancelResume() }

    // The prompt is started at once when the activity is already RESUMED and on every ON_RESUME. The active prompt
    // is tracked by instance (no stale flag): it is cancelled when the activity stops, so the next resume shows a
    // new one. Cancel, dismiss or any error keeps the overlay; the Unlock button starts the prompt again.
    val showPrompt = remember(activity) { mutableStateOf<() -> Unit>({}) }
    DisposableEffect(activity) {
        if (activity == null) {
            lockState.cancelResume()
            return@DisposableEffect onDispose {}
        }
        val handler = Handler(Looper.getMainLooper())
        var active: BiometricPrompt? = null
        var retried = false

        fun show() {
            if (!lockState.resumePending.value) return
            if (active != null) return
            if (!activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) return
            var started: BiometricPrompt? = null
            started = authenticate(
                activity = activity,
                lockState = lockState,
                resume = true,
                onFailed = { if (active === started) active = null },
            ) {}
            if (started != null) {
                active = started
                retried = false
            } else if (!retried) {
                // activity was not ready: retry once on the next frame, later on the next ON_RESUME or the button
                retried = true
                handler.post { show() }
            }
        }

        fun cancelActive() {
            val prompt = active ?: return
            active = null
            try {
                prompt.cancelAuthentication()
            } catch (e: IllegalStateException) {
                // nothing to cancel
            }
        }

        showPrompt.value = {
            // button: always opens a prompt again
            cancelActive()
            retried = false
            handler.post { show() }
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> show()
                // the biometric dialog does not stop the activity: a stopped activity means the prompt is gone
                Lifecycle.Event.ON_STOP -> cancelActive()
                else -> {}
            }
        }
        activity.lifecycle.addObserver(observer)
        show()
        onDispose {
            activity.lifecycle.removeObserver(observer)
            handler.removeCallbacksAndMessages(null)
            cancelActive()
        }
    }

    // opaque: nothing of the screen below is visible before auth, and touches do not pass through
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) { awaitPointerEventScope { while (true) awaitPointerEvent().changes.forEach { it.consume() } } },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(48.dp))
            Text(stringResource(Res.string.adult_lock_locked), style = MaterialTheme.typography.titleMedium)
            Button(onClick = { showPrompt.value() }) {
                Text(stringResource(Res.string.adult_lock_unlock))
            }
        }
    }
}

@Composable
actual fun AdultRecentsPreviewEffect(lockState: AdultLockState) {
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() } ?: return
    val hidden by lockState.viewingRestrictedContent.collectAsState(initial = lockState.isViewingRestrictedContent())

    // set while the content is on screen (not on pause/stop): the recents snapshot is taken after that
    DisposableEffect(activity, hidden) {
        setRecentsPreviewHidden(activity, hidden)
        // restore also on dispose; rotation recreates the activity and this effect sets the flag again
        onDispose { setRecentsPreviewHidden(activity, false) }
    }
}

private fun setRecentsPreviewHidden(activity: FragmentActivity, hidden: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // blank recents preview only, user screenshots still work
        activity.setRecentsScreenshotEnabled(!hidden)
    } else if (hidden) {
        // also blocks screenshots on these versions
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    } else {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}
