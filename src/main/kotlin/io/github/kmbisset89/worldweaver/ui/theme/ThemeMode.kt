package io.github.kmbisset89.worldweaver.ui.theme

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
}
