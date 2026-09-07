package io.github.kmbisset89.worldweaver.ui.assets

internal sealed interface AssetsInteraction {
    data object ScreenStarted : AssetsInteraction
    data object RetrySelected : AssetsInteraction
    data object CreateWorldSelected : AssetsInteraction
    data class FilesChosen(val paths: List<String>) : AssetsInteraction
    data class AssetSelected(val assetId: String) : AssetsInteraction
    data class AssetOpened(val assetId: String) : AssetsInteraction
    data class DisplayNameChanged(val name: String) : AssetsInteraction
    data class NotesChanged(val notes: String) : AssetsInteraction
    data class OpenFileSelected(val assetId: String) : AssetsInteraction
    data class DeleteAssetSelected(val assetId: String) : AssetsInteraction
    data object DeleteConfirmed : AssetsInteraction
    data object DeleteCancelled : AssetsInteraction
}
