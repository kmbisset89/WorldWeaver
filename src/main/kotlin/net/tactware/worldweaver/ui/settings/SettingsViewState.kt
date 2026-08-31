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
    ) : SettingsViewState() {
        val isProfileDirty: Boolean
            get() = draftDisplayName != savedDisplayName || draftEmail != savedEmail
    }
}
