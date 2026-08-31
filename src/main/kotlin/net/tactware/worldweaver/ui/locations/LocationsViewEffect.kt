package net.tactware.worldweaver.ui.locations

internal sealed interface LocationsViewEffect {
    data object OpenWorlds : LocationsViewEffect
    data class OpenLore(val loreId: String) : LocationsViewEffect
    data class OpenQuest(val questId: String) : LocationsViewEffect
}
