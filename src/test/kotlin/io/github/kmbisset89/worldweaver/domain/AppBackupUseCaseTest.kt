package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.io.File
import java.nio.file.Files
import java.time.Instant
import java.util.prefs.Preferences
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class AppBackupUseCaseTest {
    private val tempDir = Files.createTempDirectory("ww-backup-usecase").toFile()
    private val preferences = Preferences.userRoot().node(TEST_NODE)
    private val dicePreferences = Preferences.userRoot().node(DICE_NODE)
    private val atmospherePreferences = Preferences.userRoot().node(ATMOSPHERE_NODE)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
        preferences.removeNode()
        dicePreferences.removeNode()
        atmospherePreferences.removeNode()
    }

    @Test
    fun exportThenRestoreReplacesFilesAndPrefs() = runTest {
        val harness = Harness()
        harness.dataDirectory.ensureExists()
        File(harness.dataDirectory.avatarsDir, "world/ada.png").also {
            it.parentFile.mkdirs()
            it.writeBytes(byteArrayOf(1, 2, 3))
        }
        File(harness.dataDirectory.mapsDir, "map-1/original.png").also {
            it.parentFile.mkdirs()
            it.writeBytes(byteArrayOf(4, 5))
        }
        File(harness.dataDirectory.voicesDir, "location/loc-1.wav").also {
            it.parentFile.mkdirs()
            it.writeBytes(byteArrayOf(6, 7))
        }
        harness.context.setActiveWorldId("world-1")
        harness.context.setActiveCampaignId("camp-1")
        harness.settings.setThemeMode(ThemeMode.DARK)
        harness.settings.setThemeSkin(ThemeSkin.GOTHIC)
        harness.settings.setNavExpanded(false)
        harness.settings.setProfile("Ada", "ada@local")
        DiceColorStyleStore(dicePreferences).saveName("ONYX")
        harness.atmosphere.replaceAll(
            AtmosphereSettings(
                connection = HomeAssistantConnection("http://ha.local:8123", "secret-token"),
                hue = HueConnection("192.168.1.40", "hue-key"),
                goveeDevices = listOf(GoveeDevice("AA:BB", "192.168.1.50", "H6072")),
                hueLights = listOf(HueLight("light-1", "Table lamp")),
                scenes = listOf(
                    AtmosphereScene("s1", "Tavern", "scene.tavern", 0),
                ),
                moods = listOf(AtmosphereMood("m1", "Temple", true, 40, "#C4A35A", 0)),
                selectedHueLightIds = listOf("light-1"),
                selectedGoveeDeviceIds = listOf("AA:BB"),
                isAlwaysOnTop = true,
                lookTransitionMs = 1_200,
                musicTracks = listOf(
                    AtmosphereMusicTrack("t1", "Tavern", "/tmp/tavern.mp3", 0),
                ),
                musicVolume = 40,
                musicLoopEnabled = false,
            ),
        )
        val dest = File(tempDir, "app.wwbackup")

        assertIs<ExportAppBackupUseCase.Result.Written>(harness.export(dest))
        assertTrue(dest.isFile)

        File(harness.dataDirectory.avatarsDir, "world/ada.png").delete()
        File(harness.dataDirectory.mapsDir, "map-1/replaced.png").also {
            it.parentFile.mkdirs()
            it.writeBytes(byteArrayOf(9))
        }
        harness.context.setActiveWorldId("other-world")
        harness.settings.setProfile("Other", "other@local")
        harness.settings.setThemeMode(ThemeMode.LIGHT)
        DiceColorStyleStore(dicePreferences).saveName("BONE")
        harness.atmosphere.replaceAll(
            AtmosphereSettings(
                connection = HomeAssistantConnection("", ""),
                hue = HueConnection("", ""),
                goveeDevices = emptyList(),
                scenes = emptyList(),
                isAlwaysOnTop = false,
            ),
        )

        assertIs<RestoreAppBackupUseCase.Result.Restored>(harness.restore(dest))

        assertEquals(
            byteArrayOf(1, 2, 3).toList(),
            harness.dataDirectory.databaseFile.readBytes().toList(),
        )
        assertEquals(
            byteArrayOf(1, 2, 3).toList(),
            File(harness.dataDirectory.avatarsDir, "world/ada.png").readBytes().toList(),
        )
        assertEquals(
            byteArrayOf(4, 5).toList(),
            File(harness.dataDirectory.mapsDir, "map-1/original.png").readBytes().toList(),
        )
        assertEquals(
            byteArrayOf(6, 7).toList(),
            File(harness.dataDirectory.voicesDir, "location/loc-1.wav").readBytes().toList(),
        )
        assertFalse(File(harness.dataDirectory.mapsDir, "map-1/replaced.png").exists())
        assertEquals("world-1", harness.context.get().activeWorldId)
        assertEquals("camp-1", harness.context.get().activeCampaignId)
        assertEquals("Ada", harness.settings.settings.value.displayName)
        assertEquals(ThemeMode.DARK, harness.settings.settings.value.themeMode)
        assertEquals(ThemeSkin.GOTHIC, harness.settings.settings.value.themeSkin)
        assertEquals(false, harness.settings.settings.value.navExpanded)
        assertEquals("ONYX", DiceColorStyleStore(dicePreferences).loadName())
        assertEquals("http://ha.local:8123", harness.atmosphere.settings.value.connection.baseUrl)
        assertEquals("secret-token", harness.atmosphere.settings.value.connection.token)
        assertEquals("192.168.1.40", harness.atmosphere.settings.value.hue.bridgeHost)
        assertEquals("hue-key", harness.atmosphere.settings.value.hue.applicationKey)
        assertEquals(
            listOf(GoveeDevice("AA:BB", "192.168.1.50", "H6072")),
            harness.atmosphere.settings.value.goveeDevices,
        )
        assertEquals(
            listOf(HueLight("light-1", "Table lamp")),
            harness.atmosphere.settings.value.hueLights,
        )
        assertEquals(true, harness.atmosphere.settings.value.isAlwaysOnTop)
        assertEquals(1_200, harness.atmosphere.settings.value.lookTransitionMs)
        assertEquals(
            listOf(AtmosphereScene("s1", "Tavern", "scene.tavern", 0)),
            harness.atmosphere.settings.value.scenes,
        )
        assertEquals(
            listOf(AtmosphereMood("m1", "Temple", true, 40, "#C4A35A", 0)),
            harness.atmosphere.settings.value.moods,
        )
        assertEquals(listOf("light-1"), harness.atmosphere.settings.value.selectedHueLightIds)
        assertEquals(listOf("AA:BB"), harness.atmosphere.settings.value.selectedGoveeDeviceIds)
        assertEquals(
            listOf(AtmosphereMusicTrack("t1", "Tavern", "/tmp/tavern.mp3", 0)),
            harness.atmosphere.settings.value.musicTracks,
        )
        assertEquals(40, harness.atmosphere.settings.value.musicVolume)
        assertEquals(false, harness.atmosphere.settings.value.musicLoopEnabled)
        assertTrue(harness.closed)
    }

    @Test
    fun blankPathIsRejected() = runTest {
        val harness = Harness()
        val exported = harness.export(File(""))
        val failedExport = assertIs<ExportAppBackupUseCase.Result.Failed>(exported)
        assertEquals("Choose a backup file", failedExport.message)

        val restored = harness.restore(File(""))
        val failedRestore = assertIs<RestoreAppBackupUseCase.Result.Failed>(restored)
        assertEquals("Choose a backup file", failedRestore.message)
    }

    @Test
    fun invalidZipIsRejected() = runTest {
        val harness = Harness()
        val dest = File(tempDir, "not-a-backup.zip")
        dest.writeText("nope")
        assertIs<RestoreAppBackupUseCase.Result.InvalidArchive>(harness.restore(dest))
        assertFalse(harness.closed)
    }

    private inner class Harness {
        val dataDirectory = WorldWeaverDataDirectory(File(tempDir, "data"))
        val context = FakeActiveContextRepository()
        val settings = ShellSettingsStore(preferences)
        val atmosphere = AtmosphereSettingsStore(atmospherePreferences)
        var snapshotBytes: ByteArray = byteArrayOf(1, 2, 3)
        var closed: Boolean = false
        private val snapshot = object : DatabaseSnapshotExporter {
            override suspend fun exportConsistentCopy(dest: File) {
                dest.parentFile?.mkdirs()
                dest.writeBytes(snapshotBytes)
            }

            override fun close() {
                closed = true
            }
        }
        private val converter = AppBackupArchiveConverter()
        private val instantProvider = InstantProvider { Instant.parse("2026-08-30T12:00:00Z") }

        suspend fun export(dest: File): ExportAppBackupUseCase.Result {
            return ExportAppBackupUseCase(
                dataDirectory = dataDirectory,
                snapshotExporter = snapshot,
                archiveConverter = converter,
                activeContextRepository = context,
                shellSettingsStore = settings,
                atmosphereSettingsStore = atmosphere,
                instantProvider = instantProvider,
                diceColorStyleStore = DiceColorStyleStore(dicePreferences),
            )(dest)
        }

        suspend fun restore(source: File): RestoreAppBackupUseCase.Result {
            return RestoreAppBackupUseCase(
                dataDirectory = dataDirectory,
                snapshotExporter = snapshot,
                archiveConverter = converter,
                activeContextRepository = context,
                shellSettingsStore = settings,
                atmosphereSettingsStore = atmosphere,
                diceColorStyleStore = DiceColorStyleStore(dicePreferences),
            )(source)
        }
    }

    private companion object {
        const val TEST_NODE = "io.github.kmbisset89.worldweaver.test.backup"
        const val DICE_NODE = "io.github.kmbisset89.worldweaver.test.backup.dice"
        const val ATMOSPHERE_NODE = "io.github.kmbisset89.worldweaver.test.backup.atmosphere"
    }
}
