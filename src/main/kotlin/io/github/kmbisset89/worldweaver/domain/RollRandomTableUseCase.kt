package io.github.kmbisset89.worldweaver.domain

internal class RollRandomTableUseCase(
    private val randomTableRepository: RandomTableRepository,
    private val calculator: RandomTableRollCalculator = RandomTableRollCalculator(),
) {
    sealed interface Result {
        data class Rolled(val roll: RandomTableRoll) : Result
        data object Empty : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(tableId: String): Result {
        val table = randomTableRepository.getById(tableId) ?: return Result.NotFound
        val worldTables = randomTableRepository.getByWorld(table.worldId)
        val roll = calculator.roll(
            table = table,
            tablesById = worldTables.associateBy { it.id },
        ) ?: return Result.Empty
        return Result.Rolled(roll)
    }
}
