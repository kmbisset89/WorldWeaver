package io.github.kmbisset89.worldweaver.ui.locations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PublicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.Location
import io.github.kmbisset89.worldweaver.domain.LocationType
import io.github.kmbisset89.worldweaver.ui.components.AdaptiveListDetailBreakpoint
import io.github.kmbisset89.worldweaver.ui.components.AdaptiveListDetailComposeWidget
import io.github.kmbisset89.worldweaver.ui.components.AdaptiveScreenHeaderComposeWidget
import io.github.kmbisset89.worldweaver.ui.components.ConfirmDestructiveDialog
import io.github.kmbisset89.worldweaver.ui.components.FeatureEmptyState
import io.github.kmbisset89.worldweaver.ui.components.FeatureErrorState
import io.github.kmbisset89.worldweaver.ui.components.rememberAdaptiveListOpen
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun LocationsScreen(
    viewState: LocationsViewState,
    onInteraction: (LocationsInteraction) -> Unit,
) {
    LaunchedEffect(Unit) {
        onInteraction(LocationsInteraction.ScreenStarted)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        when (viewState) {
            LocationsViewState.Loading -> {
                LocationsHeader(subtitle = "Places in the active world", showCreate = false, onInteraction = onInteraction)
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is LocationsViewState.Error -> {
                LocationsHeader(subtitle = "Places in the active world", showCreate = false, onInteraction = onInteraction)
                FeatureErrorState(
                    message = viewState.message,
                    canRetry = viewState.canRetry,
                    onRetry = { onInteraction(LocationsInteraction.RetrySelected) },
                )
            }
            LocationsViewState.NoActiveWorld -> {
                LocationsHeader(subtitle = "Select a world first", showCreate = false, onInteraction = onInteraction)
                FeatureEmptyState(
                    icon = Icons.Default.PublicOff,
                    title = "No active world",
                    message = "Create or select a world first so locations have a setting.",
                    actionLabel = "Go to Worlds",
                    onAction = { onInteraction(LocationsInteraction.CreateWorldSelected) },
                )
            }
            is LocationsViewState.Empty -> {
                LocationsHeader(
                    subtitle = viewState.worldName,
                    showCreate = true,
                    hasWorldRootMap = viewState.hasWorldRootMap,
                    onInteraction = onInteraction,
                )
                FeatureEmptyState(
                    icon = Icons.Default.Place,
                    title = "No locations yet",
                    message = "Create a continent to start mapping this world.",
                    actionLabel = "New location",
                    onAction = { onInteraction(LocationsInteraction.NewLocationSelected) },
                )
                viewState.editor?.let { editor ->
                    LocationEditorDialog(editor = editor, onInteraction = onInteraction)
                }
            }
            is LocationsViewState.Content -> {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val compact = maxWidth < AdaptiveListDetailBreakpoint
                    var listOpen by rememberAdaptiveListOpen(compact)
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        LocationsHeader(
                            subtitle = viewState.worldName,
                            showCreate = true,
                            hasWorldRootMap = viewState.hasWorldRootMap,
                            compact = compact,
                            selectedName = viewState.selectedLocation?.name,
                            onListToggle = { listOpen = !listOpen },
                            onInteraction = onInteraction,
                        )
                        LocationsContent(
                            state = viewState,
                            compact = compact,
                            listOpen = listOpen,
                            onListDismissed = { listOpen = false },
                            onInteraction = onInteraction,
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationsHeader(
    subtitle: String,
    showCreate: Boolean,
    onInteraction: (LocationsInteraction) -> Unit,
    hasWorldRootMap: Boolean = false,
    compact: Boolean = false,
    selectedName: String? = null,
    onListToggle: () -> Unit = {},
) {
    AdaptiveScreenHeaderComposeWidget(
        title = "Locations",
        subtitle = subtitle,
        compact = compact,
        switcherName = selectedName,
        listLabel = "locations list",
        onListToggle = onListToggle,
    ) {
        if (showCreate) {
            Button(
                onClick = { onInteraction(LocationsInteraction.OpenWorldMapSelected) },
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                Text(if (hasWorldRootMap) "Open world map" else "Add world map")
            }
            Button(
                onClick = { onInteraction(LocationsInteraction.NewLocationSelected) },
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text("New location")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LocationsContent(
    state: LocationsViewState.Content,
    compact: Boolean,
    listOpen: Boolean,
    onListDismissed: () -> Unit,
    onInteraction: (LocationsInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listInteraction: (LocationsInteraction) -> Unit = { interaction ->
        if (interaction is LocationsInteraction.LocationSelected) {
            onListDismissed()
        }
        onInteraction(interaction)
    }
    Column(modifier = modifier.fillMaxSize()) {
        if (state.blockDeleteReason != null) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).clickable {
                    onInteraction(LocationsInteraction.BlockReasonDismissed)
                },
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Text(
                    text = state.blockDeleteReason,
                    modifier = Modifier.padding(16.dp),
                    color = TextPrimary,
                    fontSize = 13.sp
                )
            }
        }
        AdaptiveListDetailComposeWidget(
            compact = compact,
            listOpen = listOpen,
            hasSelection = state.selectedLocation != null,
            listPane = { listModifier ->
                Column(
                    modifier = listModifier,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { onInteraction(LocationsInteraction.SearchQueryChanged(it)) },
                        label = { Text("Search locations") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        FilterChip(
                            selected = state.typeFilter == null,
                            onClick = { onInteraction(LocationsInteraction.TypeFilterSelected(null)) },
                            label = { Text("All types") },
                        )
                        LocationType.entries.forEach { type ->
                            FilterChip(
                                selected = state.typeFilter == type,
                                onClick = { onInteraction(LocationsInteraction.TypeFilterSelected(type)) },
                                label = { Text(type.displayName) },
                            )
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (state.visibleTree.isEmpty()) {
                            item {
                                Text(
                                    text = "No locations match this search.",
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        } else {
                            items(flattenTree(state.visibleTree), key = { it.location.id }) { row ->
                                LocationTreeRow(
                                    location = row.location,
                                    depth = row.depth,
                                    isSelected = row.location.id == state.selectedLocation?.id,
                                    onInteraction = listInteraction,
                                )
                            }
                        }
                    }
                }
            },
            detailPane = { detailModifier ->
                if (state.selectedLocation != null) {
                    LocationDetailPane(
                        location = state.selectedLocation,
                        breadcrumbs = state.breadcrumbs,
                        overlay = state.overlay,
                        campaignName = state.campaignName,
                        attachedLore = state.attachedLore,
                        attachedQuests = state.attachedQuests,
                        voiceClipPath = state.voiceClipPath,
                        isRecordingVoice = state.isRecordingVoice,
                        isPlayingVoice = state.isPlayingVoice,
                        selectedLocationHasMap = state.selectedLocationHasMap,
                        onInteraction = onInteraction,
                        modifier = detailModifier,
                    )
                } else {
                    Text(
                        text = "Select a location to see its details.",
                        color = TextSecondary,
                        modifier = detailModifier.padding(16.dp)
                    )
                }
            },
            onListDismissed = onListDismissed,
            dismissListLabel = "Dismiss locations list",
            modifier = Modifier.weight(1f).fillMaxWidth(),
        )
    }

    state.editor?.let { editor ->
        LocationEditorDialog(editor = editor, onInteraction = onInteraction)
    }
    state.pendingDelete?.let { pending ->
        ConfirmDestructiveDialog(
            title = "Delete location?",
            message = "Delete “${pending.locationName}”? Child locations must be deleted first.",
            confirmLabel = "Delete",
            onConfirm = { onInteraction(LocationsInteraction.DeleteConfirmed) },
            onDismiss = { onInteraction(LocationsInteraction.DeleteCancelled) },
        )
    }
}

@Composable
private fun LocationTreeRow(
    location: Location,
    depth: Int,
    isSelected: Boolean,
    onInteraction: (LocationsInteraction) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (depth * 16).dp)
            .clickable { onInteraction(LocationsInteraction.LocationSelected(location.id)) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isSelected) {
                        Modifier.background(NavyBlue.copy(alpha = 0.08f))
                    } else {
                        Modifier
                    }
                )
                .padding(14.dp)
        ) {
            Text(
                text = location.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = location.type.displayName,
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private data class LocationTreeRowModel(
    val location: Location,
    val depth: Int,
)

private fun flattenTree(
    nodes: List<LocationsViewState.LocationTreeNode>,
    depth: Int = 0,
): List<LocationTreeRowModel> {
    return nodes.flatMap { node ->
        listOf(LocationTreeRowModel(node.location, depth)) + flattenTree(node.children, depth + 1)
    }
}
