package net.tactware.worldweaver.domain

import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class AppBackupArchiveConverterTest {
    private val converter = AppBackupArchiveConverter()

    @Test
    fun writeAndReadRoundTripsDatabaseAndSidecarFiles() {
        val temp = Files.createTempDirectory("ww-backup-converter").toFile()
        try {
            val db = File(temp, "source-db").also { it.writeBytes(byteArrayOf(9, 8, 7)) }
            val avatars = File(temp, "avatars")
            File(avatars, "world/ada.png").also { it.parentFile.mkdirs(); it.writeBytes(byteArrayOf(1, 2)) }
            val maps = File(temp, "maps")
            File(maps, "map-1/original.png").also { it.parentFile.mkdirs(); it.writeBytes(byteArrayOf(3, 4, 5)) }
            val voices = File(temp, "voices")
            File(voices, "location/loc-1.wav").also { it.parentFile.mkdirs(); it.writeBytes(byteArrayOf(6, 7)) }
            val dest = File(temp, "app.wwbackup")
            val extractTo = File(temp, "extract")

            converter.write(
                destFile = dest,
                manifest = sampleManifest(),
                prefs = samplePrefs(),
                databaseFile = db,
                avatarsDir = avatars,
                mapsDir = maps,
                voicesDir = voices,
            )

            val result = converter.read(dest, extractTo)
            val ready = assertIs<AppBackupArchiveConverter.ReadResult.Ready>(result)
            assertEquals(AppBackupManifest.FORMAT_VERSION, ready.manifest.formatVersion)
            assertEquals("Ada", ready.prefs.displayName)
            assertEquals(byteArrayOf(9, 8, 7).toList(), File(ready.extractedDataDir, "ww.db").readBytes().toList())
            assertEquals(
                byteArrayOf(1, 2).toList(),
                File(ready.extractedDataDir, "avatars/world/ada.png").readBytes().toList(),
            )
            assertEquals(
                byteArrayOf(3, 4, 5).toList(),
                File(ready.extractedDataDir, "maps/map-1/original.png").readBytes().toList(),
            )
            assertEquals(
                byteArrayOf(6, 7).toList(),
                File(ready.extractedDataDir, "voices/location/loc-1.wav").readBytes().toList(),
            )
        } finally {
            temp.deleteRecursively()
        }
    }

    @Test
    fun newerFormatVersionIsRejected() {
        val temp = Files.createTempDirectory("ww-backup-future").toFile()
        try {
            val dest = File(temp, "future.wwbackup")
            writeRawArchive(
                dest = dest,
                manifestJson = Json.encodeToString(
                    AppBackupManifest.serializer(),
                    sampleManifest().copy(formatVersion = AppBackupManifest.FORMAT_VERSION + 1),
                ),
                prefsJson = Json.encodeToString(AppBackupPrefs.serializer(), samplePrefs()),
            )
            val result = converter.read(dest, File(temp, "extract"))
            assertIs<AppBackupArchiveConverter.ReadResult.UnsupportedVersion>(result)
        } finally {
            temp.deleteRecursively()
        }
    }

    @Test
    fun missingDatabaseIsInvalid() {
        val temp = Files.createTempDirectory("ww-backup-invalid").toFile()
        try {
            val dest = File(temp, "empty.wwbackup")
            ZipOutputStream(dest.outputStream()).use { zip ->
                zip.putNextEntry(ZipEntry("manifest.json"))
                zip.write(
                    Json.encodeToString(AppBackupManifest.serializer(), sampleManifest()).encodeToByteArray(),
                )
                zip.closeEntry()
                zip.putNextEntry(ZipEntry("prefs.json"))
                zip.write(Json.encodeToString(AppBackupPrefs.serializer(), samplePrefs()).encodeToByteArray())
                zip.closeEntry()
            }
            val result = converter.read(dest, File(temp, "extract"))
            assertIs<AppBackupArchiveConverter.ReadResult.InvalidArchive>(result)
        } finally {
            temp.deleteRecursively()
        }
    }

    @Test
    fun missingFileIsInvalid() {
        val missing = File("does-not-exist.wwbackup")
        val result = converter.read(missing, File("extract"))
        assertIs<AppBackupArchiveConverter.ReadResult.InvalidArchive>(result)
        assertTrue(!missing.exists())
    }

    private fun writeRawArchive(dest: File, manifestJson: String, prefsJson: String) {
        ZipOutputStream(dest.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry("manifest.json"))
            zip.write(manifestJson.encodeToByteArray())
            zip.closeEntry()
            zip.putNextEntry(ZipEntry("prefs.json"))
            zip.write(prefsJson.encodeToByteArray())
            zip.closeEntry()
            zip.putNextEntry(ZipEntry("data/ww.db"))
            zip.write(byteArrayOf(1))
            zip.closeEntry()
        }
    }

    private fun sampleManifest(): AppBackupManifest {
        return AppBackupManifest(
            formatVersion = AppBackupManifest.FORMAT_VERSION,
            appVersion = AppBackupManifest.APP_VERSION,
            dbSchemaVersion = AppBackupManifest.DB_SCHEMA_VERSION,
            exportedAtEpochMillis = 1_700_000_000_000,
        )
    }

    private fun samplePrefs(): AppBackupPrefs {
        return AppBackupPrefs(
            activeWorldId = "world-1",
            activeCampaignId = "camp-1",
            displayName = "Ada",
            email = "ada@local",
            themeMode = "DARK",
            themeSkin = "GOTHIC",
            navExpanded = false,
            diceColorStyle = "ONYX",
        )
    }
}
