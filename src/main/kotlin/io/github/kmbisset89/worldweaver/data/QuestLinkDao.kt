package io.github.kmbisset89.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface QuestLinkDao {
    @Query("SELECT * FROM quest_links WHERE questId = :questId")
    suspend fun getByQuest(questId: String): List<QuestLinkEntity>

    @Query(
        """
        SELECT links.* FROM quest_links AS links
        INNER JOIN quests ON quests.id = links.questId
        WHERE quests.campaignId = :campaignId
        ORDER BY links.questId ASC
        """
    )
    fun observeByCampaign(campaignId: String): Flow<List<QuestLinkEntity>>

    @Query(
        """
        SELECT links.* FROM quest_links AS links
        INNER JOIN quests ON quests.id = links.questId
        WHERE quests.campaignId = :campaignId
        ORDER BY links.questId ASC
        """
    )
    suspend fun getByCampaign(campaignId: String): List<QuestLinkEntity>

    @Insert
    suspend fun insertAll(entities: List<QuestLinkEntity>)

    @Query("DELETE FROM quest_links WHERE questId = :questId")
    suspend fun deleteByQuest(questId: String)

    @Query("DELETE FROM quest_links WHERE kind = :kind AND targetId = :targetId")
    suspend fun deleteByTarget(kind: String, targetId: String)
}
