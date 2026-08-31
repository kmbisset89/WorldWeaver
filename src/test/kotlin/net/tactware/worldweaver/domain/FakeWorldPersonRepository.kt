package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeWorldPersonRepository : WorldPersonRepository {
    private val people = MutableStateFlow<List<WorldPerson>>(emptyList())

    fun all(): List<WorldPerson> = people.value

    override fun observeByWorld(worldId: String): Flow<List<WorldPerson>> {
        return people.map { list -> list.filter { it.worldId == worldId } }
    }

    override fun observeCount(): Flow<Int> {
        return people.map { it.size }
    }

    override suspend fun getById(id: String): WorldPerson? {
        return people.value.firstOrNull { it.id == id }
    }

    override suspend fun getByWorld(worldId: String): List<WorldPerson> {
        return people.value.filter { it.worldId == worldId }
    }

    override suspend fun search(query: String): List<WorldPerson> {
        return people.value.filter { person ->
            person.name.contains(query, ignoreCase = true) ||
                person.description.contains(query, ignoreCase = true)
        }
    }

    override suspend fun insert(person: WorldPerson) {
        people.value = people.value + person
    }

    override suspend fun update(person: WorldPerson) {
        people.value = people.value.map { current ->
            if (current.id == person.id) person else current
        }
    }

    override suspend fun delete(id: String) {
        people.value = people.value.filterNot { it.id == id }
    }
}
