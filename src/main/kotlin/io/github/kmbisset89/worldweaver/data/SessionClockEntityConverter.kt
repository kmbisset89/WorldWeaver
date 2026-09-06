package io.github.kmbisset89.worldweaver.data

import io.github.kmbisset89.worldweaver.domain.SessionClock

internal class SessionClockEntityConverter {
    fun toClock(entity: SessionClockEntity): SessionClock {
        return SessionClock(
            id = entity.id,
            sessionId = entity.sessionId,
            label = entity.label,
            segmentCount = entity.segmentCount,
            filledCount = entity.filledCount,
            sortIndex = entity.sortIndex,
        )
    }

    fun toEntity(clock: SessionClock): SessionClockEntity {
        return SessionClockEntity(
            id = clock.id,
            sessionId = clock.sessionId,
            label = clock.label,
            segmentCount = clock.segmentCount,
            filledCount = clock.filledCount,
            sortIndex = clock.sortIndex,
        )
    }
}
