package io.github.kmbisset89.worldweaver.data

import io.github.kmbisset89.worldweaver.domain.CelestialBodyKind
import io.github.kmbisset89.worldweaver.domain.WorldCelestialBody
import java.time.Instant

internal class WorldCelestialBodyEntityConverter {
    fun toBody(entity: WorldCelestialBodyEntity): WorldCelestialBody {
        return WorldCelestialBody(
            id = entity.id,
            worldId = entity.worldId,
            name = entity.name,
            notes = entity.notes,
            kind = CelestialBodyKind.fromStorage(entity.kind),
            periodDays = entity.periodDays,
            epochOffsetDays = entity.epochOffsetDays,
            sortIndex = entity.sortIndex,
            createdAt = Instant.ofEpochMilli(entity.createdAtEpochMillis),
            updatedAt = Instant.ofEpochMilli(entity.updatedAtEpochMillis),
        )
    }

    fun toEntity(body: WorldCelestialBody): WorldCelestialBodyEntity {
        return WorldCelestialBodyEntity(
            id = body.id,
            worldId = body.worldId,
            name = body.name,
            notes = body.notes,
            kind = body.kind.name,
            periodDays = body.periodDays,
            epochOffsetDays = body.epochOffsetDays,
            sortIndex = body.sortIndex,
            createdAtEpochMillis = body.createdAt.toEpochMilli(),
            updatedAtEpochMillis = body.updatedAt.toEpochMilli(),
        )
    }
}
