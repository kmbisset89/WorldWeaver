package io.github.kmbisset89.worldweaver.data

import io.github.kmbisset89.worldweaver.domain.CombatState
import io.github.kmbisset89.worldweaver.domain.Encounter
import io.github.kmbisset89.worldweaver.domain.EncounterDifficulty
import io.github.kmbisset89.worldweaver.domain.EncounterParticipant
import io.github.kmbisset89.worldweaver.domain.EncounterParticipantSource
import io.github.kmbisset89.worldweaver.domain.EncounterStatus
import java.time.Instant

internal class EncounterEntityConverter {
    fun toEncounter(
        entity: EncounterEntity,
        participants: List<EncounterParticipant>,
    ): Encounter {
        return Encounter(
            id = entity.id,
            campaignId = entity.campaignId,
            name = entity.name,
            locationId = entity.locationId,
            battleMapId = entity.battleMapId,
            difficulty = EncounterDifficulty.fromStorage(entity.difficulty),
            notes = entity.notes,
            outcomeNote = entity.outcomeNote,
            status = EncounterStatus.fromStorage(entity.status),
            currentRound = entity.currentRound,
            currentTurnIndex = entity.currentTurnIndex,
            participants = participants,
            createdAt = Instant.ofEpochMilli(entity.createdAtEpochMillis),
            updatedAt = Instant.ofEpochMilli(entity.updatedAtEpochMillis),
        )
    }

    fun toEntity(encounter: Encounter): EncounterEntity {
        return EncounterEntity(
            id = encounter.id,
            campaignId = encounter.campaignId,
            name = encounter.name,
            locationId = encounter.locationId,
            battleMapId = encounter.battleMapId,
            difficulty = encounter.difficulty.name,
            notes = encounter.notes,
            outcomeNote = encounter.outcomeNote,
            status = encounter.status.name,
            currentRound = encounter.currentRound,
            currentTurnIndex = encounter.currentTurnIndex,
            createdAtEpochMillis = encounter.createdAt.toEpochMilli(),
            updatedAtEpochMillis = encounter.updatedAt.toEpochMilli(),
        )
    }

    fun toParticipantEntities(encounter: Encounter): List<EncounterParticipantEntity> {
        return encounter.participants.mapIndexed { index, participant ->
            EncounterParticipantEntity(
                id = participant.id,
                encounterId = encounter.id,
                name = participant.name,
                source = participant.source.name,
                sourceId = participant.sourceId,
                initiativeRoll = participant.initiativeRoll,
                initiativeBonus = participant.initiativeBonus,
                armorClass = participant.armorClass,
                hitPoints = participant.hitPoints,
                maxHitPoints = participant.maxHitPoints,
                temporaryHitPoints = participant.temporaryHitPoints,
                conditions = participant.conditions.joinToString("\n"),
                groupCount = participant.groupCount,
                combatState = participant.combatState.name,
                sortIndex = index,
                gridColumn = participant.gridColumn,
                gridRow = participant.gridRow,
                visibleToPlayers = participant.visibleToPlayers,
                attacksAllowed = participant.attacksAllowed,
                attacksUsed = participant.attacksUsed,
                bonusActionUsed = participant.bonusActionUsed,
                reactionUsed = participant.reactionUsed,
            )
        }
    }

    fun toParticipants(entities: List<EncounterParticipantEntity>): List<EncounterParticipant> {
        return entities.map { entity ->
            EncounterParticipant(
                id = entity.id,
                name = entity.name,
                source = EncounterParticipantSource.fromStorage(entity.source),
                sourceId = entity.sourceId,
                initiativeRoll = entity.initiativeRoll,
                initiativeBonus = entity.initiativeBonus,
                armorClass = entity.armorClass,
                hitPoints = entity.hitPoints,
                maxHitPoints = entity.maxHitPoints,
                temporaryHitPoints = entity.temporaryHitPoints,
                conditions = entity.conditions
                    .split('\n')
                    .map { it.trim() }
                    .filter { it.isNotEmpty() },
                groupCount = entity.groupCount,
                combatState = CombatState.fromStorage(entity.combatState),
                gridColumn = entity.gridColumn,
                gridRow = entity.gridRow,
                visibleToPlayers = entity.visibleToPlayers,
                attacksAllowed = entity.attacksAllowed,
                attacksUsed = entity.attacksUsed,
                bonusActionUsed = entity.bonusActionUsed,
                reactionUsed = entity.reactionUsed,
            )
        }
    }
}
