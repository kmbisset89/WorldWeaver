package net.tactware.worldweaver.domain

import java.time.Instant

internal data class WorldPerson(
    val id: String,
    val worldId: String,
    val kind: PersonKind,
    val name: String,
    val description: String,
    val sheet: PersonSheet,
    val createdAt: Instant,
    val updatedAt: Instant,
)
