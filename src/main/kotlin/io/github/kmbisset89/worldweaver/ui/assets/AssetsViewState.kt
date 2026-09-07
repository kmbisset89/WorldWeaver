package io.github.kmbisset89.worldweaver.ui.assets

internal sealed class AssetsViewState {
    data object Loading : AssetsViewState()

    data class Error(
        val message: String,
        val canRetry: Boolean,
    ) : AssetsViewState()

    data object NoActiveWorld : AssetsViewState()

    data class Empty(
        val worldName: String,
    ) : AssetsViewState()

    data class Content(
        val worldName: String,
        val assets: List<AssetItem>,
        val selectedAsset: AssetItem?,
        val pendingDelete: PendingDelete?,
        val nameError: String?,
    ) : AssetsViewState()

    data class AssetItem(
        val id: String,
        val displayName: String,
        val originalFileName: String,
        val notes: String,
        val sizeLabel: String,
        val isImage: Boolean,
        val filePath: String?,
    )

    data class PendingDelete(
        val assetId: String,
        val displayName: String,
    )
}
