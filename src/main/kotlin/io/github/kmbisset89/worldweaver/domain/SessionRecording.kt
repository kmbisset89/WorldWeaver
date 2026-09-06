package io.github.kmbisset89.worldweaver.domain

import java.time.Instant

internal data class SessionRecording(
    val id: String,
    val sessionId: String,
    val kind: SessionRecordingKind,
    val path: String,
    val startedAt: Instant,
    val byteSize: Long,
)
