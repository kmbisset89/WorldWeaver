package io.github.kmbisset89.worldweaver.ui.quests

import io.github.kmbisset89.worldweaver.domain.SearchHit

internal sealed interface QuestsViewEffect {
    data object OpenWorlds : QuestsViewEffect
    data object OpenCampaigns : QuestsViewEffect
    data object OpenLocations : QuestsViewEffect
    data class OpenLore(val loreId: String) : QuestsViewEffect
    data object OpenCharacters : QuestsViewEffect
    data class OpenSession(val sessionId: String) : QuestsViewEffect
    data class OpenSearchHit(val hit: SearchHit) : QuestsViewEffect
}
