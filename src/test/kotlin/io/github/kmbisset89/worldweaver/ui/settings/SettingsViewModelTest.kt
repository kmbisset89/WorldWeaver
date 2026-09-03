package io.github.kmbisset89.worldweaver.ui.settings

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import kotlinx.coroutines.withTimeout
import io.github.kmbisset89.worldweaver.core.AppCoroutineScope
import io.github.kmbisset89.worldweaver.domain.AppBackupArchiveConverter
import io.github.kmbisset89.worldweaver.domain.BundledSrdCatalogLoader
import io.github.kmbisset89.worldweaver.domain.ClearSrdCatalogUseCase
import io.github.kmbisset89.worldweaver.domain.DatabaseSnapshotExporter
import io.github.kmbisset89.worldweaver.domain.ExportAppBackupUseCase
import io.github.kmbisset89.worldweaver.domain.FakeActiveContextRepository
import io.github.kmbisset89.worldweaver.domain.FakeSrdCatalogRepository
import io.github.kmbisset89.worldweaver.domain.ImportSrdCatalogUseCase
import io.github.kmbisset89.worldweaver.domain.InstantProvider
import io.github.kmbisset89.worldweaver.domain.ObserveSrdCatalogUseCase
import io.github.kmbisset89.worldweaver.domain.RestoreAppBackupUseCase
import io.github.kmbisset89.worldweaver.domain.SrdCatalogJsonConverter
import io.github.kmbisset89.worldweaver.domain.WorldWeaverDataDirectory
import io.github.kmbisset89.worldweaver.ui.theme.ThemeMode
import io.github.kmbisset89.worldweaver.ui.theme.ThemeSkin
import java.io.File
import java.nio.file.Files
import java.time.Instant
import java.util.prefs.Preferences
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class SettingsViewModelTest {
    private val preferences = Preferences.userRoot().node(TEST_NODE)
    private val dicePreferences = Preferences.userRoot().node(DICE_NODE)
    private val scope = AppCoroutineScope()
    private val tempDir = Files.createTempDirectory("ww-settings-backup").toFile()

    @AfterTest
    fun tearDown() {
        scope.cancel()
        preferences.removeNode()
        dicePreferences.removeNode()
        tempDir.deleteRecursively()
    }

    @Test
    fun themeSkinAndNavWriteThroughToTheStore() {
        val viewModel = viewModel()

        viewModel.onInteraction(SettingsInteraction.ThemeModeSelected(ThemeMode.DARK))
        viewModel.onInteraction(SettingsInteraction.ThemeSkinSelected(ThemeSkin.COZY_TAVERN))
        viewModel.onInteraction(SettingsInteraction.NavExpandedChanged(false))

        val store = ShellSettingsStore(preferences)
        assertEquals(ThemeMode.DARK, store.settings.value.themeMode)
        assertEquals(ThemeSkin.COZY_TAVERN, store.settings.value.themeSkin)
        assertEquals(false, store.settings.value.navExpanded)
    }

    @Test
    fun blankProfileNameIsRejected() {
        val store = ShellSettingsStore(preferences)
        val viewModel = viewModel(store)

        viewModel.onInteraction(SettingsInteraction.DisplayNameChanged("   "))
        viewModel.onInteraction(SettingsInteraction.ProfileSaved)

        val state = assertIs<SettingsViewState.Content>(viewModel.state.value)
        assertEquals("Name is required", state.profileError)
        assertEquals(ShellSettings.DEFAULT_DISPLAY_NAME, store.settings.value.displayName)
    }

    @Test
    fun profileSavePersistsTrimmedNameAndEmail() {
        val store = ShellSettingsStore(preferences)
        val viewModel = viewModel(store)

        viewModel.onInteraction(SettingsInteraction.DisplayNameChanged("  Ada  "))
        viewModel.onInteraction(SettingsInteraction.EmailChanged("  ada@local  "))
        viewModel.onInteraction(SettingsInteraction.ProfileSaved)

        assertEquals("Ada", store.settings.value.displayName)
        assertEquals("ada@local", store.settings.value.email)
        assertNull(assertIs<SettingsViewState.Content>(viewModel.state.value).profileError)
    }

    @Test
    fun blankExportPathIsRejected() = runBlocking {
        val viewModel = viewModel()
        val effect = async { viewModel.effects.first() }
        yield()
        viewModel.onInteraction(SettingsInteraction.ExportPathChosen(""))
        assertEquals(
            "Choose a backup file",
            assertIs<SettingsViewEffect.Failed>(withTimeout(2_000) { effect.await() }).message,
        )
    }

    @Test
    fun restoreConfirmCancelClearsPendingPath() {
        val viewModel = viewModel()
        val path = File(tempDir, "app.wwbackup").absolutePath
        viewModel.onInteraction(SettingsInteraction.RestorePathChosen(path))
        val pending = assertIs<SettingsViewState.Content>(viewModel.state.value)
        assertEquals(path, pending.pendingRestorePath)

        viewModel.onInteraction(SettingsInteraction.RestoreCancelled)
        val cancelled = assertIs<SettingsViewState.Content>(viewModel.state.value)
        assertNull(cancelled.pendingRestorePath)
    }

    @Test
    fun importBundledSrdUpdatesStatus() = runBlocking {
        val catalogs = FakeSrdCatalogRepository()
        val viewModel = viewModel(catalogs = catalogs)
        val effect = async { viewModel.effects.first() }
        yield()
        viewModel.onInteraction(SettingsInteraction.ImportBundledSrdSelected)
        assertIs<SettingsViewEffect.SrdImported>(withTimeout(2_000) { effect.await() })
        val state = assertIs<SettingsViewState.Content>(viewModel.state.value)
        val imported = assertIs<SettingsViewState.SrdStatus.Imported>(state.srdStatus)
        assertEquals("5E SRD 5.1", imported.sourceLabel)
        assertTrue(imported.monsterCount > 0)
        assertEquals("5E SRD 5.1", catalogs.get()?.sourceLabel)
    }

    @Test
    fun exportSetsTransferringThenClearsIt() = runBlocking {
        val gate = CompletableDeferred<Unit>()
        val viewModel = viewModel(snapshot = GatedSnapshotExporter(gate))
        val dest = File(tempDir, "app.wwbackup")

        val effect = async { viewModel.effects.first() }
        yield()
        viewModel.onInteraction(SettingsInteraction.ExportPathChosen(dest.absolutePath))
        val transferring = viewModel.state.filterIsInstance<SettingsViewState.Content>().first { it.isTransferring }
        assertTrue(transferring.isTransferring)

        gate.complete(Unit)
        assertIs<SettingsViewEffect.Exported>(withTimeout(2_000) { effect.await() })
        val done = viewModel.state.filterIsInstance<SettingsViewState.Content>().first { !it.isTransferring }
        assertEquals(false, done.isTransferring)
    }

    private fun viewModel(
        store: ShellSettingsStore = ShellSettingsStore(preferences),
        snapshot: DatabaseSnapshotExporter = ImmediateSnapshotExporter(),
        catalogs: FakeSrdCatalogRepository = FakeSrdCatalogRepository(),
    ): SettingsViewModel {
        val dataDirectory = WorldWeaverDataDirectory(File(tempDir, "data"))
        val converter = AppBackupArchiveConverter()
        val context = FakeActiveContextRepository()
        val instantProvider = InstantProvider { Instant.parse("2026-08-30T12:00:00Z") }
        val srdConverter = SrdCatalogJsonConverter()
        return SettingsViewModel(
            shellSettingsStore = store,
            exportAppBackup = ExportAppBackupUseCase(
                dataDirectory = dataDirectory,
                snapshotExporter = snapshot,
                archiveConverter = converter,
                activeContextRepository = context,
                shellSettingsStore = store,
                instantProvider = instantProvider,
                dicePreferences = dicePreferences,
            ),
            restoreAppBackup = RestoreAppBackupUseCase(
                dataDirectory = dataDirectory,
                snapshotExporter = snapshot,
                archiveConverter = converter,
                activeContextRepository = context,
                shellSettingsStore = store,
                dicePreferences = dicePreferences,
            ),
            observeSrdCatalog = ObserveSrdCatalogUseCase(catalogs),
            importSrdCatalog = ImportSrdCatalogUseCase(
                catalogRepository = catalogs,
                bundledLoader = BundledSrdCatalogLoader(srdConverter),
                converter = srdConverter,
                instantProvider = instantProvider,
            ),
            clearSrdCatalog = ClearSrdCatalogUseCase(catalogs),
            appScope = scope,
        )
    }

    private class ImmediateSnapshotExporter : DatabaseSnapshotExporter {
        override suspend fun exportConsistentCopy(dest: File) {
            dest.parentFile?.mkdirs()
            dest.writeBytes(byteArrayOf(1, 2, 3))
        }

        override fun close() = Unit
    }

    private class GatedSnapshotExporter(
        private val gate: CompletableDeferred<Unit>,
    ) : DatabaseSnapshotExporter {
        override suspend fun exportConsistentCopy(dest: File) {
            gate.await()
            dest.parentFile?.mkdirs()
            dest.writeBytes(byteArrayOf(1, 2, 3))
        }

        override fun close() = Unit
    }

    private companion object {
        const val TEST_NODE = "io.github.kmbisset89.worldweaver.test.settings"
        const val DICE_NODE = "io.github.kmbisset89.worldweaver.test.settings.dice"
    }
}
