package io.github.kmbisset89.worldweaver.domain

internal class RollEncounterInitiativeUseCase(
    private val diceRoller: DiceRoller,
) {
    operator fun invoke(
        participants: List<EncounterParticipant>,
        overwriteExisting: Boolean,
    ): List<EncounterParticipant> {
        return participants.map { participant ->
            if (!overwriteExisting && participant.initiativeRoll != null) {
                participant
            } else {
                val roll = diceRoller.roll(
                    DiceRollRequest(sides = DieSides.D20.sides, count = 1),
                )
                participant.copy(initiativeRoll = roll.total.coerceIn(1, 20))
            }
        }
    }
}
