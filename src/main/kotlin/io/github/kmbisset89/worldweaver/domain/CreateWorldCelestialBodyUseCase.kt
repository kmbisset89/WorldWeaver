package io.github.kmbisset89.worldweaver.domain

internal class CreateWorldCelestialBodyUseCase(
    private val bodyRepository: WorldCelestialBodyRepository,
    private val activeContextRepository: ActiveContextRepository,
    private val entityIdFactory: EntityIdFactory,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data class Created(val body: WorldCelestialBody) : Result
        data object InvalidName : Result
        data object DuplicateName : Result
        data object InvalidPeriod : Result
        data object NoActiveWorld : Result
    }

    suspend operator fun invoke(draft: WorldCelestialBodyDraft): Result {
        val worldId = activeContextRepository.get().activeWorldId ?: return Result.NoActiveWorld
        val name = draft.name.trim()
        if (name.isEmpty()) {
            return Result.InvalidName
        }
        val existing = bodyRepository.getByWorld(worldId)
        if (existing.any { it.name.equals(name, ignoreCase = true) }) {
            return Result.DuplicateName
        }
        if (draft.periodDays < 1) {
            return Result.InvalidPeriod
        }
        val now = instantProvider.now()
        val body = WorldCelestialBody(
            id = entityIdFactory.create(),
            worldId = worldId,
            name = name,
            notes = draft.notes.trim(),
            kind = draft.kind,
            periodDays = draft.periodDays,
            epochOffsetDays = draft.epochOffsetDays,
            sortIndex = existing.maxOfOrNull { it.sortIndex }?.plus(1) ?: 0,
            createdAt = now,
            updatedAt = now,
        )
        bodyRepository.insert(body)
        return Result.Created(body)
    }
}
