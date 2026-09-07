package io.github.kmbisset89.worldweaver.domain

internal data class WikilinkBacklink(
    val sourceKind: WikilinkSourceKind,
    val sourceId: String,
    val sourceTitle: String,
    val snippet: String,
    val worldId: String?,
    val campaignId: String?,
) {
    fun toSearchHit(): SearchHit {
        val searchKind = when (sourceKind) {
            WikilinkSourceKind.Lore -> SearchKind.Lore
            WikilinkSourceKind.SessionNotes,
            WikilinkSourceKind.SessionScratch,
            WikilinkSourceKind.SessionRecap,
            WikilinkSourceKind.SessionScene,
            -> SearchKind.Session
        }
        return SearchHit(
            kind = searchKind,
            id = sourceId,
            title = sourceTitle,
            snippet = snippet,
            worldId = worldId,
            campaignId = campaignId,
        )
    }
}
