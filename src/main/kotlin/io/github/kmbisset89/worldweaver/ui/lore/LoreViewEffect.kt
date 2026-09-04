package io.github.kmbisset89.worldweaver.ui.lore

internal sealed interface LoreViewEffect {
    data object OpenWorlds : LoreViewEffect
    data class OpenCalendar(val observanceId: String) : LoreViewEffect
}
