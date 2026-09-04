package io.github.kmbisset89.worldweaver.domain

import io.github.kmbisset89.worldweaver.ui.dice.DiceColorStyle
import io.github.kmbisset89.worldweaver.ui.settings.ShellSettingsStore
import io.github.kmbisset89.worldweaver.ui.theme.ThemeMode
import io.github.kmbisset89.worldweaver.ui.theme.ThemeSkin
import java.io.File
import java.util.prefs.Preferences

internal class RestoreAppBackupUseCase(
    private val dataDirectory: WorldWeaverDataDirectory,
    private val snapshotExporter: DatabaseSnapshotExporter,
    private val archiveConverter: AppBackupArchiveConverter,
    private val activeContextRepository: ActiveContextRepository,
    private val shellSettingsStore: ShellSettingsStore,
    private val dicePreferences: Preferences = Preferences.userRoot(),
) {
    sealed interface Result {
        data object Restored : Result
        data object UnsupportedVersion : Result
        data object InvalidArchive : Result
        data class Failed(val message: String) : Result
    }

    suspend operator fun invoke(sourceFile: File): Result {
        if (sourceFile.path.isBlank()) {
            return Result.Failed("Choose a backup file")
        }
        val extractDir = File(dataDirectory.root, EXTRACT_DIR_NAME)
        extractDir.deleteRecursively()
        val read = archiveConverter.read(sourceFile, extractDir)
        val archive = when (read) {
            is AppBackupArchiveConverter.ReadResult.Ready -> read
            AppBackupArchiveConverter.ReadResult.UnsupportedVersion -> {
                extractDir.deleteRecursively()
                return Result.UnsupportedVersion
            }
            AppBackupArchiveConverter.ReadResult.InvalidArchive -> {
                extractDir.deleteRecursively()
                return Result.InvalidArchive
            }
        }
        return try {
            snapshotExporter.close()
            replaceFile(
                dest = dataDirectory.databaseFile,
                source = File(archive.extractedDataDir, WorldWeaverDataDirectory.DATABASE_FILE_NAME),
            )
            deleteSidecar(dataDirectory.databaseFile)
            replaceDirectory(
                dest = dataDirectory.avatarsDir,
                source = File(archive.extractedDataDir, WorldWeaverDataDirectory.AVATARS_DIR_NAME),
            )
            replaceDirectory(
                dest = dataDirectory.mapsDir,
                source = File(archive.extractedDataDir, WorldWeaverDataDirectory.MAPS_DIR_NAME),
            )
            replaceDirectory(
                dest = dataDirectory.worldMapsDir,
                source = File(archive.extractedDataDir, WorldWeaverDataDirectory.WORLD_MAPS_DIR_NAME),
            )
            replaceDirectory(
                dest = dataDirectory.voicesDir,
                source = File(archive.extractedDataDir, WorldWeaverDataDirectory.VOICES_DIR_NAME),
            )
            replaceDirectory(
                dest = dataDirectory.srdDir,
                source = File(archive.extractedDataDir, WorldWeaverDataDirectory.SRD_DIR_NAME),
            )
            applyPrefs(archive.prefs)
            Result.Restored
        } catch (error: Exception) {
            Result.Failed(error.message ?: "Could not restore the app backup")
        } finally {
            extractDir.deleteRecursively()
        }
    }

    private fun applyPrefs(prefs: AppBackupPrefs) {
        activeContextRepository.setActiveWorldId(prefs.activeWorldId)
        activeContextRepository.setActiveCampaignId(prefs.activeCampaignId)
        activeContextRepository.setActiveSessionId(prefs.activeSessionId)
        shellSettingsStore.setThemeMode(parseThemeMode(prefs.themeMode))
        shellSettingsStore.setThemeSkin(parseThemeSkin(prefs.themeSkin))
        shellSettingsStore.setNavExpanded(prefs.navExpanded)
        shellSettingsStore.setProfile(
            displayName = prefs.displayName,
            email = prefs.email,
        )
        val diceStyle = DiceColorStyle.entries.firstOrNull { it.name == prefs.diceColorStyle }
            ?: DiceColorStyle.BONE
        DiceColorStyle.save(diceStyle, dicePreferences)
    }

    private fun parseThemeMode(raw: String): ThemeMode {
        return ThemeMode.entries.firstOrNull { it.name == raw } ?: ThemeMode.SYSTEM
    }

    private fun parseThemeSkin(raw: String): ThemeSkin {
        return ThemeSkin.entries.firstOrNull { it.name == raw } ?: ThemeSkin.FANTASY
    }

    private fun replaceFile(dest: File, source: File) {
        dest.parentFile?.mkdirs()
        if (dest.exists()) {
            dest.delete()
        }
        if (source.isFile) {
            source.copyTo(dest, overwrite = true)
        }
    }

    private fun deleteSidecar(databaseFile: File) {
        File(databaseFile.path + "-wal").delete()
        File(databaseFile.path + "-shm").delete()
    }

    private fun replaceDirectory(dest: File, source: File) {
        if (dest.exists()) {
            dest.deleteRecursively()
        }
        if (source.isDirectory) {
            source.copyRecursively(dest)
        }
    }

    private companion object {
        const val EXTRACT_DIR_NAME = ".backup-restore"
    }
}
