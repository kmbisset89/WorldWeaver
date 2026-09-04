package io.github.kmbisset89.worldweaver.ui.locations

internal sealed interface LocationsViewEffect {
    data object OpenWorlds : LocationsViewEffect
    data class OpenLore(val loreId: String) : LocationsViewEffect
    data class OpenQuest(val questId: String) : LocationsViewEffect
    data class OpenWorldMap(val locationId: String?) : LocationsViewEffect
}
