package io.github.kmbisset89.worldweaver.domain

internal class SearchRecordsUseCase(
    private val worldRepository: WorldRepository,
    private val campaignRepository: CampaignRepository,
    private val locationRepository: LocationRepository,
    private val loreRepository: LoreRepository,
    private val observanceRepository: WorldCalendarObservanceRepository,
    private val celestialBodyRepository: WorldCelestialBodyRepository,
    private val factionRepository: FactionRepository,
    private val worldPersonRepository: WorldPersonRepository,
    private val campaignPersonRepository: CampaignPersonRepository,
    private val questRepository: QuestRepository,
    private val sessionRepository: SessionRepository,
    private val randomTableRepository: RandomTableRepository,
    private val assetRepository: AssetRepository,
) {
    suspend operator fun invoke(query: String): List<SearchHit> {
        val trimmed = query.trim()
        if (trimmed.length < MIN_QUERY_LENGTH) {
            return emptyList()
        }
        val worlds = worldRepository.search(trimmed).map { world ->
            hit(
                kind = SearchKind.World,
                id = world.id,
                title = world.name,
                snippet = snippet(world.description),
                worldId = world.id,
                campaignId = null,
            )
        }
        val campaigns = campaignRepository.search(trimmed).map { campaign ->
            hit(
                kind = SearchKind.Campaign,
                id = campaign.id,
                title = campaign.name,
                snippet = snippet(campaign.description.ifBlank { campaign.notes }),
                worldId = campaign.worldId,
                campaignId = campaign.id,
            )
        }
        val locations = locationRepository.search(trimmed).map { location ->
            hit(
                kind = SearchKind.Location,
                id = location.id,
                title = location.name,
                snippet = snippet(location.description.ifBlank { location.notes }),
                worldId = location.worldId,
                campaignId = null,
            )
        }
        val lore = loreRepository.search(trimmed).map { entry ->
            hit(
                kind = SearchKind.Lore,
                id = entry.id,
                title = entry.title,
                snippet = snippet(entry.content),
                worldId = entry.worldId,
                campaignId = null,
            )
        }
        val observances = observanceRepository.search(trimmed).map { observance ->
            hit(
                kind = SearchKind.Observance,
                id = observance.id,
                title = observance.name,
                snippet = snippet(observance.notes),
                worldId = observance.worldId,
                campaignId = null,
            )
        }
        val celestialBodies = celestialBodyRepository.search(trimmed).map { body ->
            hit(
                kind = SearchKind.CelestialBody,
                id = body.id,
                title = body.name,
                snippet = snippet(body.notes.ifBlank { body.kind.displayName }),
                worldId = body.worldId,
                campaignId = null,
            )
        }
        val factions = factionRepository.search(trimmed).map { faction ->
            hit(
                kind = SearchKind.Faction,
                id = faction.id,
                title = faction.name,
                snippet = snippet(faction.description.ifBlank { faction.goals }),
                worldId = faction.worldId,
                campaignId = null,
            )
        }
        val worldPeople = worldPersonRepository.search(trimmed).map { person ->
            hit(
                kind = SearchKind.WorldPerson,
                id = person.id,
                title = person.name,
                snippet = snippet(person.description),
                worldId = person.worldId,
                campaignId = null,
            )
        }
        val campaignPeople = campaignPersonRepository.search(trimmed).map { person ->
            val campaign = campaignRepository.getById(person.campaignId)
            hit(
                kind = SearchKind.CampaignPerson,
                id = person.id,
                title = person.name,
                snippet = snippet(person.description),
                worldId = campaign?.worldId,
                campaignId = person.campaignId,
            )
        }
        val quests = questRepository.search(trimmed).map { quest ->
            val campaign = campaignRepository.getById(quest.campaignId)
            hit(
                kind = SearchKind.Quest,
                id = quest.id,
                title = quest.title,
                snippet = snippet(quest.summary),
                worldId = campaign?.worldId,
                campaignId = quest.campaignId,
            )
        }
        val sessions = sessionRepository.search(trimmed).map { session ->
            val campaign = campaignRepository.getById(session.campaignId)
            hit(
                kind = SearchKind.Session,
                id = session.id,
                title = session.name,
                snippet = snippet(session.notes),
                worldId = campaign?.worldId,
                campaignId = session.campaignId,
            )
        }
        val tables = randomTableRepository.search(trimmed).map { table ->
            hit(
                kind = SearchKind.RandomTable,
                id = table.id,
                title = table.name,
                snippet = snippet(table.notes),
                worldId = table.worldId,
                campaignId = null,
            )
        }
        val assets = assetRepository.search(trimmed).map { asset ->
            hit(
                kind = SearchKind.Asset,
                id = asset.id,
                title = asset.displayName,
                snippet = snippet(asset.notes.ifBlank { asset.originalFileName }),
                worldId = asset.worldId,
                campaignId = null,
            )
        }
        return worlds + campaigns + locations + lore + observances + celestialBodies + factions +
            worldPeople + campaignPeople + quests + sessions + tables + assets
    }

    private fun hit(
        kind: SearchKind,
        id: String,
        title: String,
        snippet: String,
        worldId: String?,
        campaignId: String?,
    ): SearchHit {
        return SearchHit(
            kind = kind,
            id = id,
            title = title,
            snippet = snippet,
            worldId = worldId,
            campaignId = campaignId,
        )
    }

    private fun snippet(text: String): String {
        val collapsed = text.trim().replace(WHITESPACE, " ")
        return if (collapsed.length <= SNIPPET_LIMIT) {
            collapsed
        } else {
            collapsed.take(SNIPPET_LIMIT - 3) + "..."
        }
    }

    private companion object {
        const val MIN_QUERY_LENGTH = 2
        const val SNIPPET_LIMIT = 120
        val WHITESPACE = Regex("\\s+")
    }
}
