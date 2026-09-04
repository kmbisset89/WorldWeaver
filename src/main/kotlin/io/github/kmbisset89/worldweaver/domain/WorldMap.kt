package io.github.kmbisset89.worldweaver.domain

import java.time.Instant

internal data class WorldMap(
    val id: String,
    val worldId: String,
    val locationId: String?,
    val originalWidth: Int,
    val originalHeight: Int,
    val tileSizePx: Int,
    val minZoom: Int,
    val maxZoom: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    val levelCount: Int
        get() = (maxZoom - minZoom + 1).coerceAtLeast(1)
}
