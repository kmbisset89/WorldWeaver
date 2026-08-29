package net.tactware.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface CampaignDao {
    @Query("SELECT * FROM campaigns WHERE worldId = :worldId ORDER BY updatedAtEpochMillis DESC")
    fun observeByWorld(worldId: String): Flow<List<CampaignEntity>>

    @Query("SELECT * FROM campaigns WHERE id = :id")
    fun observeById(id: String): Flow<CampaignEntity?>

    @Query("SELECT * FROM campaigns WHERE id = :id")
    suspend fun getById(id: String): CampaignEntity?

    @Query("SELECT COUNT(*) FROM campaigns WHERE worldId = :worldId")
    suspend fun countByWorld(worldId: String): Int

    @Insert
    suspend fun insert(entity: CampaignEntity)

    @Update
    suspend fun update(entity: CampaignEntity)

    @Query("DELETE FROM campaigns WHERE id = :id")
    suspend fun delete(id: String)
}
