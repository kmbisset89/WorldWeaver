package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal interface FactionMembershipRepository {
    fun observeAll(): Flow<List<FactionMembership>>
    suspend fun getAll(): List<FactionMembership>
    suspend fun getById(id: String): FactionMembership?
    suspend fun getByFaction(factionId: String): List<FactionMembership>
    suspend fun getByPerson(ref: PersonRef): List<FactionMembership>
    suspend fun countByFaction(factionId: String): Int
    suspend fun insert(membership: FactionMembership)
    suspend fun delete(id: String)
    suspend fun deleteByPerson(ref: PersonRef)
    suspend fun deleteByFaction(factionId: String)
}
