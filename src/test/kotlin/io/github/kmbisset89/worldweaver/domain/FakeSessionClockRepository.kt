package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeSessionClockRepository : SessionClockRepository {
    private val clocks = MutableStateFlow<List<SessionClock>>(emptyList())

    fun all(): List<SessionClock> = clocks.value

    override fun observeBySession(sessionId: String): Flow<List<SessionClock>> {
        return clocks.map { list ->
            list.filter { it.sessionId == sessionId }.sortedBy { it.sortIndex }
        }
    }

    override suspend fun getById(id: String): SessionClock? {
        return clocks.value.firstOrNull { it.id == id }
    }

    override suspend fun getBySession(sessionId: String): List<SessionClock> {
        return clocks.value.filter { it.sessionId == sessionId }.sortedBy { it.sortIndex }
    }

    override suspend fun getByCampaign(campaignId: String): List<SessionClock> {
        return clocks.value.sortedWith(compareBy({ it.sessionId }, { it.sortIndex }))
    }

    override suspend fun insert(clock: SessionClock) {
        clocks.value = clocks.value + clock
    }

    override suspend fun update(clock: SessionClock) {
        clocks.value = clocks.value.map { current ->
            if (current.id == clock.id) clock else current
        }
    }

    override suspend fun delete(id: String) {
        clocks.value = clocks.value.filterNot { it.id == id }
    }
}
