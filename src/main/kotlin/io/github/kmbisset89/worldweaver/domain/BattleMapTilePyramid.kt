package io.github.kmbisset89.worldweaver.domain

internal data class BattleMapTilePyramid(
    val originalWidth: Int,
    val originalHeight: Int,
    val tileSizePx: Int,
    val minZoom: Int,
    val maxZoom: Int,
    val originalPng: ByteArray,
    val tiles: List<BattleMapTile>,
)
