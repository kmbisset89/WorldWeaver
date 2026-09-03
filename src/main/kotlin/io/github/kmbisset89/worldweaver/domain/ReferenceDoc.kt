package io.github.kmbisset89.worldweaver.domain

import java.time.Instant

internal data class ReferenceDoc(
    val id: String,
    val campaignId: String,
    val sessionId: String?,
    val title: String,
    val pathOrUrl: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
