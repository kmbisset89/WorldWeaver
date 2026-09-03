package net.tactware.worldweaver.ui.home

internal sealed interface HomeViewEffect {
    data object OpenWorldCreator : HomeViewEffect
    data object OpenOneShotWizard : HomeViewEffect
    data object OpenWorlds : HomeViewEffect
    data object OpenCampaigns : HomeViewEffect
    data object OpenCharacters : HomeViewEffect
    data object OpenRun : HomeViewEffect
}
