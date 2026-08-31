package net.tactware.worldweaver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import net.tactware.worldweaver.domain.Encounter
import net.tactware.worldweaver.domain.EncounterRepository

internal class EncounterRepositoryImpl(
    private val encounterDao: EncounterDao,
    private val participantDao: EncounterParticipantDao,
    private val converter: EncounterEntityConverter,
) : EncounterRepository {
    override fun observeByCampaign(campaignId: String): Flow<List<Encounter>> {
        return combine(
            encounterDao.observeByCampaign(campaignId),
            participantDao.observeByCampaign(campaignId),
        ) { encounters, participants ->
            assemble(encounters, participants)
        }
    }

    override suspend fun getById(id: String): Encounter? {
        val entity = encounterDao.getById(id) ?: return null
        return converter.toEncounter(
            entity,
            converter.toParticipants(participantDao.getByEncounter(id)),
        )
    }

    override suspend fun getByCampaign(campaignId: String): List<Encounter> {
        return assemble(
            encounterDao.getByCampaign(campaignId),
            participantDao.getByCampaign(campaignId),
        )
    }

    override suspend fun insert(encounter: Encounter) {
        encounterDao.insert(converter.toEntity(encounter))
        replaceChildren(encounter)
    }

    override suspend fun update(encounter: Encounter) {
        encounterDao.update(converter.toEntity(encounter))
        replaceChildren(encounter)
    }

    override suspend fun delete(id: String) {
        encounterDao.delete(id)
    }

    private suspend fun replaceChildren(encounter: Encounter) {
        participantDao.deleteByEncounter(encounter.id)
        val participants = converter.toParticipantEntities(encounter)
        if (participants.isNotEmpty()) {
            participantDao.insertAll(participants)
        }
    }

    private fun assemble(
        encounters: List<EncounterEntity>,
        participants: List<EncounterParticipantEntity>,
    ): List<Encounter> {
        val participantsByEncounter = participants.groupBy { it.encounterId }
        return encounters.map { entity ->
            converter.toEncounter(
                entity,
                converter.toParticipants(participantsByEncounter[entity.id].orEmpty()),
            )
        }
    }
}
