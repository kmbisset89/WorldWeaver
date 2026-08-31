package net.tactware.worldweaver.domain

internal class DeleteWorldPersonUseCase(
    private val worldPersonRepository: WorldPersonRepository,
    private val campaignPersonRepository: CampaignPersonRepository,
    private val personRelationshipRepository: PersonRelationshipRepository,
    private val personCompanionRepository: PersonCompanionRepository,
    private val questRepository: QuestRepository,
    private val avatarFileStore: PersonAvatarFileStore,
    private val voiceClipFileStore: VoiceClipFileStore,
) {
    sealed interface Result {
        data object Deleted : Result
        data class Blocked(val referenceCount: Int) : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(personId: String): Result {
        worldPersonRepository.getById(personId) ?: return Result.NotFound
        val referenceCount = campaignPersonRepository.countByWorldPerson(personId)
        if (referenceCount > 0) {
            return Result.Blocked(referenceCount)
        }
        personRelationshipRepository.deleteByPerson(PersonRef.World(personId))
        personCompanionRepository.deleteByPerson(PersonRef.World(personId))
        questRepository.deleteLinksByTarget(QuestLinkKind.WORLD_PERSON, personId)
        avatarFileStore.delete(PersonRef.World(personId))
        voiceClipFileStore.delete(VoiceClipRef.WorldPerson(personId))
        worldPersonRepository.delete(personId)
        return Result.Deleted
    }
}
