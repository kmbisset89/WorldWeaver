package io.github.kmbisset89.worldweaver.ui.maps

internal sealed interface BattleMapBoardHudInteraction {
    data object MoveToolSelected : BattleMapBoardHudInteraction
    data object MeasureToolSelected : BattleMapBoardHudInteraction
    data object FogToolSelected : BattleMapBoardHudInteraction
    data object TerrainToolSelected : BattleMapBoardHudInteraction
    data object ItemToolSelected : BattleMapBoardHudInteraction
    data object LayersToolSelected : BattleMapBoardHudInteraction
    data class MovementSpeedChanged(val speed: String) : BattleMapBoardHudInteraction
    data object MovementCleared : BattleMapBoardHudInteraction
    data object MeasureCleared : BattleMapBoardHudInteraction
    data object FogHideBrushSelected : BattleMapBoardHudInteraction
    data object FogRevealBrushSelected : BattleMapBoardHudInteraction
    data object FogHideAllSelected : BattleMapBoardHudInteraction
    data object FogRevealAllSelected : BattleMapBoardHudInteraction
    data class TerrainPaintSelected(val kind: TerrainPaintKind) : BattleMapBoardHudInteraction
    data class ItemNameChanged(val name: String) : BattleMapBoardHudInteraction
    data object ItemRemoved : BattleMapBoardHudInteraction
    data class SituationToggled(val situationId: String) : BattleMapBoardHudInteraction
    data class SituationDeleteSelected(val situationId: String) : BattleMapBoardHudInteraction
    data object AddLayerSelected : BattleMapBoardHudInteraction
    data object PlayerViewSelected : BattleMapBoardHudInteraction
    data object ExportSelected : BattleMapBoardHudInteraction
    data object DeleteSelected : BattleMapBoardHudInteraction
}
