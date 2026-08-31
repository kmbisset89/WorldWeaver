package net.tactware.worldweaver.domain

import java.time.Instant

internal data class PlotThread(
    val id: String,
    val campaignId: String,
    val sessionId: String?,
    val title: String,
    val details: String,
    val status: PlotThreadStatus,
    val priority: PlotThreadPriority,
    val createdAt: Instant,
    val updatedAt: Instant,
)
