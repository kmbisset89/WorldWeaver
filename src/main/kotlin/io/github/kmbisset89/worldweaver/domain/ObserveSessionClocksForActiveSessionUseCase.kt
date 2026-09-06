package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

internal class ObserveSessionClocksForActiveSessionUseCase(
    private val sessionClockRepository: SessionClockRepository,
    private val activeContextRepository: ActiveContextRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<SessionClock>> {
        return activeContextRepository.observe().flatMapLatest { context ->
            val sessionId = context.activeSessionId
            if (sessionId == null) {
                flowOf(emptyList())
            } else {
                sessionClockRepository.observeBySession(sessionId)
            }
        }
    }
}
