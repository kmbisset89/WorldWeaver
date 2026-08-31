package net.tactware.worldweaver.data

import net.tactware.worldweaver.domain.BattleMapSituation
import java.time.Instant

internal class BattleMapSituationEntityConverter {
    fun toSituation(entity: BattleMapSituationEntity): BattleMapSituation {
        return BattleMapSituation(
            id = entity.id,
            battleMapId = entity.battleMapId,
            name = entity.name,
            visible = entity.visible,
            sortIndex = entity.sortIndex,
            createdAt = Instant.ofEpochMilli(entity.createdAtEpochMillis),
            updatedAt = Instant.ofEpochMilli(entity.updatedAtEpochMillis),
        )
    }

    fun toEntity(situation: BattleMapSituation): BattleMapSituationEntity {
        return BattleMapSituationEntity(
            id = situation.id,
            battleMapId = situation.battleMapId,
            name = situation.name,
            visible = situation.visible,
            sortIndex = situation.sortIndex,
            createdAtEpochMillis = situation.createdAt.toEpochMilli(),
            updatedAtEpochMillis = situation.updatedAt.toEpochMilli(),
        )
    }
}
