package net.tactware.worldweaver.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
internal interface CampaignPersonDao {
    @Query("SELECT * FROM campaign_people WHERE campaignId = :campaignId ORDER BY name ASC")
    fun observeByCampaign(campaignId: String): Flow<List<CampaignPersonEntity>>

    @Query("SELECT * FROM campaign_people WHERE campaignId = :campaignId ORDER BY name ASC")
    suspend fun getByCampaign(campaignId: String): List<CampaignPersonEntity>

    @Query("SELECT * FROM campaign_people WHERE id = :id")
    suspend fun getById(id: String): CampaignPersonEntity?

    @Query(
        """
        SELECT * FROM campaign_people
        WHERE name LIKE '%' || :query || '%'
           OR description LIKE '%' || :query || '%'
        ORDER BY name ASC
        """
    )
    suspend fun searchLike(query: String): List<CampaignPersonEntity>

    @Query("SELECT COUNT(*) FROM campaign_people WHERE worldPersonId = :worldPersonId")
    suspend fun countByWorldPerson(worldPersonId: String): Int

    @Query("SELECT COUNT(*) FROM campaign_people")
    fun observeCount(): Flow<Int>

    @Insert
    suspend fun insert(entity: CampaignPersonEntity)

    @Update
    suspend fun update(entity: CampaignPersonEntity)

    @Query("DELETE FROM campaign_people WHERE id = :id")
    suspend fun delete(id: String)
}
