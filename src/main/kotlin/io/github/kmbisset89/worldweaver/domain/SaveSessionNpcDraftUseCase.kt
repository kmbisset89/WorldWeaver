package io.github.kmbisset89.worldweaver.domain

internal class SaveSessionNpcDraftUseCase(
    private val createWorldPerson: CreateWorldPersonUseCase,
    private val createCampaignPerson: CreateCampaignPersonUseCase,
) {
    sealed interface Result {
        data class SavedToWorld(val person: WorldPerson) : Result
        data class SavedToCampaign(val person: CampaignPerson) : Result
        data object InvalidName : Result
        data object NoActiveWorld : Result
        data object NoActiveCampaign : Result
    }

    suspend operator fun invoke(
        draft: RandomNpcDraft,
        destination: SessionNpcDraftDestination,
    ): Result {
        val name = draft.name.trim()
        if (name.isEmpty()) {
            return Result.InvalidName
        }
        val sheet = FifthEditionSheet.empty().copy(
            race = draft.race.trim(),
            abilityScores = draft.abilityScores,
        )
        return when (destination) {
            SessionNpcDraftDestination.WorldLibrary -> {
                when (
                    val created = createWorldPerson(
                        WorldPersonDraft(
                            kind = PersonKind.Npc,
                            name = name,
                            description = "",
                            sheet = sheet,
                        )
                    )
                ) {
                    is CreateWorldPersonUseCase.Result.Created -> Result.SavedToWorld(created.person)
                    CreateWorldPersonUseCase.Result.InvalidName -> Result.InvalidName
                    CreateWorldPersonUseCase.Result.NoActiveWorld -> Result.NoActiveWorld
                    CreateWorldPersonUseCase.Result.InvalidKind -> Result.InvalidName
                }
            }
            SessionNpcDraftDestination.CampaignOnly -> {
                when (
                    val created = createCampaignPerson(
                        CampaignPersonDraft(
                            kind = PersonKind.Npc,
                            name = name,
                            description = "",
                            sheet = sheet,
                            overlayHitPoints = null,
                            overlayNotes = "",
                        )
                    )
                ) {
                    is CreateCampaignPersonUseCase.Result.Created -> {
                        Result.SavedToCampaign(created.person)
                    }
                    CreateCampaignPersonUseCase.Result.InvalidName -> Result.InvalidName
                    CreateCampaignPersonUseCase.Result.NoActiveCampaign -> Result.NoActiveCampaign
                    CreateCampaignPersonUseCase.Result.InvalidKind,
                    CreateCampaignPersonUseCase.Result.InvalidClassLevels,
                    -> Result.InvalidName
                }
            }
        }
    }
}
