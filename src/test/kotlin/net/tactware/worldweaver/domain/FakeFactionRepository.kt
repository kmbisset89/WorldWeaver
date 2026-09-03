package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeFactionRepository : FactionRepository {
    private val factions = MutableStateFlow<List<Faction>>(emptyList())

    fun all(): List<Faction> = factions.value

    override fun observeByWorld(worldId: String): Flow<List<Faction>> {
        return factions.map { list -> list.filter { it.worldId == worldId } }
    }

    override suspend fun getById(id: String): Faction? {
        return factions.value.firstOrNull { it.id == id }
    }

    override suspend fun getByWorld(worldId: String): List<Faction> {
        return factions.value.filter { it.worldId == worldId }
    }

    override suspend fun search(query: String): List<Faction> {
        return factions.value.filter { faction ->
            faction.name.contains(query, ignoreCase = true) ||
                faction.description.contains(query, ignoreCase = true) ||
                faction.goals.contains(query, ignoreCase = true) ||
                faction.notes.contains(query, ignoreCase = true)
        }
    }

    override suspend fun insert(faction: Faction) {
        factions.value = factions.value + faction
    }

    override suspend fun update(faction: Faction) {
        factions.value = factions.value.map { current ->
            if (current.id == faction.id) faction else current
        }
    }

    override suspend fun delete(id: String) {
        factions.value = factions.value.filterNot { it.id == id }
    }
}
