package net.tactware.worldweaver.domain

internal class EncounterParticipantVisibilityResolver {
    fun isVisibleToPlayers(
        participant: EncounterParticipant,
        people: PeopleSnapshot,
    ): Boolean {
        if (!participant.visibleToPlayers) {
            return false
        }
        return isPlayerCharacter(participant, people) ||
            !hasInvisible(participant.conditions)
    }

    fun isPlayerCharacter(
        participant: EncounterParticipant,
        people: PeopleSnapshot,
    ): Boolean {
        val sourceId = participant.sourceId ?: return false
        val kind = when (participant.source) {
            EncounterParticipantSource.CampaignPerson -> {
                people.campaignPeople.firstOrNull { it.id == sourceId }?.kind
            }
            EncounterParticipantSource.WorldPerson -> {
                people.worldPeople.firstOrNull { it.id == sourceId }?.kind
            }
            EncounterParticipantSource.Nameless -> null
        }
        return kind == PersonKind.PlayerCharacter
    }

    fun visibleToPlayersAfterConditions(
        currentVisible: Boolean,
        currentConditions: List<String>,
        nextConditions: List<String>,
        isPlayerCharacter: Boolean,
    ): Boolean {
        if (isPlayerCharacter) {
            return currentVisible
        }
        val wasInvisible = hasInvisible(currentConditions)
        val nowInvisible = hasInvisible(nextConditions)
        return when {
            !wasInvisible && nowInvisible -> false
            wasInvisible && !nowInvisible -> true
            else -> currentVisible
        }
    }

    private fun hasInvisible(conditions: List<String>): Boolean {
        return conditions.any { label ->
            FifthEditionCondition.fromDisplayName(label) == FifthEditionCondition.Invisible
        }
    }
}
