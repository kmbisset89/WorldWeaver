package net.tactware.worldweaver.ui.search

import net.tactware.worldweaver.domain.SearchHit

internal sealed interface SearchViewEffect {
    data class RecordOpened(val hit: SearchHit) : SearchViewEffect
}
