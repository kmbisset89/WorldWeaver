package net.tactware.worldweaver.data

import net.tactware.worldweaver.domain.GameSystem
import net.tactware.worldweaver.domain.World
import java.time.Instant

internal class WorldEntityConverter {
    fun toWorld(entity: WorldEntity): World {
        return World(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            defaultGameSystem = GameSystem.fromStorage(entity.defaultGameSystem),
            createdAt = Instant.ofEpochMilli(entity.createdAtEpochMillis),
            updatedAt = Instant.ofEpochMilli(entity.updatedAtEpochMillis),
        )
    }

    fun toEntity(world: World): WorldEntity {
        return WorldEntity(
            id = world.id,
            name = world.name,
            description = world.description,
            defaultGameSystem = world.defaultGameSystem.name,
            createdAtEpochMillis = world.createdAt.toEpochMilli(),
            updatedAtEpochMillis = world.updatedAt.toEpochMilli(),
        )
    }
}
