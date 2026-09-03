package io.github.kmbisset89.worldweaver.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.SearchHit
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun SearchBar(
    viewState: SearchViewState,
    onInteraction: (SearchInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (viewState) {
        is SearchViewState.Content -> SearchBarContent(
            state = viewState,
            onInteraction = onInteraction,
            modifier = modifier,
        )
    }
}

@Composable
private fun SearchBarContent(
    state: SearchViewState.Content,
    onInteraction: (SearchInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)) {
        OutlinedTextField(
            value = state.query,
            onValueChange = { onInteraction(SearchInteraction.QueryChanged(it)) },
            label = { Text("Search worlds, campaigns, locations, lore, people, quests, and sessions") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        if (state.resultsVisible) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                if (state.results.isEmpty()) {
                    Text(
                        text = "No matching records.",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        state.results.groupBy { it.kind.displayName }.forEach { (section, hits) ->
                            item(key = "header-$section") {
                                Text(
                                    text = section,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                            items(hits, key = { hitKey(it) }) { hit ->
                                SearchResultRow(
                                    hit = hit,
                                    onSelected = {
                                        onInteraction(SearchInteraction.ResultSelected(hit))
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    hit: SearchHit,
    onSelected: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelected)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = hit.title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        if (hit.snippet.isNotBlank()) {
            Text(
                text = hit.snippet,
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

private fun hitKey(hit: SearchHit): String {
    return "${hit.kind.name}:${hit.id}"
}
