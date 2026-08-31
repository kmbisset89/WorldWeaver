package net.tactware.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface QuestObjectiveDao {
    @Query("SELECT * FROM quest_objectives WHERE questId = :questId ORDER BY sortIndex ASC")
    suspend fun getByQuest(questId: String): List<QuestObjectiveEntity>

    @Query(
        """
        SELECT objectives.* FROM quest_objectives AS objectives
        INNER JOIN quests ON quests.id = objectives.questId
        WHERE quests.campaignId = :campaignId
        ORDER BY objectives.questId ASC, objectives.sortIndex ASC
        """
    )
    fun observeByCampaign(campaignId: String): Flow<List<QuestObjectiveEntity>>

    @Query(
        """
        SELECT objectives.* FROM quest_objectives AS objectives
        INNER JOIN quests ON quests.id = objectives.questId
        WHERE quests.campaignId = :campaignId
        ORDER BY objectives.questId ASC, objectives.sortIndex ASC
        """
    )
    suspend fun getByCampaign(campaignId: String): List<QuestObjectiveEntity>

    @Insert
    suspend fun insertAll(entities: List<QuestObjectiveEntity>)

    @Query("DELETE FROM quest_objectives WHERE questId = :questId")
    suspend fun deleteByQuest(questId: String)
}
