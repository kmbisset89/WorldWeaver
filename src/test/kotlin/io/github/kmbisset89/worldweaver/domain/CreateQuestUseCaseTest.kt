package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class CreateQuestUseCaseTest {
    @Test
    fun createRequiresActiveCampaign() = runTest {
        val harness = Harness()

        val result = harness.createQuest(harness.draft())

        assertIs<CreateQuestUseCase.Result.NoActiveCampaign>(result)
        assertTrue(harness.quests.all().isEmpty())
    }

    @Test
    fun createRejectsBlankTitle() = runTest {
        val harness = Harness()
        harness.activateCampaign()

        val result = harness.createQuest(harness.draft(title = "  "))

        assertIs<CreateQuestUseCase.Result.InvalidTitle>(result)
    }

    @Test
    fun createStoresCampaignOwnedQuestWithObjectives() = runTest {
        val harness = Harness()
        harness.activateCampaign()

        val result = harness.createQuest(
            harness.draft(
                title = "Rescue the smith",
                summary = "Find the missing smith.",
                objectives = listOf(
                    QuestObjective(id = "", title = "Ask at the inn", status = QuestObjectiveStatus.Open),
                    QuestObjective(id = "", title = "  ", status = QuestObjectiveStatus.Open),
                    QuestObjective(id = "", title = "Search the woods", status = QuestObjectiveStatus.Complete),
                ),
            )
        )

        val created = assertIs<CreateQuestUseCase.Result.Created>(result)
        assertEquals("campaign-1", created.quest.campaignId)
        assertEquals("Rescue the smith", created.quest.title)
        assertEquals(QuestStatus.Active, created.quest.status)
        assertEquals(2, created.quest.objectives.size)
        assertEquals("Ask at the inn", created.quest.objectives[0].title)
        assertEquals(QuestObjectiveStatus.Complete, created.quest.objectives[1].status)
        assertTrue(created.quest.objectives[0].id.isNotBlank())
        assertNull(created.quest.locationId)
    }

    @Test
    fun createAttachesValidLocationAndDropsBrokenLinks() = runTest {
        val harness = Harness()
        harness.activateCampaign()
        val location = harness.insertLocation("Waterdeep")
        val lore = harness.insertLore("lore-1", "Old Myth")

        val result = harness.createQuest(
            harness.draft(
                locationId = location.id,
                links = listOf(
                    QuestLink(id = "", kind = QuestLinkKind.LORE, targetId = lore.id),
                    QuestLink(id = "", kind = QuestLinkKind.LORE, targetId = "missing"),
                    QuestLink(id = "", kind = QuestLinkKind.SESSION, targetId = "no-session"),
                ),
            )
        )

        val created = assertIs<CreateQuestUseCase.Result.Created>(result)
        assertEquals(location.id, created.quest.locationId)
        assertEquals(1, created.quest.links.size)
        assertEquals(lore.id, created.quest.links[0].targetId)
    }

    @Test
    fun createRejectsLocationFromAnotherWorld() = runTest {
        val harness = Harness()
        harness.activateCampaign()
        val other = harness.insertLocation("Baldur's Gate", worldId = "world-2")

        val result = harness.createQuest(harness.draft(locationId = other.id))

        assertIs<CreateQuestUseCase.Result.InvalidLocation>(result)
    }

    private class Harness {
        val quests = FakeQuestRepository()
        val campaigns = FakeCampaignRepository()
        val locations = FakeLocationRepository()
        val lore = FakeLoreRepository()
        val worldPeople = FakeWorldPersonRepository()
        val campaignPeople = FakeCampaignPersonRepository()
        val sessions = FakeSessionRepository()
        val context = FakeActiveContextRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T12:00:00Z") }
        private var nextId = 0
        private val ids = EntityIdFactory { "quest-${++nextId}" }
        val createQuest = CreateQuestUseCase(
            quests,
            campaigns,
            locations,
            lore,
            worldPeople,
            campaignPeople,
            sessions,
            context,
            ids,
            instant,
        )

        suspend fun activateCampaign() {
            context.setActiveWorldId("world-1")
            context.setActiveCampaignId("campaign-1")
            campaigns.insert(sampleCampaign())
        }

        fun draft(
            title: String = "Title",
            summary: String = "Summary",
            status: QuestStatus = QuestStatus.Active,
            locationId: String? = null,
            objectives: List<QuestObjective> = emptyList(),
            links: List<QuestLink> = emptyList(),
        ): QuestDraft {
            return QuestDraft(
                title = title,
                summary = summary,
                status = status,
                locationId = locationId,
                objectives = objectives,
                links = links,
            )
        }

        suspend fun insertLocation(
            name: String,
            worldId: String = "world-1",
        ): Location {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            val location = Location(
                id = "loc-${name.lowercase()}",
                worldId = worldId,
                type = LocationType.City,
                parentLocationId = null,
                name = name,
                description = "",
                climate = "",
                terrain = "",
                government = "",
                landmarks = emptyList(),
                history = "",
                notes = "",
                createdAt = now,
                updatedAt = now,
            )
            locations.insert(location)
            return location
        }

        suspend fun insertLore(id: String, title: String): Lore {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            val entry = Lore(
                id = id,
                worldId = "world-1",
                title = title,
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

        private fun sampleCampaign(): Campaign {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            return Campaign(
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
        }
    }
}
