package io.github.kmbisset89.worldweaver.ui.worldmap

internal sealed class WorldMapViewState {
    data object Loading : WorldMapViewState()

    data class Error(
        val message: String,
        val canRetry: Boolean,
    ) : WorldMapViewState()

    data object NoActiveWorld : WorldMapViewState()

    data class Empty(
        val worldName: String,
        val locationId: String?,
        val locationName: String?,
        val importError: String?,
    ) : WorldMapViewState()

    data class Content(
        val worldName: String,
        val locationId: String?,
        val title: String,
        val breadcrumbs: List<Breadcrumb>,
        val pins: List<Pin>,
        val unplacedChildren: List<UnplacedChild>,
        val selectedLocationId: String?,
        val selectedLocationName: String?,
        val selectedHasMap: Boolean,
        val placingLocationId: String?,
        val importError: String?,
        val pendingDelete: PendingDelete?,
    ) : WorldMapViewState()

    data class Breadcrumb(
        val locationId: String?,
        val name: String,
        val hasMap: Boolean,
    )

    data class Pin(
        val locationId: String,
        val name: String,
        val x: Double,
        val y: Double,
        val hasMap: Boolean,
    )

    data class UnplacedChild(
        val locationId: String,
        val name: String,
    )

    data class PendingDelete(
        val worldMapId: String,
        val title: String,
    )
}
