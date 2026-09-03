package io.github.kmbisset89.worldweaver.ui.settings

internal sealed interface SettingsViewEffect {
    data object Exported : SettingsViewEffect
    data object RestoreReadyToQuit : SettingsViewEffect
    data object SrdImported : SettingsViewEffect
    data object SrdCleared : SettingsViewEffect
    data class Failed(val message: String) : SettingsViewEffect
}
