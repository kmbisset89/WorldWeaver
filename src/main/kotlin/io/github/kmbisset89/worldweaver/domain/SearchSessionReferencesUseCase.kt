package io.github.kmbisset89.worldweaver.domain

internal class SearchSessionReferencesUseCase(
    private val searchRecords: SearchRecordsUseCase,
) {
    suspend operator fun invoke(
        query: String,
        worldId: String,
        campaignId: String,
    ): List<SearchHit> {
        return searchRecords(query).filter { hit ->
            hit.kind in REFERENCE_KINDS && matchesContext(hit, worldId, campaignId)
        }
    }

    private fun matchesContext(hit: SearchHit, worldId: String, campaignId: String): Boolean {
        return when (hit.kind) {
            SearchKind.CampaignPerson -> hit.campaignId == campaignId
            SearchKind.Location, SearchKind.Lore, SearchKind.WorldPerson -> hit.worldId == worldId
            else -> false
        }
    }

    private companion object {
        val REFERENCE_KINDS = setOf(
            SearchKind.Location,
            SearchKind.Lore,
            SearchKind.WorldPerson,
            SearchKind.CampaignPerson,
        )
    }
}
