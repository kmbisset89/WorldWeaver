package io.github.kmbisset89.worldweaver.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
internal interface LocationOverlayDao {
    @Query("SELECT * FROM location_overlays WHERE campaignId = :campaignId")
    fun observeByCampaign(campaignId: String): Flow<List<LocationOverlayEntity>>

    @Query(
        "SELECT * FROM location_overlays WHERE campaignId = :campaignId AND locationId = :locationId"
    )
    suspend fun get(campaignId: String, locationId: String): LocationOverlayEntity?

    @Query("SELECT * FROM location_overlays WHERE campaignId = :campaignId")
    suspend fun getByCampaign(campaignId: String): List<LocationOverlayEntity>

    @Upsert
    suspend fun upsert(entity: LocationOverlayEntity)

    @Query("DELETE FROM location_overlays WHERE locationId = :locationId")
    suspend fun deleteByLocation(locationId: String)
}
