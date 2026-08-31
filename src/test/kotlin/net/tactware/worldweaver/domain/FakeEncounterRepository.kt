package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeEncounterRepository : EncounterRepository {
    private val encounters = MutableStateFlow<List<Encounter>>(emptyList())

    fun all(): List<Encounter> = encounters.value

    override fun observeByCampaign(campaignId: String): Flow<List<Encounter>> {
        return encounters.map { list -> list.filter { it.campaignId == campaignId } }
    }

    override suspend fun getById(id: String): Encounter? {
        return encounters.value.firstOrNull { it.id == id }
    }

    override suspend fun getByCampaign(campaignId: String): List<Encounter> {
        return encounters.value.filter { it.campaignId == campaignId }
    }

    override suspend fun insert(encounter: Encounter) {
        encounters.value = encounters.value + encounter
    }

    override suspend fun update(encounter: Encounter) {
        encounters.value = encounters.value.map { current ->
            if (current.id == encounter.id) encounter else current
        }
    }

    override suspend fun delete(id: String) {
        encounters.value = encounters.value.filterNot { it.id == id }
    }
}
