package io.github.kmbisset89.worldweaver.domain

internal data class RandomTableRow(
    val id: String,
    val label: String,
    val weight: Int,
    val nestedTableId: String?,
)
