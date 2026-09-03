package io.github.kmbisset89.worldweaver.ui.search

import io.github.kmbisset89.worldweaver.domain.SearchHit

internal sealed class SearchViewState {
    data class Content(
        val query: String,
        val results: List<SearchHit>,
        val resultsVisible: Boolean,
    ) : SearchViewState()
}
