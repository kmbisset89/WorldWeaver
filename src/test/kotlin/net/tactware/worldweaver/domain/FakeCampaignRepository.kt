package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeCampaignRepository : CampaignRepository {
    private val campaigns = MutableStateFlow<List<Campaign>>(emptyList())

    fun all(): List<Campaign> = campaigns.value

    override fun observeByWorld(worldId: String): Flow<List<Campaign>> {
        return campaigns.map { list -> list.filter { it.worldId == worldId } }
    }

    override fun observeById(id: String): Flow<Campaign?> {
        return campaigns.map { list -> list.firstOrNull { it.id == id } }
    }

    override suspend fun getById(id: String): Campaign? {
        return campaigns.value.firstOrNull { it.id == id }
    }

    override suspend fun getByWorld(worldId: String): List<Campaign> {
        return campaigns.value.filter { it.worldId == worldId }
    }

    override suspend fun search(query: String): List<Campaign> {
        return campaigns.value.filter { campaign ->
            campaign.name.contains(query, ignoreCase = true) ||
                campaign.description.contains(query, ignoreCase = true) ||
                campaign.notes.contains(query, ignoreCase = true)
        }
    }

    override suspend fun countByWorld(worldId: String): Int {
        return campaigns.value.count { it.worldId == worldId }
    }

    override fun observeCount(): Flow<Int> {
        return campaigns.map { it.size }
    }

    override suspend fun insert(campaign: Campaign) {
        campaigns.value = campaigns.value + campaign
    }

    override suspend fun update(campaign: Campaign) {
        campaigns.value = campaigns.value.map { current ->
            if (current.id == campaign.id) campaign else current
        }
    }

    override suspend fun delete(id: String) {
        campaigns.value = campaigns.value.filterNot { it.id == id }
    }
}
