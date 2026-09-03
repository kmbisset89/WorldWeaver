package io.github.kmbisset89.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface EncounterDao {
    @Query("SELECT * FROM encounters WHERE campaignId = :campaignId ORDER BY name ASC")
    fun observeByCampaign(campaignId: String): Flow<List<EncounterEntity>>

    @Query("SELECT * FROM encounters WHERE campaignId = :campaignId ORDER BY name ASC")
    suspend fun getByCampaign(campaignId: String): List<EncounterEntity>

    @Query("SELECT * FROM encounters WHERE id = :id")
    suspend fun getById(id: String): EncounterEntity?

    @Insert
    suspend fun insert(entity: EncounterEntity)

    @Update
    suspend fun update(entity: EncounterEntity)

    @Query("DELETE FROM encounters WHERE id = :id")
    suspend fun delete(id: String)
}
