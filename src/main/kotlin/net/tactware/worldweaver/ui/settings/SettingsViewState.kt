package net.tactware.worldweaver.ui.settings

internal sealed class SettingsViewState {
    data class Content(
        val themeLabel: String,
        val displayName: String,
        val email: String,
    ) : SettingsViewState()
}
