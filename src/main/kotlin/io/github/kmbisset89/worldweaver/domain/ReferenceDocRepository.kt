package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal interface ReferenceDocRepository {
    fun observeByCampaign(campaignId: String): Flow<List<ReferenceDoc>>
    suspend fun getById(id: String): ReferenceDoc?
    suspend fun getByCampaign(campaignId: String): List<ReferenceDoc>
    suspend fun insert(doc: ReferenceDoc)
    suspend fun update(doc: ReferenceDoc)
    suspend fun delete(id: String)
}
