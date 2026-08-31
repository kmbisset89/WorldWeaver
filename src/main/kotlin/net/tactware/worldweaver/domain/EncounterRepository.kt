package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal interface EncounterRepository {
    fun observeByCampaign(campaignId: String): Flow<List<Encounter>>
    suspend fun getById(id: String): Encounter?
    suspend fun getByCampaign(campaignId: String): List<Encounter>
    suspend fun insert(encounter: Encounter)
    suspend fun update(encounter: Encounter)
    suspend fun delete(id: String)
}
