package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeLoreRepository : LoreRepository {
    private val entries = MutableStateFlow<List<Lore>>(emptyList())

    fun all(): List<Lore> = entries.value

    override fun observeByWorld(worldId: String): Flow<List<Lore>> {
        return entries.map { list -> list.filter { it.worldId == worldId } }
    }

    override suspend fun getById(id: String): Lore? {
        return entries.value.firstOrNull { it.id == id }
    }

    override suspend fun getByWorld(worldId: String): List<Lore> {
        return entries.value.filter { it.worldId == worldId }
    }

    override suspend fun search(query: String): List<Lore> {
        return entries.value.filter { lore ->
            lore.title.contains(query, ignoreCase = true) ||
                lore.content.contains(query, ignoreCase = true) ||
                lore.tags.any { it.contains(query, ignoreCase = true) }
        }.map { lore -> lore.copy(secrets = emptyList()) }
    }

    override suspend fun insert(lore: Lore) {
        entries.value = entries.value + lore
    }

    override suspend fun update(lore: Lore) {
        entries.value = entries.value.map { current ->
            if (current.id == lore.id) lore else current
        }
    }

    override suspend fun delete(id: String) {
        entries.value = entries.value.filterNot { it.id == id }
    }
}
