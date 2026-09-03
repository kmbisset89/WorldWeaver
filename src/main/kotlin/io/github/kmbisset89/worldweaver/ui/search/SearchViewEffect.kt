package io.github.kmbisset89.worldweaver.ui.search

import io.github.kmbisset89.worldweaver.domain.SearchHit

internal sealed interface SearchViewEffect {
    data class RecordOpened(val hit: SearchHit) : SearchViewEffect
}
