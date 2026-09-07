package io.github.kmbisset89.worldweaver.domain

internal data class RandomTableDraft(
    val name: String,
    val notes: String,
    val rows: List<RandomTableRowDraft>,
)
