package io.github.kmbisset89.worldweaver.ui.maps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.ui.components.ActionIconButtonComposeWidget
import io.github.kmbisset89.worldweaver.ui.theme.ErrorRed
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary
import ovh.plrapps.mapcompose.ui.state.MapState

@Composable
internal fun BattleMapBoardHudComposeWidget(
    state: BattleMapBoardHudState,
    mapState: MapState,
    onInteraction: (BattleMapBoardHudInteraction) -> Unit,
    onMapTapped: (x: Double, y: Double) -> Unit,
    onMarkerClicked: (id: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        BattleMapViewerComposeWidget(
            mapState = mapState,
            modifier = Modifier.matchParentSize(),
            onMapTapped = onMapTapped,
            onMarkerClicked = onMarkerClicked,
        )
        HudTitleChip(
            state = state,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
        )
        HudActionChip(
            state = state,
            onInteraction = onInteraction,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
        )
        if (state.statusText.isNotBlank()) {
            HudStatusChip(
                text = state.statusText,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, end = 12.dp, bottom = 72.dp),
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HudToolPanel(
                state = state,
                onInteraction = onInteraction,
            )
            HudToolDock(
                state = state,
                onInteraction = onInteraction,
            )
        }
    }
}

@Composable
private fun HudTitleChip(
    state: BattleMapBoardHudState,
    modifier: Modifier = Modifier,
) {
    HudGlass(modifier = modifier.widthIn(max = 320.dp)) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                text = state.mapName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = state.gridSubtitle,
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun HudActionChip(
    state: BattleMapBoardHudState,
    onInteraction: (BattleMapBoardHudInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    HudGlass(modifier = modifier) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HudIconButton(
                icon = if (state.playerViewOpen) Icons.Default.CastConnected else Icons.Default.Cast,
                contentDescription = if (state.playerViewOpen) "Player view open" else "Player view",
                selected = state.playerViewOpen,
                onClick = { onInteraction(BattleMapBoardHudInteraction.PlayerViewSelected) },
            )
            if (state.showExport) {
                HudIconButton(
                    icon = Icons.Default.IosShare,
                    contentDescription = "Export VTT",
                    selected = false,
                    onClick = { onInteraction(BattleMapBoardHudInteraction.ExportSelected) },
                )
            }
            if (state.showDelete) {
                HudIconButton(
                    icon = Icons.Default.Delete,
                    contentDescription = "Delete map",
                    selected = false,
                    onClick = { onInteraction(BattleMapBoardHudInteraction.DeleteSelected) },
                )
            }
        }
    }
}

@Composable
private fun HudStatusChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    HudGlass(modifier = modifier.widthIn(max = 360.dp)) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = TextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun HudToolDock(
    state: BattleMapBoardHudState,
    onInteraction: (BattleMapBoardHudInteraction) -> Unit,
) {
    HudGlass {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HudIconButton(
                icon = Icons.Default.NearMe,
                contentDescription = "Move",
                selected = state.activeTool == BattleMapBoardHudState.Tool.Move,
                onClick = { onInteraction(BattleMapBoardHudInteraction.MoveToolSelected) },
            )
            HudIconButton(
                icon = Icons.Default.Straighten,
                contentDescription = "Measure",
                selected = state.activeTool == BattleMapBoardHudState.Tool.Measure,
                onClick = { onInteraction(BattleMapBoardHudInteraction.MeasureToolSelected) },
            )
            HudIconButton(
                icon = Icons.Default.Cloud,
                contentDescription = "Fog",
                selected = state.activeTool == BattleMapBoardHudState.Tool.Fog,
                onClick = { onInteraction(BattleMapBoardHudInteraction.FogToolSelected) },
            )
            HudIconButton(
                icon = Icons.Default.Terrain,
                contentDescription = "Terrain",
                selected = state.activeTool == BattleMapBoardHudState.Tool.Terrain,
                onClick = {
                    if (state.activeTool == BattleMapBoardHudState.Tool.Terrain) {
                        onInteraction(BattleMapBoardHudInteraction.MoveToolSelected)
                    } else {
                        onInteraction(BattleMapBoardHudInteraction.TerrainToolSelected)
                    }
                },
            )
            HudIconButton(
                icon = Icons.Default.Inventory2,
                contentDescription = "Item",
                selected = state.activeTool == BattleMapBoardHudState.Tool.Item,
                onClick = { onInteraction(BattleMapBoardHudInteraction.ItemToolSelected) },
            )
            if (state.showLayersTool) {
                HudIconButton(
                    icon = Icons.Default.Layers,
                    contentDescription = "Layers",
                    selected = state.activeTool == BattleMapBoardHudState.Tool.Layers,
                    onClick = { onInteraction(BattleMapBoardHudInteraction.LayersToolSelected) },
                )
            }
        }
    }
}

@Composable
private fun HudToolPanel(
    state: BattleMapBoardHudState,
    onInteraction: (BattleMapBoardHudInteraction) -> Unit,
) {
    when (state.activeTool) {
        BattleMapBoardHudState.Tool.Move -> MoveToolPanel(state = state, onInteraction = onInteraction)
        BattleMapBoardHudState.Tool.Measure -> MeasureToolPanel(state = state, onInteraction = onInteraction)
        BattleMapBoardHudState.Tool.Fog -> FogToolPanel(state = state, onInteraction = onInteraction)
        BattleMapBoardHudState.Tool.Terrain -> TerrainToolPanel(state = state, onInteraction = onInteraction)
        BattleMapBoardHudState.Tool.Item -> ItemToolPanel(state = state, onInteraction = onInteraction)
        BattleMapBoardHudState.Tool.Layers -> LayersToolPanel(state = state, onInteraction = onInteraction)
    }
}

