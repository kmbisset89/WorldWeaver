package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeWorldCelestialBodyRepository : WorldCelestialBodyRepository {
    private val bodies = MutableStateFlow<List<WorldCelestialBody>>(emptyList())

    fun all(): List<WorldCelestialBody> = bodies.value

    override fun observeByWorld(worldId: String): Flow<List<WorldCelestialBody>> {
        return bodies.map { list ->
            list.filter { it.worldId == worldId }.sortedWith(
                compareBy<WorldCelestialBody> { it.sortIndex }.thenBy { it.name }
            )
        }
    }

    override suspend fun getById(id: String): WorldCelestialBody? {
        return bodies.value.firstOrNull { it.id == id }
    }

    override suspend fun getByWorld(worldId: String): List<WorldCelestialBody> {
        return bodies.value.filter { it.worldId == worldId }.sortedWith(
            compareBy<WorldCelestialBody> { it.sortIndex }.thenBy { it.name }
        )
    }

    override suspend fun search(query: String): List<WorldCelestialBody> {
        return bodies.value.filter { body ->
            body.name.contains(query, ignoreCase = true) ||
                body.notes.contains(query, ignoreCase = true)
        }
    }

    override suspend fun insert(body: WorldCelestialBody) {
        bodies.value = bodies.value + body
    }

    override suspend fun update(body: WorldCelestialBody) {
        bodies.value = bodies.value.map { current ->
            if (current.id == body.id) body else current
        }
    }

    override suspend fun delete(id: String) {
        bodies.value = bodies.value.filterNot { it.id == id }
    }
}
