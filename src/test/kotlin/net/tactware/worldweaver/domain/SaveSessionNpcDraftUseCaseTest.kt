package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class SaveSessionNpcDraftUseCaseTest {
    @Test
    fun saveToWorldWritesLibraryNpc() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")

        val result = harness.save(
            RandomNpcDraft(name = "Cora", race = "Human", abilityScores = AbilityScores.average()),
            SessionNpcDraftDestination.WorldLibrary,
        )

        val saved = assertIs<SaveSessionNpcDraftUseCase.Result.SavedToWorld>(result)
        assertEquals("world-1", saved.person.worldId)
        assertEquals(PersonKind.Npc, saved.person.kind)
        assertEquals(1, harness.worldPeople.all().size)
        assertTrue(harness.campaignPeople.all().isEmpty())
    }

    @Test
    fun saveToCampaignStaysOffWorldLibrary() = runTest {
        val harness = Harness()
        harness.context.setActiveCampaignId("campaign-1")

        val result = harness.save(
            RandomNpcDraft(name = "Dara", race = "Elf", abilityScores = AbilityScores.average()),
            SessionNpcDraftDestination.CampaignOnly,
        )

        val saved = assertIs<SaveSessionNpcDraftUseCase.Result.SavedToCampaign>(result)
        assertEquals("campaign-1", saved.person.campaignId)
        assertEquals(PersonKind.Npc, saved.person.kind)
        assertTrue(harness.worldPeople.all().isEmpty())
        assertEquals(1, harness.campaignPeople.all().size)
    }

    private class Harness {
        val worldPeople = FakeWorldPersonRepository()
        val campaignPeople = FakeCampaignPersonRepository()
        val context = FakeActiveContextRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T12:00:00Z") }
        private var nextId = 0
        private val ids = EntityIdFactory { "person-${++nextId}" }
        val save = SaveSessionNpcDraftUseCase(
            CreateWorldPersonUseCase(worldPeople, context, ids, instant),
            CreateCampaignPersonUseCase(campaignPeople, context, ids, instant),
        )
    }
}
