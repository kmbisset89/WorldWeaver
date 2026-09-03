package net.tactware.worldweaver.domain

internal class CreateWorldPersonUseCase(
    private val worldPersonRepository: WorldPersonRepository,
    private val activeContextRepository: ActiveContextRepository,
    private val entityIdFactory: EntityIdFactory,
    private val instantProvider: InstantProvider,
    private val sheetFactory: PersonSheetFactory = PersonSheetFactory(),
) {
    sealed interface Result {
        data class Created(val person: WorldPerson) : Result
        data object InvalidName : Result
        data object InvalidKind : Result
        data object NoActiveWorld : Result
    }

    suspend operator fun invoke(draft: WorldPersonDraft): Result {
        val worldId = activeContextRepository.get().activeWorldId ?: return Result.NoActiveWorld
        val name = draft.name.trim()
        if (name.isEmpty()) {
            return Result.InvalidName
        }
        if (draft.kind == PersonKind.PlayerCharacter) {
            return Result.InvalidKind
        }
        val now = instantProvider.now()
        val person = WorldPerson(
            id = entityIdFactory.create(),
            worldId = worldId,
            kind = draft.kind,
            name = name,
            description = draft.description.trim(),
            sheet = sheetFactory.sanitize(draft.sheet),
            createdAt = now,
            updatedAt = now,
        )
        worldPersonRepository.insert(person)
        return Result.Created(person)
    }

}
