package snd.komelia.ui.platform

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
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
    // process lifecycle changes to STOPPED only when no activity of the app is visible.
    // The biometric dialog is a fragment of the same activity, so it does not trigger it.
    DisposableEffect(lockState) {
        val lifecycle = ProcessLifecycleOwner.get().lifecycle
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) lockState.lockOnBackground()
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
}

private fun authenticate(
    activity: FragmentActivity,
    lockState: AdultLockState,
    resume: Boolean = false,
    onFailed: (errorCode: Int) -> Unit = {},
    onUnlocked: () -> Unit,
) {
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

    try {
        prompt.authenticate(promptInfo)
    } catch (e: IllegalArgumentException) {
        // unsupported authenticator combination: stay locked
        onFailed(BiometricPrompt.ERROR_HW_UNAVAILABLE)
    } catch (e: IllegalStateException) {
        // activity is not in a state that can show the prompt: stay locked
        onFailed(BiometricPrompt.ERROR_HW_UNAVAILABLE)
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

    // prompt is shown only while the activity is RESUMED; system cancel (app left again) keeps the overlay
    // and prompts again on the next resume, any other error leaves the restricted screens
    DisposableEffect(activity) {
        if (activity == null) {
            lockState.cancelResume()
            return@DisposableEffect onDispose {}
        }
        var promptShown = false
        fun showPrompt() {
            if (promptShown || !lockState.resumePending.value) return
            promptShown = true
            authenticate(
                activity = activity,
                lockState = lockState,
                resume = true,
                onFailed = { code ->
                    promptShown = false
                    if (code != BiometricPrompt.ERROR_CANCELED) lockState.cancelResume()
                },
            ) {}
        }

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) showPrompt()
        }
        activity.lifecycle.addObserver(observer)
        onDispose { activity.lifecycle.removeObserver(observer) }
    }

    // opaque: nothing of the screen below is visible before auth, and touches do not pass through
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) { awaitPointerEventScope { while (true) awaitPointerEvent().changes.forEach { it.consume() } } }
    )
}
