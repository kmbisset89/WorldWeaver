package net.tactware.worldweaver.domain

internal class UpdateCampaignPersonUseCase(
    private val campaignPersonRepository: CampaignPersonRepository,
    private val instantProvider: InstantProvider,
    private val sheetFactory: PersonSheetFactory = PersonSheetFactory(),
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
                sheet = sheetFactory.sanitize(draft.sheet),
                overlayHitPoints = draft.overlayHitPoints,
                overlayNotes = draft.overlayNotes.trim(),
                updatedAt = instantProvider.now(),
            )
        )
        return Result.Updated
    }

    private fun classLevelsMatch(sheet: PersonSheet): Boolean {
        return when (sheet) {
            is FifthEditionSheet -> {
                val classLevels = sheet.classLevels.filter { it.className.isNotBlank() }
                if (classLevels.isEmpty()) {
                    true
                } else {
                    classLevels.sumOf { it.level } == sheet.totalLevel()
                }
            }
            is Pathfinder2ESheet -> true
        }
    }
}
