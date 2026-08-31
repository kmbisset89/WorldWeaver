package net.tactware.worldweaver.ui.dice

import androidx.compose.ui.graphics.Color
import java.util.prefs.Preferences

internal enum class DiceColorStyle(
    val displayName: String,
    val body: Color,
    val pip: Color,
) {
    BONE("Bone", Color(0xFFF4E8D0), Color(0xFF2A2118)),
    ONYX("Onyx", Color(0xFF1C1C1E), Color(0xFFF2F2F2)),
    CRIMSON("Crimson", Color(0xFF7B141B), Color(0xFFFBF0F1)),
    FOREST("Forest", Color(0xFF1F4D32), Color(0xFFE8F5EC)),
    AZURE("Azure", Color(0xFF1A3A5C), Color(0xFFE8F1F8)),
    ;

    companion object {
        private const val PREF_KEY = "dice_color_style"

        fun load(preferences: Preferences = Preferences.userRoot()): DiceColorStyle {
            val stored = preferences.get(PREF_KEY, BONE.name)
            return entries.firstOrNull { it.name == stored } ?: BONE
        }

        fun save(style: DiceColorStyle, preferences: Preferences = Preferences.userRoot()) {
            preferences.put(PREF_KEY, style.name)
        }
    }
}
