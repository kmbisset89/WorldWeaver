package io.github.kmbisset89.worldweaver.domain

internal class CreateQuestUseCase(
    private val questRepository: QuestRepository,
    private val campaignRepository: CampaignRepository,
    private val locationRepository: LocationRepository,
    private val loreRepository: LoreRepository,
    private val worldPersonRepository: WorldPersonRepository,
    private val campaignPersonRepository: CampaignPersonRepository,
    private val sessionRepository: SessionRepository,
    private val activeContextRepository: ActiveContextRepository,
    private val entityIdFactory: EntityIdFactory,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data class Created(val quest: Quest) : Result
        data object InvalidTitle : Result
        data object InvalidLocation : Result
        data object NoActiveCampaign : Result
    }

    suspend operator fun invoke(draft: QuestDraft): Result {
        val campaignId = activeContextRepository.get().activeCampaignId
            ?: return Result.NoActiveCampaign
        val campaign = campaignRepository.getById(campaignId) ?: return Result.NoActiveCampaign
        val title = draft.title.trim()
        if (title.isEmpty()) {
            return Result.InvalidTitle
        }
        val locationId = when (val resolved = resolveLocation(draft.locationId, campaign.worldId)) {
            LocationResolution.None -> null
            LocationResolution.Invalid -> return Result.InvalidLocation
            is LocationResolution.Found -> resolved.id
        }
        val now = instantProvider.now()
        val quest = Quest(
            id = entityIdFactory.create(),
            campaignId = campaignId,
            title = title,
            summary = draft.summary.trim(),
            status = draft.status,
            locationId = locationId,
            objectives = assignObjectives(draft.objectives),
            links = resolveLinks(draft.links, campaign),
            createdAt = now,
            updatedAt = now,
        )
        questRepository.insert(quest)
        return Result.Created(quest)
    }

    private suspend fun resolveLocation(
        locationId: String?,
        worldId: String,
    ): LocationResolution {
        val id = locationId?.takeIf { it.isNotBlank() } ?: return LocationResolution.None
        val location = locationRepository.getById(id) ?: return LocationResolution.Invalid
        return if (location.worldId == worldId) {
            LocationResolution.Found(id)
        } else {
            LocationResolution.Invalid
        }
    }

    private fun assignObjectives(objectives: List<QuestObjective>): List<QuestObjective> {
        return objectives.map { objective ->
            objective.copy(
                id = objective.id.ifBlank { entityIdFactory.create() },
                title = objective.title.trim(),
            )
        }.filter { it.title.isNotEmpty() }
    }

    private suspend fun resolveLinks(
        links: List<QuestLink>,
        campaign: Campaign,
    ): List<QuestLink> {
        val seen = mutableSetOf<Pair<QuestLinkKind, String>>()
        return links.mapNotNull { link ->
            val targetId = link.targetId.trim()
            if (targetId.isEmpty()) {
                return@mapNotNull null
            }
            val valid = when (link.kind) {
                QuestLinkKind.LORE -> {
                    val lore = loreRepository.getById(targetId)
                    lore != null && lore.worldId == campaign.worldId
                }
                QuestLinkKind.WORLD_PERSON -> {
                    val person = worldPersonRepository.getById(targetId)
                    person != null && person.worldId == campaign.worldId
                }
                QuestLinkKind.CAMPAIGN_PERSON -> {
                    val person = campaignPersonRepository.getById(targetId)
                    person != null && person.campaignId == campaign.id
                }
                QuestLinkKind.SESSION -> {
                    val session = sessionRepository.getById(targetId)
                    session != null && session.campaignId == campaign.id
                }
            }
            if (!valid || !seen.add(link.kind to targetId)) {
                null
            } else {
                link.copy(
                    id = link.id.ifBlank { entityIdFactory.create() },
                    targetId = targetId,
                )
            }
        }
    }

    private sealed interface LocationResolution {
        data object None : LocationResolution
        data object Invalid : LocationResolution
        data class Found(val id: String) : LocationResolution
    }
}
