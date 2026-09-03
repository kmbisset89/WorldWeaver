package io.github.kmbisset89.worldweaver.data

import io.github.kmbisset89.worldweaver.domain.ReferenceDoc
import java.time.Instant

internal class ReferenceDocEntityConverter {
    fun toDoc(entity: ReferenceDocEntity): ReferenceDoc {
        return ReferenceDoc(
            id = entity.id,
            campaignId = entity.campaignId,
            sessionId = entity.sessionId,
            title = entity.title,
            pathOrUrl = entity.pathOrUrl,
            createdAt = Instant.ofEpochMilli(entity.createdAtEpochMillis),
            updatedAt = Instant.ofEpochMilli(entity.updatedAtEpochMillis),
        )
    }

    fun toEntity(doc: ReferenceDoc): ReferenceDocEntity {
        return ReferenceDocEntity(
            id = doc.id,
            campaignId = doc.campaignId,
            sessionId = doc.sessionId,
            title = doc.title,
            pathOrUrl = doc.pathOrUrl,
            createdAtEpochMillis = doc.createdAt.toEpochMilli(),
            updatedAtEpochMillis = doc.updatedAt.toEpochMilli(),
        )
    }
}
