package io.github.kmbisset89.worldweaver.domain

internal data class SessionClock(
    val id: String,
    val sessionId: String,
    val label: String,
    val segmentCount: Int,
    val filledCount: Int,
    val sortIndex: Int,
) {
    companion object {
        const val MIN_SEGMENT_COUNT = 2
        const val MAX_SEGMENT_COUNT = 12
        const val DEFAULT_SEGMENT_COUNT = 6
    }
}
