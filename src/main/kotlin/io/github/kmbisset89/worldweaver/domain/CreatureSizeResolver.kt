package io.github.kmbisset89.worldweaver.domain

internal class CreatureSizeResolver {
    fun resolve(participant: EncounterParticipant, people: PeopleSnapshot): CreatureSize {
        val sourceId = participant.sourceId ?: return CreatureSize.Medium
        return when (participant.source) {
            EncounterParticipantSource.WorldPerson -> {
                people.worldPeople.firstOrNull { it.id == sourceId }?.sheet?.creatureSize()
                    ?: CreatureSize.Medium
            }
            EncounterParticipantSource.CampaignPerson -> {
                val campaignPerson = people.campaignPeople.firstOrNull { it.id == sourceId }
                    ?: return CreatureSize.Medium
                campaignPerson.sheet.creatureSize()
            }
            EncounterParticipantSource.Nameless -> CreatureSize.Medium
        }
    }
}
