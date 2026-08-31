package net.tactware.worldweaver.domain

import java.time.Instant

internal data class Session(
    val id: String,
    val campaignId: String,
    val name: String,
    val notes: String,
    val inWorldDate: WorldDate? = null,
    val scenes: List<SessionScene>,
    val marchOrder: List<MarchOrderEntry>,
    val createdAt: Instant,
    val updatedAt: Instant,
)
