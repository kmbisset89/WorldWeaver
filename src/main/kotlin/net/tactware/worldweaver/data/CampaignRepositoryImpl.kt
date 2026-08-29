package net.tactware.worldweaver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.tactware.worldweaver.domain.Campaign
import net.tactware.worldweaver.domain.CampaignRepository

internal class CampaignRepositoryImpl(
    private val campaignDao: CampaignDao,
    private val converter: CampaignEntityConverter,
) : CampaignRepository {
    override fun observeByWorld(worldId: String): Flow<List<Campaign>> {
        return campaignDao.observeByWorld(worldId).map { entities ->
            entities.map { converter.toCampaign(it) }
        }
    }

    override fun observeById(id: String): Flow<Campaign?> {
        return campaignDao.observeById(id).map { entity ->
            entity?.let { converter.toCampaign(it) }
        }
    }

    override suspend fun getById(id: String): Campaign? {
        return campaignDao.getById(id)?.let { converter.toCampaign(it) }
    }

    override suspend fun countByWorld(worldId: String): Int {
        return campaignDao.countByWorld(worldId)
    }

    override suspend fun insert(campaign: Campaign) {
        campaignDao.insert(converter.toEntity(campaign))
    }

    override suspend fun update(campaign: Campaign) {
        campaignDao.update(converter.toEntity(campaign))
    }

    override suspend fun delete(id: String) {
        campaignDao.delete(id)
    }
}
