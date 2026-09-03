package io.github.kmbisset89.worldweaver.ui.settings

import io.github.kmbisset89.worldweaver.ui.theme.ThemeMode
import io.github.kmbisset89.worldweaver.ui.theme.ThemeSkin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.prefs.Preferences

internal class ShellSettingsStore(
    private val preferences: Preferences,
) {
    private val _settings = MutableStateFlow(read())
    val settings: StateFlow<ShellSettings> = _settings.asStateFlow()

    fun setThemeMode(themeMode: ThemeMode) {
        preferences.put(KEY_THEME_MODE, themeMode.name)
        _settings.update { it.copy(themeMode = themeMode) }
    }

    fun setThemeSkin(themeSkin: ThemeSkin) {
        preferences.put(KEY_THEME_SKIN, themeSkin.name)
        _settings.update { it.copy(themeSkin = themeSkin) }
    }

    fun setNavExpanded(navExpanded: Boolean) {
        preferences.putBoolean(KEY_NAV_EXPANDED, navExpanded)
        _settings.update { it.copy(navExpanded = navExpanded) }
    }

    fun setProfile(displayName: String, email: String) {
        preferences.put(KEY_DISPLAY_NAME, displayName)
        preferences.put(KEY_EMAIL, email)
        _settings.update { it.copy(displayName = displayName, email = email) }
    }

    private fun read(): ShellSettings {
        return ShellSettings(
            themeMode = readThemeMode(),
            themeSkin = readThemeSkin(),
            displayName = readNonBlank(KEY_DISPLAY_NAME, ShellSettings.DEFAULT_DISPLAY_NAME),
            email = readNonBlank(KEY_EMAIL, ShellSettings.DEFAULT_EMAIL),
            navExpanded = preferences.getBoolean(KEY_NAV_EXPANDED, true),
        )
    }

    private fun readThemeMode(): ThemeMode {
        val stored = preferences.get(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return parseThemeMode(stored)
    }

    private fun readThemeSkin(): ThemeSkin {
        val stored = preferences.get(KEY_THEME_SKIN, ThemeSkin.FANTASY.name)
        return ThemeSkin.entries.firstOrNull { it.name == stored } ?: ThemeSkin.FANTASY
    }

    private fun parseThemeMode(raw: String): ThemeMode {
        return ThemeMode.entries.firstOrNull { it.name == raw } ?: ThemeMode.SYSTEM
    }

    private fun readNonBlank(key: String, default: String): String {
        return preferences.get(key, default).takeIf { it.isNotBlank() } ?: default
    }

    companion object {
        const val PREF_NODE = "io.github.kmbisset89.worldweaver"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_THEME_SKIN = "theme_skin"
        private const val KEY_NAV_EXPANDED = "nav_expanded"
        private const val KEY_DISPLAY_NAME = "local_display_name"
        private const val KEY_EMAIL = "local_email"
    }
}
