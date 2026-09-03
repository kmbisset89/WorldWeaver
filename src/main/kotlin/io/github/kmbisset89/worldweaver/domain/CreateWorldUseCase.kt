package io.github.kmbisset89.worldweaver.domain

internal class CreateWorldUseCase(
    private val worldRepository: WorldRepository,
    private val worldCalendarRepository: WorldCalendarRepository,
    private val defaultCalendarFactory: DefaultWorldCalendarFactory,
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
        defaultGameSystem: GameSystem = GameSystem.FifthEdition,
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
            defaultGameSystem = defaultGameSystem,
            createdAt = now,
            updatedAt = now,
        )
        worldRepository.insert(world)
        worldCalendarRepository.insert(defaultCalendarFactory.create(world.id, now))
        setActiveWorld(world.id)
        return Result.Created(world)
    }
}
