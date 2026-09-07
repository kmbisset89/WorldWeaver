package io.github.kmbisset89.worldweaver.ui.lore

import io.github.kmbisset89.worldweaver.domain.SearchHit

internal sealed interface LoreViewEffect {
    data object OpenWorlds : LoreViewEffect
    data class OpenCalendar(val observanceId: String) : LoreViewEffect
    data class OpenSearchHit(val hit: SearchHit) : LoreViewEffect
}
