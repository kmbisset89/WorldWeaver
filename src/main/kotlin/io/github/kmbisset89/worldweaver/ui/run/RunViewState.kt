package io.github.kmbisset89.worldweaver.ui.run

import io.github.kmbisset89.worldweaver.ui.characters.PersonMembership

internal sealed class RunViewState {
    data object Loading : RunViewState()

    data class Error(
        val message: String,
        val canRetry: Boolean,
    ) : RunViewState()

    data object NoActiveWorld : RunViewState()

    data object NoActiveCampaign : RunViewState()

    data class NoActiveSession(
        val worldName: String,
        val campaignName: String,
    ) : RunViewState()

    data class Content(
        val worldName: String,
        val campaignName: String,
        val sessionId: String,
        val sessionName: String,
        val sessionNotes: String,
        val recap: String,
        val inWorldDateLabel: String?,
        val calendarTodayLabel: String?,
        val party: List<PartyMember>,
        val questObjectives: List<QuestObjectiveLine>,
        val scenes: List<SceneLine>,
        val activeEncounter: EncounterLine?,
        val partyLocations: List<String>,
        val whyItMatters: String,
        val isClosing: Boolean,
        val closeError: String?,
    ) : RunViewState()

    data class PartyMember(
        val personId: String,
        val membership: PersonMembership,
        val name: String,
        val hitPoints: Int,
        val maxHitPoints: Int,
        val armorClass: Int,
        val concentratingSpell: String,
        val spellSlotsLabel: String,
    )

    data class QuestObjectiveLine(
        val questTitle: String,
        val objectiveTitle: String,
        val status: String,
    )

    data class SceneLine(
        val title: String,
        val notes: String,
    )

    data class EncounterLine(
        val name: String,
        val status: String,
        val hasMap: Boolean,
        val roundLabel: String?,
    )
}
