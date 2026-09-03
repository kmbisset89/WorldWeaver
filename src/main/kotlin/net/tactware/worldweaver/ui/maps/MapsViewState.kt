package net.tactware.worldweaver.ui.maps

import net.tactware.worldweaver.domain.BattleMap
import net.tactware.worldweaver.domain.BattleMapSituation
import net.tactware.worldweaver.domain.GridCell
import net.tactware.worldweaver.domain.GridDistance

internal sealed class MapsViewState {
    data object Loading : MapsViewState()

    data class Error(
        val message: String,
        val canRetry: Boolean,
    ) : MapsViewState()

    data object NoActiveWorld : MapsViewState()

    data object NoActiveCampaign : MapsViewState()

    data class Empty(
        val worldName: String,
        val campaignName: String,
        val starterCatalogAvailable: Boolean,
    ) : MapsViewState()

    data class Content(
        val worldName: String,
        val campaignName: String,
        val maps: List<BattleMap>,
        val selectedMap: BattleMap?,
        val situations: List<BattleMapSituation>,
        val situationError: String?,
        val isSavingSituation: Boolean,
        val pendingDelete: PendingDelete?,
        val playerViewOpen: Boolean,
        val movementSpeedText: String,
        val movementOrigin: GridCell?,
        val reachableCells: List<GridCell>,
        val measureEnabled: Boolean,
        val measureOrigin: GridCell?,
        val measureDestination: GridCell?,
        val measureDistance: GridDistance?,
        val fogPaintEnabled: Boolean,
        val fogRevealBrush: Boolean,
        val terrainPaint: TerrainPaintKind?,
        val itemDropEnabled: Boolean,
        val itemNameText: String,
        val selectedItemId: String?,
        val selectedItemName: String?,
        val tokens: List<BattleMapBoardToken>,
        val selectedTokenName: String?,
        val unplacedTokenCount: Int,
        val starterCatalogAvailable: Boolean,
    ) : MapsViewState()

    data class StarterCatalog(
        val worldName: String,
        val campaignName: String,
        val entries: List<StarterCatalogEntry>,
        val importingId: String?,
        val error: String?,
    ) : MapsViewState()

    data class StarterCatalogEntry(
        val id: String,
        val name: String,
        val detail: String,
        val alreadyAdded: Boolean,
    )

    data class Maker(
        val worldName: String,
        val campaignName: String,
        val editor: MakerEditorState,
    ) : MapsViewState()

    data class MakerEditorState(
        val name: String,
        val imagePath: String?,
        val imageWidth: Int,
        val imageHeight: Int,
        val columnsText: String,
        val rowsText: String,
        val unitNameText: String,
        val unitsPerTileText: String,
        val scalePercentText: String,
        val showGrid: Boolean,
        val showRenderTiles: Boolean,
        val sceneryText: String,
        val imagePrompt: String,
        val nameError: String?,
        val imageError: String?,
        val gridError: String?,
        val isSaving: Boolean,
    )

    data class PendingDelete(
        val battleMapId: String,
        val battleMapName: String,
    )
}
