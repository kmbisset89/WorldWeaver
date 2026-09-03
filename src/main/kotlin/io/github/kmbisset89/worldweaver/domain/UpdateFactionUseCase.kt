package io.github.kmbisset89.worldweaver.domain

internal class UpdateFactionUseCase(
    private val factionRepository: FactionRepository,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data object Updated : Result
        data object InvalidName : Result
        data object DuplicateName : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(
        factionId: String,
        draft: FactionDraft,
    ): Result {
        val existing = factionRepository.getById(factionId) ?: return Result.NotFound
        val name = draft.name.trim()
        if (name.isEmpty()) {
            return Result.InvalidName
        }
        val duplicate = factionRepository.getByWorld(existing.worldId).any { faction ->
            faction.id != factionId && faction.name.equals(name, ignoreCase = true)
        }
        if (duplicate) {
            return Result.DuplicateName
        }
        factionRepository.update(
            existing.copy(
                name = name,
                description = draft.description.trim(),
                goals = draft.goals.trim(),
                notes = draft.notes.trim(),
                updatedAt = instantProvider.now(),
            )
        )
        return Result.Updated
    }
}
