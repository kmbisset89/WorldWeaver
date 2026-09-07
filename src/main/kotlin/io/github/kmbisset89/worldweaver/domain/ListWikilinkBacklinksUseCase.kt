package io.github.kmbisset89.worldweaver.domain

internal class ListWikilinkBacklinksUseCase(
    private val loreRepository: LoreRepository,
    private val campaignRepository: CampaignRepository,
    private val sessionRepository: SessionRepository,
    private val loadCatalog: LoadWikilinkCatalogUseCase,
    private val parser: WikilinkTextParser = WikilinkTextParser(),
) {
    suspend operator fun invoke(
        worldId: String,
        targetId: String,
    ): List<WikilinkBacklink> {
        val catalog = loadCatalog(worldId)
        val lore = loreRepository.getByWorld(worldId)
        val campaigns = campaignRepository.getByWorld(worldId)
        val sessions = campaigns.flatMap { campaign ->
            sessionRepository.getByCampaign(campaign.id)
        }
        val campaignWorldIds = campaigns.associate { it.id to worldId }
        val backlinks = mutableListOf<WikilinkBacklink>()
        lore.forEach { entry ->
            if (entry.id == targetId) {
                return@forEach
            }
            collectMentions(
                text = entry.content,
                catalog = catalog,
                targetId = targetId,
                sourceKind = WikilinkSourceKind.Lore,
                sourceId = entry.id,
                sourceTitle = entry.title,
                worldId = entry.worldId,
                campaignId = null,
                into = backlinks,
            )
        }
        sessions.forEach { session ->
            val worldForSession = campaignWorldIds[session.campaignId]
            collectMentions(
                text = session.notes,
                catalog = catalog,
                targetId = targetId,
                sourceKind = WikilinkSourceKind.SessionNotes,
                sourceId = session.id,
                sourceTitle = session.name,
                worldId = worldForSession,
                campaignId = session.campaignId,
                into = backlinks,
            )
            collectMentions(
                text = session.scratchNotes,
                catalog = catalog,
                targetId = targetId,
                sourceKind = WikilinkSourceKind.SessionScratch,
                sourceId = session.id,
                sourceTitle = session.name,
                worldId = worldForSession,
                campaignId = session.campaignId,
                into = backlinks,
            )
            collectMentions(
                text = session.recap,
                catalog = catalog,
                targetId = targetId,
                sourceKind = WikilinkSourceKind.SessionRecap,
                sourceId = session.id,
                sourceTitle = session.name,
                worldId = worldForSession,
                campaignId = session.campaignId,
                into = backlinks,
            )
            session.scenes.forEach { scene ->
                collectMentions(
                    text = scene.notes,
                    catalog = catalog,
                    targetId = targetId,
                    sourceKind = WikilinkSourceKind.SessionScene,
                    sourceId = session.id,
                    sourceTitle = "${session.name} · ${scene.title}",
                    worldId = worldForSession,
                    campaignId = session.campaignId,
                    into = backlinks,
                )
            }
        }
        return backlinks
    }

    private fun collectMentions(
        text: String,
        catalog: WikilinkCatalog,
        targetId: String,
        sourceKind: WikilinkSourceKind,
        sourceId: String,
        sourceTitle: String,
        worldId: String?,
        campaignId: String?,
        into: MutableList<WikilinkBacklink>,
    ) {
        if (text.isBlank()) {
            return
        }
        parser.parse(text).forEach { segment ->
            val link = segment as? WikilinkSegment.Link ?: return@forEach
            val resolved = catalog.resolve(link) ?: return@forEach
            if (resolved.id != targetId) {
                return@forEach
            }
            into += WikilinkBacklink(
                sourceKind = sourceKind,
                sourceId = sourceId,
                sourceTitle = sourceTitle,
                snippet = snippetAround(text, link.raw),
                worldId = worldId,
                campaignId = campaignId,
            )
        }
    }

    private fun snippetAround(text: String, raw: String): String {
        val index = text.indexOf(raw)
        if (index < 0) {
            return text.trim().replace(WHITESPACE, " ").take(SNIPPET_LIMIT)
        }
        val start = (index - 32).coerceAtLeast(0)
        val end = (index + raw.length + 32).coerceAtMost(text.length)
        val snippet = text.substring(start, end).trim().replace(WHITESPACE, " ")
        return if (snippet.length <= SNIPPET_LIMIT) {
            snippet
        } else {
            snippet.take(SNIPPET_LIMIT - 3) + "..."
        }
    }

    private companion object {
        const val SNIPPET_LIMIT = 120
        val WHITESPACE = Regex("\\s+")
    }
}
