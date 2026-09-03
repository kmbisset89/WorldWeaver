package io.github.kmbisset89.worldweaver.ui.links

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.PublicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.RelationshipType
import io.github.kmbisset89.worldweaver.ui.components.FeatureEmptyState
import io.github.kmbisset89.worldweaver.ui.components.FeatureErrorState
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun LinksScreen(
    viewState: LinksViewState,
    onInteraction: (LinksInteraction) -> Unit,
) {
    LaunchedEffect(Unit) {
        onInteraction(LinksInteraction.ScreenStarted)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (viewState) {
            LinksViewState.Loading -> {
                LinksHeader(subtitle = "Relationship web", filters = null, onInteraction = onInteraction)
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is LinksViewState.Error -> {
                LinksHeader(subtitle = "Relationship web", filters = null, onInteraction = onInteraction)
                FeatureErrorState(
                    message = viewState.message,
                    canRetry = viewState.canRetry,
                    onRetry = { onInteraction(LinksInteraction.RetrySelected) },
                )
            }
            LinksViewState.NoActiveWorld -> {
                LinksHeader(subtitle = "Select a world first", filters = null, onInteraction = onInteraction)
                FeatureEmptyState(
                    icon = Icons.Default.PublicOff,
                    title = "No active world",
                    message = "Create or select a world first so the relationship web has a setting.",
                    actionLabel = "Go to Worlds",
                    onAction = { onInteraction(LinksInteraction.CreateWorldSelected) },
                )
            }
            is LinksViewState.Empty -> {
                LinksHeader(
                    subtitle = headerSubtitle(viewState.worldName, viewState.campaignName),
                    filters = FilterBarState(
                        searchQuery = viewState.searchQuery,
                        showIsolates = viewState.showIsolates,
                        showMemberships = viewState.showMemberships,
                        enabledRelationshipTypes = viewState.enabledRelationshipTypes,
                    ),
                    onInteraction = onInteraction,
                )
                FeatureEmptyState(
                    icon = Icons.Default.AccountTree,
                    title = if (viewState.hiddenByFilters) "Nothing visible" else "No linkages yet",
                    message = if (viewState.hiddenByFilters) {
                        "No people or factions match the current filters. Show unlinked nodes or turn relationship types back on."
                    } else {
                        "Add relationships on Characters or memberships on Factions to see the web."
                    },
                )
            }
            is LinksViewState.Content -> {
                LinksHeader(
                    subtitle = headerSubtitle(viewState.worldName, viewState.campaignName),
                    filters = FilterBarState(
                        searchQuery = viewState.searchQuery,
                        showIsolates = viewState.showIsolates,
                        showMemberships = viewState.showMemberships,
                        enabledRelationshipTypes = viewState.enabledRelationshipTypes,
                    ),
                    onInteraction = onInteraction,
                )
                LinksContent(state = viewState, onInteraction = onInteraction)
            }
        }
    }
}

@Composable
private fun LinksHeader(
    subtitle: String,
    filters: FilterBarState?,
    onInteraction: (LinksInteraction) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column {
            Text(
                text = "Links",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
        if (filters != null) {
            FilterBar(state = filters, onInteraction = onInteraction)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterBar(
    state: FilterBarState,
    onInteraction: (LinksInteraction) -> Unit,
) {
    OutlinedTextField(
        value = state.searchQuery,
        onValueChange = { onInteraction(LinksInteraction.SearchQueryChanged(it)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("Highlight a name") },
        label = { Text("Search") },
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        FilterChip(
            selected = state.showIsolates,
            onClick = { onInteraction(LinksInteraction.IsolateVisibilityToggled) },
            label = { Text("Show unlinked") },
        )
        FilterChip(
            selected = state.showMemberships,
            onClick = { onInteraction(LinksInteraction.MembershipEdgesToggled) },
            label = { Text("Memberships") },
        )
        RelationshipType.entries.forEach { type ->
            FilterChip(
                selected = type in state.enabledRelationshipTypes,
                onClick = { onInteraction(LinksInteraction.RelationshipTypeFilterToggled(type)) },
                label = { Text(type.displayName) },
            )
        }
    }
}

@Composable
private fun LinksContent(
    state: LinksViewState.Content,
    onInteraction: (LinksInteraction) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            RelationshipWebCanvasComposeWidget(
                nodes = state.nodes,
                edges = state.edges,
                positions = state.positions,
                selectedNodeId = state.selectedNodeId,
                searchQuery = state.searchQuery,
                onNodeSelected = { onInteraction(LinksInteraction.NodeSelected(it)) },
                onSelectionCleared = { onInteraction(LinksInteraction.SelectionCleared) },
            )
        }
        LinksInspectorPane(
            inspector = state.inspector,
            onInteraction = onInteraction,
            modifier = Modifier.width(320.dp).fillMaxHeight(),
        )
    }
}

@Composable
private fun LinksInspectorPane(
    inspector: LinksViewState.Inspector?,
    onInteraction: (LinksInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        if (inspector == null) {
            Text(
                text = "Select a person or faction to see their links.",
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(16.dp)
            )
            return@Card
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = inspector.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = inspector.subtitle,
                fontSize = 13.sp,
                color = TextSecondary
            )
            Button(
                onClick = { onInteraction(LinksInteraction.NodeOpened(inspector.nodeId)) },
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                Text("Open")
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Linkages",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            if (inspector.edges.isEmpty()) {
                Text(
                    text = "No visible links.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            } else {
                inspector.edges.forEach { edge ->
                    Text(
                        text = edge.label,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

private fun headerSubtitle(worldName: String, campaignName: String?): String {
    return if (campaignName.isNullOrBlank()) {
        worldName
    } else {
        "$worldName · $campaignName"
    }
}

private data class FilterBarState(
    val searchQuery: String,
    val showIsolates: Boolean,
    val showMemberships: Boolean,
    val enabledRelationshipTypes: Set<RelationshipType>,
)
