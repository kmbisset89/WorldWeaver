package io.github.kmbisset89.worldweaver.domain

internal class UpdateWorldCelestialBodyUseCase(
    private val bodyRepository: WorldCelestialBodyRepository,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data object Updated : Result
        data object InvalidName : Result
        data object DuplicateName : Result
        data object InvalidPeriod : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(
        bodyId: String,
        draft: WorldCelestialBodyDraft,
    ): Result {
        val existing = bodyRepository.getById(bodyId) ?: return Result.NotFound
        val name = draft.name.trim()
        if (name.isEmpty()) {
            return Result.InvalidName
        }
        val duplicate = bodyRepository.getByWorld(existing.worldId).any { body ->
            body.id != bodyId && body.name.equals(name, ignoreCase = true)
        }
        if (duplicate) {
            return Result.DuplicateName
        }
        if (draft.periodDays < 1) {
            return Result.InvalidPeriod
        }
        bodyRepository.update(
            existing.copy(
                name = name,
                notes = draft.notes.trim(),
                kind = draft.kind,
                periodDays = draft.periodDays,
                epochOffsetDays = draft.epochOffsetDays,
                sortIndex = draft.sortIndex ?: existing.sortIndex,
                updatedAt = instantProvider.now(),
            )
        )
        return Result.Updated
    }
}
