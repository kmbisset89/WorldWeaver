package io.github.kmbisset89.worldweaver.domain

internal class CreateFactionUseCase(
    private val factionRepository: FactionRepository,
    private val activeContextRepository: ActiveContextRepository,
    private val entityIdFactory: EntityIdFactory,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data class Created(val faction: Faction) : Result
        data object InvalidName : Result
        data object DuplicateName : Result
        data object NoActiveWorld : Result
    }

    suspend operator fun invoke(draft: FactionDraft): Result {
        val worldId = activeContextRepository.get().activeWorldId ?: return Result.NoActiveWorld
        val name = draft.name.trim()
        if (name.isEmpty()) {
            return Result.InvalidName
        }
        if (nameTaken(worldId, name, excludeId = null)) {
            return Result.DuplicateName
        }
        val now = instantProvider.now()
        val faction = Faction(
            id = entityIdFactory.create(),
            worldId = worldId,
            name = name,
            description = draft.description.trim(),
            goals = draft.goals.trim(),
            notes = draft.notes.trim(),
            createdAt = now,
            updatedAt = now,
        )
        factionRepository.insert(faction)
        return Result.Created(faction)
    }

    private suspend fun nameTaken(
        worldId: String,
        name: String,
        excludeId: String?,
    ): Boolean {
        return factionRepository.getByWorld(worldId).any { faction ->
            faction.id != excludeId && faction.name.equals(name, ignoreCase = true)
        }
    }
}
