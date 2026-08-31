package net.tactware.worldweaver.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

internal class ObserveWorldCalendarForActiveWorldUseCase(
    private val worldCalendarRepository: WorldCalendarRepository,
    private val activeContextRepository: ActiveContextRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<WorldCalendar?> {
        return activeContextRepository.observe().flatMapLatest { context ->
            val worldId = context.activeWorldId
            if (worldId == null) {
                flowOf(null)
            } else {
                worldCalendarRepository.observeByWorld(worldId)
            }
        }
    }
}
