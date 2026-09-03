package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakePlotThreadRepository : PlotThreadRepository {
    private val threads = MutableStateFlow<List<PlotThread>>(emptyList())

    fun all(): List<PlotThread> = threads.value

    override fun observeByCampaign(campaignId: String): Flow<List<PlotThread>> {
        return threads.map { list -> list.filter { it.campaignId == campaignId } }
    }

    override suspend fun getById(id: String): PlotThread? {
        return threads.value.firstOrNull { it.id == id }
    }

    override suspend fun getByCampaign(campaignId: String): List<PlotThread> {
        return threads.value.filter { it.campaignId == campaignId }
    }

    override suspend fun insert(thread: PlotThread) {
        threads.value = threads.value + thread
    }

    override suspend fun update(thread: PlotThread) {
        threads.value = threads.value.map { current ->
            if (current.id == thread.id) thread else current
        }
    }

    override suspend fun delete(id: String) {
        threads.value = threads.value.filterNot { it.id == id }
    }
}
