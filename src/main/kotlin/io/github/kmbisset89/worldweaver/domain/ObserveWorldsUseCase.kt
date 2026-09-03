package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal class ObserveWorldsUseCase(
    private val worldRepository: WorldRepository,
) {
    operator fun invoke(): Flow<List<World>> = worldRepository.observeAll()
}
