package net.tactware.worldweaver.ui.settings

import net.tactware.worldweaver.ui.theme.ThemeMode
import net.tactware.worldweaver.ui.theme.ThemeSkin

internal sealed class SettingsViewState {
    data class Content(
        val themeMode: ThemeMode,
        val themeSkin: ThemeSkin,
        val navExpanded: Boolean,
        val draftDisplayName: String,
        val draftEmail: String,
        val savedDisplayName: String,
        val savedEmail: String,
        val profileError: String? = null,
        val isTransferring: Boolean = false,
        val pendingRestorePath: String? = null,
        val srdStatus: SrdStatus = SrdStatus.BundledPickers,
        val pendingClearSrd: Boolean = false,
    ) : SettingsViewState() {
        val isProfileDirty: Boolean
            get() = draftDisplayName != savedDisplayName || draftEmail != savedEmail
    }

    sealed interface SrdStatus {
        data object BundledPickers : SrdStatus

        data class Imported(
            val sourceLabel: String,
            val importedAtEpochMillis: Long,
            val raceCount: Int,
            val classCount: Int,
            val spellCount: Int,
            val monsterCount: Int,
        ) : SrdStatus
    }
}
