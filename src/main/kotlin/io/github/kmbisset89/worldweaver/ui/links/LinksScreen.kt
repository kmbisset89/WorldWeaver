package io.github.kmbisset89.worldweaver.ui.links

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.PublicOff
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.ui.components.ActionIconButtonComposeWidget
import io.github.kmbisset89.worldweaver.ui.components.FeatureEmptyState
import io.github.kmbisset89.worldweaver.ui.components.FeatureErrorState
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
                LinksHeader(
                    subtitle = "Relationship web",
                    searchQuery = null,
                    onDrawerToggle = null,
                    onInteraction = onInteraction,
                )
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is LinksViewState.Error -> {
                LinksHeader(
                    subtitle = "Relationship web",
                    searchQuery = null,
                    onDrawerToggle = null,
                    onInteraction = onInteraction,
                )
                FeatureErrorState(
                    message = viewState.message,
                    canRetry = viewState.canRetry,
                    onRetry = { onInteraction(LinksInteraction.RetrySelected) },
                )
            }
            LinksViewState.NoActiveWorld -> {
                LinksHeader(
                    subtitle = "Select a world first",
                    searchQuery = null,
                    onDrawerToggle = null,
                    onInteraction = onInteraction,
                )
                FeatureEmptyState(
                    icon = Icons.Default.PublicOff,
                    title = "No active world",
                    message = "Create or select a world first so the relationship web has a setting.",
                    actionLabel = "Go to Worlds",
                    onAction = { onInteraction(LinksInteraction.CreateWorldSelected) },
                )
            }
            is LinksViewState.Empty -> {
                var drawerOpen by remember { mutableStateOf(false) }
                LinksHeader(
                    subtitle = headerSubtitle(viewState.worldName, viewState.campaignName),
                    searchQuery = viewState.searchQuery,
                    onDrawerToggle = { drawerOpen = !drawerOpen },
                    onInteraction = onInteraction,
                )
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    FeatureEmptyState(
                        icon = Icons.Default.AccountTree,
                        title = if (viewState.hiddenByFilters) "Nothing visible" else "No linkages yet",
                        message = if (viewState.hiddenByFilters) {
                            "No people or factions match the current filters. Show unlinked nodes or turn relationship types back on."
                        } else {
                            "Add relationships on Characters or memberships on Factions to see the web."
                        },
                    )
                    LinksDrawerComposeWidget(
                        visible = drawerOpen,
                        filters = LinksDrawerFilterState(
                            showIsolates = viewState.showIsolates,
                            showMemberships = viewState.showMemberships,
                            enabledRelationshipTypes = viewState.enabledRelationshipTypes,
                        ),
                        inspector = null,
                        onDismissed = { drawerOpen = false },
                        onInteraction = onInteraction,
                    )
                }
            }
            is LinksViewState.Content -> {
                var drawerOpen by remember { mutableStateOf(false) }
                LaunchedEffect(viewState.selectedNodeId) {
                    if (viewState.selectedNodeId != null) {
                        drawerOpen = true
                    }
                }
                LinksHeader(
                    subtitle = headerSubtitle(viewState.worldName, viewState.campaignName),
                    searchQuery = viewState.searchQuery,
                    onDrawerToggle = { drawerOpen = !drawerOpen },
                    onInteraction = onInteraction,
                )
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        RelationshipWebCanvasComposeWidget(
                            nodes = viewState.nodes,
                            edges = viewState.edges,
                            selectedNodeId = viewState.selectedNodeId,
                            searchQuery = viewState.searchQuery,
                            onNodeSelected = { onInteraction(LinksInteraction.NodeSelected(it)) },
                            onSelectionCleared = { onInteraction(LinksInteraction.SelectionCleared) },
                        )
                    }
                    LinksDrawerComposeWidget(
                        visible = drawerOpen,
                        filters = LinksDrawerFilterState(
                            showIsolates = viewState.showIsolates,
                            showMemberships = viewState.showMemberships,
                            enabledRelationshipTypes = viewState.enabledRelationshipTypes,
                        ),
                        inspector = viewState.inspector,
                        onDismissed = { drawerOpen = false },
                        onInteraction = onInteraction,
                    )
                }
            }
        }
    }
}

@Composable
private fun LinksHeader(
    subtitle: String,
    searchQuery: String?,
    onDrawerToggle: (() -> Unit)?,
    onInteraction: (LinksInteraction) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
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
            if (onDrawerToggle != null) {
                ActionIconButtonComposeWidget(
                    icon = Icons.Default.Tune,
                    tooltip = "Filters and details",
                    onClick = onDrawerToggle,
                )
            }
        }
        if (searchQuery != null) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { onInteraction(LinksInteraction.SearchQueryChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Highlight a name") },
                label = { Text("Search") },
            )
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
