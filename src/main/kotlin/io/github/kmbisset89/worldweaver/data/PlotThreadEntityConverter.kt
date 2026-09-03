package io.github.kmbisset89.worldweaver.data

import io.github.kmbisset89.worldweaver.domain.PlotThread
import io.github.kmbisset89.worldweaver.domain.PlotThreadPriority
import io.github.kmbisset89.worldweaver.domain.PlotThreadStatus
import java.time.Instant

internal class PlotThreadEntityConverter {
    fun toThread(entity: PlotThreadEntity): PlotThread {
        return PlotThread(
            id = entity.id,
            campaignId = entity.campaignId,
            sessionId = entity.sessionId,
            title = entity.title,
            details = entity.details,
            status = PlotThreadStatus.fromStorage(entity.status),
            priority = PlotThreadPriority.fromStorage(entity.priority),
            createdAt = Instant.ofEpochMilli(entity.createdAtEpochMillis),
            updatedAt = Instant.ofEpochMilli(entity.updatedAtEpochMillis),
        )
    }

    fun toEntity(thread: PlotThread): PlotThreadEntity {
        return PlotThreadEntity(
            id = thread.id,
            campaignId = thread.campaignId,
            sessionId = thread.sessionId,
            title = thread.title,
            details = thread.details,
            status = thread.status.name,
            priority = thread.priority.name,
            createdAtEpochMillis = thread.createdAt.toEpochMilli(),
            updatedAtEpochMillis = thread.updatedAt.toEpochMilli(),
        )
    }
}
