package io.github.kmbisset89.worldweaver.domain

internal class AddWorldPersonToCampaignUseCase(
    private val worldPersonRepository: WorldPersonRepository,
    private val campaignPersonRepository: CampaignPersonRepository,
    private val activeContextRepository: ActiveContextRepository,
    private val entityIdFactory: EntityIdFactory,
    private val instantProvider: InstantProvider,
    private val avatarFileStore: PersonAvatarFileStore,
    private val voiceClipFileStore: VoiceClipFileStore,
) {
    sealed interface Result {
        data class Added(val person: CampaignPerson) : Result
        data object NoActiveCampaign : Result
        data object WorldPersonNotFound : Result
        data object AlreadyAdded : Result
    }

    suspend operator fun invoke(worldPersonId: String): Result {
        val campaignId = activeContextRepository.get().activeCampaignId
            ?: return Result.NoActiveCampaign
        val worldPerson = worldPersonRepository.getById(worldPersonId)
            ?: return Result.WorldPersonNotFound
        val alreadyAdded = campaignPersonRepository.getByCampaign(campaignId)
            .any { it.worldPersonId == worldPersonId }
        if (alreadyAdded) {
            return Result.AlreadyAdded
        }
        val now = instantProvider.now()
        val person = CampaignPerson(
            id = entityIdFactory.create(),
            campaignId = campaignId,
            worldPersonId = worldPerson.id,
            kind = worldPerson.kind,
            name = worldPerson.name,
            description = worldPerson.description,
            sheet = FifthEditionSheet.empty(),
            overlayHitPoints = worldPerson.sheet.hitPoints,
            overlayNotes = "",
            createdAt = now,
            updatedAt = now,
        )
        campaignPersonRepository.insert(person)
        avatarFileStore.copy(PersonRef.World(worldPerson.id), PersonRef.Campaign(person.id))
        voiceClipFileStore.copy(
            VoiceClipRef.WorldPerson(worldPerson.id),
            VoiceClipRef.CampaignPerson(person.id),
        )
        return Result.Added(person)
    }
}
