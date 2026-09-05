package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeWorldCalendarObservanceRepository : WorldCalendarObservanceRepository {
    private val observances = MutableStateFlow<List<WorldCalendarObservance>>(emptyList())

    fun all(): List<WorldCalendarObservance> = observances.value

    override fun observeByWorld(worldId: String): Flow<List<WorldCalendarObservance>> {
        return observances.map { list -> list.filter { it.worldId == worldId } }
    }

    override suspend fun getById(id: String): WorldCalendarObservance? {
        return observances.value.firstOrNull { it.id == id }
    }

    override suspend fun getByWorld(worldId: String): List<WorldCalendarObservance> {
        return observances.value.filter { it.worldId == worldId }
    }

    override suspend fun search(query: String): List<WorldCalendarObservance> {
        return observances.value.filter { observance ->
            observance.name.contains(query, ignoreCase = true) ||
                observance.notes.contains(query, ignoreCase = true)
        }
    }

    override suspend fun insert(observance: WorldCalendarObservance) {
        observances.value = observances.value + observance
    }

    override suspend fun update(observance: WorldCalendarObservance) {
        observances.value = observances.value.map { current ->
            if (current.id == observance.id) observance else current
        }
    }

    override suspend fun delete(id: String) {
        observances.value = observances.value.filterNot { it.id == id }
    }
}
