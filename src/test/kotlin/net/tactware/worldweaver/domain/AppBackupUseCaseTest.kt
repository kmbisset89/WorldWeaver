package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import net.tactware.worldweaver.ui.dice.DiceColorStyle
import net.tactware.worldweaver.ui.settings.ShellSettingsStore
import net.tactware.worldweaver.ui.theme.ThemeMode
import net.tactware.worldweaver.ui.theme.ThemeSkin
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

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
        preferences.removeNode()
        dicePreferences.removeNode()
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
        DiceColorStyle.save(DiceColorStyle.ONYX, dicePreferences)
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
        DiceColorStyle.save(DiceColorStyle.BONE, dicePreferences)

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
        assertEquals(DiceColorStyle.ONYX, DiceColorStyle.load(dicePreferences))
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
                instantProvider = instantProvider,
                dicePreferences = dicePreferences,
            )(dest)
        }

        suspend fun restore(source: File): RestoreAppBackupUseCase.Result {
            return RestoreAppBackupUseCase(
                dataDirectory = dataDirectory,
                snapshotExporter = snapshot,
                archiveConverter = converter,
                activeContextRepository = context,
                shellSettingsStore = settings,
                dicePreferences = dicePreferences,
            )(source)
        }
    }

    private companion object {
        const val TEST_NODE = "net.tactware.worldweaver.test.backup"
        const val DICE_NODE = "net.tactware.worldweaver.test.backup.dice"
    }
}
