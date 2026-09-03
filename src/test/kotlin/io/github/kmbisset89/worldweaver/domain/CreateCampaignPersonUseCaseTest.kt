package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class CreateCampaignPersonUseCaseTest {
    @Test
    fun createRequiresActiveCampaign() = runTest {
        val harness = Harness()

        val result = harness.createPerson(name = "Aelar")

        assertIs<CreateCampaignPersonUseCase.Result.NoActiveCampaign>(result)
        assertTrue(harness.campaignPeople.all().isEmpty())
    }

    @Test
    fun createPersistsCampaignMonster() = runTest {
        val harness = Harness()
        harness.context.setActiveCampaignId("campaign-1")

        val result = harness.createPerson(name = "Owlbear", kind = PersonKind.Monster)

        val created = assertIs<CreateCampaignPersonUseCase.Result.Created>(result)
        assertEquals(PersonKind.Monster, created.person.kind)
        assertNull(created.person.worldPersonId)
        assertEquals(1, harness.campaignPeople.all().size)
    }

    @Test
    fun createPersistsPcWithClassLevelsSummingToLevel() = runTest {
        val harness = Harness()
        harness.context.setActiveCampaignId("campaign-1")
        val sheet = FifthEditionSheet.empty().copy(
            race = "Elf (High)",
            classLevels = listOf(
                ClassLevel("Fighter", "Champion", 2),
                ClassLevel("Rogue", "Thief", 1),
            ),
        )

        val result = harness.createPerson(
            name = "Aelar",
            kind = PersonKind.PlayerCharacter,
            sheet = sheet,
        )

        val created = assertIs<CreateCampaignPersonUseCase.Result.Created>(result)
        assertEquals("campaign-1", created.person.campaignId)
        assertNull(created.person.worldPersonId)
        val createdSheet = assertIs<FifthEditionSheet>(created.person.sheet)
        assertEquals(3, createdSheet.totalLevel())
        assertEquals(2, createdSheet.classLevels.size)
    }

    @Test
    fun campaignOnlyNpcIsNotStoredInWorldLibrary() = runTest {
        val harness = Harness()
        harness.context.setActiveCampaignId("campaign-1")

        val result = harness.createPerson(name = "Greta", kind = PersonKind.Npc)

        val created = assertIs<CreateCampaignPersonUseCase.Result.Created>(result)
        assertNull(created.person.worldPersonId)
        assertTrue(harness.worldPeople.all().isEmpty())
        assertEquals(1, harness.campaignPeople.all().size)
    }

    @Test
    fun createPersistsPathfinderSheet() = runTest {
        val harness = Harness()
        harness.context.setActiveCampaignId("campaign-1")
        val sheet = Pathfinder2ESheet.empty().copy(
            ancestry = "Human",
            className = "Champion",
            subclass = "Paladin",
            level = 2,
        )

        val result = harness.createPerson(
            name = "Iomedae",
            kind = PersonKind.PlayerCharacter,
            sheet = sheet,
        )

        val created = assertIs<CreateCampaignPersonUseCase.Result.Created>(result)
        val persisted = assertIs<Pathfinder2ESheet>(created.person.sheet)
        assertEquals("Human", persisted.ancestry)
        assertEquals("Champion", persisted.className)
        assertEquals(2, persisted.level)
        assertEquals(GameSystem.Pathfinder2E, persisted.gameSystem())
    }

    private class Harness {
        val worldPeople = FakeWorldPersonRepository()
        val campaignPeople = FakeCampaignPersonRepository()
        val context = FakeActiveContextRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T12:00:00Z") }
        private var nextId = 0
        private val ids = EntityIdFactory { "campaign-person-${++nextId}" }
        val createCampaignPerson = CreateCampaignPersonUseCase(campaignPeople, context, ids, instant)

        suspend fun createPerson(
            name: String,
            kind: PersonKind = PersonKind.PlayerCharacter,
            sheet: PersonSheet = FifthEditionSheet.empty(),
        ): CreateCampaignPersonUseCase.Result {
            return createCampaignPerson(
                CampaignPersonDraft(
                    kind = kind,
                    name = name,
                    description = "",
                    sheet = sheet,
                    overlayHitPoints = null,
                    overlayNotes = "",
                )
            )
        }
    }
}
