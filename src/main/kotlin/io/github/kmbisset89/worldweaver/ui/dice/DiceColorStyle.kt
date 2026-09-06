package io.github.kmbisset89.worldweaver.ui.dice

import androidx.compose.ui.graphics.Color
import io.github.kmbisset89.worldweaver.domain.DiceColorStyleStore
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
        fun load(preferences: Preferences = Preferences.userRoot()): DiceColorStyle {
            val stored = DiceColorStyleStore(preferences).loadName()
            return entries.firstOrNull { it.name == stored } ?: BONE
        }

        fun save(style: DiceColorStyle, preferences: Preferences = Preferences.userRoot()) {
            DiceColorStyleStore(preferences).saveName(style.name)
        }
    }
}
