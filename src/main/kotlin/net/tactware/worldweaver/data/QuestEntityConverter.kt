package net.tactware.worldweaver.data

import net.tactware.worldweaver.domain.Quest
import net.tactware.worldweaver.domain.QuestLink
import net.tactware.worldweaver.domain.QuestLinkKind
import net.tactware.worldweaver.domain.QuestObjective
import net.tactware.worldweaver.domain.QuestObjectiveStatus
import net.tactware.worldweaver.domain.QuestStatus
import java.time.Instant

internal class QuestEntityConverter {
    fun toQuest(
        entity: QuestEntity,
        objectives: List<QuestObjective>,
        links: List<QuestLink>,
    ): Quest {
        return Quest(
            id = entity.id,
            campaignId = entity.campaignId,
            title = entity.title,
            summary = entity.summary,
            status = QuestStatus.fromStorage(entity.status),
            locationId = entity.locationId,
            objectives = objectives,
            links = links,
            createdAt = Instant.ofEpochMilli(entity.createdAtEpochMillis),
            updatedAt = Instant.ofEpochMilli(entity.updatedAtEpochMillis),
        )
    }

    fun toEntity(quest: Quest): QuestEntity {
        return QuestEntity(
            id = quest.id,
            campaignId = quest.campaignId,
            title = quest.title,
            summary = quest.summary,
            status = quest.status.name,
            locationId = quest.locationId,
            createdAtEpochMillis = quest.createdAt.toEpochMilli(),
            updatedAtEpochMillis = quest.updatedAt.toEpochMilli(),
        )
    }

    fun toObjectiveEntities(quest: Quest): List<QuestObjectiveEntity> {
        return quest.objectives.mapIndexed { index, objective ->
            QuestObjectiveEntity(
                id = objective.id,
                questId = quest.id,
                title = objective.title,
                status = objective.status.name,
                sortIndex = index,
            )
        }
    }

    fun toLinkEntities(quest: Quest): List<QuestLinkEntity> {
        return quest.links.map { link ->
            QuestLinkEntity(
                id = link.id,
                questId = quest.id,
                kind = link.kind.name,
                targetId = link.targetId,
            )
        }
    }

    fun toObjectives(entities: List<QuestObjectiveEntity>): List<QuestObjective> {
        return entities.map { entity ->
            QuestObjective(
                id = entity.id,
                title = entity.title,
                status = QuestObjectiveStatus.fromStorage(entity.status),
            )
        }
    }

    fun toLinks(entities: List<QuestLinkEntity>): List<QuestLink> {
        return entities.map { entity ->
            QuestLink(
                id = entity.id,
                kind = QuestLinkKind.fromStorage(entity.kind),
                targetId = entity.targetId,
            )
        }
    }
}
