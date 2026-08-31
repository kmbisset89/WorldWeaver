package net.tactware.worldweaver.ui.locations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.tactware.worldweaver.domain.Location
import net.tactware.worldweaver.domain.LocationType
import net.tactware.worldweaver.ui.components.ConfirmDestructiveDialog
import net.tactware.worldweaver.ui.components.FeatureEmptyState
import net.tactware.worldweaver.ui.components.FeatureErrorState
import net.tactware.worldweaver.ui.theme.NavyBlue
import net.tactware.worldweaver.ui.theme.SurfaceCard
import net.tactware.worldweaver.ui.theme.TextPrimary
import net.tactware.worldweaver.ui.theme.TextSecondary

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
                LocationsHeader(subtitle = viewState.worldName, showCreate = true, onInteraction = onInteraction)
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
                LocationsHeader(subtitle = viewState.worldName, showCreate = true, onInteraction = onInteraction)
                LocationsContent(state = viewState, onInteraction = onInteraction)
            }
        }
    }
}

@Composable
private fun LocationsHeader(
    subtitle: String,
    showCreate: Boolean,
    onInteraction: (LocationsInteraction) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Locations",
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
        if (showCreate) {
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

@Composable
private fun LocationsContent(
    state: LocationsViewState.Content,
    onInteraction: (LocationsInteraction) -> Unit,
) {
    if (state.blockDeleteReason != null) {
        Card(
            modifier = Modifier.fillMaxWidth().clickable {
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

    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { onInteraction(LocationsInteraction.SearchQueryChanged(it)) },
            label = { Text("Search locations") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.width(320.dp).fillMaxHeight(),
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
                            onInteraction = onInteraction,
                        )
                    }
                }
            }

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
                    onInteraction = onInteraction,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            } else {
                Text(
                    text = "Select a location to see its details.",
                    color = TextSecondary,
                    modifier = Modifier.weight(1f).padding(16.dp)
                )
            }
        }
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
