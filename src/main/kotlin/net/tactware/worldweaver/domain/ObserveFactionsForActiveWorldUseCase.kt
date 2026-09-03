package net.tactware.worldweaver.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

internal class ObserveFactionsForActiveWorldUseCase(
    private val factionRepository: FactionRepository,
    private val activeContextRepository: ActiveContextRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<Faction>> {
        return activeContextRepository.observe().flatMapLatest { context ->
            val worldId = context.activeWorldId
            if (worldId == null) {
                flowOf(emptyList())
            } else {
                factionRepository.observeByWorld(worldId)
            }
        }
    }
}
