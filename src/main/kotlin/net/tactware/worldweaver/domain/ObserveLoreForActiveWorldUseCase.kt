package net.tactware.worldweaver.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

internal class ObserveLoreForActiveWorldUseCase(
    private val loreRepository: LoreRepository,
    private val activeContextRepository: ActiveContextRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<Lore>> {
        return activeContextRepository.observe().flatMapLatest { context ->
            val worldId = context.activeWorldId
            if (worldId == null) {
                flowOf(emptyList())
            } else {
                loreRepository.observeByWorld(worldId)
            }
        }
    }
}
