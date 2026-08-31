package net.tactware.worldweaver.domain

internal class PlaceEncounterTokenUseCase(
    private val encounterRepository: EncounterRepository,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data class Placed(val participant: EncounterParticipant) : Result
        data object NotFound : Result
        data object InvalidCell : Result
    }

    suspend operator fun invoke(
        encounterId: String,
        participantId: String,
        cell: GridCell,
        columns: Int,
        rows: Int,
    ): Result {
        if (cell.column !in 0 until columns || cell.row !in 0 until rows) {
            return Result.InvalidCell
        }
        val encounter = encounterRepository.getById(encounterId) ?: return Result.NotFound
        var placed: EncounterParticipant? = null
        val participants = encounter.participants.map { participant ->
            if (participant.id == participantId) {
                val updated = participant.copy(
                    gridColumn = cell.column,
                    gridRow = cell.row,
                )
                placed = updated
                updated
            } else {
                participant
            }
        }
        val participant = placed ?: return Result.NotFound
        encounterRepository.update(
            encounter.copy(
                participants = participants,
                updatedAt = instantProvider.now(),
            )
        )
        return Result.Placed(participant)
    }
}