@Composable
private fun MoveToolPanel(
    state: BattleMapBoardHudState,
    onInteraction: (BattleMapBoardHudInteraction) -> Unit,
) {
    HudGlass {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = state.movementSpeedText,
                onValueChange = { onInteraction(BattleMapBoardHudInteraction.MovementSpeedChanged(it)) },
                label = { Text("Speed") },
                singleLine = true,
                modifier = Modifier.width(100.dp),
            )
            if (state.canClearRange) {
                TextButton(onClick = { onInteraction(BattleMapBoardHudInteraction.MovementCleared) }) {
                    Text("Clear range")
                }
            }
        }
    }
}

@Composable
private fun MeasureToolPanel(
    state: BattleMapBoardHudState,
    onInteraction: (BattleMapBoardHudInteraction) -> Unit,
) {
    if (!state.canClearMeasure) {
        return
    }
    HudGlass {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = { onInteraction(BattleMapBoardHudInteraction.MeasureCleared) }) {
                Text("Clear measure")
            }
        }
    }
}

@Composable
private fun FogToolPanel(
    state: BattleMapBoardHudState,
    onInteraction: (BattleMapBoardHudInteraction) -> Unit,
) {
    HudGlass {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterChip(
                selected = !state.fogRevealBrush,
                onClick = { onInteraction(BattleMapBoardHudInteraction.FogHideBrushSelected) },
                label = { Text("Hide") },
            )
            FilterChip(
                selected = state.fogRevealBrush,
                onClick = { onInteraction(BattleMapBoardHudInteraction.FogRevealBrushSelected) },
                label = { Text("Reveal") },
            )
            TextButton(onClick = { onInteraction(BattleMapBoardHudInteraction.FogHideAllSelected) }) {
                Text("Hide all")
            }
            TextButton(onClick = { onInteraction(BattleMapBoardHudInteraction.FogRevealAllSelected) }) {
                Text("Reveal all")
            }
        }
    }
}

@Composable
private fun TerrainToolPanel(
    state: BattleMapBoardHudState,
    onInteraction: (BattleMapBoardHudInteraction) -> Unit,
) {
    HudGlass {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterChip(
                selected = state.terrainPaint == TerrainPaintKind.Blocked,
                onClick = {
                    onInteraction(BattleMapBoardHudInteraction.TerrainPaintSelected(TerrainPaintKind.Blocked))
                },
                label = { Text("Blocked") },
            )
            FilterChip(
                selected = state.terrainPaint == TerrainPaintKind.Difficult,
                onClick = {
                    onInteraction(BattleMapBoardHudInteraction.TerrainPaintSelected(TerrainPaintKind.Difficult))
                },
                label = { Text("Difficult") },
            )
            FilterChip(
                selected = state.terrainPaint == TerrainPaintKind.Clear,
                onClick = {
                    onInteraction(BattleMapBoardHudInteraction.TerrainPaintSelected(TerrainPaintKind.Clear))
                },
                label = { Text("Clear") },
            )
        }
    }
}

@Composable
private fun ItemToolPanel(
    state: BattleMapBoardHudState,
    onInteraction: (BattleMapBoardHudInteraction) -> Unit,
) {
    HudGlass {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = state.itemNameText,
                onValueChange = { onInteraction(BattleMapBoardHudInteraction.ItemNameChanged(it)) },
                label = { Text("Item name") },
                singleLine = true,
                modifier = Modifier.width(160.dp),
            )
            if (state.selectedItemName != null) {
                Text(
                    text = state.selectedItemName,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 120.dp),
                )
                ActionIconButtonComposeWidget(
                    icon = Icons.Default.Close,
                    tooltip = "Remove",
                    tint = ErrorRed,
                    onClick = { onInteraction(BattleMapBoardHudInteraction.ItemRemoved) },
                )
            }
        }
    }
}

@Composable
private fun LayersToolPanel(
    state: BattleMapBoardHudState,
    onInteraction: (BattleMapBoardHudInteraction) -> Unit,
) {
    HudGlass {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                state.situations.forEach { situation ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilterChip(
                            selected = situation.visible,
                            onClick = {
                                onInteraction(BattleMapBoardHudInteraction.SituationToggled(situation.id))
                            },
                            label = { Text(if (situation.visible) situation.name else "${situation.name} (off)") },
                        )
                        ActionIconButtonComposeWidget(
                            icon = Icons.Default.Close,
                            tooltip = "Remove ${situation.name}",
                            modifier = Modifier.size(32.dp),
                            onClick = {
                                onInteraction(BattleMapBoardHudInteraction.SituationDeleteSelected(situation.id))
                            },
                        )
                    }
                }
                TextButton(
                    onClick = { onInteraction(BattleMapBoardHudInteraction.AddLayerSelected) },
                    enabled = !state.isSavingSituation,
                ) {
                    Text(if (state.isSavingSituation) "Adding…" else "Add layer")
                }
            }
            if (state.situationError != null) {
                Text(text = state.situationError, fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HudIconButton(
    icon: ImageVector,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val selectedColor = NavyBlue
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(contentDescription) } },
        state = rememberTooltipState(),
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = if (selected) selectedColor else Color.Transparent,
                    shape = RoundedCornerShape(10.dp),
                ),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (selected) Color.White else TextPrimary,
            )
        }
    }
}

@Composable
private fun HudGlass(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
    ) {
        content()
    }
}
