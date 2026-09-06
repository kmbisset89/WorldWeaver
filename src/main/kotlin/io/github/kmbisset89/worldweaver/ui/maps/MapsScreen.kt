package io.github.kmbisset89.worldweaver.ui.maps

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Map
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
import io.github.kmbisset89.worldweaver.domain.BattleMap
import io.github.kmbisset89.worldweaver.domain.BattleMapImageScaler
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
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val compact = maxWidth < AdaptiveListDetailBreakpoint
                    var listOpen by rememberAdaptiveListOpen(compact)
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        MapsHeader(
                            subtitle = "${viewState.campaignName} · ${viewState.worldName}",
                            showImport = true,
                            showStarterCatalog = viewState.starterCatalogAvailable,
                            compact = compact,
                            selectedName = viewState.selectedMap?.name,
                            onListToggle = { listOpen = !listOpen },
                            onInteraction = onInteraction,
                        )
                        MapsContent(
                            state = viewState,
                            mapState = mapState,
                            compact = compact,
                            listOpen = listOpen,
                            onListDismissed = { listOpen = false },
                            onInteraction = onInteraction,
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                        )
                    }
                }
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
    compact: Boolean = false,
    selectedName: String? = null,
    onListToggle: () -> Unit = {},
) {
    AdaptiveScreenHeaderComposeWidget(
        title = "Maps",
        subtitle = subtitle,
        compact = compact,
        switcherName = selectedName,
        listLabel = "maps list",
        onListToggle = onListToggle,
    ) {
        if (showImport) {
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
    compact: Boolean,
    listOpen: Boolean,
    onListDismissed: () -> Unit,
    onInteraction: (MapsInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    AdaptiveListDetailComposeWidget(
        compact = compact,
        listOpen = listOpen,
        hasSelection = state.selectedMap != null,
        listPane = { listModifier ->
            LazyColumn(
                modifier = listModifier,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.maps, key = { it.id }) { battleMap ->
                    MapListRow(
                        battleMap = battleMap,
                        selected = battleMap.id == state.selectedMap?.id,
                        onClick = {
                            onListDismissed()
                            onInteraction(MapsInteraction.MapSelected(battleMap.id))
                        },
                    )
                }
            }
        },
        detailPane = { detailModifier ->
        Column(
            modifier = detailModifier,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val selected = state.selectedMap
            if (selected != null && mapState != null) {
                val hudState = BattleMapBoardHudState.fromMaps(state)
                if (hudState != null) {
                    BattleMapBoardHudComposeWidget(
                        state = hudState,
                        mapState = mapState,
                        onInteraction = { interaction ->
                            dispatchMapsHud(
                                interaction = interaction,
                                mapId = selected.id,
                                mapName = selected.name,
                                onInteraction = onInteraction,
                            )
                        },
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
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                    )
                }
            } else {
                Text(
                    text = "Select a map to open the viewer.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }
        },
        onListDismissed = onListDismissed,
        dismissListLabel = "Dismiss maps list",
        listWidth = 260.dp,
        modifier = modifier,
    )
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

private fun dispatchMapsHud(
    interaction: BattleMapBoardHudInteraction,
    mapId: String,
    mapName: String,
    onInteraction: (MapsInteraction) -> Unit,
) {
    when (interaction) {
        BattleMapBoardHudInteraction.MoveToolSelected ->
            onInteraction(MapsInteraction.BoardToolCleared)
        BattleMapBoardHudInteraction.MeasureToolSelected ->
            onInteraction(MapsInteraction.MeasureToggled)
        BattleMapBoardHudInteraction.FogToolSelected ->
            onInteraction(MapsInteraction.FogToggled)
        BattleMapBoardHudInteraction.TerrainToolSelected ->
            onInteraction(MapsInteraction.TerrainPaintSelected(TerrainPaintKind.Blocked))
        BattleMapBoardHudInteraction.ItemToolSelected ->
            onInteraction(MapsInteraction.ItemDropToggled)
        BattleMapBoardHudInteraction.LayersToolSelected ->
            onInteraction(MapsInteraction.LayersToggled)
        is BattleMapBoardHudInteraction.MovementSpeedChanged ->
            onInteraction(MapsInteraction.MovementSpeedChanged(interaction.speed))
        BattleMapBoardHudInteraction.MovementCleared ->
            onInteraction(MapsInteraction.MovementCleared)
        BattleMapBoardHudInteraction.MeasureCleared ->
            onInteraction(MapsInteraction.MeasureCleared)
        BattleMapBoardHudInteraction.FogHideBrushSelected ->
            onInteraction(MapsInteraction.FogHideBrushSelected)
        BattleMapBoardHudInteraction.FogRevealBrushSelected ->
            onInteraction(MapsInteraction.FogRevealBrushSelected)
        BattleMapBoardHudInteraction.FogHideAllSelected ->
            onInteraction(MapsInteraction.FogHideAllSelected)
        BattleMapBoardHudInteraction.FogRevealAllSelected ->
            onInteraction(MapsInteraction.FogRevealAllSelected)
        is BattleMapBoardHudInteraction.TerrainPaintSelected ->
            onInteraction(MapsInteraction.TerrainPaintSelected(interaction.kind))
        is BattleMapBoardHudInteraction.ItemNameChanged ->
            onInteraction(MapsInteraction.ItemNameChanged(interaction.name))
        BattleMapBoardHudInteraction.ItemRemoved ->
            onInteraction(MapsInteraction.ItemRemoved)
        is BattleMapBoardHudInteraction.SituationToggled ->
            onInteraction(MapsInteraction.SituationToggled(interaction.situationId))
        is BattleMapBoardHudInteraction.SituationDeleteSelected ->
            onInteraction(MapsInteraction.SituationDeleteSelected(interaction.situationId))
        BattleMapBoardHudInteraction.AddLayerSelected -> {
            choosePngPath("Add situation layer")?.let { path ->
                onInteraction(MapsInteraction.SituationImageChosen(path))
            }
        }
        BattleMapBoardHudInteraction.PlayerViewSelected ->
            onInteraction(MapsInteraction.PlayerViewSelected)
        BattleMapBoardHudInteraction.ExportSelected -> {
            chooseUniversalVttPath(mapName)?.let { path ->
                onInteraction(MapsInteraction.UniversalVttExportPathChosen(mapId, path))
            }
        }
        BattleMapBoardHudInteraction.DeleteSelected ->
            onInteraction(MapsInteraction.DeleteMapSelected(mapId))
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

private fun chooseUniversalVttPath(mapName: String): String? {
    val dialog = FileDialog(null as Frame?, "Export Universal VTT", FileDialog.SAVE)
    dialog.filenameFilter = FilenameFilter { _, name ->
        name.lowercase().endsWith(UNIVERSAL_VTT_EXTENSION)
    }
    dialog.file = sanitizedUniversalVttFileName(mapName)
    dialog.isVisible = true
    val fileName = dialog.file ?: return null
    val directory = dialog.directory ?: return null
    val file = File(directory, fileName)
    return if (!file.name.endsWith(UNIVERSAL_VTT_EXTENSION, ignoreCase = true)) {
        File(directory, file.name + UNIVERSAL_VTT_EXTENSION).absolutePath
    } else {
        file.absolutePath
    }
}

private fun sanitizedUniversalVttFileName(mapName: String): String {
    val cleaned = mapName.replace(Regex("[^A-Za-z0-9._-]+"), "_").trim('_')
    val base = cleaned.ifBlank { "battle-map" }
    return base + UNIVERSAL_VTT_EXTENSION
}

private fun formatUnits(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        value.toString()
    }
}

private const val RENDER_TILE_SIZE_PX = 256
private const val UNIVERSAL_VTT_EXTENSION = ".uvtt"
