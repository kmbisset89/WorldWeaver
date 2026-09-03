package io.github.kmbisset89.worldweaver.domain

import java.time.Instant

internal data class LocationOverlay(
    val campaignId: String,
    val locationId: String,
    val hasPartyPresence: Boolean,
    val notes: String,
    val updatedAt: Instant,
)
