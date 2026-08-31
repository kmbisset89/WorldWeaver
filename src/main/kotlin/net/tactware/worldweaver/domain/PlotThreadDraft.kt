package net.tactware.worldweaver.domain

internal data class PlotThreadDraft(
    val sessionId: String?,
    val title: String,
    val details: String,
    val status: PlotThreadStatus,
    val priority: PlotThreadPriority,
)
