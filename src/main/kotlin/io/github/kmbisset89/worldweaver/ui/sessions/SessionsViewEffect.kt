package io.github.kmbisset89.worldweaver.ui.sessions

import io.github.kmbisset89.worldweaver.domain.SearchHit

internal sealed interface SessionsViewEffect {
    data object OpenWorlds : SessionsViewEffect
    data object OpenCampaigns : SessionsViewEffect
    data class OpenQuest(val questId: String) : SessionsViewEffect
    data class OpenSearchHit(val hit: SearchHit) : SessionsViewEffect
}
