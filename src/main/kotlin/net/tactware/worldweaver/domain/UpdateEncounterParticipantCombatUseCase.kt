package net.tactware.worldweaver.domain

internal class UpdateEncounterParticipantCombatUseCase(
    private val encounterRepository: EncounterRepository,
    private val campaignPersonRepository: CampaignPersonRepository,
    private val instantProvider: InstantProvider,
    private val diceRoller: DiceRoller,
    private val visibilityResolver: EncounterParticipantVisibilityResolver =
        EncounterParticipantVisibilityResolver(),
) {
    sealed interface Result {
        data object Updated : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(
        encounterId: String,
        participantId: String,
        action: EncounterParticipantCombatAction,
    ): Result {
        val encounter = encounterRepository.getById(encounterId) ?: return Result.NotFound
        val target = encounter.participants.firstOrNull { it.id == participantId }
            ?: return Result.NotFound
        val isPlayerCharacter = isPlayerCharacter(target)
        val participants = encounter.participants.map { participant ->
            if (participant.id == participantId) {
                applyAction(participant, action, isPlayerCharacter)
            } else {
                participant
            }
        }
        if (participants == encounter.participants) {
            return Result.NotFound
        }
        encounterRepository.update(
            encounter.copy(
                participants = participants,
                updatedAt = instantProvider.now(),
            )
        )
        return Result.Updated
    }

    private suspend fun isPlayerCharacter(participant: EncounterParticipant): Boolean {
        val sourceId = participant.sourceId ?: return false
        if (participant.source != EncounterParticipantSource.CampaignPerson) {
            return false
        }
        return campaignPersonRepository.getById(sourceId)?.kind == PersonKind.PlayerCharacter
    }

    private fun applyAction(
        participant: EncounterParticipant,
        action: EncounterParticipantCombatAction,
        isPlayerCharacter: Boolean,
    ): EncounterParticipant {
        return when (action) {
            is EncounterParticipantCombatAction.Damage -> applyDamage(participant, action.amount)
            is EncounterParticipantCombatAction.Heal -> applyHeal(participant, action.amount)
            is EncounterParticipantCombatAction.SetTemporaryHitPoints -> {
                participant.copy(temporaryHitPoints = action.amount.coerceAtLeast(0))
            }
            is EncounterParticipantCombatAction.SetConditions -> {
                val conditions = action.conditions.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
                participant.copy(
                    conditions = conditions,
                    visibleToPlayers = visibilityResolver.visibleToPlayersAfterConditions(
                        currentVisible = participant.visibleToPlayers,
                        currentConditions = participant.conditions,
                        nextConditions = conditions,
                        isPlayerCharacter = isPlayerCharacter,
                    ),
                )
            }
            is EncounterParticipantCombatAction.SetCombatState -> {
                participant.copy(combatState = action.state)
            }
            is EncounterParticipantCombatAction.SetVisibleToPlayers -> {
                participant.copy(visibleToPlayers = action.visible)
            }
            is EncounterParticipantCombatAction.SetInitiative -> {
                participant.copy(
                    initiativeRoll = action.roll?.coerceIn(1, 20),
                    initiativeBonus = action.bonus,
                )
            }
            EncounterParticipantCombatAction.RollInitiative -> {
                val roll = diceRoller.roll(
                    DiceRollRequest(sides = DieSides.D20.sides, count = 1),
                )
                participant.copy(initiativeRoll = roll.total.coerceIn(1, 20))
            }
            is EncounterParticipantCombatAction.SetAttacksUsed -> {
                participant.copy(attacksUsed = clampAttacksUsed(action.count, participant.attacksAllowed))
            }
            is EncounterParticipantCombatAction.SetAttacksAllowed -> {
                val allowed = clampAttacksAllowed(action.count)
                participant.copy(
                    attacksAllowed = allowed,
                    attacksUsed = clampAttacksUsed(participant.attacksUsed, allowed),
                )
            }
            is EncounterParticipantCombatAction.SetBonusActionUsed -> {
                participant.copy(bonusActionUsed = action.used)
            }
            is EncounterParticipantCombatAction.SetReactionUsed -> {
                participant.copy(reactionUsed = action.used)
            }
        }
    }

    private fun clampAttacksAllowed(count: Int): Int {
        return count.coerceIn(
            EncounterParticipant.MIN_ATTACKS_ALLOWED,
            EncounterParticipant.MAX_ATTACKS_ALLOWED,
        )
    }

    private fun clampAttacksUsed(count: Int, allowed: Int): Int {
        return count.coerceIn(0, clampAttacksAllowed(allowed))
    }

    private fun applyDamage(
        participant: EncounterParticipant,
        amount: Int,
    ): EncounterParticipant {
        var remaining = amount.coerceAtLeast(0)
        var temp = participant.temporaryHitPoints
        if (temp > 0 && remaining > 0) {
            val absorbed = minOf(temp, remaining)
            temp -= absorbed
            remaining -= absorbed
        }
        val hitPoints = (participant.hitPoints - remaining).coerceAtLeast(0)
        val combatState = when {
            participant.combatState == CombatState.Dead -> CombatState.Dead
            hitPoints == 0 -> CombatState.Downed
            else -> participant.combatState
        }
        return participant.copy(
            hitPoints = hitPoints,
            temporaryHitPoints = temp,
            combatState = combatState,
        )
    }

    private fun applyHeal(
        participant: EncounterParticipant,
        amount: Int,
    ): EncounterParticipant {
        val hitPoints = (participant.hitPoints + amount.coerceAtLeast(0))
            .coerceAtMost(participant.maxHitPoints)
        val combatState = when {
            participant.combatState == CombatState.Dead -> CombatState.Dead
            hitPoints > 0 -> CombatState.Conscious
            else -> CombatState.Downed
        }
        return participant.copy(
            hitPoints = hitPoints,
            combatState = combatState,
        )
    }
}
