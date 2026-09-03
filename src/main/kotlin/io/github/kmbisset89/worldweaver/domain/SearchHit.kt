package io.github.kmbisset89.worldweaver.domain

internal data class SearchHit(
    val kind: SearchKind,
    val id: String,
    val title: String,
    val snippet: String,
    val worldId: String?,
    val campaignId: String?,
)
