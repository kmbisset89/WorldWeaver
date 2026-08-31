package net.tactware.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface ReferenceDocDao {
    @Query("SELECT * FROM reference_docs WHERE campaignId = :campaignId ORDER BY title ASC")
    fun observeByCampaign(campaignId: String): Flow<List<ReferenceDocEntity>>

    @Query("SELECT * FROM reference_docs WHERE id = :id")
    suspend fun getById(id: String): ReferenceDocEntity?

    @Query("SELECT * FROM reference_docs WHERE campaignId = :campaignId ORDER BY title ASC")
    suspend fun getByCampaign(campaignId: String): List<ReferenceDocEntity>

    @Insert
    suspend fun insert(entity: ReferenceDocEntity)

    @Update
    suspend fun update(entity: ReferenceDocEntity)

    @Query("DELETE FROM reference_docs WHERE id = :id")
    suspend fun delete(id: String)
}
