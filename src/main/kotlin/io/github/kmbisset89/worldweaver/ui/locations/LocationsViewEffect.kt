package io.github.kmbisset89.worldweaver.ui.locations

import io.github.kmbisset89.worldweaver.domain.SearchHit

internal sealed interface LocationsViewEffect {
    data object OpenWorlds : LocationsViewEffect
    data class OpenLore(val loreId: String) : LocationsViewEffect
    data class OpenQuest(val questId: String) : LocationsViewEffect
    data class OpenWorldMap(val locationId: String?) : LocationsViewEffect
    data class OpenSearchHit(val hit: SearchHit) : LocationsViewEffect
}
