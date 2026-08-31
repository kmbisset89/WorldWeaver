package net.tactware.worldweaver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.tactware.worldweaver.domain.CampaignPerson
import net.tactware.worldweaver.domain.CampaignPersonRepository

internal class CampaignPersonRepositoryImpl(
    private val campaignPersonDao: CampaignPersonDao,
    private val converter: CampaignPersonEntityConverter,
) : CampaignPersonRepository {
    override fun observeByCampaign(campaignId: String): Flow<List<CampaignPerson>> {
        return campaignPersonDao.observeByCampaign(campaignId).map { entities ->
            entities.map { converter.toPerson(it) }
        }
    }

    override suspend fun getById(id: String): CampaignPerson? {
        return campaignPersonDao.getById(id)?.let { converter.toPerson(it) }
    }

    override suspend fun getByCampaign(campaignId: String): List<CampaignPerson> {
        return campaignPersonDao.getByCampaign(campaignId).map { converter.toPerson(it) }
    }

    override suspend fun search(query: String): List<CampaignPerson> {
        return campaignPersonDao.searchLike(query).map { converter.toPerson(it) }
    }

    override suspend fun countByWorldPerson(worldPersonId: String): Int {
        return campaignPersonDao.countByWorldPerson(worldPersonId)
    }

    override fun observeCount(): Flow<Int> {
        return campaignPersonDao.observeCount()
    }

    override suspend fun insert(person: CampaignPerson) {
        campaignPersonDao.insert(converter.toEntity(person))
    }

    override suspend fun update(person: CampaignPerson) {
        campaignPersonDao.update(converter.toEntity(person))
    }

    override suspend fun delete(id: String) {
        campaignPersonDao.delete(id)
    }
}
