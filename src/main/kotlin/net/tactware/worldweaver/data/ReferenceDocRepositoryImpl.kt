package net.tactware.worldweaver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.tactware.worldweaver.domain.ReferenceDoc
import net.tactware.worldweaver.domain.ReferenceDocRepository

internal class ReferenceDocRepositoryImpl(
    private val dao: ReferenceDocDao,
    private val converter: ReferenceDocEntityConverter,
) : ReferenceDocRepository {
    override fun observeByCampaign(campaignId: String): Flow<List<ReferenceDoc>> {
        return dao.observeByCampaign(campaignId).map { entities ->
            entities.map(converter::toDoc)
        }
    }

    override suspend fun getById(id: String): ReferenceDoc? {
        return dao.getById(id)?.let(converter::toDoc)
    }

    override suspend fun getByCampaign(campaignId: String): List<ReferenceDoc> {
        return dao.getByCampaign(campaignId).map(converter::toDoc)
    }

    override suspend fun insert(doc: ReferenceDoc) {
        dao.insert(converter.toEntity(doc))
    }

    override suspend fun update(doc: ReferenceDoc) {
        dao.update(converter.toEntity(doc))
    }

    override suspend fun delete(id: String) {
        dao.delete(id)
    }
}
