package net.tactware.worldweaver.domain

internal class CreateWorldUseCase(
    private val worldRepository: WorldRepository,
    private val entityIdFactory: EntityIdFactory,
    private val instantProvider: InstantProvider,
    private val setActiveWorld: SetActiveWorldUseCase,
) {
    sealed interface Result {
        data class Created(val world: World) : Result
        data object InvalidName : Result
    }

    suspend operator fun invoke(
        name: String,
        description: String,
    ): Result {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return Result.InvalidName
        }
        val now = instantProvider.now()
        val world = World(
            id = entityIdFactory.create(),
            name = trimmedName,
            description = description.trim(),
            defaultGameSystem = GameSystem.FifthEdition,
            createdAt = now,
            updatedAt = now,
        )
        worldRepository.insert(world)
        setActiveWorld(world.id)
        return Result.Created(world)
    }
}
