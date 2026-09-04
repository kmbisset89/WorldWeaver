package io.github.kmbisset89.worldweaver.ui.worldmap

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PublicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.ui.components.ConfirmDestructiveDialog
import io.github.kmbisset89.worldweaver.ui.components.FeatureEmptyState
import io.github.kmbisset89.worldweaver.ui.components.FeatureErrorState
import io.github.kmbisset89.worldweaver.ui.maps.BattleMapViewerComposeWidget
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary
import ovh.plrapps.mapcompose.ui.state.MapState
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FilenameFilter

@Composable
internal fun WorldMapScreen(
    viewState: WorldMapViewState,
    mapState: MapState?,
    onInteraction: (WorldMapInteraction) -> Unit,
) {
    LaunchedEffect(Unit) {
        onInteraction(WorldMapInteraction.ScreenStarted)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (viewState) {
            WorldMapViewState.Loading -> {
                WorldMapHeader(subtitle = "World cartography", onInteraction = onInteraction)
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is WorldMapViewState.Error -> {
                WorldMapHeader(subtitle = "World cartography", onInteraction = onInteraction)
                FeatureErrorState(
                    message = viewState.message,
                    canRetry = viewState.canRetry,
                    onRetry = { onInteraction(WorldMapInteraction.RetrySelected) },
                )
            }
            WorldMapViewState.NoActiveWorld -> {
                WorldMapHeader(subtitle = "Select a world first", onInteraction = onInteraction)
                FeatureEmptyState(
                    icon = Icons.Default.PublicOff,
                    title = "No active world",
                    message = "Create or select a world first so cartography has a setting.",
                    actionLabel = "Go to Worlds",
                    onAction = { onInteraction(WorldMapInteraction.CreateWorldSelected) },
                )
            }
            is WorldMapViewState.Empty -> {
                WorldMapHeader(
                    subtitle = viewState.locationName ?: viewState.worldName,
                    onInteraction = onInteraction,
                )
                if (viewState.importError != null) {
                    Text(viewState.importError, color = TextSecondary, fontSize = 13.sp)
                }
                FeatureEmptyState(
                    icon = Icons.Default.Map,
                    title = if (viewState.locationId == null) "No world map yet" else "No map for this location",
                    message = "Import a PNG to start nested cartography at this zoom.",
                    actionLabel = "Import PNG",
                    onAction = {
                        choosePngPath("Choose world map")?.let { path ->
                            onInteraction(WorldMapInteraction.ImageChosen(path))
                        }
                    },
                )
            }
            is WorldMapViewState.Content -> {
                WorldMapHeader(subtitle = viewState.title, onInteraction = onInteraction)
                WorldMapContent(
                    state = viewState,
                    mapState = mapState,
                    onInteraction = onInteraction,
                )
            }
        }
    }
}

@Composable
private fun WorldMapHeader(
    subtitle: String,
    onInteraction: (WorldMapInteraction) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "World map",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(text = subtitle, fontSize = 14.sp, color = TextSecondary)
        }
        TextButton(onClick = { onInteraction(WorldMapInteraction.BackToLocationsSelected) }) {
            Icon(Icons.Default.ArrowBack, contentDescription = null)
            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            Text("Locations")
        }
    }
}

@Composable
private fun WorldMapContent(
    state: WorldMapViewState.Content,
    mapState: MapState?,
    onInteraction: (WorldMapInteraction) -> Unit,
) {
    if (state.importError != null) {
        Text(state.importError, color = TextSecondary, fontSize = 13.sp)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        state.breadcrumbs.forEachIndexed { index, crumb ->
            if (index > 0) {
                Text(">", color = TextSecondary, fontSize = 13.sp)
            }
            val current = crumb.locationId == state.locationId
            Text(
                text = crumb.name,
                fontSize = 13.sp,
                color = if (current) TextPrimary else NavyBlue,
                modifier = Modifier.clickable {
                    onInteraction(WorldMapInteraction.BreadcrumbSelected(crumb.locationId))
                }
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                choosePngPath("Replace world map")?.let { path ->
                    onInteraction(WorldMapInteraction.ImageChosen(path))
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
        ) {
            Text("Replace PNG")
        }
        TextButton(onClick = { onInteraction(WorldMapInteraction.DeleteMapSelected) }) {
            Text("Delete map")
        }
    }
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            if (mapState != null) {
                BattleMapViewerComposeWidget(
                    mapState = mapState,
                    onMapTapped = { x, y ->
                        onInteraction(WorldMapInteraction.MapTapped(x, y))
                    },
                    onMarkerClicked = { id ->
                        WorldMapPinOverlay.locationIdFrom(id)?.let { locationId ->
                            onInteraction(WorldMapInteraction.PinSelected(locationId))
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        WorldMapSidePane(
            state = state,
            onInteraction = onInteraction,
            modifier = Modifier.width(280.dp).fillMaxHeight(),
        )
    }
    state.pendingDelete?.let { pending ->
        ConfirmDestructiveDialog(
            title = "Delete map?",
            message = "Delete the map for “${pending.title}”? Pins on this map stay on the locations.",
            confirmLabel = "Delete",
            onConfirm = { onInteraction(WorldMapInteraction.DeleteConfirmed) },
            onDismiss = { onInteraction(WorldMapInteraction.DeleteCancelled) },
        )
    }
}

@Composable
private fun WorldMapSidePane(
    state: WorldMapViewState.Content,
    onInteraction: (WorldMapInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (state.placingLocationId != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tap the map to place this pin.", fontSize = 13.sp, color = TextPrimary)
                    TextButton(onClick = { onInteraction(WorldMapInteraction.PlacePinCancelled) }) {
                        Text("Cancel placement")
                    }
                }
            }
        }
        if (state.selectedLocationName != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(state.selectedLocationName, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    if (state.selectedHasMap) {
                        Text("Has a nested map.", fontSize = 12.sp, color = TextSecondary)
                    } else {
                        Text("No nested map yet.", fontSize = 12.sp, color = TextSecondary)
                    }
                    if (state.selectedLocationId != null) {
                        TextButton(
                            onClick = {
                                onInteraction(WorldMapInteraction.ClearPinSelected(state.selectedLocationId))
                            }
                        ) {
                            Text("Clear pin")
                        }
                    }
                }
            }
        }
        Text("Unplaced", fontWeight = FontWeight.SemiBold, color = TextPrimary)
        if (state.unplacedChildren.isEmpty()) {
            Text("Every child is on this map, or there are no children.", fontSize = 12.sp, color = TextSecondary)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.unplacedChildren, key = { it.locationId }) { child ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onInteraction(WorldMapInteraction.PlacePinSelected(child.locationId))
                            },
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = child.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (state.placingLocationId == child.locationId) {
                                        Modifier.background(NavyBlue.copy(alpha = 0.08f))
                                    } else {
                                        Modifier
                                    }
                                )
                                .padding(12.dp),
                            color = TextPrimary,
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }
    }
}

private fun choosePngPath(title: String): String? {
    val dialog = FileDialog(null as Frame?, title, FileDialog.LOAD)
    dialog.filenameFilter = FilenameFilter { _, name ->
        name.lowercase().endsWith(".png")
    }
    dialog.isVisible = true
    val fileName = dialog.file ?: return null
    val directory = dialog.directory ?: return null
    return File(directory, fileName).absolutePath
}
