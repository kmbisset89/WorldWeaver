package io.github.kmbisset89.worldweaver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import io.github.kmbisset89.worldweaver.domain.PersonRef
import io.github.kmbisset89.worldweaver.domain.PersonRelationship
import io.github.kmbisset89.worldweaver.domain.PersonRelationshipRepository

internal class PersonRelationshipRepositoryImpl(
    private val personRelationshipDao: PersonRelationshipDao,
    private val converter: PersonRelationshipEntityConverter,
) : PersonRelationshipRepository {
    override fun observeAll(): Flow<List<PersonRelationship>> {
        return personRelationshipDao.observeAll().map { entities ->
            entities.map { converter.toRelationship(it) }
        }
    }

    override suspend fun getById(id: String): PersonRelationship? {
        return personRelationshipDao.getById(id)?.let { converter.toRelationship(it) }
    }

    override suspend fun getAll(): List<PersonRelationship> {
        return personRelationshipDao.getAll().map { converter.toRelationship(it) }
    }

    override suspend fun countByFaction(factionId: String): Int {
        return personRelationshipDao.countByFaction(factionId)
    }

    override suspend fun insert(relationship: PersonRelationship) {
        personRelationshipDao.insert(converter.toEntity(relationship))
    }

    override suspend fun delete(id: String) {
        personRelationshipDao.delete(id)
    }

    override suspend fun deleteByPerson(ref: PersonRef) {
        personRelationshipDao.deleteByPerson(
            kind = converter.kindName(ref),
            personId = ref.id,
        )
    }

    override suspend fun deleteByFaction(factionId: String) {
        personRelationshipDao.deleteByFaction(factionId)
    }
}
