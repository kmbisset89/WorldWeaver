package io.github.kmbisset89.worldweaver.domain

import java.util.prefs.Preferences

internal class DiceColorStyleStore(
    private val preferences: Preferences,
) {
    fun loadName(): String {
        return resolveName(preferences.get(PREF_KEY, DEFAULT_NAME))
    }

    fun saveName(name: String) {
        preferences.put(PREF_KEY, resolveName(name))
    }

    private fun resolveName(name: String): String {
        return if (name in KNOWN_NAMES) name else DEFAULT_NAME
    }

    companion object {
        const val PREF_KEY = "dice_color_style"
        const val DEFAULT_NAME = "BONE"
        private val KNOWN_NAMES = setOf("BONE", "ONYX", "CRIMSON", "FOREST", "AZURE")
    }
}
