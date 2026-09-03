package io.github.kmbisset89.worldweaver.domain

internal data class PlotThreadDraft(
    val sessionId: String?,
    val title: String,
    val details: String,
    val status: PlotThreadStatus,
    val priority: PlotThreadPriority,
)
