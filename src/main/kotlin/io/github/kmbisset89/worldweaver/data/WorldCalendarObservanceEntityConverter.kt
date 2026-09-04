package io.github.kmbisset89.worldweaver.data

import io.github.kmbisset89.worldweaver.domain.WorldCalendarObservance
import io.github.kmbisset89.worldweaver.domain.WorldCalendarObservanceKind
import java.time.Instant

internal class WorldCalendarObservanceEntityConverter {
    fun toObservance(
        entity: WorldCalendarObservanceEntity,
        loreIds: List<String>,
    ): WorldCalendarObservance {
        return WorldCalendarObservance(
            id = entity.id,
            worldId = entity.worldId,
            name = entity.name,
            notes = entity.notes,
            kind = WorldCalendarObservanceKind.fromStorage(entity.kind),
            monthId = entity.monthId,
            day = entity.day,
            year = entity.year,
            loreIds = loreIds,
            createdAt = Instant.ofEpochMilli(entity.createdAtEpochMillis),
            updatedAt = Instant.ofEpochMilli(entity.updatedAtEpochMillis),
        )
    }

    fun toEntity(observance: WorldCalendarObservance): WorldCalendarObservanceEntity {
        return WorldCalendarObservanceEntity(
            id = observance.id,
            worldId = observance.worldId,
            name = observance.name,
            notes = observance.notes,
            kind = observance.kind.name,
            monthId = observance.monthId,
            day = observance.day,
            year = observance.year,
            createdAtEpochMillis = observance.createdAt.toEpochMilli(),
            updatedAtEpochMillis = observance.updatedAt.toEpochMilli(),
        )
    }

    fun toLinkEntities(observance: WorldCalendarObservance): List<WorldCalendarObservanceLoreLinkEntity> {
        return observance.loreIds.distinct().map { loreId ->
            WorldCalendarObservanceLoreLinkEntity(
                observanceId = observance.id,
                loreId = loreId,
            )
        }
    }
}
