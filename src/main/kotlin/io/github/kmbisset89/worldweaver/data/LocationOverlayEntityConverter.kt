package io.github.kmbisset89.worldweaver.data

import io.github.kmbisset89.worldweaver.domain.LocationOverlay
import java.time.Instant

internal class LocationOverlayEntityConverter {
    fun toOverlay(entity: LocationOverlayEntity): LocationOverlay {
        return LocationOverlay(
            campaignId = entity.campaignId,
            locationId = entity.locationId,
            hasPartyPresence = entity.hasPartyPresence,
            notes = entity.notes,
            updatedAt = Instant.ofEpochMilli(entity.updatedAtEpochMillis),
        )
    }

    fun toEntity(overlay: LocationOverlay): LocationOverlayEntity {
        return LocationOverlayEntity(
            campaignId = overlay.campaignId,
            locationId = overlay.locationId,
            hasPartyPresence = overlay.hasPartyPresence,
            notes = overlay.notes,
            updatedAtEpochMillis = overlay.updatedAt.toEpochMilli(),
        )
    }
}
