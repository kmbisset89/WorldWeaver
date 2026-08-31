package net.tactware.worldweaver.ui.maps

internal sealed interface MapsViewEffect {
    data object OpenWorlds : MapsViewEffect
    data object OpenCampaigns : MapsViewEffect
}
