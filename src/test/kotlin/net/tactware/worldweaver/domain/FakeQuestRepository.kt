package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

internal class FakeQuestRepository : QuestRepository {
    private val quests = MutableStateFlow<List<Quest>>(emptyList())

    fun all(): List<Quest> = quests.value

    override fun observeByCampaign(campaignId: String): Flow<List<Quest>> {
        return quests.map { list -> list.filter { it.campaignId == campaignId } }
    }

    override suspend fun getById(id: String): Quest? {
        return quests.value.firstOrNull { it.id == id }
    }

    override suspend fun getByCampaign(campaignId: String): List<Quest> {
        return quests.value.filter { it.campaignId == campaignId }
    }

    override suspend fun search(query: String): List<Quest> {
        return quests.value.filter { quest ->
            quest.title.contains(query, ignoreCase = true) ||
                quest.summary.contains(query, ignoreCase = true)
        }
    }

    override suspend fun insert(quest: Quest) {
        quests.value = quests.value + quest
    }

    override suspend fun update(quest: Quest) {
        quests.value = quests.value.map { current ->
            if (current.id == quest.id) quest else current
        }
    }

    override suspend fun delete(id: String) {
        quests.value = quests.value.filterNot { it.id == id }
    }

    override suspend fun deleteLinksByTarget(kind: QuestLinkKind, targetId: String) {
        quests.value = quests.value.map { quest ->
            quest.copy(links = quest.links.filterNot { it.kind == kind && it.targetId == targetId })
        }
    }
}
