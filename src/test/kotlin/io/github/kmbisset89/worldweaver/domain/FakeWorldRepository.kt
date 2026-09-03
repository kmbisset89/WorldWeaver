package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeWorldRepository : WorldRepository {
    private val worlds = MutableStateFlow<List<World>>(emptyList())

    fun all(): List<World> = worlds.value

    override fun observeAll(): Flow<List<World>> = worlds

    override fun observeCount(): Flow<Int> {
        return worlds.map { it.size }
    }

    override fun observeById(id: String): Flow<World?> {
        return worlds.map { list -> list.firstOrNull { it.id == id } }
    }

    override suspend fun getById(id: String): World? {
        return worlds.value.firstOrNull { it.id == id }
    }

    override suspend fun search(query: String): List<World> {
        return worlds.value.filter { world ->
            world.name.contains(query, ignoreCase = true) ||
                world.description.contains(query, ignoreCase = true)
        }
    }

    override suspend fun insert(world: World) {
        worlds.value = worlds.value + world
    }

    override suspend fun update(world: World) {
        worlds.value = worlds.value.map { current ->
            if (current.id == world.id) world else current
        }
    }

    override suspend fun delete(id: String) {
        worlds.value = worlds.value.filterNot { it.id == id }
    }
}
