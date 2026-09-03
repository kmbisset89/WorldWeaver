package io.github.kmbisset89.worldweaver.domain

internal class OccupiedBoardCellsCalculator(
    private val sizeResolver: CreatureSizeResolver = CreatureSizeResolver(),
) {
    fun occupiedCells(
        encounter: Encounter,
        people: PeopleSnapshot,
        exceptParticipantId: String? = null,
    ): Set<GridCell> {
        return encounter.participants.flatMap { participant ->
            if (participant.id == exceptParticipantId) {
                return@flatMap emptyList()
            }
            val origin = participant.boardCell() ?: return@flatMap emptyList()
            sizeResolver.resolve(participant, people).occupiedCells(origin)
        }.toSet()
    }
}
