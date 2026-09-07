package io.github.kmbisset89.worldweaver.domain

import java.io.File

internal class ExportAppBackupUseCase(
    private val dataDirectory: WorldWeaverDataDirectory,
    private val snapshotExporter: DatabaseSnapshotExporter,
    private val archiveConverter: AppBackupArchiveConverter,
    private val activeContextRepository: ActiveContextRepository,
    private val shellSettingsStore: ShellSettingsStore,
    private val atmosphereSettingsStore: AtmosphereSettingsStore,
    private val instantProvider: InstantProvider,
    private val diceColorStyleStore: DiceColorStyleStore,
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
            val atmosphere = atmosphereSettingsStore.settings.value
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
                    activeSessionId = context.activeSessionId,
                    displayName = settings.displayName,
                    email = settings.email,
                    themeMode = settings.themeMode.name,
                    themeSkin = settings.themeSkin.name,
                    navExpanded = settings.navExpanded,
                    diceColorStyle = diceColorStyleStore.loadName(),
                    homeAssistantBaseUrl = atmosphere.connection.baseUrl,
                    homeAssistantToken = atmosphere.connection.token,
                    hueBridgeHost = atmosphere.hue.bridgeHost,
                    hueApplicationKey = atmosphere.hue.applicationKey,
                    goveeDevices = atmosphere.goveeDevices,
                    hueLights = atmosphere.hueLights,
                    atmosphereAlwaysOnTop = atmosphere.isAlwaysOnTop,
                    atmosphereScenes = atmosphere.scenes,
                    atmosphereMoods = atmosphere.moods,
                    atmosphereSelectedHueLightIds = atmosphere.selectedHueLightIds,
                    atmosphereSelectedGoveeDeviceIds = atmosphere.selectedGoveeDeviceIds,
                    atmosphereLookTransitionMs = atmosphere.lookTransitionMs,
                    atmosphereMusicTracks = atmosphere.musicTracks,
                    atmosphereMusicVolume = atmosphere.musicVolume,
                    atmosphereMusicLoopEnabled = atmosphere.musicLoopEnabled,
                ),
                databaseFile = snapshotDb,
                avatarsDir = dataDirectory.avatarsDir,
                mapsDir = dataDirectory.mapsDir,
                worldMapsDir = dataDirectory.worldMapsDir,
                voicesDir = dataDirectory.voicesDir,
                srdDir = dataDirectory.srdDir,
                assetsDir = dataDirectory.assetsDir,
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
