package snd.komelia.updates

/**
 * Fork-owned update settings and helpers (PAGES, menkinds/Komelia).
 * Kept in its own file so the weekly upstream sync does not overwrite the repo owner/name.
 */
const val FORK_RELEASES_REPO = "menkinds/Komelia"

private val tagRegex = Regex("""^v?(\d+)\.(\d+)\.(\d+)(?:[-+].*)?$""")

/** "v0.2.1", "0.2.1" or "v0.2.1-rc1" -> 0.2.1; anything else -> null */
fun parseReleaseTag(tag: String): AppVersion? {
    val match = tagRegex.matchEntire(tag.trim()) ?: return null
    val (major, minor, patch) = match.destructured
    return AppVersion(
        major.toIntOrNull() ?: return null,
        minor.toIntOrNull() ?: return null,
        patch.toIntOrNull() ?: return null,
    )
}

/** MAJOR*10000 + MINOR*100 + PATCH. Must match the computation in android-release.yml. */
fun AppVersion.toVersionCode(): Int = major * 10000 + minor * 100 + patch

/** The arm64 APK of a release: prefers a name containing "arm64", falls back to the first .apk. */
fun <T> selectApkAsset(assets: List<T>, name: (T) -> String): T? {
    val apks = assets.filter { name(it).endsWith(".apk") }
    return apks.firstOrNull { name(it).contains("arm64") } ?: apks.firstOrNull()
}

/** Extracts the hex digest from `sha256sum` output ("<hash>  <file>"); null if it is not a valid SHA-256. */
fun parseSha256(text: String): String? {
    val token = text.trim().split(Regex("\\s+")).firstOrNull()?.lowercase() ?: return null
    return token.takeIf { Regex("^[0-9a-f]{64}$").matches(it) }
}

class UpdateCheckException(message: String) : Exception(message)
