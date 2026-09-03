package net.tactware.worldweaver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.tactware.worldweaver.domain.FactionMembership
import net.tactware.worldweaver.domain.FactionMembershipRepository
import net.tactware.worldweaver.domain.PersonRef

internal class FactionMembershipRepositoryImpl(
    private val factionMembershipDao: FactionMembershipDao,
    private val converter: FactionMembershipEntityConverter,
) : FactionMembershipRepository {
    override fun observeAll(): Flow<List<FactionMembership>> {
        return factionMembershipDao.observeAll().map { entities ->
            entities.map { converter.toMembership(it) }
        }
    }

    override suspend fun getAll(): List<FactionMembership> {
        return factionMembershipDao.getAll().map { converter.toMembership(it) }
    }

    override suspend fun getById(id: String): FactionMembership? {
        return factionMembershipDao.getById(id)?.let { converter.toMembership(it) }
    }

    override suspend fun getByFaction(factionId: String): List<FactionMembership> {
        return factionMembershipDao.getByFaction(factionId).map { converter.toMembership(it) }
    }

    override suspend fun getByPerson(ref: PersonRef): List<FactionMembership> {
        return factionMembershipDao.getByPerson(
            kind = converter.kindName(ref),
            personId = ref.id,
        ).map { converter.toMembership(it) }
    }

    override suspend fun countByFaction(factionId: String): Int {
        return factionMembershipDao.countByFaction(factionId)
    }

    override suspend fun insert(membership: FactionMembership) {
        factionMembershipDao.insert(converter.toEntity(membership))
    }

    override suspend fun delete(id: String) {
        factionMembershipDao.delete(id)
    }

    override suspend fun deleteByPerson(ref: PersonRef) {
        factionMembershipDao.deleteByPerson(
            kind = converter.kindName(ref),
            personId = ref.id,
        )
    }

    override suspend fun deleteByFaction(factionId: String) {
        factionMembershipDao.deleteByFaction(factionId)
    }
}
