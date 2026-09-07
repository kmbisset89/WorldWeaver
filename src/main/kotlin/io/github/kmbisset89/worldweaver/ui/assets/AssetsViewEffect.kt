package io.github.kmbisset89.worldweaver.ui.assets

internal sealed interface AssetsViewEffect {
    data object OpenWorlds : AssetsViewEffect
    data class OpenFile(val path: String) : AssetsViewEffect
    data class Failed(val message: String) : AssetsViewEffect
}
