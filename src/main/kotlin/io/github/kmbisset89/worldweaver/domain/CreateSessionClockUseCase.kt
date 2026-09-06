package io.github.kmbisset89.worldweaver.domain

internal class CreateSessionClockUseCase(
    private val sessionClockRepository: SessionClockRepository,
    private val sessionRepository: SessionRepository,
    private val activeContextRepository: ActiveContextRepository,
    private val entityIdFactory: EntityIdFactory,
) {
    sealed interface Result {
        data class Created(val clock: SessionClock) : Result
        data object InvalidLabel : Result
        data object InvalidSegmentCount : Result
        data object NoActiveSession : Result
    }

    suspend operator fun invoke(
        label: String,
        segmentCount: Int = SessionClock.DEFAULT_SEGMENT_COUNT,
    ): Result {
        val sessionId = activeContextRepository.get().activeSessionId
            ?: return Result.NoActiveSession
        sessionRepository.getById(sessionId) ?: return Result.NoActiveSession
        val trimmed = label.trim()
        if (trimmed.isEmpty()) {
            return Result.InvalidLabel
        }
        if (segmentCount !in SessionClock.MIN_SEGMENT_COUNT..SessionClock.MAX_SEGMENT_COUNT) {
            return Result.InvalidSegmentCount
        }
        val nextIndex = sessionClockRepository.getBySession(sessionId).maxOfOrNull { it.sortIndex }?.plus(1) ?: 0
        val clock = SessionClock(
            id = entityIdFactory.create(),
            sessionId = sessionId,
            label = trimmed,
            segmentCount = segmentCount,
            filledCount = 0,
            sortIndex = nextIndex,
        )
        sessionClockRepository.insert(clock)
        return Result.Created(clock)
    }
}
