package net.tactware.worldweaver.domain

internal class DeleteCampaignPersonUseCase(
    private val campaignPersonRepository: CampaignPersonRepository,
    private val personRelationshipRepository: PersonRelationshipRepository,
    private val personCompanionRepository: PersonCompanionRepository,
    private val questRepository: QuestRepository,
    private val avatarFileStore: PersonAvatarFileStore,
    private val voiceClipFileStore: VoiceClipFileStore,
) {
    sealed interface Result {
        data object Deleted : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(personId: String): Result {
        campaignPersonRepository.getById(personId) ?: return Result.NotFound
        personRelationshipRepository.deleteByPerson(PersonRef.Campaign(personId))
        personCompanionRepository.deleteByPerson(PersonRef.Campaign(personId))
        questRepository.deleteLinksByTarget(QuestLinkKind.CAMPAIGN_PERSON, personId)
        avatarFileStore.delete(PersonRef.Campaign(personId))
        voiceClipFileStore.delete(VoiceClipRef.CampaignPerson(personId))
        campaignPersonRepository.delete(personId)
        return Result.Deleted
    }
}
