package net.tactware.worldweaver.ui.encounters

internal sealed interface EncountersViewEffect {
    data object OpenWorlds : EncountersViewEffect
    data object OpenCampaigns : EncountersViewEffect
    data object OpenLocations : EncountersViewEffect
    data class OpenMap(val battleMapId: String) : EncountersViewEffect
}
