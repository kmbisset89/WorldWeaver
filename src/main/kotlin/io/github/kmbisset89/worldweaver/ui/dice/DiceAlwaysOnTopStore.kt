package io.github.kmbisset89.worldweaver.ui.dice

import java.util.prefs.Preferences

internal object DiceAlwaysOnTopStore {
    private const val PREF_KEY = "dice_always_on_top"

    fun load(preferences: Preferences = Preferences.userRoot()): Boolean {
        return preferences.getBoolean(PREF_KEY, false)
    }

    fun save(alwaysOnTop: Boolean, preferences: Preferences = Preferences.userRoot()) {
        preferences.putBoolean(PREF_KEY, alwaysOnTop)
    }
}
