package net.tactware.worldweaver.ui.maps

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PublicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.tactware.worldweaver.domain.BattleMap
import net.tactware.worldweaver.domain.BattleMapImageScaler
import net.tactware.worldweaver.ui.components.ConfirmDestructiveDialog
import net.tactware.worldweaver.ui.components.FeatureEmptyState
import net.tactware.worldweaver.ui.components.FeatureErrorState
import net.tactware.worldweaver.ui.theme.NavyBlue
import net.tactware.worldweaver.ui.theme.SurfaceCard
import net.tactware.worldweaver.ui.theme.TextPrimary
import net.tactware.worldweaver.ui.theme.TextSecondary
import ovh.plrapps.mapcompose.ui.state.MapState
import java.awt.FileDialog
import java.awt.Frame
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FilenameFilter
import javax.imageio.ImageIO

@Composable
internal fun MapsScreen(
    viewState: MapsViewState,
    mapState: MapState?,
    onInteraction: (MapsInteraction) -> Unit,
) {
    LaunchedEffect(Unit) {
        onInteraction(MapsInteraction.ScreenStarted)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        when (viewState) {
            MapsViewState.Loading -> {
                MapsHeader(subtitle = "Campaign battle maps", showImport = false, onInteraction = onInteraction)
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is MapsViewState.Error -> {
                MapsHeader(subtitle = "Campaign battle maps", showImport = false, onInteraction = onInteraction)
                FeatureErrorState(
                    message = viewState.message,
                    canRetry = viewState.canRetry,
                    onRetry = { onInteraction(MapsInteraction.RetrySelected) },
                )
            }
            MapsViewState.NoActiveWorld -> {
                MapsHeader(subtitle = "Select a world first", showImport = false, onInteraction = onInteraction)
                FeatureEmptyState(
                    icon = Icons.Default.PublicOff,
                    title = "No active world",
                    message = "Create or select a world first so maps have a setting.",
                    actionLabel = "Go to Worlds",
                    onAction = { onInteraction(MapsInteraction.CreateWorldSelected) },
                )
            }
            MapsViewState.NoActiveCampaign -> {
                MapsHeader(subtitle = "Select a campaign first", showImport = false, onInteraction = onInteraction)
                FeatureEmptyState(
                    icon = Icons.Default.Flag,
                    title = "No active campaign",
                    message = "Create or select a campaign to import battle maps.",
                    actionLabel = "Go to Campaigns",
                    onAction = { onInteraction(MapsInteraction.CreateCampaignSelected) },
                )
            }
            is MapsViewState.Empty -> {
                MapsHeader(
                    subtitle = "${viewState.campaignName} · ${viewState.worldName}",
                    showImport = true,
                    showStarterCatalog = viewState.starterCatalogAvailable,
                    onInteraction = onInteraction,
                )
                FeatureEmptyState(
                    icon = Icons.Default.Map,
                    title = "No battle maps yet",
                    message = if (viewState.starterCatalogAvailable) {
                        "Add a starter encounter map, or open the maker to import your own PNG."
                    } else {
                        "Open the maker to preview a PNG, set a grid, and save tiles."
                    },
                    actionLabel = if (viewState.starterCatalogAvailable) "Starter maps" else "New map",
                    onAction = {
                        onInteraction(
                            if (viewState.starterCatalogAvailable) {
                                MapsInteraction.StarterCatalogSelected
                            } else {
                                MapsInteraction.ImportSelected
                            }
                        )
                    },
                )
            }
            is MapsViewState.Content -> {
                MapsHeader(
                    subtitle = "${viewState.campaignName} · ${viewState.worldName}",
                    showImport = true,
                    showStarterCatalog = viewState.starterCatalogAvailable,
                    onInteraction = onInteraction,
                )
                MapsContent(state = viewState, mapState = mapState, onInteraction = onInteraction)
            }
            is MapsViewState.Maker -> {
                MapsHeader(
                    subtitle = "Maker · ${viewState.campaignName}",
                    showImport = false,
                    onInteraction = onInteraction,
                )
                BattleMapMakerPane(editor = viewState.editor, onInteraction = onInteraction)
            }
            is MapsViewState.StarterCatalog -> {
                MapsHeader(
                    subtitle = "Starter maps · ${viewState.campaignName}",
                    showImport = false,
                    onInteraction = onInteraction,
                )
                StarterCatalogPane(state = viewState, onInteraction = onInteraction)
            }
        }
    }
}

@Composable
private fun MapsHeader(
    subtitle: String,
    showImport: Boolean,
    showStarterCatalog: Boolean = false,
    onInteraction: (MapsInteraction) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Maps",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(text = subtitle, fontSize = 13.sp, color = TextSecondary)
        }
        if (showImport) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showStarterCatalog) {
                    TextButton(onClick = { onInteraction(MapsInteraction.StarterCatalogSelected) }) {
                        Text("Starter maps")
                    }
                }
                Button(
                    onClick = { onInteraction(MapsInteraction.ImportSelected) },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("New map")
                }
            }
        }
    }
}

