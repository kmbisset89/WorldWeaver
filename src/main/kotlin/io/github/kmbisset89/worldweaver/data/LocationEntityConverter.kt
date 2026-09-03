package io.github.kmbisset89.worldweaver.data

import io.github.kmbisset89.worldweaver.domain.Location
import io.github.kmbisset89.worldweaver.domain.LocationType
import java.time.Instant

internal class LocationEntityConverter {
    fun toLocation(entity: LocationEntity): Location {
        return Location(
            id = entity.id,
            worldId = entity.worldId,
            type = LocationType.fromStorage(entity.type),
            parentLocationId = entity.parentLocationId,
            name = entity.name,
            description = entity.description,
            climate = entity.climate,
            terrain = entity.terrain,
            government = entity.government,
            landmarks = decodeLandmarks(entity.landmarks),
            history = entity.history,
            notes = entity.notes,
            createdAt = Instant.ofEpochMilli(entity.createdAtEpochMillis),
            updatedAt = Instant.ofEpochMilli(entity.updatedAtEpochMillis),
        )
    }

    fun toEntity(location: Location): LocationEntity {
        return LocationEntity(
            id = location.id,
            worldId = location.worldId,
            type = location.type.name,
            parentLocationId = location.parentLocationId,
            name = location.name,
            description = location.description,
            climate = location.climate,
            terrain = location.terrain,
            government = location.government,
            landmarks = encodeLandmarks(location.landmarks),
            history = location.history,
            notes = location.notes,
            createdAtEpochMillis = location.createdAt.toEpochMilli(),
            updatedAtEpochMillis = location.updatedAt.toEpochMilli(),
        )
    }

    private fun encodeLandmarks(landmarks: List<String>): String {
        return landmarks.joinToString(LANDMARK_SEPARATOR)
    }

    private fun decodeLandmarks(value: String): List<String> {
        if (value.isEmpty()) {
            return emptyList()
        }
        return value.split(LANDMARK_SEPARATOR).filter { it.isNotEmpty() }
    }

    private companion object {
        const val LANDMARK_SEPARATOR = "\n"
    }
}
