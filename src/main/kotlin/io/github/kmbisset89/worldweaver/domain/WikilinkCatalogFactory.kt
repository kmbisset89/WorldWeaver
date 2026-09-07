package io.github.kmbisset89.worldweaver.domain

internal class WikilinkCatalogFactory {
    fun create(
        locations: List<Location>,
        lore: List<Lore>,
        factions: List<Faction>,
        worldPeople: List<WorldPerson>,
        campaignPeople: List<CampaignPerson>,
        quests: List<Quest>,
        sessions: List<Session>,
        campaignWorldIds: Map<String, String>,
    ): WikilinkCatalog {
        val targets = buildList {
            locations.forEach { location ->
                add(
                    WikilinkTarget(
                        kind = WikilinkTargetKind.Location,
                        id = location.id,
                        title = location.name,
                        worldId = location.worldId,
                        campaignId = null,
                    )
                )
            }
            lore.forEach { entry ->
                add(
                    WikilinkTarget(
                        kind = WikilinkTargetKind.Lore,
                        id = entry.id,
                        title = entry.title,
                        worldId = entry.worldId,
                        campaignId = null,
                    )
                )
            }
            factions.forEach { faction ->
                add(
                    WikilinkTarget(
                        kind = WikilinkTargetKind.Faction,
                        id = faction.id,
                        title = faction.name,
                        worldId = faction.worldId,
                        campaignId = null,
                    )
                )
            }
            worldPeople.forEach { person ->
                add(
                    WikilinkTarget(
                        kind = WikilinkTargetKind.WorldPerson,
                        id = person.id,
                        title = person.name,
                        worldId = person.worldId,
                        campaignId = null,
                    )
                )
            }
            campaignPeople.forEach { person ->
                add(
                    WikilinkTarget(
                        kind = WikilinkTargetKind.CampaignPerson,
                        id = person.id,
                        title = person.name,
                        worldId = campaignWorldIds[person.campaignId],
                        campaignId = person.campaignId,
                    )
                )
            }
            quests.forEach { quest ->
                add(
                    WikilinkTarget(
                        kind = WikilinkTargetKind.Quest,
                        id = quest.id,
                        title = quest.title,
                        worldId = campaignWorldIds[quest.campaignId],
                        campaignId = quest.campaignId,
                    )
                )
            }
            sessions.forEach { session ->
                add(
                    WikilinkTarget(
                        kind = WikilinkTargetKind.Session,
                        id = session.id,
                        title = session.name,
                        worldId = campaignWorldIds[session.campaignId],
                        campaignId = session.campaignId,
                    )
                )
            }
        }
        return WikilinkCatalog(targets)
    }
}
