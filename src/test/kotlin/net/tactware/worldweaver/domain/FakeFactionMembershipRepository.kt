package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeFactionMembershipRepository : FactionMembershipRepository {
    private val memberships = MutableStateFlow<List<FactionMembership>>(emptyList())

    fun all(): List<FactionMembership> = memberships.value

    override fun observeAll(): Flow<List<FactionMembership>> = memberships

    override suspend fun getAll(): List<FactionMembership> {
        return memberships.value
    }

    override suspend fun getById(id: String): FactionMembership? {
        return memberships.value.firstOrNull { it.id == id }
    }

    override suspend fun getByFaction(factionId: String): List<FactionMembership> {
        return memberships.value.filter { it.factionId == factionId }
    }

    override suspend fun getByPerson(ref: PersonRef): List<FactionMembership> {
        return memberships.value.filter { sameRef(it.person, ref) }
    }

    override suspend fun countByFaction(factionId: String): Int {
        return memberships.value.count { it.factionId == factionId }
    }

    override suspend fun insert(membership: FactionMembership) {
        memberships.value = memberships.value + membership
    }

    override suspend fun delete(id: String) {
        memberships.value = memberships.value.filterNot { it.id == id }
    }

    override suspend fun deleteByPerson(ref: PersonRef) {
        memberships.value = memberships.value.filterNot { sameRef(it.person, ref) }
    }

    override suspend fun deleteByFaction(factionId: String) {
        memberships.value = memberships.value.filterNot { it.factionId == factionId }
    }

    private fun sameRef(left: PersonRef, right: PersonRef): Boolean {
        return left.id == right.id && left::class == right::class
    }
}
