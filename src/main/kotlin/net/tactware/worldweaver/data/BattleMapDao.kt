package net.tactware.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface BattleMapDao {
    @Query("SELECT * FROM battle_maps WHERE campaignId = :campaignId ORDER BY name ASC")
    fun observeByCampaign(campaignId: String): Flow<List<BattleMapEntity>>

    @Query("SELECT * FROM battle_maps WHERE campaignId = :campaignId ORDER BY name ASC")
    suspend fun getByCampaign(campaignId: String): List<BattleMapEntity>

    @Query("SELECT * FROM battle_maps WHERE id = :id")
    suspend fun getById(id: String): BattleMapEntity?

    @Insert
    suspend fun insert(entity: BattleMapEntity)

    @Update
    suspend fun update(entity: BattleMapEntity)

    @Query("DELETE FROM battle_maps WHERE id = :id")
    suspend fun delete(id: String)
}
