package net.tactware.worldweaver.ui.campaigns

internal sealed interface CampaignsViewEffect {
    data object OpenWorlds : CampaignsViewEffect
}
