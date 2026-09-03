package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class DeleteQuestUseCaseTest {
    @Test
    fun deleteRemovesQuest() = runTest {
        val harness = Harness()
        val quest = harness.insertQuest()

        val result = harness.deleteQuest(quest.id)

        assertIs<DeleteQuestUseCase.Result.Deleted>(result)
        assertTrue(harness.quests.all().isEmpty())
    }

    @Test
    fun deleteReturnsNotFound() = runTest {
        val harness = Harness()

        val result = harness.deleteQuest("missing")

        assertIs<DeleteQuestUseCase.Result.NotFound>(result)
    }

    @Test
    fun deleteLoreStripsInboundQuestLinks() = runTest {
        val harness = Harness()
        val lore = harness.insertLore()
        harness.insertQuest(
            links = listOf(QuestLink(id = "link-1", kind = QuestLinkKind.LORE, targetId = lore.id))
        )

        val result = harness.deleteLore(lore.id)

        assertIs<DeleteLoreUseCase.Result.Deleted>(result)
        assertTrue(harness.quests.all().single().links.isEmpty())
    }

    private class Harness {
        val quests = FakeQuestRepository()
        val lore = FakeLoreRepository()
        val deleteQuest = DeleteQuestUseCase(quests)
        val deleteLore = DeleteLoreUseCase(lore, quests)

        suspend fun insertQuest(
            links: List<QuestLink> = emptyList(),
        ): Quest {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            val quest = Quest(
                id = "quest-1",
                campaignId = "campaign-1",
                title = "Rescue",
                summary = "",
                status = QuestStatus.Active,
                locationId = null,
                objectives = emptyList(),
                links = links,
                createdAt = now,
                updatedAt = now,
            )
            quests.insert(quest)
            return quest
        }

        suspend fun insertLore(): Lore {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            val entry = Lore(
                id = "lore-1",
                worldId = "world-1",
                title = "Myth",
                content = "Body",
                category = LoreCategory.Myth,
                tags = emptyList(),
                relatedEntryIds = emptyList(),
                secrets = emptyList(),
                locationId = null,
                characterId = null,
                createdAt = now,
                updatedAt = now,
            )
            lore.insert(entry)
            return entry
        }
    }
}
