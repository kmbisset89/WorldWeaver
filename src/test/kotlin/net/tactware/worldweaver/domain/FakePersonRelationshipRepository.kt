package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakePersonRelationshipRepository : PersonRelationshipRepository {
    private val relationships = MutableStateFlow<List<PersonRelationship>>(emptyList())

    fun all(): List<PersonRelationship> = relationships.value

    override fun observeAll(): Flow<List<PersonRelationship>> = relationships

    override suspend fun getById(id: String): PersonRelationship? {
        return relationships.value.firstOrNull { it.id == id }
    }

    override suspend fun getAll(): List<PersonRelationship> {
        return relationships.value
    }

    override suspend fun countByFaction(factionId: String): Int {
        return relationships.value.count { it.factionId == factionId }
    }

    override suspend fun insert(relationship: PersonRelationship) {
        relationships.value = relationships.value + relationship
    }

    override suspend fun delete(id: String) {
        relationships.value = relationships.value.filterNot { it.id == id }
    }

    override suspend fun deleteByPerson(ref: PersonRef) {
        relationships.value = relationships.value.filterNot { relationship ->
            sameRef(relationship.from, ref) || sameRef(relationship.to, ref)
        }
    }

    override suspend fun deleteByFaction(factionId: String) {
        relationships.value = relationships.value.filterNot { it.factionId == factionId }
    }

    private fun sameRef(left: PersonRef, right: PersonRef): Boolean {
        return left.id == right.id && left::class == right::class
    }
}
