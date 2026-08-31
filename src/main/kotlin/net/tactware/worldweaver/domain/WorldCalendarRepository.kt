package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal interface WorldCalendarRepository {
    fun observeByWorld(worldId: String): Flow<WorldCalendar?>
    suspend fun getByWorld(worldId: String): WorldCalendar?
    suspend fun getById(id: String): WorldCalendar?
    suspend fun insert(calendar: WorldCalendar)
    suspend fun update(calendar: WorldCalendar)
}
