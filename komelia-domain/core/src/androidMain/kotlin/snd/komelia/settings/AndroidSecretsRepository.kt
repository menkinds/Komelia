package snd.komelia.settings

import android.content.SharedPreferences
import androidx.core.content.edit
import io.ktor.http.Url

private const val cookieKey = "cookieKey"
private const val cfAccessHostKey = "cfAccessHost"
private const val cfAccessIdKey = "cfAccessClientId"
private const val cfAccessSecretKey = "cfAccessClientSecret"

class AndroidSecretsRepository(
    private val preferences: SharedPreferences
) : SecretsRepository {
    override suspend fun getCookie(url: String): String? {
        return preferences.getString(cookieKey, null)
    }

    override suspend fun setCookie(url: String, cookie: String) {
        preferences.edit { putString(cookieKey, cookie) }
    }

    override suspend fun deleteCookie(url: String) {
        preferences.edit { remove(cookieKey) }
    }

    override suspend fun getApiKey(url: String): String? {
        return null
    }

    override suspend fun setApiKey(url: String, apiKey: String) {
    }

    override suspend fun deleteApiKey(url: String) {
    }

    override suspend fun getCfAccessToken(url: String): Pair<String, String>? =
        runCatching { Url(url).host }.getOrNull()?.let { cfAccessToken(it) }

    override suspend fun setCfAccessToken(url: String, clientId: String, clientSecret: String) {
        val host = runCatching { Url(url).host }.getOrNull()
        preferences.edit {
            if (host == null || clientId.isBlank() || clientSecret.isBlank()) {
                remove(cfAccessHostKey); remove(cfAccessIdKey); remove(cfAccessSecretKey)
            } else {
                putString(cfAccessHostKey, host)
                putString(cfAccessIdKey, clientId.trim())
                putString(cfAccessSecretKey, clientSecret.trim())
            }
        }
    }

    // non-suspend for the OkHttp interceptor; only returns the token for the host it was saved for
    fun cfAccessToken(host: String): Pair<String, String>? {
        if (!host.equals(preferences.getString(cfAccessHostKey, null), ignoreCase = true)) return null
        val id = preferences.getString(cfAccessIdKey, null) ?: return null
        val secret = preferences.getString(cfAccessSecretKey, null) ?: return null
        return id to secret
    }
}
