package io.github.kmbisset89.worldweaver.ui.worldmap

internal sealed interface WorldMapInteraction {
    data object ScreenStarted : WorldMapInteraction
    data class MapOpened(val locationId: String?) : WorldMapInteraction
    data object RetrySelected : WorldMapInteraction
    data object CreateWorldSelected : WorldMapInteraction
    data object BackToLocationsSelected : WorldMapInteraction
    data class ImageChosen(val path: String) : WorldMapInteraction
    data object DeleteMapSelected : WorldMapInteraction
    data object DeleteConfirmed : WorldMapInteraction
    data object DeleteCancelled : WorldMapInteraction
    data class BreadcrumbSelected(val locationId: String?) : WorldMapInteraction
    data class PinSelected(val locationId: String) : WorldMapInteraction
    data class PlacePinSelected(val locationId: String) : WorldMapInteraction
    data object PlacePinCancelled : WorldMapInteraction
    data class ClearPinSelected(val locationId: String) : WorldMapInteraction
    data class MapTapped(val x: Double, val y: Double) : WorldMapInteraction
}
