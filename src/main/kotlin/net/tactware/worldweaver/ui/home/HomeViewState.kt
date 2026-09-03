package net.tactware.worldweaver.ui.home

import net.tactware.worldweaver.domain.World

internal sealed class HomeViewState {
    data object Loading : HomeViewState()

    data class Error(
        val message: String,
        val canRetry: Boolean,
    ) : HomeViewState()

    data class Empty(
        val displayName: String,
    ) : HomeViewState()

    data class Content(
        val displayName: String,
        val recentWorlds: List<World>,
        val continueCampaign: ContinueCampaign?,
        val worldCount: Int,
        val campaignCount: Int,
        val peopleCount: Int,
    ) : HomeViewState()

    data class ContinueCampaign(
        val campaignName: String,
        val worldName: String,
        val sessionName: String?,
    )
}
