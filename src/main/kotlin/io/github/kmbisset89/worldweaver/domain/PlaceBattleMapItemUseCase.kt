package io.github.kmbisset89.worldweaver.domain

internal class PlaceBattleMapItemUseCase(
    private val battleMapRepository: BattleMapRepository,
    private val entityIdFactory: EntityIdFactory,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data class Placed(val item: BattleMapItem) : Result
        data object InvalidName : Result
        data object InvalidCell : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(
        battleMapId: String,
        name: String,
        cell: GridCell,
    ): Result {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return Result.InvalidName
        }
        val existing = battleMapRepository.getById(battleMapId) ?: return Result.NotFound
        if (
            cell.column !in 0 until existing.columns ||
            cell.row !in 0 until existing.rows
        ) {
            return Result.InvalidCell
        }
        val item = BattleMapItem(
            id = entityIdFactory.create(),
            name = trimmedName,
            cell = cell,
        )
        battleMapRepository.update(
            existing.copy(
                items = existing.items + item,
                updatedAt = instantProvider.now(),
            ),
        )
        return Result.Placed(item)
    }
}
