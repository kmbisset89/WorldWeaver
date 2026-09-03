package io.github.kmbisset89.worldweaver.ui.home

internal sealed interface HomeInteraction {
    data object ScreenStarted : HomeInteraction
    data object RetrySelected : HomeInteraction
    data object NewWorldSelected : HomeInteraction
    data object OneShotSelected : HomeInteraction
    data class WorldSelected(val worldId: String) : HomeInteraction
    data object ContinueCampaignSelected : HomeInteraction
    data object WorldsCountSelected : HomeInteraction
    data object CampaignsCountSelected : HomeInteraction
    data object PeopleCountSelected : HomeInteraction
}
