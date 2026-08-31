package net.tactware.worldweaver.domain

internal class UpdateCampaignPersonUseCase(
    private val campaignPersonRepository: CampaignPersonRepository,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data object Updated : Result
        data object InvalidName : Result
        data object InvalidClassLevels : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(
        personId: String,
        draft: CampaignPersonDraft,
    ): Result {
        val existing = campaignPersonRepository.getById(personId) ?: return Result.NotFound
        val name = draft.name.trim()
        if (name.isEmpty()) {
            return Result.InvalidName
        }
        if (existing.kind == PersonKind.PlayerCharacter && !classLevelsMatch(draft.sheet)) {
            return Result.InvalidClassLevels
        }
        campaignPersonRepository.update(
            existing.copy(
                name = name,
                description = draft.description.trim(),
                sheet = draft.sheet.copy(
                    race = draft.sheet.race.trim(),
                    notes = draft.sheet.notes.trim(),
                ),
                overlayHitPoints = draft.overlayHitPoints,
                overlayNotes = draft.overlayNotes.trim(),
                updatedAt = instantProvider.now(),
            )
        )
        return Result.Updated
    }

    private fun classLevelsMatch(sheet: FifthEditionSheet): Boolean {
        val classLevels = sheet.classLevels.filter { it.className.isNotBlank() }
        if (classLevels.isEmpty()) {
            return true
        }
        return classLevels.sumOf { it.level } == sheet.totalLevel()
    }
}
