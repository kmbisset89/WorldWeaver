package net.tactware.worldweaver.domain

internal class UpdateWorldPersonUseCase(
    private val worldPersonRepository: WorldPersonRepository,
    private val instantProvider: InstantProvider,
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
                sheet = draft.sheet.copy(
                    race = draft.sheet.race.trim(),
                    classLevels = draft.sheet.classLevels
                        .map { it.copy(className = it.className.trim(), subclass = it.subclass.trim()) }
                        .filter { it.className.isNotEmpty() },
                    notes = draft.sheet.notes.trim(),
                ),
                updatedAt = instantProvider.now(),
            )
        )
        return Result.Updated
    }
}
