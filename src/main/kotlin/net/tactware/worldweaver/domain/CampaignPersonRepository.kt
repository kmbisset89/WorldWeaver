package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal interface CampaignPersonRepository {
    fun observeByCampaign(campaignId: String): Flow<List<CampaignPerson>>
    suspend fun getById(id: String): CampaignPerson?
    suspend fun getByCampaign(campaignId: String): List<CampaignPerson>
    suspend fun search(query: String): List<CampaignPerson>
    suspend fun countByWorldPerson(worldPersonId: String): Int
    fun observeCount(): Flow<Int>
    suspend fun insert(person: CampaignPerson)
    suspend fun update(person: CampaignPerson)
    suspend fun delete(id: String)
}