@Composable
private fun StarterCatalogPane(
    state: MapsViewState.StarterCatalog,
    onInteraction: (MapsInteraction) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Add a starter encounter map to this campaign. Small maps are 20×20; medium maps are 30×30. Dynamic maps include extra stages you can toggle as situation layers.",
            fontSize = 13.sp,
            color = TextSecondary,
        )
        if (state.error != null) {
            Text(text = state.error, fontSize = 13.sp, color = TextSecondary)
        }
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.entries, key = { it.id }) { entry ->
                val importing = state.importingId == entry.id
                val enabled = state.importingId == null && !entry.alreadyAdded
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = enabled) {
                            onInteraction(MapsInteraction.BundledMapSelected(entry.id))
                        },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = entry.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = when {
                                    importing -> "Adding…"
                                    entry.alreadyAdded -> "Already in this campaign"
                                    else -> entry.detail
                                },
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        if (importing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
        TextButton(
            onClick = { onInteraction(MapsInteraction.StarterCatalogDismissed) },
            enabled = state.importingId == null,
        ) {
            Text("Back")
        }
    }
}

@Composable
private fun MapsContent(
    state: MapsViewState.Content,
    mapState: MapState?,
    onInteraction: (MapsInteraction) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .width(260.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.maps, key = { it.id }) { battleMap ->
                MapListRow(
                    battleMap = battleMap,
                    selected = battleMap.id == state.selectedMap?.id,
                    onClick = { onInteraction(MapsInteraction.MapSelected(battleMap.id)) },
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val selected = state.selectedMap
            if (selected != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = selected.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${selected.originalWidth}×${selected.originalHeight} · " +
                                "${selected.columns}×${selected.rows} · " +
                                "${formatUnits(selected.unitsPerTile)} ${selected.unitName}",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(
                            onClick = { onInteraction(MapsInteraction.PlayerViewSelected) }
                        ) {
                            Text(if (state.playerViewOpen) "Player view open" else "Player view")
                        }
                        TextButton(
                            onClick = { onInteraction(MapsInteraction.DeleteMapSelected(selected.id)) }
                        ) {
                            Text("Delete")
                        }
                    }
                }
                MovementRangeRow(state = state, onInteraction = onInteraction)
                FogPaintRow(state = state, onInteraction = onInteraction)
                SituationLayerRow(state = state, onInteraction = onInteraction)
            }
            if (mapState != null) {
                BattleMapViewerComposeWidget(
                    mapState = mapState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    onMapTapped = { x, y ->
                        onInteraction(MapsInteraction.MapCellSelected(x, y))
                    },
                    onMarkerClicked = { markerId ->
                        BattleMapTokenOverlay.participantIdFrom(markerId)?.let { participantId ->
                            onInteraction(MapsInteraction.TokenSelected(participantId))
                        }
                        BattleMapItemOverlay.itemIdFrom(markerId)?.let { itemId ->
                            onInteraction(MapsInteraction.ItemSelected(itemId))
                        }
                    },
                )
            } else {
                Text(
                    text = "Select a map to open the viewer.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }
    }
    state.pendingDelete?.let { pending ->
        ConfirmDestructiveDialog(
            title = "Delete battle map?",
            message = "Delete “${pending.battleMapName}”? Attached encounters will lose this map.",
            confirmLabel = "Delete",
            onConfirm = { onInteraction(MapsInteraction.DeleteConfirmed) },
            onDismiss = { onInteraction(MapsInteraction.DeleteCancelled) },
        )
    }
}

@Composable
private fun MovementRangeRow(
    state: MapsViewState.Content,
    onInteraction: (MapsInteraction) -> Unit,
) {
    val selected = state.selectedMap ?: return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = state.movementSpeedText,
            onValueChange = { onInteraction(MapsInteraction.MovementSpeedChanged(it)) },
            label = { Text("Speed") },
            singleLine = true,
            modifier = Modifier.width(100.dp),
        )
        val tokenLabel = when {
            state.selectedTokenName != null && state.unplacedTokenCount > 0 -> {
                "Place ${state.selectedTokenName} · ${state.unplacedTokenCount} unplaced"
            }
            state.selectedTokenName != null -> "Move ${state.selectedTokenName}"
            state.tokens.isNotEmpty() -> "${state.tokens.size} on the board"
            else -> null
        }
        if (tokenLabel != null) {
            Text(text = tokenLabel, fontSize = 13.sp, color = TextSecondary)
        }
        val rangeLabel = when {
            state.measureEnabled && state.measureDistance != null -> {
                val distance = state.measureDistance
                "${distance.squares} squares · ${distance.unitsLabel()} ${selected.unitName}"
            }
            state.measureEnabled && state.measureOrigin != null -> "Click a second cell to measure"
            state.measureEnabled -> "Click a cell to start measuring"
            state.movementOrigin == null -> "Click a cell to show range"
            else -> {
                val squares = if (selected.unitsPerTile > 0.0) {
                    (state.movementSpeedText.toIntOrNull() ?: 0) / selected.unitsPerTile
                } else {
                    0.0
                }
                val squareCount = kotlin.math.floor(squares).toInt()
                "${state.reachableCells.size} cells · $squareCount squares · ${state.movementSpeedText} ${selected.unitName}"
            }
        }
        Text(text = rangeLabel, fontSize = 13.sp, color = TextSecondary)
        FilterChip(
            selected = state.measureEnabled,
            onClick = { onInteraction(MapsInteraction.MeasureToggled) },
            label = { Text("Measure") },
        )
        if (state.measureOrigin != null) {
            TextButton(onClick = { onInteraction(MapsInteraction.MeasureCleared) }) {
                Text("Clear measure")
            }
        }
        if (state.movementOrigin != null) {
            TextButton(onClick = { onInteraction(MapsInteraction.MovementCleared) }) {
                Text("Clear range")
            }
        }
    }
}

