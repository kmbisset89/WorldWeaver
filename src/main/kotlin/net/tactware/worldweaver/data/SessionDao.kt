package net.tactware.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface SessionDao {
    @Query("SELECT * FROM sessions WHERE campaignId = :campaignId ORDER BY updatedAtEpochMillis DESC")
    fun observeByCampaign(campaignId: String): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE campaignId = :campaignId ORDER BY updatedAtEpochMillis DESC")
    suspend fun getByCampaign(campaignId: String): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getById(id: String): SessionEntity?

    @Query(
        """
        SELECT * FROM sessions
        WHERE name LIKE '%' || :query || '%'
           OR notes LIKE '%' || :query || '%'
        ORDER BY name ASC
        """
    )
    suspend fun searchLike(query: String): List<SessionEntity>

    @Insert
    suspend fun insert(entity: SessionEntity)

    @Update
    suspend fun update(entity: SessionEntity)

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun delete(id: String)
}
