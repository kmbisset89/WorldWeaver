package io.github.kmbisset89.worldweaver.ui.search

import io.github.kmbisset89.worldweaver.domain.SearchHit

internal sealed interface SearchInteraction {
    data class QueryChanged(val query: String) : SearchInteraction
    data class ResultSelected(val hit: SearchHit) : SearchInteraction
    data object ResultsDismissed : SearchInteraction
}
