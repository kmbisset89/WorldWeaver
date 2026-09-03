package net.tactware.worldweaver.domain

internal class UpdateWorldUseCase(
    private val worldRepository: WorldRepository,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data object Updated : Result
        data object InvalidName : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(
        worldId: String,
        name: String,
        description: String,
        defaultGameSystem: GameSystem,
    ): Result {
        val existing = worldRepository.getById(worldId) ?: return Result.NotFound
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return Result.InvalidName
        }
        worldRepository.update(
            existing.copy(
                name = trimmedName,
                description = description.trim(),
                defaultGameSystem = defaultGameSystem,
                updatedAt = instantProvider.now(),
            )
        )
        return Result.Updated
    }
}
