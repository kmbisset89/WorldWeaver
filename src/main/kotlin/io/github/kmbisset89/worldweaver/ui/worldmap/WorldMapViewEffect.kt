package io.github.kmbisset89.worldweaver.ui.worldmap

internal sealed interface WorldMapViewEffect {
    data object OpenWorlds : WorldMapViewEffect
    data class OpenLocations(val locationId: String?) : WorldMapViewEffect
}
