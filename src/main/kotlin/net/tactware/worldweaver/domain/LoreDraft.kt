package net.tactware.worldweaver.domain

internal data class LoreDraft(
    val title: String,
    val content: String,
    val category: LoreCategory,
    val tags: List<String>,
    val relatedEntryIds: List<String>,
    val secrets: List<LoreSecret>,
    val locationId: String?,
    val characterId: String?,
)
