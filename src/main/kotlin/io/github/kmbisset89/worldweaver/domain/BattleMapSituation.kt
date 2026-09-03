package io.github.kmbisset89.worldweaver.domain

import java.time.Instant

internal data class BattleMapSituation(
    val id: String,
    val battleMapId: String,
    val name: String,
    val visible: Boolean,
    val sortIndex: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
)
