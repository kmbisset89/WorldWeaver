package io.github.kmbisset89.worldweaver.ui.run

import io.github.kmbisset89.worldweaver.ui.characters.PersonMembership

internal sealed interface RunViewEffect {
    data object OpenWorlds : RunViewEffect
    data object OpenCampaigns : RunViewEffect
    data object OpenSessions : RunViewEffect
    data object OpenEncounters : RunViewEffect
    data object OpenMaps : RunViewEffect
    data object OpenPlayerView : RunViewEffect
    data object OpenDiceTray : RunViewEffect
    data class OpenPersonSheet(
        val membership: PersonMembership,
        val personId: String,
    ) : RunViewEffect
}
