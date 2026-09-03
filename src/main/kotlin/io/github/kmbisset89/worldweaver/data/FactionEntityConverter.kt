package io.github.kmbisset89.worldweaver.data

import io.github.kmbisset89.worldweaver.domain.Faction
import java.time.Instant

internal class FactionEntityConverter {
    fun toFaction(entity: FactionEntity): Faction {
        return Faction(
            id = entity.id,
            worldId = entity.worldId,
            name = entity.name,
            description = entity.description,
            goals = entity.goals,
            notes = entity.notes,
            createdAt = Instant.ofEpochMilli(entity.createdAtEpochMillis),
            updatedAt = Instant.ofEpochMilli(entity.updatedAtEpochMillis),
        )
    }

    fun toEntity(faction: Faction): FactionEntity {
        return FactionEntity(
            id = faction.id,
            worldId = faction.worldId,
            name = faction.name,
            description = faction.description,
            goals = faction.goals,
            notes = faction.notes,
            createdAtEpochMillis = faction.createdAt.toEpochMilli(),
            updatedAtEpochMillis = faction.updatedAt.toEpochMilli(),
        )
    }
}
