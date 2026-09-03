package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeLocationOverlayRepository : LocationOverlayRepository {
    private val overlays = MutableStateFlow<List<LocationOverlay>>(emptyList())

    fun all(): List<LocationOverlay> = overlays.value

    override fun observeByCampaign(campaignId: String): Flow<List<LocationOverlay>> {
        return overlays.map { list -> list.filter { it.campaignId == campaignId } }
    }

    override suspend fun get(campaignId: String, locationId: String): LocationOverlay? {
        return overlays.value.firstOrNull {
            it.campaignId == campaignId && it.locationId == locationId
        }
    }

    override suspend fun getByCampaign(campaignId: String): List<LocationOverlay> {
        return overlays.value.filter { it.campaignId == campaignId }
    }

    override suspend fun upsert(overlay: LocationOverlay) {
        val without = overlays.value.filterNot {
            it.campaignId == overlay.campaignId && it.locationId == overlay.locationId
        }
        overlays.value = without + overlay
    }

    override suspend fun deleteByLocation(locationId: String) {
        overlays.value = overlays.value.filterNot { it.locationId == locationId }
    }
}
