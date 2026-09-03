package io.github.kmbisset89.worldweaver.ui.quests

internal sealed interface QuestsViewEffect {
    data object OpenWorlds : QuestsViewEffect
    data object OpenCampaigns : QuestsViewEffect
    data object OpenLocations : QuestsViewEffect
    data class OpenLore(val loreId: String) : QuestsViewEffect
    data object OpenCharacters : QuestsViewEffect
    data class OpenSession(val sessionId: String) : QuestsViewEffect
}
