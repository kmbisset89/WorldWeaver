package io.github.kmbisset89.worldweaver.domain

internal data class BattleMapSituationDraft(
    val battleMapId: String,
    val name: String,
    val imagePng: ByteArray,
    val visible: Boolean = true,
)
