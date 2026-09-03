package net.tactware.worldweaver.domain

internal class UpdateWorldPersonUseCase(
    private val worldPersonRepository: WorldPersonRepository,
    private val instantProvider: InstantProvider,
    private val sheetFactory: PersonSheetFactory = PersonSheetFactory(),
) {
    sealed interface Result {
        data object Updated : Result
        data object InvalidName : Result
        data object InvalidKind : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(
        personId: String,
        draft: WorldPersonDraft,
    ): Result {
        val existing = worldPersonRepository.getById(personId) ?: return Result.NotFound
        val name = draft.name.trim()
        if (name.isEmpty()) {
            return Result.InvalidName
        }
        if (draft.kind == PersonKind.PlayerCharacter) {
            return Result.InvalidKind
        }
        worldPersonRepository.update(
            existing.copy(
                kind = draft.kind,
                name = name,
                description = draft.description.trim(),
                sheet = sheetFactory.sanitize(draft.sheet),
                updatedAt = instantProvider.now(),
            )
        )
        return Result.Updated
    }
}
