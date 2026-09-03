package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal interface PersonRelationshipRepository {
    fun observeAll(): Flow<List<PersonRelationship>>
    suspend fun getById(id: String): PersonRelationship?
    suspend fun getAll(): List<PersonRelationship>
    suspend fun countByFaction(factionId: String): Int
    suspend fun insert(relationship: PersonRelationship)
    suspend fun delete(id: String)
    suspend fun deleteByPerson(ref: PersonRef)
    suspend fun deleteByFaction(factionId: String)
}
