package snd.komelia.ui.platform

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import snd.komelia.adult.AdultLockState

@Composable
actual fun rememberAdultUnlockRequest(lockState: AdultLockState): () -> Unit {
    val context = LocalContext.current
    return remember(context, lockState) {
        val activity = context.findFragmentActivity()
        val request: () -> Unit = { if (activity != null) authenticate(activity, lockState) }
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
            if (event == Lifecycle.Event.ON_STOP) lockState.lock()
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
}

private fun authenticate(activity: FragmentActivity, lockState: AdultLockState) {
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
                lockState.unlock()
            }

            // error or cancel: stay locked, no message
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) = Unit
        }
    )

    try {
        prompt.authenticate(promptInfo)
    } catch (e: IllegalArgumentException) {
        // unsupported authenticator combination: stay locked
    } catch (e: IllegalStateException) {
        // activity is not in a state that can show the prompt: stay locked
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
