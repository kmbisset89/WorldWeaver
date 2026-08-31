package net.tactware.worldweaver.ui.search

import net.tactware.worldweaver.domain.SearchHit

internal sealed class SearchViewState {
    data class Content(
        val query: String,
        val results: List<SearchHit>,
        val resultsVisible: Boolean,
    ) : SearchViewState()
}
