package net.tactware.worldweaver.domain

internal class CreateEncounterUseCase(
    private val encounterRepository: EncounterRepository,
    private val campaignRepository: CampaignRepository,
    private val locationRepository: LocationRepository,
    private val battleMapRepository: BattleMapRepository,
    private val worldPersonRepository: WorldPersonRepository,
    private val campaignPersonRepository: CampaignPersonRepository,
    private val activeContextRepository: ActiveContextRepository,
    private val entityIdFactory: EntityIdFactory,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data class Created(val encounter: Encounter) : Result
        data object InvalidName : Result
        data object InvalidLocation : Result
        data object InvalidBattleMap : Result
        data object NoActiveCampaign : Result
    }

    suspend operator fun invoke(draft: EncounterDraft): Result {
        val campaignId = activeContextRepository.get().activeCampaignId
            ?: return Result.NoActiveCampaign
        val campaign = campaignRepository.getById(campaignId) ?: return Result.NoActiveCampaign
        val name = draft.name.trim()
        if (name.isEmpty()) {
            return Result.InvalidName
        }
        val locationId = when (val resolved = resolveLocation(draft.locationId, campaign.worldId)) {
            LocationResolution.None -> null
            LocationResolution.Invalid -> return Result.InvalidLocation
            is LocationResolution.Found -> resolved.id
        }
        val battleMapId = when (val resolved = resolveBattleMap(draft.battleMapId, campaignId)) {
            BattleMapResolution.None -> null
            BattleMapResolution.Invalid -> return Result.InvalidBattleMap
            is BattleMapResolution.Found -> resolved.id
        }
        val now = instantProvider.now()
        val encounter = Encounter(
            id = entityIdFactory.create(),
            campaignId = campaignId,
            name = name,
            locationId = locationId,
            battleMapId = battleMapId,
            difficulty = draft.difficulty,
            notes = draft.notes.trim(),
            outcomeNote = draft.outcomeNote.trim(),
            status = EncounterStatus.Planned,
            currentRound = 1,
            currentTurnIndex = 0,
            participants = assignParticipants(draft.participants, campaign, existingIds = emptySet()),
            createdAt = now,
            updatedAt = now,
        )
        encounterRepository.insert(encounter)
        return Result.Created(encounter)
    }

    private suspend fun resolveLocation(
        locationId: String?,
        worldId: String,
    ): LocationResolution {
        val id = locationId?.takeIf { it.isNotBlank() } ?: return LocationResolution.None
        val location = locationRepository.getById(id) ?: return LocationResolution.Invalid
        return if (location.worldId == worldId) {
            LocationResolution.Found(id)
        } else {
            LocationResolution.Invalid
        }
    }

    private suspend fun assignParticipants(
        participants: List<EncounterParticipant>,
        campaign: Campaign,
        existingIds: Set<String>,
    ): List<EncounterParticipant> {
        val seenLinked = mutableSetOf<Pair<EncounterParticipantSource, String>>()
        return participants.mapNotNull { participant ->
            normalizeParticipant(participant, campaign, existingIds, seenLinked)
        }
    }

    private suspend fun normalizeParticipant(
        participant: EncounterParticipant,
        campaign: Campaign,
        existingIds: Set<String>,
        seenLinked: MutableSet<Pair<EncounterParticipantSource, String>>,
    ): EncounterParticipant? {
        val name = participant.name.trim()
        if (name.isEmpty()) {
            return null
        }
        val sourceId = participant.sourceId?.trim()?.takeIf { it.isNotEmpty() }
        when (participant.source) {
            EncounterParticipantSource.Nameless -> {
                return clampStats(
                    participant.copy(
                        id = participant.id.ifBlank { entityIdFactory.create() },
                        name = name,
                        sourceId = null,
                        groupCount = participant.groupCount.coerceAtLeast(1),
                        conditions = cleanConditions(participant.conditions),
                    )
                )
            }
            EncounterParticipantSource.WorldPerson -> {
                if (sourceId == null) {
                    return null
                }
                if (!seenLinked.add(EncounterParticipantSource.WorldPerson to sourceId)) {
                    return null
                }
                val keepExisting = participant.id in existingIds
                if (!keepExisting) {
                    val person = worldPersonRepository.getById(sourceId)
                    if (person == null || person.worldId != campaign.worldId) {
                        return null
                    }
                }
                return clampStats(
                    participant.copy(
                        id = participant.id.ifBlank { entityIdFactory.create() },
                        name = name,
                        sourceId = sourceId,
                        groupCount = participant.groupCount.coerceAtLeast(1),
                        conditions = cleanConditions(participant.conditions),
                    )
                )
            }
            EncounterParticipantSource.CampaignPerson -> {
                if (sourceId == null) {
                    return null
                }
                if (!seenLinked.add(EncounterParticipantSource.CampaignPerson to sourceId)) {
                    return null
                }
                val keepExisting = participant.id in existingIds
                if (!keepExisting) {
                    val person = campaignPersonRepository.getById(sourceId)
                    if (person == null || person.campaignId != campaign.id) {
                        return null
                    }
                }
                return clampStats(
                    participant.copy(
                        id = participant.id.ifBlank { entityIdFactory.create() },
                        name = name,
                        sourceId = sourceId,
                        groupCount = participant.groupCount.coerceAtLeast(1),
                        conditions = cleanConditions(participant.conditions),
                    )
                )
            }
        }
    }

    private fun clampStats(participant: EncounterParticipant): EncounterParticipant {
        val maxHitPoints = participant.maxHitPoints.coerceAtLeast(1)
        val hitPoints = participant.hitPoints.coerceIn(0, maxHitPoints)
        val combatState = when {
            participant.combatState == CombatState.Dead -> CombatState.Dead
            hitPoints == 0 -> CombatState.Downed
            else -> participant.combatState
        }
        val attacksAllowed = participant.attacksAllowed.coerceIn(
            EncounterParticipant.MIN_ATTACKS_ALLOWED,
            EncounterParticipant.MAX_ATTACKS_ALLOWED,
        )
        return participant.copy(
            initiativeRoll = participant.initiativeRoll?.coerceIn(1, 20),
            initiativeBonus = participant.initiativeBonus,
            armorClass = participant.armorClass.coerceAtLeast(0),
            hitPoints = hitPoints,
            maxHitPoints = maxHitPoints,
            temporaryHitPoints = participant.temporaryHitPoints.coerceAtLeast(0),
            combatState = combatState,
            attacksAllowed = attacksAllowed,
            attacksUsed = participant.attacksUsed.coerceIn(0, attacksAllowed),
        )
    }

    private fun cleanConditions(conditions: List<String>): List<String> {
        return conditions.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
    }

    private suspend fun resolveBattleMap(
        battleMapId: String?,
        campaignId: String,
    ): BattleMapResolution {
        val id = battleMapId?.takeIf { it.isNotBlank() } ?: return BattleMapResolution.None
        val battleMap = battleMapRepository.getById(id) ?: return BattleMapResolution.Invalid
        return if (battleMap.campaignId == campaignId) {
            BattleMapResolution.Found(id)
        } else {
            BattleMapResolution.Invalid
        }
    }

    private sealed interface LocationResolution {
        data object None : LocationResolution
        data object Invalid : LocationResolution
        data class Found(val id: String) : LocationResolution
    }

    private sealed interface BattleMapResolution {
        data object None : BattleMapResolution
        data object Invalid : BattleMapResolution
        data class Found(val id: String) : BattleMapResolution
    }
}
