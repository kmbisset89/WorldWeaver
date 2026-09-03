package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class UpdateQuestUseCaseTest {
    @Test
    fun updateCanCompleteQuestAndMarkObjectivesFailed() = runTest {
        val harness = Harness()
        val quest = harness.insertQuest()

        val result = harness.updateQuest(
            quest.id,
            QuestDraft(
                title = quest.title,
                summary = quest.summary,
                status = QuestStatus.Completed,
                locationId = null,
                objectives = listOf(
                    QuestObjective(
                        id = "obj-1",
                        title = "Find the key",
                        status = QuestObjectiveStatus.Failed,
                    )
                ),
                links = emptyList(),
            ),
        )

        assertIs<UpdateQuestUseCase.Result.Updated>(result)
        val updated = harness.quests.getById(quest.id)
        assertEquals(QuestStatus.Completed, updated?.status)
        assertEquals(QuestObjectiveStatus.Failed, updated?.objectives?.single()?.status)
    }

    @Test
    fun updateRejectsBlankTitle() = runTest {
        val harness = Harness()
        val quest = harness.insertQuest()

        val result = harness.updateQuest(
            quest.id,
            QuestDraft(
                title = " ",
                summary = quest.summary,
                status = quest.status,
                locationId = null,
                objectives = emptyList(),
                links = emptyList(),
            ),
        )

        assertIs<UpdateQuestUseCase.Result.InvalidTitle>(result)
    }

    private class Harness {
        val quests = FakeQuestRepository()
        val campaigns = FakeCampaignRepository()
        val locations = FakeLocationRepository()
        val lore = FakeLoreRepository()
        val worldPeople = FakeWorldPersonRepository()
        val campaignPeople = FakeCampaignPersonRepository()
        val sessions = FakeSessionRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T13:00:00Z") }
        private var nextId = 0
        private val ids = EntityIdFactory { "quest-update-${++nextId}" }
        val updateQuest = UpdateQuestUseCase(
            quests,
            campaigns,
            locations,
            lore,
            worldPeople,
            campaignPeople,
            sessions,
            ids,
            instant,
        )

        suspend fun insertQuest(): Quest {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            campaigns.insert(
                Campaign(
                    id = "campaign-1",
                    worldId = "world-1",
                    name = "Lost Mine",
                    description = "",
                    notes = "",
                    gameSystem = GameSystem.FifthEdition,
                    status = CampaignStatus.Active,
                    createdAt = now,
                    updatedAt = now,
                )
            )
            val quest = Quest(
                id = "quest-1",
                campaignId = "campaign-1",
                title = "Rescue the smith",
                summary = "Find the missing smith.",
                status = QuestStatus.Active,
                locationId = null,
                objectives = listOf(
                    QuestObjective(
                        id = "obj-1",
                        title = "Find the key",
                        status = QuestObjectiveStatus.Open,
                    )
                ),
                links = emptyList(),
                createdAt = now,
                updatedAt = now,
            )
            quests.insert(quest)
            return quest
        }
    }
}
