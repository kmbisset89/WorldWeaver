package io.github.kmbisset89.worldweaver.domain

internal data class MarchOrderEntry(
    val id: String,
    val person: PersonRef,
    val displayName: String,
)
