package io.github.kmbisset89.worldweaver.domain

internal data class BattleMapTile(
    val zoom: Int,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val imagePng: ByteArray,
)
