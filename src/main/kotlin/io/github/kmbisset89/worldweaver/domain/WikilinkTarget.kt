package io.github.kmbisset89.worldweaver.domain

internal data class WikilinkTarget(
    val kind: WikilinkTargetKind,
    val id: String,
    val title: String,
    val worldId: String?,
    val campaignId: String?,
) {
    fun insertionToken(alias: String = title): String {
        val display = alias.ifBlank { title }
        return "[[${kind.tokenPrefix}:$id|$display]]"
    }

    fun toSearchHit(): SearchHit {
        return SearchHit(
            kind = kind.searchKind,
            id = id,
            title = title,
            snippet = kind.searchKind.displayName,
            worldId = worldId,
            campaignId = campaignId,
        )
    }
}
