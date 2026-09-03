package net.tactware.worldweaver.ui.maps

internal sealed interface MapsInteraction {
    data object ScreenStarted : MapsInteraction
    data object RetrySelected : MapsInteraction
    data object CreateWorldSelected : MapsInteraction
    data object CreateCampaignSelected : MapsInteraction
    data object ImportSelected : MapsInteraction
    data object StarterCatalogSelected : MapsInteraction
    data object StarterCatalogDismissed : MapsInteraction
    data class BundledMapSelected(val entryId: String) : MapsInteraction
    data class MakerNameChanged(val name: String) : MapsInteraction
    data class MakerImageChosen(val path: String) : MapsInteraction
    data class MakerColumnsChanged(val columns: String) : MapsInteraction
    data class MakerRowsChanged(val rows: String) : MapsInteraction
    data class MakerUnitNameChanged(val unitName: String) : MapsInteraction
    data class MakerUnitsPerTileChanged(val unitsPerTile: String) : MapsInteraction
    data class MakerSceneryChanged(val scenery: String) : MapsInteraction
    data class MakerScaleChanged(val scalePercent: String) : MapsInteraction
    data object MakerGridToggled : MapsInteraction
    data object MakerRenderTilesToggled : MapsInteraction
    data object MakerSaved : MapsInteraction
    data object MakerDismissed : MapsInteraction
    data class MapSelected(val battleMapId: String) : MapsInteraction
    data class MapOpened(val battleMapId: String) : MapsInteraction
    data object PlayerViewSelected : MapsInteraction
    data object PlayerViewClosed : MapsInteraction
    data class PlayerViewOpened(
        val battleMapId: String,
        val walkSpeed: Int?,
    ) : MapsInteraction
    data class MapCellSelected(val x: Double, val y: Double) : MapsInteraction
    data class TokenSelected(val participantId: String) : MapsInteraction
    data class MovementSpeedChanged(val speed: String) : MapsInteraction
    data object MovementCleared : MapsInteraction
    data object MeasureToggled : MapsInteraction
    data object MeasureCleared : MapsInteraction
    data object FogToggled : MapsInteraction
    data object FogRevealBrushSelected : MapsInteraction
    data object FogHideBrushSelected : MapsInteraction
    data object FogRevealAllSelected : MapsInteraction
    data object FogHideAllSelected : MapsInteraction
    data class TerrainPaintSelected(val kind: TerrainPaintKind?) : MapsInteraction
    data object ItemDropToggled : MapsInteraction
    data class ItemNameChanged(val name: String) : MapsInteraction
    data class ItemSelected(val itemId: String) : MapsInteraction
    data object ItemRemoved : MapsInteraction
    data class SituationImageChosen(val path: String) : MapsInteraction
    data class SituationToggled(val situationId: String) : MapsInteraction
    data class SituationDeleteSelected(val situationId: String) : MapsInteraction
    data class DeleteMapSelected(val battleMapId: String) : MapsInteraction
    data object DeleteConfirmed : MapsInteraction
    data object DeleteCancelled : MapsInteraction
}
