package io.github.kmbisset89.worldweaver.domain

internal class LoadWikilinkCatalogUseCase(
    private val campaignRepository: CampaignRepository,
    private val locationRepository: LocationRepository,
    private val loreRepository: LoreRepository,
    private val factionRepository: FactionRepository,
    private val worldPersonRepository: WorldPersonRepository,
    private val campaignPersonRepository: CampaignPersonRepository,
    private val questRepository: QuestRepository,
    private val sessionRepository: SessionRepository,
    private val catalogFactory: WikilinkCatalogFactory = WikilinkCatalogFactory(),
) {
    suspend operator fun invoke(worldId: String): WikilinkCatalog {
        val campaigns = campaignRepository.getByWorld(worldId)
        val campaignWorldIds = campaigns.associate { campaign -> campaign.id to worldId }
        val campaignIds = campaigns.map { it.id }
        return catalogFactory.create(
            locations = locationRepository.getByWorld(worldId),
            lore = loreRepository.getByWorld(worldId),
            factions = factionRepository.getByWorld(worldId),
            worldPeople = worldPersonRepository.getByWorld(worldId),
            campaignPeople = campaignIds.flatMap { campaignPersonRepository.getByCampaign(it) },
            quests = campaignIds.flatMap { questRepository.getByCampaign(it) },
            sessions = campaignIds.flatMap { sessionRepository.getByCampaign(it) },
            campaignWorldIds = campaignWorldIds,
        )
    }
}