@Composable
private fun FogPaintRow(
    state: MapsViewState.Content,
    onInteraction: (MapsInteraction) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterChip(
            selected = state.fogPaintEnabled,
            onClick = { onInteraction(MapsInteraction.FogToggled) },
            label = { Text("Fog") },
        )
        if (state.fogPaintEnabled) {
            FilterChip(
                selected = !state.fogRevealBrush,
                onClick = { onInteraction(MapsInteraction.FogHideBrushSelected) },
                label = { Text("Hide") },
            )
            FilterChip(
                selected = state.fogRevealBrush,
                onClick = { onInteraction(MapsInteraction.FogRevealBrushSelected) },
                label = { Text("Reveal") },
            )
            TextButton(onClick = { onInteraction(MapsInteraction.FogHideAllSelected) }) {
                Text("Hide all")
            }
            TextButton(onClick = { onInteraction(MapsInteraction.FogRevealAllSelected) }) {
                Text("Reveal all")
            }
            Text(
                text = if (state.fogRevealBrush) {
                    "Click cells to reveal them on Player View"
                } else {
                    "Click cells to hide them from Player View"
                },
                fontSize = 13.sp,
                color = TextSecondary,
            )
        }
        FilterChip(
            selected = state.terrainPaint == TerrainPaintKind.Blocked,
            onClick = { onInteraction(MapsInteraction.TerrainPaintSelected(TerrainPaintKind.Blocked)) },
            label = { Text("Blocked") },
        )
        FilterChip(
            selected = state.terrainPaint == TerrainPaintKind.Difficult,
            onClick = { onInteraction(MapsInteraction.TerrainPaintSelected(TerrainPaintKind.Difficult)) },
            label = { Text("Difficult") },
        )
        FilterChip(
            selected = state.terrainPaint == TerrainPaintKind.Clear,
            onClick = { onInteraction(MapsInteraction.TerrainPaintSelected(TerrainPaintKind.Clear)) },
            label = { Text("Clear terrain") },
        )
        if (state.terrainPaint != null) {
            Text(
                text = "Click cells to paint ${state.terrainPaint.name.lowercase()} terrain",
                fontSize = 13.sp,
                color = TextSecondary,
            )
        }
        FilterChip(
            selected = state.itemDropEnabled,
            onClick = { onInteraction(MapsInteraction.ItemDropToggled) },
            label = { Text("Item") },
        )
        if (state.itemDropEnabled) {
            OutlinedTextField(
                value = state.itemNameText,
                onValueChange = { onInteraction(MapsInteraction.ItemNameChanged(it)) },
                label = { Text("Item name") },
                singleLine = true,
                modifier = Modifier.width(160.dp),
            )
            Text(
                text = if (state.itemNameText.isBlank()) {
                    "Name the item, then click a cell"
                } else {
                    "Click a cell to drop ${state.itemNameText.trim()}"
                },
                fontSize = 13.sp,
                color = TextSecondary,
            )
        }
        if (state.selectedItemName != null) {
            Text(text = state.selectedItemName, fontSize = 13.sp, color = TextSecondary)
            TextButton(onClick = { onInteraction(MapsInteraction.ItemRemoved) }) {
                Text("Remove")
            }
        }
    }
}

