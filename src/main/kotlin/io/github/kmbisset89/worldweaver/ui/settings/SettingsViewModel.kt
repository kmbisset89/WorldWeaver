package io.github.kmbisset89.worldweaver.ui.settings

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import io.github.kmbisset89.worldweaver.core.AppCoroutineScope
import io.github.kmbisset89.worldweaver.domain.ClearSrdCatalogUseCase
import io.github.kmbisset89.worldweaver.domain.ExportAppBackupUseCase
import io.github.kmbisset89.worldweaver.domain.ImportSrdCatalogUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveSrdCatalogUseCase
import io.github.kmbisset89.worldweaver.domain.RestoreAppBackupUseCase
import io.github.kmbisset89.worldweaver.domain.SrdCatalog
import io.github.kmbisset89.worldweaver.ui.theme.ThemeMode
import io.github.kmbisset89.worldweaver.ui.theme.ThemeSkin
import java.io.File

internal class SettingsViewModel(
    private val shellSettingsStore: ShellSettingsStore,
    private val exportAppBackup: ExportAppBackupUseCase,
    private val restoreAppBackup: RestoreAppBackupUseCase,
    private val observeSrdCatalog: ObserveSrdCatalogUseCase,
    private val importSrdCatalog: ImportSrdCatalogUseCase,
    private val clearSrdCatalog: ClearSrdCatalogUseCase,
    private val appScope: AppCoroutineScope,
) {
    private val _state = MutableStateFlow<SettingsViewState>(contentFrom(shellSettingsStore.settings.value))
    val state: StateFlow<SettingsViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<SettingsViewEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<SettingsViewEffect> = _effects.asSharedFlow()

    init {
        appScope.scope.launch {
            shellSettingsStore.settings.collect { settings ->
                syncFromStore(settings)
            }
        }
        appScope.scope.launch {
            observeSrdCatalog().collect { catalog ->
                updateContent { current ->
                    current.copy(srdStatus = statusFrom(catalog))
                }
            }
        }
    }

    fun onInteraction(interaction: SettingsInteraction) {
        when (interaction) {
            SettingsInteraction.ScreenStarted -> Unit
            is SettingsInteraction.ThemeModeSelected -> selectThemeMode(interaction.themeMode)
            is SettingsInteraction.ThemeSkinSelected -> selectThemeSkin(interaction.themeSkin)
            is SettingsInteraction.NavExpandedChanged -> setNavExpanded(interaction.expanded)
            is SettingsInteraction.DisplayNameChanged -> updateDisplayName(interaction.displayName)
            is SettingsInteraction.EmailChanged -> updateEmail(interaction.email)
            SettingsInteraction.ProfileSaved -> saveProfile()
            SettingsInteraction.ExportBackupSelected -> Unit
            is SettingsInteraction.ExportPathChosen -> exportBackup(interaction.path)
            SettingsInteraction.RestoreBackupSelected -> Unit
            is SettingsInteraction.RestorePathChosen -> requestRestore(interaction.path)
            SettingsInteraction.RestoreConfirmed -> confirmRestore()
            SettingsInteraction.RestoreCancelled -> clearPendingRestore()
            SettingsInteraction.ImportBundledSrdSelected -> importSrd(ImportSrdCatalogUseCase.Source.Bundled)
            SettingsInteraction.ImportSrdFileSelected -> Unit
            is SettingsInteraction.SrdFilePathChosen -> importSrd(
                ImportSrdCatalogUseCase.Source.File(File(interaction.path)),
            )
            SettingsInteraction.ClearSrdSelected -> requestClearSrd()
            SettingsInteraction.ClearSrdConfirmed -> confirmClearSrd()
            SettingsInteraction.ClearSrdCancelled -> clearPendingClearSrd()
        }
    }

    private fun selectThemeMode(themeMode: ThemeMode) {
        shellSettingsStore.setThemeMode(themeMode)
    }

    private fun selectThemeSkin(themeSkin: ThemeSkin) {
        shellSettingsStore.setThemeSkin(themeSkin)
    }

    private fun setNavExpanded(expanded: Boolean) {
        shellSettingsStore.setNavExpanded(expanded)
    }

    private fun updateDisplayName(displayName: String) {
        updateContent { current ->
            current.copy(
                draftDisplayName = displayName,
                profileError = null,
            )
        }
    }

    private fun updateEmail(email: String) {
        updateContent { current ->
            current.copy(
                draftEmail = email,
                profileError = null,
            )
        }
    }

    private fun saveProfile() {
        val content = _state.value as? SettingsViewState.Content ?: return
        val displayName = content.draftDisplayName.trim()
        if (displayName.isBlank()) {
            updateContent { current ->
                current.copy(profileError = "Name is required")
            }
            return
        }
        shellSettingsStore.setProfile(
            displayName = displayName,
            email = content.draftEmail.trim(),
        )
    }

    private fun exportBackup(path: String) {
        val content = _state.value as? SettingsViewState.Content ?: return
        if (content.isTransferring) {
            return
        }
        if (path.isBlank()) {
            _effects.tryEmit(SettingsViewEffect.Failed("Choose a backup file"))
            return
        }
        setTransferring(true)
        appScope.scope.launch {
            when (val result = exportAppBackup(File(path))) {
                ExportAppBackupUseCase.Result.Written -> {
                    setTransferring(false)
                    _effects.tryEmit(SettingsViewEffect.Exported)
                }
                is ExportAppBackupUseCase.Result.Failed -> {
                    setTransferring(false)
                    _effects.tryEmit(SettingsViewEffect.Failed(result.message))
                }
            }
        }
    }

    private fun requestRestore(path: String) {
        val content = _state.value as? SettingsViewState.Content ?: return
        if (content.isTransferring) {
            return
        }
        if (path.isBlank()) {
            _effects.tryEmit(SettingsViewEffect.Failed("Choose a backup file"))
            return
        }
        updateContent { current ->
            current.copy(pendingRestorePath = path)
        }
    }

    private fun confirmRestore() {
        val content = _state.value as? SettingsViewState.Content ?: return
        val path = content.pendingRestorePath ?: return
        if (content.isTransferring) {
            return
        }
        setTransferring(true)
        appScope.scope.launch {
            when (val result = restoreAppBackup(File(path))) {
                RestoreAppBackupUseCase.Result.Restored -> {
                    _effects.tryEmit(SettingsViewEffect.RestoreReadyToQuit)
                }
                RestoreAppBackupUseCase.Result.UnsupportedVersion -> {
                    setTransferring(false)
                    clearPendingRestore()
                    _effects.tryEmit(
                        SettingsViewEffect.Failed("This backup was made with a newer WorldWeaver version")
                    )
                }
                RestoreAppBackupUseCase.Result.InvalidArchive -> {
                    setTransferring(false)
                    clearPendingRestore()
                    _effects.tryEmit(SettingsViewEffect.Failed("That file is not a valid WorldWeaver backup"))
                }
                is RestoreAppBackupUseCase.Result.Failed -> {
                    setTransferring(false)
                    clearPendingRestore()
                    _effects.tryEmit(SettingsViewEffect.Failed(result.message))
                }
            }
        }
    }

    private fun clearPendingRestore() {
        updateContent { current ->
            current.copy(pendingRestorePath = null)
        }
    }

    private fun importSrd(source: ImportSrdCatalogUseCase.Source) {
        val content = _state.value as? SettingsViewState.Content ?: return
        if (content.isTransferring) {
            return
        }
        if (source is ImportSrdCatalogUseCase.Source.File && source.file.path.isBlank()) {
            _effects.tryEmit(SettingsViewEffect.Failed("Choose an SRD JSON file"))
            return
        }
        setTransferring(true)
        appScope.scope.launch {
            when (val result = importSrdCatalog(source)) {
                is ImportSrdCatalogUseCase.Result.Imported -> {
                    updateContent { current ->
                        current.copy(srdStatus = statusFrom(result.catalog))
                    }
                    setTransferring(false)
                    _effects.tryEmit(SettingsViewEffect.SrdImported)
                }
                ImportSrdCatalogUseCase.Result.InvalidFile -> {
                    setTransferring(false)
                    _effects.tryEmit(SettingsViewEffect.Failed("That file is not a valid WorldWeaver SRD catalog"))
                }
                is ImportSrdCatalogUseCase.Result.Failed -> {
                    setTransferring(false)
                    _effects.tryEmit(SettingsViewEffect.Failed(result.message))
                }
            }
        }
    }

    private fun requestClearSrd() {
        val content = _state.value as? SettingsViewState.Content ?: return
        if (content.isTransferring || content.srdStatus !is SettingsViewState.SrdStatus.Imported) {
            return
        }
        updateContent { current ->
            current.copy(pendingClearSrd = true)
        }
    }

    private fun confirmClearSrd() {
        val content = _state.value as? SettingsViewState.Content ?: return
        if (content.isTransferring) {
            return
        }
        setTransferring(true)
        appScope.scope.launch {
            clearSrdCatalog()
            updateContent { current ->
                current.copy(srdStatus = SettingsViewState.SrdStatus.BundledPickers)
            }
            setTransferring(false)
            clearPendingClearSrd()
            _effects.tryEmit(SettingsViewEffect.SrdCleared)
        }
    }

    private fun clearPendingClearSrd() {
        updateContent { current ->
            current.copy(pendingClearSrd = false)
        }
    }

    private fun setTransferring(isTransferring: Boolean) {
        updateContent { current ->
            current.copy(isTransferring = isTransferring)
        }
    }

    private fun updateContent(transform: (SettingsViewState.Content) -> SettingsViewState.Content) {
        _state.update { current ->
            when (current) {
                is SettingsViewState.Content -> transform(current)
            }
        }
    }

    private fun syncFromStore(settings: ShellSettings) {
        _state.update { current ->
            val content = current as? SettingsViewState.Content
            if (content != null && content.isProfileDirty) {
                content.copy(
                    themeMode = settings.themeMode,
                    themeSkin = settings.themeSkin,
                    navExpanded = settings.navExpanded,
                    savedDisplayName = settings.displayName,
                    savedEmail = settings.email,
                )
            } else if (content != null) {
                contentFrom(settings).copy(
                    profileError = content.profileError,
                    isTransferring = content.isTransferring,
                    pendingRestorePath = content.pendingRestorePath,
                    srdStatus = content.srdStatus,
                    pendingClearSrd = content.pendingClearSrd,
                )
            } else {
                contentFrom(settings)
            }
        }
    }

    private companion object {
        fun contentFrom(settings: ShellSettings): SettingsViewState.Content {
            return SettingsViewState.Content(
                themeMode = settings.themeMode,
                themeSkin = settings.themeSkin,
                navExpanded = settings.navExpanded,
                draftDisplayName = settings.displayName,
                draftEmail = settings.email,
                savedDisplayName = settings.displayName,
                savedEmail = settings.email,
            )
        }

        fun statusFrom(catalog: SrdCatalog?): SettingsViewState.SrdStatus {
            if (catalog == null) {
                return SettingsViewState.SrdStatus.BundledPickers
            }
            return SettingsViewState.SrdStatus.Imported(
                sourceLabel = catalog.sourceLabel,
                importedAtEpochMillis = catalog.importedAt.toEpochMilli(),
                raceCount = catalog.races.size,
                classCount = catalog.classes.size,
                spellCount = catalog.spells.size,
                monsterCount = catalog.monsters.size,
            )
        }
    }
}
