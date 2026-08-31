package net.tactware.worldweaver.ui.search

import net.tactware.worldweaver.domain.SearchHit

internal sealed interface SearchInteraction {
    data class QueryChanged(val query: String) : SearchInteraction
    data class ResultSelected(val hit: SearchHit) : SearchInteraction
    data object ResultsDismissed : SearchInteraction
}