@Composable
private fun SituationLayerRow(
    state: MapsViewState.Content,
    onInteraction: (MapsInteraction) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Situations",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            state.situations.forEach { situation ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = situation.visible,
                        onClick = { onInteraction(MapsInteraction.SituationToggled(situation.id)) },
                        label = { Text(if (situation.visible) situation.name else "${situation.name} (off)") },
                    )
                    IconButton(
                        onClick = { onInteraction(MapsInteraction.SituationDeleteSelected(situation.id)) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove ${situation.name}",
                        )
                    }
                }
            }
            TextButton(
                onClick = {
                    choosePngPath("Add situation layer")?.let { path ->
                        onInteraction(MapsInteraction.SituationImageChosen(path))
                    }
                },
                enabled = !state.isSavingSituation,
            ) {
                Text(if (state.isSavingSituation) "Adding…" else "Add layer")
            }
        }
        if (state.situationError != null) {
            Text(text = state.situationError, fontSize = 13.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun MapListRow(
    battleMap: BattleMap,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) NavyBlue.copy(alpha = 0.12f) else SurfaceCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = battleMap.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "${battleMap.columns}×${battleMap.rows} · ${formatUnits(battleMap.unitsPerTile)} ${battleMap.unitName}",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun BattleMapMakerPane(
    editor: MapsViewState.MakerEditorState,
    onInteraction: (MapsInteraction) -> Unit,
) {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = editor.name,
            onValueChange = { onInteraction(MapsInteraction.MakerNameChanged(it)) },
            label = { Text("Name") },
            isError = editor.nameError != null,
            supportingText = editor.nameError?.let { error -> { Text(error) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = editor.columnsText,
                onValueChange = { onInteraction(MapsInteraction.MakerColumnsChanged(it)) },
                label = { Text("Columns") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = editor.rowsText,
                onValueChange = { onInteraction(MapsInteraction.MakerRowsChanged(it)) },
                label = { Text("Rows") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = editor.unitsPerTileText,
                onValueChange = { onInteraction(MapsInteraction.MakerUnitsPerTileChanged(it)) },
                label = { Text("Units/tile") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = editor.unitNameText,
                onValueChange = { onInteraction(MapsInteraction.MakerUnitNameChanged(it)) },
                label = { Text("Unit") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
        MakerPromptHelper(editor = editor, onInteraction = onInteraction)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = editor.scalePercentText,
                onValueChange = { onInteraction(MapsInteraction.MakerScaleChanged(it)) },
                label = { Text("Import scale (%)") },
                singleLine = true,
                modifier = Modifier.width(160.dp)
            )
            FilterChip(
                selected = editor.showGrid,
                onClick = { onInteraction(MapsInteraction.MakerGridToggled) },
                label = { Text(if (editor.showGrid) "Grid: On" else "Grid: Off") },
            )
            FilterChip(
                selected = editor.showRenderTiles,
                onClick = { onInteraction(MapsInteraction.MakerRenderTilesToggled) },
                label = { Text(if (editor.showRenderTiles) "Render tiles: On" else "Render tiles: Off") },
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                onClick = {
                    choosePngPath("Choose a PNG")?.let { path ->
                        onInteraction(MapsInteraction.MakerImageChosen(path))
                    }
                }
            ) {
                Text("Choose PNG")
            }
        }
        if (editor.imageError != null) {
            Text(text = editor.imageError, fontSize = 13.sp, color = TextSecondary)
        }
        if (editor.gridError != null) {
            Text(text = editor.gridError, fontSize = 13.sp, color = TextSecondary)
        }
        Text(
            text = if (editor.imagePath == null) {
                "Gameplay grid (rows/cols) is separate from 256px render tiles."
            } else {
                val scale = editor.scalePercentText.toIntOrNull()?.coerceIn(10, 400) ?: 100
                val saveWidth = (editor.imageWidth * scale / 100).coerceAtLeast(1)
                val saveHeight = (editor.imageHeight * scale / 100).coerceAtLeast(1)
                "${File(editor.imagePath).name} · ${editor.imageWidth}×${editor.imageHeight}px · save ${saveWidth}×${saveHeight}px"
            },
            fontSize = 13.sp,
            color = TextSecondary
        )
        MakerPreview(editor = editor)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { onInteraction(MapsInteraction.MakerSaved) },
                enabled = !editor.isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                Text(if (editor.isSaving) "Saving…" else "Save")
            }
            TextButton(
                onClick = { onInteraction(MapsInteraction.MakerDismissed) },
                enabled = !editor.isSaving,
            ) {
                Text("Cancel")
            }
        }
    }
}

@Composable
private fun MakerPromptHelper(
    editor: MapsViewState.MakerEditorState,
    onInteraction: (MapsInteraction) -> Unit,
) {
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(editor.imagePrompt) {
        copied = false
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Describe the scene, copy the prompt into an image generator, then choose the PNG.",
            fontSize = 13.sp,
            color = TextSecondary,
        )
        OutlinedTextField(
            value = editor.sceneryText,
            onValueChange = { onInteraction(MapsInteraction.MakerSceneryChanged(it)) },
            label = { Text("Scenery") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = editor.imagePrompt,
            onValueChange = {},
            readOnly = true,
            label = { Text("AI image prompt") },
            minLines = 4,
            modifier = Modifier.fillMaxWidth()
        )
        TextButton(
            onClick = {
                copyPromptToClipboard(editor.imagePrompt)
                copied = true
            }
        ) {
            Text(if (copied) "Copied" else "Copy prompt")
        }
    }
}

@Composable
private fun MakerPreview(editor: MapsViewState.MakerEditorState) {
    val preview = remember(editor.imagePath, editor.scalePercentText) {
        loadPreviewBitmap(editor.imagePath, editor.scalePercentText)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (preview == null) {
            Text(
                text = "Choose a PNG to preview it with a grid overlay.",
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(16.dp)
            )
            return@Card
        }
        val columns = editor.columnsText.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val rows = editor.rowsText.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val scale = editor.scalePercentText.toIntOrNull()?.coerceIn(10, 400) ?: 100
        val imageWidth = (editor.imageWidth * scale / 100).coerceAtLeast(1)
        val imageHeight = (editor.imageHeight * scale / 100).coerceAtLeast(1)
        var renderedSize by remember { mutableStateOf(IntSize.Zero) }
        val hScroll = rememberScrollState()
        val vScroll = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 280.dp, max = 520.dp)
                .horizontalScroll(hScroll)
                .verticalScroll(vScroll)
                .padding(12.dp)
        ) {
            Box {
                Image(
                    bitmap = preview,
                    contentDescription = editor.name.ifBlank { "Battle map preview" },
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .heightIn(max = 500.dp)
                        .onSizeChanged { renderedSize = it }
                )
                if (renderedSize.width > 0 && renderedSize.height > 0) {
                    val gridColor = NavyBlue.copy(alpha = 0.35f)
                    val tileColor = NavyBlue.copy(alpha = 0.55f)
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val scaleX = size.width / imageWidth.toFloat()
                        val scaleY = size.height / imageHeight.toFloat()
                        if (editor.showGrid) {
                            val cellWidth = imageWidth / columns
                            val remWidth = imageWidth % columns
                            val cellHeight = imageHeight / rows
                            val remHeight = imageHeight % rows
                            var pixelX = 0
                            for (column in 0..columns) {
                                val x = pixelX * scaleX
                                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), 1f)
                                if (column == columns) {
                                    break
                                }
                                pixelX += cellWidth + if (column < remWidth) 1 else 0
                            }
                            var pixelY = 0
                            for (row in 0..rows) {
                                val y = pixelY * scaleY
                                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 1f)
                                if (row == rows) {
                                    break
                                }
                                pixelY += cellHeight + if (row < remHeight) 1 else 0
                            }
                        }
                        if (editor.showRenderTiles) {
                            val tilesX = (imageWidth + RENDER_TILE_SIZE_PX - 1) / RENDER_TILE_SIZE_PX
                            val tilesY = (imageHeight + RENDER_TILE_SIZE_PX - 1) / RENDER_TILE_SIZE_PX
                            drawRect(
                                color = tileColor.copy(alpha = 0.10f),
                                topLeft = Offset.Zero,
                                size = androidx.compose.ui.geometry.Size(
                                    RENDER_TILE_SIZE_PX.coerceAtMost(imageWidth) * scaleX,
                                    RENDER_TILE_SIZE_PX.coerceAtMost(imageHeight) * scaleY,
                                ),
                            )
                            for (x in 0..tilesX) {
                                val pixelX = (x * RENDER_TILE_SIZE_PX).coerceAtMost(imageWidth)
                                val dx = pixelX * scaleX
                                drawLine(tileColor, Offset(dx, 0f), Offset(dx, size.height), 1.5f)
                            }
                            for (y in 0..tilesY) {
                                val pixelY = (y * RENDER_TILE_SIZE_PX).coerceAtMost(imageHeight)
                                val dy = pixelY * scaleY
                                drawLine(tileColor, Offset(0f, dy), Offset(size.width, dy), 1.5f)
                            }
                        }
                    }
                }
            }
        }
        val tilesX = (imageWidth + RENDER_TILE_SIZE_PX - 1) / RENDER_TILE_SIZE_PX
        val tilesY = (imageHeight + RENDER_TILE_SIZE_PX - 1) / RENDER_TILE_SIZE_PX
        Text(
            text = "Preview ${imageWidth}×${imageHeight}px · render tiles ${tilesX}×${tilesY} @${RENDER_TILE_SIZE_PX}px",
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
        )
    }
}

private fun loadPreviewBitmap(
    path: String?,
    scalePercentText: String,
): androidx.compose.ui.graphics.ImageBitmap? {
    if (path.isNullOrBlank()) {
        return null
    }
    val file = File(path)
    if (!file.isFile) {
        return null
    }
    return try {
        val percent = scalePercentText.toIntOrNull()?.coerceIn(10, 400) ?: 100
        val scaled = BattleMapImageScaler().scale(file.readBytes(), percent) ?: return null
        ImageIO.read(ByteArrayInputStream(scaled))?.toComposeImageBitmap()
    } catch (_: Exception) {
        null
    }
}

private fun copyPromptToClipboard(text: String) {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    clipboard.setContents(StringSelection(text), null)
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

private fun formatUnits(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        value.toString()
    }
}

private const val RENDER_TILE_SIZE_PX = 256
