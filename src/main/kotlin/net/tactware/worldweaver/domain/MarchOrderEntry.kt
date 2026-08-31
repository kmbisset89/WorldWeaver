package net.tactware.worldweaver.domain

internal data class MarchOrderEntry(
    val id: String,
    val person: PersonRef,
    val displayName: String,
)
