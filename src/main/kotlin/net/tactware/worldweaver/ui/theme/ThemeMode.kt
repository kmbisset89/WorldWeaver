package net.tactware.worldweaver.ui.theme

import java.util.prefs.Preferences

internal enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM;

    fun next(): ThemeMode = when (this) {
        LIGHT -> DARK
        DARK -> SYSTEM
        SYSTEM -> LIGHT
    }

    fun label(): String = when (this) {
        LIGHT -> "Light"
        DARK -> "Dark"
        SYSTEM -> "System"
    }

    companion object {
        private const val PREF_KEY = "theme_mode"

        fun load(): ThemeMode {
            val stored = Preferences.userRoot().get(PREF_KEY, SYSTEM.name)
            return entries.firstOrNull { it.name == stored } ?: SYSTEM
        }

        fun save(mode: ThemeMode) {
            Preferences.userRoot().put(PREF_KEY, mode.name)
        }
    }
}
