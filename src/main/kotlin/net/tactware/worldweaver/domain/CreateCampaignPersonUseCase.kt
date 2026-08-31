package net.tactware.worldweaver.domain

internal class CreateCampaignPersonUseCase(
    private val campaignPersonRepository: CampaignPersonRepository,
    private val activeContextRepository: ActiveContextRepository,
    private val entityIdFactory: EntityIdFactory,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data class Created(val person: CampaignPerson) : Result
        data object InvalidName : Result
        data object InvalidKind : Result
        data object InvalidClassLevels : Result
        data object NoActiveCampaign : Result
    }

    suspend operator fun invoke(draft: CampaignPersonDraft): Result {
        val campaignId = activeContextRepository.get().activeCampaignId
            ?: return Result.NoActiveCampaign
        val name = draft.name.trim()
        if (name.isEmpty()) {
            return Result.InvalidName
        }
        if (draft.kind == PersonKind.PlayerCharacter && !classLevelsMatch(draft.sheet)) {
            return Result.InvalidClassLevels
        }
        val now = instantProvider.now()
        val person = CampaignPerson(
            id = entityIdFactory.create(),
            campaignId = campaignId,
            worldPersonId = null,
            kind = draft.kind,
            name = name,
            description = draft.description.trim(),
            sheet = draft.sheet.copy(
                race = draft.sheet.race.trim(),
                notes = draft.sheet.notes.trim(),
            ),
            overlayHitPoints = draft.overlayHitPoints,
            overlayNotes = draft.overlayNotes.trim(),
            createdAt = now,
            updatedAt = now,
        )
        campaignPersonRepository.insert(person)
        return Result.Created(person)
    }

    private fun classLevelsMatch(sheet: FifthEditionSheet): Boolean {
        val classLevels = sheet.classLevels.filter { it.className.isNotBlank() }
        if (classLevels.isEmpty()) {
            return true
        }
        return classLevels.sumOf { it.level } == sheet.totalLevel()
    }
}
