package snd.komelia.updates

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

private const val komeliaBaseUrl = "https://api.github.com/repos/$FORK_RELEASES_REPO"
private const val onnxRuntimeBaseUrl = "https://api.github.com/repos/microsoft/onnxruntime"

class UpdateClient(
    private val ktor: HttpClient,
    private val ktorWithoutCache: HttpClient
) {

    suspend fun getKomeliaReleases(): List<GithubRelease> {
        val releases: List<GithubRelease> = getChecked("$komeliaBaseUrl/releases") {
            parameter("per_page", 10)
        }
        return releases.filter { !it.draft && !it.prerelease }
    }

    suspend fun getKomeliaLatestRelease(): GithubRelease {
        return getChecked("$komeliaBaseUrl/releases/latest")
    }

    suspend fun getOnnxRuntimeRelease(tagName: String): GithubRelease {
        return ktor.get("$onnxRuntimeBaseUrl/releases/tags/$tagName").body()
    }

    suspend fun getText(url: String): String {
        val response = ktorWithoutCache.get(url)
        checkStatus(response)
        return response.bodyAsText()
    }

    private suspend inline fun <reified T> getChecked(
        url: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ): T {
        val response = ktor.get(url, block)
        checkStatus(response)
        return response.body()
    }

    private fun checkStatus(response: HttpResponse) {
        val status = response.status.value
        if (status in 200..299) return
        val rateLimited = status == 429 || (status == 403 && response.headers["X-RateLimit-Remaining"] == "0")
        throw UpdateCheckException(
            if (rateLimited) "GitHub rate limit reached, try again later" else "Update server returned HTTP $status"
        )
    }

    suspend fun streamFile(url: String, block: suspend (response: HttpResponse) -> Unit) {
        ktorWithoutCache.prepareGet(url).execute(block)
    }
}

@Serializable
data class GithubRelease(
    val id: Int,
    @SerialName("published_at")
    val publishedAt: Instant,
    @SerialName("tag_name")
    val tagName: String,
    @SerialName("html_url")
    val htmlUrl: String,
    val body: String,
    val draft: Boolean = false,
    val prerelease: Boolean = false,
    val assets: List<GithubReleaseAsset>
)

@Serializable
data class GithubReleaseAsset(
    val id: Int,
    val name: String,
    @SerialName("content_type")
    val contentType: String,
    @SerialName("browser_download_url")
    val browserDownloadUrl: String
)