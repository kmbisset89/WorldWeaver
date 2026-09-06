package io.github.kmbisset89.worldweaver.domain

internal data class ShellSettings(
    val themeMode: ThemeMode,
    val themeSkin: ThemeSkin,
    val displayName: String,
    val email: String,
    val navExpanded: Boolean,
) {
    companion object {
        const val DEFAULT_DISPLAY_NAME = "Local Author"
        const val DEFAULT_EMAIL = "author@local"
    }
}
