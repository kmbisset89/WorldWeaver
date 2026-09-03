package net.tactware.worldweaver.ui.encounters

import net.tactware.worldweaver.domain.EncounterParticipantSource

internal sealed interface EncountersViewEffect {
    data object OpenWorlds : EncountersViewEffect
    data object OpenCampaigns : EncountersViewEffect
    data object OpenLocations : EncountersViewEffect
    data class OpenMap(val battleMapId: String) : EncountersViewEffect
    data class OpenSheet(
        val source: EncounterParticipantSource,
        val sourceId: String?,
    ) : EncountersViewEffect
}
