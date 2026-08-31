package net.tactware.worldweaver.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import net.tactware.worldweaver.domain.Quest
import net.tactware.worldweaver.domain.QuestLinkKind
import net.tactware.worldweaver.domain.QuestRepository

internal class QuestRepositoryImpl(
    private val questDao: QuestDao,
    private val objectiveDao: QuestObjectiveDao,
    private val linkDao: QuestLinkDao,
    private val converter: QuestEntityConverter,
) : QuestRepository {
    override fun observeByCampaign(campaignId: String): Flow<List<Quest>> {
        return combine(
            questDao.observeByCampaign(campaignId),
            objectiveDao.observeByCampaign(campaignId),
            linkDao.observeByCampaign(campaignId),
        ) { quests, objectives, links ->
            assemble(quests, objectives, links)
        }
    }

    override suspend fun getById(id: String): Quest? {
        val entity = questDao.getById(id) ?: return null
        return converter.toQuest(
            entity,
            converter.toObjectives(objectiveDao.getByQuest(id)),
            converter.toLinks(linkDao.getByQuest(id)),
        )
    }

    override suspend fun getByCampaign(campaignId: String): List<Quest> {
        return assemble(
            questDao.getByCampaign(campaignId),
            objectiveDao.getByCampaign(campaignId),
            linkDao.getByCampaign(campaignId),
        )
    }

    override suspend fun search(query: String): List<Quest> {
        return questDao.searchLike(query).map { entity ->
            converter.toQuest(entity, emptyList(), emptyList())
        }
    }

    override suspend fun insert(quest: Quest) {
        questDao.insert(converter.toEntity(quest))
        replaceChildren(quest)
    }

    override suspend fun update(quest: Quest) {
        questDao.update(converter.toEntity(quest))
        replaceChildren(quest)
    }

    override suspend fun delete(id: String) {
        questDao.delete(id)
    }

    override suspend fun deleteLinksByTarget(kind: QuestLinkKind, targetId: String) {
        linkDao.deleteByTarget(kind.name, targetId)
    }

    private suspend fun replaceChildren(quest: Quest) {
        objectiveDao.deleteByQuest(quest.id)
        linkDao.deleteByQuest(quest.id)
        val objectives = converter.toObjectiveEntities(quest)
        if (objectives.isNotEmpty()) {
            objectiveDao.insertAll(objectives)
        }
        val links = converter.toLinkEntities(quest)
        if (links.isNotEmpty()) {
            linkDao.insertAll(links)
        }
    }

    private fun assemble(
        quests: List<QuestEntity>,
        objectives: List<QuestObjectiveEntity>,
        links: List<QuestLinkEntity>,
    ): List<Quest> {
        val objectivesByQuest = objectives.groupBy { it.questId }
        val linksByQuest = links.groupBy { it.questId }
        return quests.map { entity ->
            converter.toQuest(
                entity,
                converter.toObjectives(objectivesByQuest[entity.id].orEmpty()),
                converter.toLinks(linksByQuest[entity.id].orEmpty()),
            )
        }
    }
}
