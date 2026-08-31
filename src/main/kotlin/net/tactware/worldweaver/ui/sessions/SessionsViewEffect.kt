package net.tactware.worldweaver.ui.sessions

internal sealed interface SessionsViewEffect {
    data object OpenWorlds : SessionsViewEffect
    data object OpenCampaigns : SessionsViewEffect
    data class OpenQuest(val questId: String) : SessionsViewEffect
}
