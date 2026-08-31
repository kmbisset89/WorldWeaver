package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal interface LocationOverlayRepository {
    fun observeByCampaign(campaignId: String): Flow<List<LocationOverlay>>
    suspend fun get(campaignId: String, locationId: String): LocationOverlay?
    suspend fun getByCampaign(campaignId: String): List<LocationOverlay>
    suspend fun upsert(overlay: LocationOverlay)
    suspend fun deleteByLocation(locationId: String)
}
