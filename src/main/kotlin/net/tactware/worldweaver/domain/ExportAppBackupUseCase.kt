package net.tactware.worldweaver.domain

import net.tactware.worldweaver.ui.dice.DiceColorStyle
import net.tactware.worldweaver.ui.settings.ShellSettingsStore
import java.io.File
import java.util.prefs.Preferences

internal class ExportAppBackupUseCase(
    private val dataDirectory: WorldWeaverDataDirectory,
    private val snapshotExporter: DatabaseSnapshotExporter,
    private val archiveConverter: AppBackupArchiveConverter,
    private val activeContextRepository: ActiveContextRepository,
    private val shellSettingsStore: ShellSettingsStore,
    private val instantProvider: InstantProvider,
    private val dicePreferences: Preferences = Preferences.userRoot(),
) {
    sealed interface Result {
        data object Written : Result
        data class Failed(val message: String) : Result
    }

    suspend operator fun invoke(destFile: File): Result {
        if (destFile.path.isBlank()) {
            return Result.Failed("Choose a backup file")
        }
        val snapshotDir = File(dataDirectory.root, SNAPSHOT_DIR_NAME)
        val snapshotDb = File(snapshotDir, WorldWeaverDataDirectory.DATABASE_FILE_NAME)
        return try {
            snapshotDir.mkdirs()
            snapshotExporter.exportConsistentCopy(snapshotDb)
            val settings = shellSettingsStore.settings.value
            val context = activeContextRepository.get()
            archiveConverter.write(
                destFile = destFile,
                manifest = AppBackupManifest(
                    formatVersion = AppBackupManifest.FORMAT_VERSION,
                    appVersion = AppBackupManifest.APP_VERSION,
                    dbSchemaVersion = AppBackupManifest.DB_SCHEMA_VERSION,
                    exportedAtEpochMillis = instantProvider.now().toEpochMilli(),
                ),
                prefs = AppBackupPrefs(
                    activeWorldId = context.activeWorldId,
                    activeCampaignId = context.activeCampaignId,
                    displayName = settings.displayName,
                    email = settings.email,
                    themeMode = settings.themeMode.name,
                    themeSkin = settings.themeSkin.name,
                    navExpanded = settings.navExpanded,
                    diceColorStyle = DiceColorStyle.load(dicePreferences).name,
                ),
                databaseFile = snapshotDb,
                avatarsDir = dataDirectory.avatarsDir,
                mapsDir = dataDirectory.mapsDir,
                voicesDir = dataDirectory.voicesDir,
            )
            Result.Written
        } catch (error: Exception) {
            Result.Failed(error.message ?: "Could not write the app backup")
        } finally {
            snapshotDir.deleteRecursively()
        }
    }

    private companion object {
        const val SNAPSHOT_DIR_NAME = ".backup-snapshot"
    }
}
