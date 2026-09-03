package io.github.kmbisset89.worldweaver.ui.search

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import io.github.kmbisset89.worldweaver.core.AppCoroutineScope
import io.github.kmbisset89.worldweaver.domain.SearchHit
import io.github.kmbisset89.worldweaver.domain.SearchRecordsUseCase

internal class SearchViewModel(
    private val appScope: AppCoroutineScope,
    private val searchRecords: SearchRecordsUseCase,
) {
    private val _state = MutableStateFlow<SearchViewState>(
        SearchViewState.Content(
            query = "",
            results = emptyList(),
            resultsVisible = false,
        )
    )
    val state: StateFlow<SearchViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<SearchViewEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<SearchViewEffect> = _effects.asSharedFlow()

    private var searchJob: Job? = null

    fun onInteraction(interaction: SearchInteraction) {
        when (interaction) {
            is SearchInteraction.QueryChanged -> updateQuery(interaction.query)
            is SearchInteraction.ResultSelected -> selectResult(interaction.hit)
            SearchInteraction.ResultsDismissed -> dismissResults()
        }
    }

    private fun updateQuery(query: String) {
        updateContent { current ->
            current.copy(query = query, resultsVisible = query.isNotBlank())
        }
        searchJob?.cancel()
        if (query.trim().isEmpty()) {
            updateContent { current -> current.copy(results = emptyList(), resultsVisible = false) }
            return
        }
        searchJob = appScope.scope.launch {
            val results = searchRecords(query)
            updateContent { current ->
                current.copy(
                    results = results,
                    resultsVisible = current.query.isNotBlank(),
                )
            }
        }
    }

    private fun selectResult(hit: SearchHit) {
        _effects.tryEmit(SearchViewEffect.RecordOpened(hit))
        updateContent {
            SearchViewState.Content(
                query = "",
                results = emptyList(),
                resultsVisible = false,
            )
        }
    }

    private fun dismissResults() {
        updateContent { current -> current.copy(resultsVisible = false) }
    }

    private fun updateContent(
        transform: (SearchViewState.Content) -> SearchViewState.Content,
    ) {
        _state.update { current ->
            when (current) {
                is SearchViewState.Content -> transform(current)
            }
        }
    }
}
