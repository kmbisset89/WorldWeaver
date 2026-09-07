package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

internal class ObserveRandomTablesForActiveWorldUseCase(
    private val randomTableRepository: RandomTableRepository,
    private val activeContextRepository: ActiveContextRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<RandomTable>> {
        return activeContextRepository.observe().flatMapLatest { context ->
            val worldId = context.activeWorldId
            if (worldId == null) {
                flowOf(emptyList())
            } else {
                randomTableRepository.observeByWorld(worldId)
            }
        }
    }
}
