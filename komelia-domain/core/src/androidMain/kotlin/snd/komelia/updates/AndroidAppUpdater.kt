package snd.komelia.updates

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.STATUS_PENDING_USER_ACTION
import android.content.pm.PackageInstaller.SessionParams.MODE_FULL_INSTALL
import android.os.Build
import android.provider.Settings
import androidx.core.content.IntentCompat
import androidx.core.net.toUri
import io.ktor.client.statement.*
import io.ktor.http.isSuccess
import io.ktor.utils.io.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.io.readByteArray
import snd.komelia.AppNotification
import snd.komelia.AppNotifications
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicBoolean

class AndroidAppUpdater(
    private val githubClient: UpdateClient,
    private val context: Context,
    private val notifications: AppNotifications,
) : AppUpdater {
    private var inProgress = AtomicBoolean(false)
    private val downloadDir = File(context.cacheDir, "app-updates")

    override suspend fun getReleases(): List<AppRelease> {
        return githubClient.getKomeliaReleases().mapNotNull { it.toAppRelease() }
    }

    override suspend fun updateToLatest(): Flow<UpdateProgress>? {
        val latest = githubClient.getKomeliaLatestRelease().toAppRelease() ?: return null
        return updateTo(latest)
    }

    override fun updateTo(release: AppRelease): Flow<UpdateProgress>? {
        if (release.assetUrl == null) return null
        if (!context.packageManager.canRequestPackageInstalls()) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, "package:${context.packageName}".toUri())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            notifications.add(AppNotification.Normal("Allow \"install unknown apps\" for this app, then press Update again."))
            return null
        }
        if (!inProgress.compareAndSet(false, true)) return null

        return flow {
            try {
                emit(UpdateProgress(0, 0))
                val checksumUrl = release.checksumUrl
                    ?: throw UpdateCheckException("Release has no .sha256 file, update cancelled")
                val expected = parseSha256(githubClient.getText(checksumUrl))
                    ?: throw UpdateCheckException("Release checksum file is invalid, update cancelled")

                downloadDir.deleteRecursively()
                downloadDir.mkdirs()
                val apk = File(downloadDir, "update.apk")
                val actual = downloadTo(apk, release.assetUrl)
                if (actual != expected) throw UpdateCheckException("Checksum mismatch, update cancelled")

                installFromFile(apk)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                notifications.addErrorNotification(e)
            } finally {
                downloadDir.deleteRecursively()
                inProgress.set(false)
            }
        }
    }

    /** Downloads [url] into [target] emitting progress, returns the lowercase hex SHA-256 of the content */
    private suspend fun FlowCollector<UpdateProgress>.downloadTo(target: File, url: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        githubClient.streamFile(url) { response ->
            if (!response.status.isSuccess()) {
                throw UpdateCheckException("Download failed: HTTP ${response.status.value}")
            }
            val length = response.headers["Content-Length"]?.toLong() ?: 0L
            emit(UpdateProgress(length, 0))
            val channel = response.bodyAsChannel().counted()
            target.outputStream().buffered().use { out ->
                while (!channel.isClosedForRead) {
                    val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                    while (!packet.exhausted()) {
                        val bytes = packet.readByteArray()
                        digest.update(bytes)
                        out.write(bytes)
                    }
                    emit(UpdateProgress(length, channel.totalBytesRead))
                }
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun installFromFile(apk: File) {
        val packageInstaller = context.packageManager.packageInstaller
        val sessionId = packageInstaller.createSession(PackageInstaller.SessionParams(MODE_FULL_INSTALL))
        packageInstaller.openSession(sessionId).use { session ->
            session.openWrite("komelia", 0, apk.length()).use { sessionStream ->
                apk.inputStream().use { it.copyTo(sessionStream) }
                session.fsync(sessionStream)
            }
            val receiverIntent = Intent(context, PackageInstallerStatusReceiver::class.java)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val receiverPendingIntent = PendingIntent.getBroadcast(context, 0, receiverIntent, flags)
            session.commit(receiverPendingIntent.intentSender)
        }
    }

    private fun GithubRelease.toAppRelease(): AppRelease? {
        val version = parseReleaseTag(tagName) ?: return null
        val asset = selectApkAsset(assets) { it.name }
        val checksum = asset?.let { apk -> assets.firstOrNull { it.name == apk.name + ".sha256" } }

        return AppRelease(
            version = version,
            publishDate = publishedAt,
            releaseNotesBody = body.replace("\r", ""),
            htmlUrl = htmlUrl,
            assetName = asset?.name,
            assetUrl = asset?.browserDownloadUrl,
            checksumUrl = checksum?.browserDownloadUrl,
        )
    }
}

class PackageInstallerStatusReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)) {
            STATUS_PENDING_USER_ACTION -> {
                val confirmationIntent = IntentCompat.getParcelableExtra(
                    intent, Intent.EXTRA_INTENT, Intent::class.java
                )
                if (confirmationIntent != null) {
                    context.startActivity(confirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }

            else -> {}
        }
    }
}
