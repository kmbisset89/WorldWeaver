package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal interface PersonCompanionRepository {
    fun observeAll(): Flow<List<PersonCompanion>>
    suspend fun getById(id: String): PersonCompanion?
    suspend fun getAll(): List<PersonCompanion>
    suspend fun findByPair(owner: PersonRef, companion: PersonRef): PersonCompanion?
    suspend fun insert(companion: PersonCompanion)
    suspend fun delete(id: String)
    suspend fun deleteByPerson(ref: PersonRef)
}
