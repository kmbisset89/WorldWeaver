package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeCampaignPersonRepository : CampaignPersonRepository {
    private val people = MutableStateFlow<List<CampaignPerson>>(emptyList())

    fun all(): List<CampaignPerson> = people.value

    override fun observeByCampaign(campaignId: String): Flow<List<CampaignPerson>> {
        return people.map { list -> list.filter { it.campaignId == campaignId } }
    }

    override suspend fun getById(id: String): CampaignPerson? {
        return people.value.firstOrNull { it.id == id }
    }

    override suspend fun getByCampaign(campaignId: String): List<CampaignPerson> {
        return people.value.filter { it.campaignId == campaignId }
    }

    override suspend fun search(query: String): List<CampaignPerson> {
        return people.value.filter { person ->
            person.name.contains(query, ignoreCase = true) ||
                person.description.contains(query, ignoreCase = true)
        }
    }

    override suspend fun countByWorldPerson(worldPersonId: String): Int {
        return people.value.count { it.worldPersonId == worldPersonId }
    }

    override fun observeCount(): Flow<Int> {
        return people.map { it.size }
    }

    override suspend fun insert(person: CampaignPerson) {
        people.value = people.value + person
    }

    override suspend fun update(person: CampaignPerson) {
        people.value = people.value.map { current ->
            if (current.id == person.id) person else current
        }
    }

    override suspend fun delete(id: String) {
        people.value = people.value.filterNot { it.id == id }
    }
}
