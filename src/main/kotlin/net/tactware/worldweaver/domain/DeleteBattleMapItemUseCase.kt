package net.tactware.worldweaver.domain

internal class DeleteBattleMapItemUseCase(
    private val battleMapRepository: BattleMapRepository,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data object Deleted : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(battleMapId: String, itemId: String): Result {
        val existing = battleMapRepository.getById(battleMapId) ?: return Result.NotFound
        if (existing.items.none { it.id == itemId }) {
            return Result.NotFound
        }
        battleMapRepository.update(
            existing.copy(
                items = existing.items.filterNot { it.id == itemId },
                updatedAt = instantProvider.now(),
            ),
        )
        return Result.Deleted
    }
}
