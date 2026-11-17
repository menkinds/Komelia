package snd.komelia.settings


interface SecretsRepository {

    suspend fun getCookie(url: String): String?

    suspend fun setCookie(url: String, cookie: String)

    suspend fun deleteCookie(url: String)

    suspend fun getApiKey(url: String): String?
    suspend fun setApiKey(url: String, apiKey: String)
    suspend fun deleteApiKey(url: String)

    // Cloudflare Access service token (client id to secret); only Android stores it
    suspend fun getCfAccessToken(url: String): Pair<String, String>? = null
    suspend fun setCfAccessToken(url: String, clientId: String, clientSecret: String) {}

}