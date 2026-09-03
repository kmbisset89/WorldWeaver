package net.tactware.worldweaver.domain

import java.time.Instant

internal data class FactionMembership(
    val id: String,
    val person: PersonRef,
    val factionId: String,
    val role: String,
    val notes: String,
    val createdAt: Instant,
)
