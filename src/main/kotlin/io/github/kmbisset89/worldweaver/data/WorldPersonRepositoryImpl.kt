package io.github.kmbisset89.worldweaver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import io.github.kmbisset89.worldweaver.domain.WorldPerson
import io.github.kmbisset89.worldweaver.domain.WorldPersonRepository

internal class WorldPersonRepositoryImpl(
    private val worldPersonDao: WorldPersonDao,
    private val converter: WorldPersonEntityConverter,
) : WorldPersonRepository {
    override fun observeByWorld(worldId: String): Flow<List<WorldPerson>> {
        return worldPersonDao.observeByWorld(worldId).map { entities ->
            entities.map { converter.toPerson(it) }
        }
    }

    override fun observeCount(): Flow<Int> {
        return worldPersonDao.observeCount()
    }

    override suspend fun getById(id: String): WorldPerson? {
        return worldPersonDao.getById(id)?.let { converter.toPerson(it) }
    }

    override suspend fun getByWorld(worldId: String): List<WorldPerson> {
        return worldPersonDao.getByWorld(worldId).map { converter.toPerson(it) }
    }

    override suspend fun search(query: String): List<WorldPerson> {
        return worldPersonDao.searchLike(query).map { converter.toPerson(it) }
    }

    override suspend fun insert(person: WorldPerson) {
        worldPersonDao.insert(converter.toEntity(person))
    }

    override suspend fun update(person: WorldPerson) {
        worldPersonDao.update(converter.toEntity(person))
    }

    override suspend fun delete(id: String) {
        worldPersonDao.delete(id)
    }
}
