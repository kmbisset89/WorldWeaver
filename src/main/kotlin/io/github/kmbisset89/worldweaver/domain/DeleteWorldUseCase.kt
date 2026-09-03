package io.github.kmbisset89.worldweaver.domain

internal class DeleteWorldUseCase(
    private val worldRepository: WorldRepository,
    private val campaignRepository: CampaignRepository,
    private val worldPersonRepository: WorldPersonRepository,
    private val locationRepository: LocationRepository,
    private val factionRepository: FactionRepository,
    private val factionMembershipRepository: FactionMembershipRepository,
    private val personRelationshipRepository: PersonRelationshipRepository,
    private val deleteWorldPerson: DeleteWorldPersonUseCase,
    private val voiceClipFileStore: VoiceClipFileStore,
    private val activeContextRepository: ActiveContextRepository,
) {
    sealed interface Result {
        data object Deleted : Result
        data class Blocked(val campaignCount: Int) : Result
    }

    suspend operator fun invoke(worldId: String): Result {
        val campaignCount = campaignRepository.countByWorld(worldId)
        if (campaignCount > 0) {
            return Result.Blocked(campaignCount)
        }
        worldPersonRepository.getByWorld(worldId).forEach { person ->
            deleteWorldPerson(person.id)
        }
        locationRepository.getByWorld(worldId).forEach { location ->
            voiceClipFileStore.delete(VoiceClipRef.Location(location.id))
        }
        factionRepository.getByWorld(worldId).forEach { faction ->
            factionMembershipRepository.deleteByFaction(faction.id)
            personRelationshipRepository.deleteByFaction(faction.id)
            factionRepository.delete(faction.id)
        }
        worldRepository.delete(worldId)
        val context = activeContextRepository.get()
        if (context.activeWorldId == worldId) {
            activeContextRepository.setActiveWorldId(null)
            activeContextRepository.setActiveCampaignId(null)
            activeContextRepository.setActiveSessionId(null)
        }
        return Result.Deleted
    }
}
