package net.tactware.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface QuestDao {
    @Query("SELECT * FROM quests WHERE campaignId = :campaignId ORDER BY title ASC")
    fun observeByCampaign(campaignId: String): Flow<List<QuestEntity>>

    @Query("SELECT * FROM quests WHERE campaignId = :campaignId ORDER BY title ASC")
    suspend fun getByCampaign(campaignId: String): List<QuestEntity>

    @Query("SELECT * FROM quests WHERE id = :id")
    suspend fun getById(id: String): QuestEntity?

    @Query(
        """
        SELECT * FROM quests
        WHERE title LIKE '%' || :query || '%'
           OR summary LIKE '%' || :query || '%'
        ORDER BY title ASC
        """
    )
    suspend fun searchLike(query: String): List<QuestEntity>

    @Insert
    suspend fun insert(entity: QuestEntity)

    @Update
    suspend fun update(entity: QuestEntity)

    @Query("DELETE FROM quests WHERE id = :id")
    suspend fun delete(id: String)
}
