package io.github.kmbisset89.worldweaver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import io.github.kmbisset89.worldweaver.domain.PersonCompanion
import io.github.kmbisset89.worldweaver.domain.PersonCompanionRepository
import io.github.kmbisset89.worldweaver.domain.PersonRef

internal class PersonCompanionRepositoryImpl(
    private val personCompanionDao: PersonCompanionDao,
    private val converter: PersonCompanionEntityConverter,
) : PersonCompanionRepository {
    override fun observeAll(): Flow<List<PersonCompanion>> {
        return personCompanionDao.observeAll().map { entities ->
            entities.map { converter.toCompanion(it) }
        }
    }

    override suspend fun getById(id: String): PersonCompanion? {
        return personCompanionDao.getById(id)?.let { converter.toCompanion(it) }
    }

    override suspend fun getAll(): List<PersonCompanion> {
        return personCompanionDao.getAll().map { converter.toCompanion(it) }
    }

    override suspend fun findByPair(owner: PersonRef, companion: PersonRef): PersonCompanion? {
        return personCompanionDao.findByPair(
            ownerKind = converter.kindName(owner),
            ownerId = owner.id,
            companionKind = converter.kindName(companion),
            companionId = companion.id,
        )?.let { converter.toCompanion(it) }
    }

    override suspend fun insert(companion: PersonCompanion) {
        personCompanionDao.insert(converter.toEntity(companion))
    }

    override suspend fun delete(id: String) {
        personCompanionDao.delete(id)
    }

    override suspend fun deleteByPerson(ref: PersonRef) {
        personCompanionDao.deleteByPerson(
            kind = converter.kindName(ref),
            personId = ref.id,
        )
    }
}
