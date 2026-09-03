package net.tactware.worldweaver.ui.run

import net.tactware.worldweaver.ui.characters.PersonMembership

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
