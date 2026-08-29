package net.tactware.worldweaver.ui.home

internal sealed interface HomeInteraction {
    data object ScreenStarted : HomeInteraction
    data object RetrySelected : HomeInteraction
    data object NewWorldSelected : HomeInteraction
    data class WorldSelected(val worldId: String) : HomeInteraction
    data object ContinueCampaignSelected : HomeInteraction
}
