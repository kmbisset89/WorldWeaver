package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeSessionRepository : SessionRepository {
    private val sessions = MutableStateFlow<List<Session>>(emptyList())

    fun all(): List<Session> = sessions.value

    override fun observeByCampaign(campaignId: String): Flow<List<Session>> {
        return sessions.map { list -> list.filter { it.campaignId == campaignId } }
    }

    override suspend fun getById(id: String): Session? {
        return sessions.value.firstOrNull { it.id == id }
    }

    override suspend fun getByCampaign(campaignId: String): List<Session> {
        return sessions.value.filter { it.campaignId == campaignId }
    }

    override suspend fun search(query: String): List<Session> {
        return sessions.value.filter { session ->
            session.name.contains(query, ignoreCase = true) ||
                session.notes.contains(query, ignoreCase = true)
        }
    }

    override suspend fun insert(session: Session) {
        sessions.value = sessions.value + session
    }

    override suspend fun update(session: Session) {
        sessions.value = sessions.value.map { current ->
            if (current.id == session.id) session else current
        }
    }

    override suspend fun delete(id: String) {
        sessions.value = sessions.value.filterNot { it.id == id }
    }
}
