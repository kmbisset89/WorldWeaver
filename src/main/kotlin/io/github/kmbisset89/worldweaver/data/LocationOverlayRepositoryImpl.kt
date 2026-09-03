package io.github.kmbisset89.worldweaver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import io.github.kmbisset89.worldweaver.domain.LocationOverlay
import io.github.kmbisset89.worldweaver.domain.LocationOverlayRepository

internal class LocationOverlayRepositoryImpl(
    private val locationOverlayDao: LocationOverlayDao,
    private val converter: LocationOverlayEntityConverter,
) : LocationOverlayRepository {
    override fun observeByCampaign(campaignId: String): Flow<List<LocationOverlay>> {
        return locationOverlayDao.observeByCampaign(campaignId).map { entities ->
            entities.map { converter.toOverlay(it) }
        }
    }

    override suspend fun get(campaignId: String, locationId: String): LocationOverlay? {
        return locationOverlayDao.get(campaignId, locationId)?.let { converter.toOverlay(it) }
    }

    override suspend fun getByCampaign(campaignId: String): List<LocationOverlay> {
        return locationOverlayDao.getByCampaign(campaignId).map { converter.toOverlay(it) }
    }

    override suspend fun upsert(overlay: LocationOverlay) {
        locationOverlayDao.upsert(converter.toEntity(overlay))
    }

    override suspend fun deleteByLocation(locationId: String) {
        locationOverlayDao.deleteByLocation(locationId)
    }
}
