package io.github.kmbisset89.worldweaver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import io.github.kmbisset89.worldweaver.domain.PlotThread
import io.github.kmbisset89.worldweaver.domain.PlotThreadRepository

internal class PlotThreadRepositoryImpl(
    private val dao: PlotThreadDao,
    private val converter: PlotThreadEntityConverter,
) : PlotThreadRepository {
    override fun observeByCampaign(campaignId: String): Flow<List<PlotThread>> {
        return dao.observeByCampaign(campaignId).map { entities ->
            entities.map(converter::toThread)
                .sortedWith(
                    compareByDescending<PlotThread> { it.priority.sortValue }
                        .thenBy { it.title.lowercase() },
                )
        }
    }

    override suspend fun getById(id: String): PlotThread? {
        return dao.getById(id)?.let(converter::toThread)
    }

    override suspend fun getByCampaign(campaignId: String): List<PlotThread> {
        return dao.getByCampaign(campaignId)
            .map(converter::toThread)
            .sortedWith(
                compareByDescending<PlotThread> { it.priority.sortValue }
                    .thenBy { it.title.lowercase() },
            )
    }

    override suspend fun insert(thread: PlotThread) {
        dao.insert(converter.toEntity(thread))
    }

    override suspend fun update(thread: PlotThread) {
        dao.update(converter.toEntity(thread))
    }

    override suspend fun delete(id: String) {
        dao.delete(id)
    }
}
