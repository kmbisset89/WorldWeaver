package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeWorldCalendarRepository : WorldCalendarRepository {
    private val calendars = MutableStateFlow<List<WorldCalendar>>(emptyList())

    fun all(): List<WorldCalendar> = calendars.value

    override fun observeByWorld(worldId: String): Flow<WorldCalendar?> {
        return calendars.map { list -> list.firstOrNull { it.worldId == worldId } }
    }

    override suspend fun getByWorld(worldId: String): WorldCalendar? {
        return calendars.value.firstOrNull { it.worldId == worldId }
    }

    override suspend fun getById(id: String): WorldCalendar? {
        return calendars.value.firstOrNull { it.id == id }
    }

    override suspend fun insert(calendar: WorldCalendar) {
        calendars.value = calendars.value + calendar
    }

    override suspend fun update(calendar: WorldCalendar) {
        calendars.value = calendars.value.map { current ->
            if (current.id == calendar.id) calendar else current
        }
    }
}
