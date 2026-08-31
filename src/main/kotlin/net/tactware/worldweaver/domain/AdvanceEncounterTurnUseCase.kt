package net.tactware.worldweaver.domain

internal class AdvanceEncounterTurnUseCase(
    private val encounterRepository: EncounterRepository,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data object Advanced : Result
        data object NotFound : Result
        data object NoParticipants : Result
    }

    suspend operator fun invoke(
        encounterId: String,
        direction: EncounterTurnDirection,
    ): Result {
        val encounter = encounterRepository.getById(encounterId) ?: return Result.NotFound
        val order = encounter.initiativeOrder()
        if (order.isEmpty()) {
            return Result.NoParticipants
        }
        val size = order.size
        val (nextIndex, nextRound) = when (direction) {
            EncounterTurnDirection.Next -> {
                val advanced = encounter.currentTurnIndex + 1
                if (advanced >= size) {
                    0 to encounter.currentRound + 1
                } else {
                    advanced to encounter.currentRound
                }
            }
            EncounterTurnDirection.Previous -> {
                val retreated = encounter.currentTurnIndex - 1
                if (retreated < 0) {
                    (size - 1) to encounter.currentRound.coerceAtLeast(2) - 1
                } else {
                    retreated to encounter.currentRound
                }
            }
        }
        val participants = when (direction) {
            EncounterTurnDirection.Next -> {
                val arrivingId = order[nextIndex].id
                encounter.participants.map { participant ->
                    if (participant.id == arrivingId) {
                        participant.resetTurnEconomy()
                    } else {
                        participant
                    }
                }
            }
            EncounterTurnDirection.Previous -> encounter.participants
        }
        encounterRepository.update(
            encounter.copy(
                currentTurnIndex = nextIndex,
                currentRound = nextRound.coerceAtLeast(1),
                participants = participants,
                updatedAt = instantProvider.now(),
            )
        )
        return Result.Advanced
    }
}
