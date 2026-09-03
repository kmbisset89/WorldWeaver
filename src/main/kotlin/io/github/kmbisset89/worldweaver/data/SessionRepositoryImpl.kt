package io.github.kmbisset89.worldweaver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import io.github.kmbisset89.worldweaver.domain.Session
import io.github.kmbisset89.worldweaver.domain.SessionRepository

internal class SessionRepositoryImpl(
    private val sessionDao: SessionDao,
    private val sceneDao: SessionSceneDao,
    private val marchDao: SessionMarchEntryDao,
    private val converter: SessionEntityConverter,
) : SessionRepository {
    override fun observeByCampaign(campaignId: String): Flow<List<Session>> {
        return combine(
            sessionDao.observeByCampaign(campaignId),
            sceneDao.observeByCampaign(campaignId),
            marchDao.observeByCampaign(campaignId),
        ) { sessions, scenes, marchEntries ->
            assemble(sessions, scenes, marchEntries)
        }
    }

    override suspend fun getById(id: String): Session? {
        val entity = sessionDao.getById(id) ?: return null
        return converter.toSession(
            entity,
            converter.toScenes(sceneDao.getBySession(id)),
            converter.toMarchOrder(marchDao.getBySession(id)),
        )
    }

    override suspend fun getByCampaign(campaignId: String): List<Session> {
        return assemble(
            sessionDao.getByCampaign(campaignId),
            sceneDao.getByCampaign(campaignId),
            marchDao.getByCampaign(campaignId),
        )
    }

    override suspend fun search(query: String): List<Session> {
        return sessionDao.searchLike(query).map { entity ->
            converter.toSession(entity, emptyList(), emptyList())
        }
    }

    override suspend fun insert(session: Session) {
        sessionDao.insert(converter.toEntity(session))
        replaceChildren(session)
    }

    override suspend fun update(session: Session) {
        sessionDao.update(converter.toEntity(session))
        replaceChildren(session)
    }

    override suspend fun delete(id: String) {
        sessionDao.delete(id)
    }

    private suspend fun replaceChildren(session: Session) {
        sceneDao.deleteBySession(session.id)
        marchDao.deleteBySession(session.id)
        val scenes = converter.toSceneEntities(session)
        if (scenes.isNotEmpty()) {
            sceneDao.insertAll(scenes)
        }
        val marchEntries = converter.toMarchEntities(session)
        if (marchEntries.isNotEmpty()) {
            marchDao.insertAll(marchEntries)
        }
    }

    private fun assemble(
        sessions: List<SessionEntity>,
        scenes: List<SessionSceneEntity>,
        marchEntries: List<SessionMarchEntryEntity>,
    ): List<Session> {
        val scenesBySession = scenes.groupBy { it.sessionId }
        val marchBySession = marchEntries.groupBy { it.sessionId }
        return sessions.map { entity ->
            converter.toSession(
                entity,
                converter.toScenes(scenesBySession[entity.id].orEmpty()),
                converter.toMarchOrder(marchBySession[entity.id].orEmpty()),
            )
        }
    }
}
