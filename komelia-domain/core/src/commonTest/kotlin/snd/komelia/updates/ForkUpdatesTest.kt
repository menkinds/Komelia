package snd.komelia.updates

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ForkUpdatesTest {
    @Test
    fun parsesTags() {
        assertEquals(AppVersion(0, 2, 0), parseReleaseTag("v0.2.0"))
        assertEquals(AppVersion(0, 2, 0), parseReleaseTag("0.2.0"))
        assertEquals(AppVersion(1, 10, 3), parseReleaseTag("v1.10.3-rc1"))
        assertNull(parseReleaseTag("v1.2"))
        assertNull(parseReleaseTag("latest"))
        assertNull(parseReleaseTag("vx.y.z"))
        assertNull(parseReleaseTag("v99999999999.0.0"))
    }

    @Test
    fun comparesVersions() {
        val a = parseReleaseTag("v0.2.0")!!
        val b = parseReleaseTag("0.2.1")!!
        val c = parseReleaseTag("v0.10.0")!!
        assertTrue(a < b)
        assertTrue(b < c)
        assertTrue(c > a)
        assertEquals(0, a.compareTo(parseReleaseTag("0.2.0")!!))
    }

    @Test
    fun versionCodeMath() {
        assertEquals(200, AppVersion(0, 2, 0).toVersionCode())
        assertEquals(1002, AppVersion(0, 10, 2).toVersionCode())
        assertEquals(10203, AppVersion(1, 2, 3).toVersionCode())
        assertTrue(AppVersion(0, 2, 0).toVersionCode() > 25)
    }

    @Test
    fun selectsArm64Apk() {
        val names = listOf("komelia-v1-arm64.apk.sha256", "komelia-v1-arm64.apk.idsig", "komelia-v1-arm64.apk")
        assertEquals("komelia-v1-arm64.apk", selectApkAsset(names) { it })
        assertEquals("a.apk", selectApkAsset(listOf("a.apk")) { it })
        assertNull(selectApkAsset(listOf("a.zip")) { it })
    }

    @Test
    fun parsesSha256() {
        val hash = "A".repeat(64)
        assertEquals("a".repeat(64), parseSha256("$hash  komelia.apk\n"))
        assertNull(parseSha256("deadbeef  x"))
        assertNull(parseSha256(""))
    }
}
