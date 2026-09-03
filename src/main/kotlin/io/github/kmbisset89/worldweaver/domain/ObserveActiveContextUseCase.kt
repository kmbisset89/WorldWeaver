package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal class ObserveActiveContextUseCase(
    private val activeContextRepository: ActiveContextRepository,
) {
    operator fun invoke(): Flow<ActiveContext> = activeContextRepository.observe()
}
