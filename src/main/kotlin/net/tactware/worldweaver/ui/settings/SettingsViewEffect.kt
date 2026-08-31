package net.tactware.worldweaver.ui.settings

internal sealed interface SettingsViewEffect {
    data object Exported : SettingsViewEffect
    data object RestoreReadyToQuit : SettingsViewEffect
    data class Failed(val message: String) : SettingsViewEffect
}
