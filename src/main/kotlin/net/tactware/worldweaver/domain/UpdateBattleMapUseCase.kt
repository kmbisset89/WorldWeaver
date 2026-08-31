package net.tactware.worldweaver.domain

internal class UpdateBattleMapUseCase(
    private val battleMapRepository: BattleMapRepository,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data object Updated : Result
        data object InvalidName : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(battleMapId: String, name: String): Result {
        val existing = battleMapRepository.getById(battleMapId) ?: return Result.NotFound
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return Result.InvalidName
        }
        battleMapRepository.update(
            existing.copy(
                name = trimmedName,
                updatedAt = instantProvider.now(),
            )
        )
        return Result.Updated
    }
}
