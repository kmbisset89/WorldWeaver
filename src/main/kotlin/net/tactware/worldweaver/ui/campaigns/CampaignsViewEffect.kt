package net.tactware.worldweaver.ui.campaigns

internal sealed interface CampaignsViewEffect {
    data object OpenWorlds : CampaignsViewEffect
    data object OpenCharacters : CampaignsViewEffect
    data object CreatePlayerCharacter : CampaignsViewEffect
    data object OpenQuests : CampaignsViewEffect
    data object OpenSessions : CampaignsViewEffect
}
