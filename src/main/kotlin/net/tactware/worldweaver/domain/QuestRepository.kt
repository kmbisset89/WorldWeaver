package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal interface QuestRepository {
    fun observeByCampaign(campaignId: String): Flow<List<Quest>>
    suspend fun getById(id: String): Quest?
    suspend fun getByCampaign(campaignId: String): List<Quest>
    suspend fun search(query: String): List<Quest>
    suspend fun insert(quest: Quest)
    suspend fun update(quest: Quest)
    suspend fun delete(id: String)
    suspend fun deleteLinksByTarget(kind: QuestLinkKind, targetId: String)
}
