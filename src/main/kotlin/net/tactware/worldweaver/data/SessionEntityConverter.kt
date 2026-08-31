package net.tactware.worldweaver.data

import net.tactware.worldweaver.domain.MarchOrderEntry
import net.tactware.worldweaver.domain.PersonRef
import net.tactware.worldweaver.domain.Session
import net.tactware.worldweaver.domain.SessionScene
import net.tactware.worldweaver.domain.WorldDate
import java.time.Instant

internal class SessionEntityConverter {
    fun toSession(
        entity: SessionEntity,
        scenes: List<SessionScene>,
        marchOrder: List<MarchOrderEntry>,
    ): Session {
        return Session(
            id = entity.id,
            campaignId = entity.campaignId,
            name = entity.name,
            notes = entity.notes,
            inWorldDate = toInWorldDate(entity),
            scenes = scenes,
            marchOrder = marchOrder,
            createdAt = Instant.ofEpochMilli(entity.createdAtEpochMillis),
            updatedAt = Instant.ofEpochMilli(entity.updatedAtEpochMillis),
        )
    }

    fun toEntity(session: Session): SessionEntity {
        return SessionEntity(
            id = session.id,
            campaignId = session.campaignId,
            name = session.name,
            notes = session.notes,
            inWorldYear = session.inWorldDate?.year,
            inWorldMonthId = session.inWorldDate?.monthId,
            inWorldDay = session.inWorldDate?.day,
            createdAtEpochMillis = session.createdAt.toEpochMilli(),
            updatedAtEpochMillis = session.updatedAt.toEpochMilli(),
        )
    }

    fun toSceneEntities(session: Session): List<SessionSceneEntity> {
        return session.scenes.mapIndexed { index, scene ->
            SessionSceneEntity(
                id = scene.id,
                sessionId = session.id,
                title = scene.title,
                notes = scene.notes,
                sortIndex = index,
            )
        }
    }

    fun toMarchEntities(session: Session): List<SessionMarchEntryEntity> {
        return session.marchOrder.mapIndexed { index, entry ->
            SessionMarchEntryEntity(
                id = entry.id,
                sessionId = session.id,
                personScope = personScope(entry.person),
                personId = entry.person.id,
                displayName = entry.displayName,
                sortIndex = index,
            )
        }
    }

    fun toScenes(entities: List<SessionSceneEntity>): List<SessionScene> {
        return entities.map { entity ->
            SessionScene(
                id = entity.id,
                title = entity.title,
                notes = entity.notes,
            )
        }
    }

    fun toMarchOrder(entities: List<SessionMarchEntryEntity>): List<MarchOrderEntry> {
        return entities.map { entity ->
            MarchOrderEntry(
                id = entity.id,
                person = personRef(entity.personScope, entity.personId),
                displayName = entity.displayName,
            )
        }
    }

    private fun toInWorldDate(entity: SessionEntity): WorldDate? {
        val year = entity.inWorldYear ?: return null
        val monthId = entity.inWorldMonthId ?: return null
        val day = entity.inWorldDay ?: return null
        return WorldDate(year = year, monthId = monthId, day = day)
    }

    private fun personScope(person: PersonRef): String {
        return when (person) {
            is PersonRef.World -> WORLD_SCOPE
            is PersonRef.Campaign -> CAMPAIGN_SCOPE
        }
    }

    private fun personRef(scope: String, personId: String): PersonRef {
        return if (scope == WORLD_SCOPE) {
            PersonRef.World(personId)
        } else {
            PersonRef.Campaign(personId)
        }
    }

    private companion object {
        const val WORLD_SCOPE = "WORLD"
        const val CAMPAIGN_SCOPE = "CAMPAIGN"
    }
}
