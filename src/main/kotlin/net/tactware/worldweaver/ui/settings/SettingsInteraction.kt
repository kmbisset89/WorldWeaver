package net.tactware.worldweaver.ui.settings

import net.tactware.worldweaver.ui.theme.ThemeMode
import net.tactware.worldweaver.ui.theme.ThemeSkin

internal sealed interface SettingsInteraction {
    data object ScreenStarted : SettingsInteraction
    data class ThemeModeSelected(val themeMode: ThemeMode) : SettingsInteraction
    data class ThemeSkinSelected(val themeSkin: ThemeSkin) : SettingsInteraction
    data class NavExpandedChanged(val expanded: Boolean) : SettingsInteraction
    data class DisplayNameChanged(val displayName: String) : SettingsInteraction
    data class EmailChanged(val email: String) : SettingsInteraction
    data object ProfileSaved : SettingsInteraction
    data object ExportBackupSelected : SettingsInteraction
    data class ExportPathChosen(val path: String) : SettingsInteraction
    data object RestoreBackupSelected : SettingsInteraction
    data class RestorePathChosen(val path: String) : SettingsInteraction
    data object RestoreConfirmed : SettingsInteraction
    data object RestoreCancelled : SettingsInteraction
}
