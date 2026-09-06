package io.github.kmbisset89.worldweaver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import io.github.kmbisset89.worldweaver.domain.SessionClock
import io.github.kmbisset89.worldweaver.domain.SessionClockRepository

internal class SessionClockRepositoryImpl(
    private val dao: SessionClockDao,
    private val converter: SessionClockEntityConverter,
) : SessionClockRepository {
    override fun observeBySession(sessionId: String): Flow<List<SessionClock>> {
        return dao.observeBySession(sessionId).map { entities ->
            entities.map(converter::toClock)
        }
    }

    override suspend fun getById(id: String): SessionClock? {
        return dao.getById(id)?.let(converter::toClock)
    }

    override suspend fun getBySession(sessionId: String): List<SessionClock> {
        return dao.getBySession(sessionId).map(converter::toClock)
    }

    override suspend fun getByCampaign(campaignId: String): List<SessionClock> {
        return dao.getByCampaign(campaignId).map(converter::toClock)
    }

    override suspend fun insert(clock: SessionClock) {
        dao.insert(converter.toEntity(clock))
    }

    override suspend fun update(clock: SessionClock) {
        dao.update(converter.toEntity(clock))
    }

    override suspend fun delete(id: String) {
        dao.delete(id)
    }
}
