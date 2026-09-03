package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class CreateOneShotUseCaseTest {
    @Test
    fun createPersistsWorldCampaignPlacesPeopleQuestSessionAndEncounter() = runTest {
        val harness = Harness()
        val draft = sampleDraft(includeEncounter = true)

        val result = harness.createOneShot(draft)

        val created = assertIs<CreateOneShotUseCase.Result.Created>(result)
        assertEquals("Ashfen", created.world.name)
        assertEquals(GameSystem.FifthEdition, created.world.defaultGameSystem)
        assertEquals("Night Watch", created.campaign.name)
        assertEquals(created.world.id, created.campaign.worldId)
        assertEquals("Session 1", created.session.name)
        assertEquals(3, created.session.scenes.size)

        val locations = harness.locations.all()
        assertEquals(5, locations.size)
        val continent = locations.single { it.type == LocationType.Continent }
        val region = locations.single { it.type == LocationType.Area }
        val city = locations.single { it.type == LocationType.City }
        val places = locations.filter { it.type == LocationType.Place }
        assertNull(continent.parentLocationId)
        assertEquals(continent.id, region.parentLocationId)
        assertEquals(region.id, city.parentLocationId)
        assertTrue(places.all { it.parentLocationId == city.id })
        assertEquals(setOf("The Crooked Lantern", "The Cellar Vault"), places.map { it.name }.toSet())

        val people = harness.worldPeople.all()
        assertEquals(setOf("Mira", "Rook"), people.map { it.name }.toSet())
        assertEquals(1, harness.factions.all().size)
        assertEquals("The Tollmen", harness.factions.all().single().name)
        val lore = harness.lore.all().single()
        assertEquals("Night Watch — Premise", lore.title)
        assertEquals("They were never missing", lore.secrets.single().secret)

        val quest = harness.quests.all().single()
        assertEquals("Find the missing watch", quest.title)
        assertEquals(2, quest.objectives.size)
        assertTrue(quest.links.any { it.kind == QuestLinkKind.LORE })
        assertTrue(quest.links.any { it.kind == QuestLinkKind.WORLD_PERSON })

        val encounter = harness.encounters.all().single()
        assertEquals("Cellar fight", encounter.name)
        assertEquals(EncounterDifficulty.Hard, encounter.difficulty)
        assertEquals(1, encounter.participants.size)
        assertEquals(EncounterParticipantSource.WorldPerson, encounter.participants.single().source)

        assertEquals(created.world.id, harness.context.get().activeWorldId)
        assertEquals(created.campaign.id, harness.context.get().activeCampaignId)
        assertEquals(created.session.id, harness.context.get().activeSessionId)
        assertEquals(1, harness.calendars.all().size)
    }

    @Test
    fun createRejectsBlankWorldNameBeforeInserting() = runTest {
        val harness = Harness()

        val result = harness.createOneShot(sampleDraft().copy(worldName = "  "))

        assertIs<CreateOneShotUseCase.Result.Failed>(result)
        assertEquals(CreateOneShotUseCase.Step.World, result.step)
        assertTrue(harness.worlds.all().isEmpty())
        assertTrue(harness.campaigns.all().isEmpty())
    }

    @Test
    fun createRejectsEmptySiteList() = runTest {
        val harness = Harness()

        val result = harness.createOneShot(sampleDraft().copy(sites = emptyList()))

        val failed = assertIs<CreateOneShotUseCase.Result.Failed>(result)
        assertEquals(CreateOneShotUseCase.Step.Places, failed.step)
        assertTrue(harness.worlds.all().isEmpty())
    }

    @Test
    fun createOmitsOptionalEncounter() = runTest {
        val harness = Harness()

        val result = harness.createOneShot(sampleDraft(includeEncounter = false))

        assertIs<CreateOneShotUseCase.Result.Created>(result)
        assertTrue(harness.encounters.all().isEmpty())
        assertEquals(1, harness.sessions.all().size)
    }

    private fun sampleDraft(includeEncounter: Boolean = true): OneShotDraft {
        return OneShotDraft(
            worldName = "Ashfen",
            worldDescription = "A worn border.",
            gameSystem = GameSystem.FifthEdition,
            campaignName = "Night Watch",
            campaignDescription = "Someone is missing.",
            campaignNotes = "The watch cannot spare another night.",
            realmName = "The Shattered Marches",
            realmDescription = "A worn world.",
            regionName = "Ashfen",
            regionClimate = "Cold and wet",
            regionTerrain = "Bogs",
            settlementName = "Hollowford",
            settlementDescription = "",
            sites = listOf(
                OneShotDraft.Site(
                    name = "The Crooked Lantern",
                    description = "A packed common room.",
                    role = OneShotDraft.Site.Role.Opening,
                ),
                OneShotDraft.Site(
                    name = "The Cellar Vault",
                    description = "Barrels and a hidden hatch.",
                    role = OneShotDraft.Site.Role.Climax,
                ),
            ),
            people = listOf(
                OneShotDraft.Person(
                    name = "Mira",
                    description = "The worried sergeant.",
                    kind = PersonKind.Npc,
                    role = OneShotDraft.Person.Role.Patron,
                ),
                OneShotDraft.Person(
                    name = "Rook",
                    description = "A road-captain.",
                    kind = PersonKind.Npc,
                    role = OneShotDraft.Person.Role.Villain,
                ),
            ),
            faction = OneShotDraft.Faction(
                name = "The Tollmen",
                description = "They tax the road.",
                goals = "Keep the night quiet.",
            ),
            loreTitle = "Night Watch — Premise",
            loreContent = "Someone is missing from the watch.",
            loreSecretTitle = "The twist",
            loreSecret = "They were never missing",
            questTitle = "Find the missing watch",
            questSummary = "Bring them home.",
            questObjectives = listOf("Follow the last patrol", "Confront the captain"),
            sessionName = "Session 1",
            sessionNotes = "Start at the lantern.",
            scenes = listOf(
                OneShotDraft.Scene(title = "The Crooked Lantern", notes = "Ask around."),
                OneShotDraft.Scene(title = "The turning point", notes = ""),
                OneShotDraft.Scene(title = "The Cellar Vault", notes = "Fight."),
            ),
            encounterName = if (includeEncounter) "Cellar fight" else null,
            encounterDifficulty = if (includeEncounter) EncounterDifficulty.Hard else null,
        )
    }

    private class Harness {
        val worlds = FakeWorldRepository()
        val calendars = FakeWorldCalendarRepository()
        val campaigns = FakeCampaignRepository()
        val locations = FakeLocationRepository()
        val worldPeople = FakeWorldPersonRepository()
        val campaignPeople = FakeCampaignPersonRepository()
        val factions = FakeFactionRepository()
        val lore = FakeLoreRepository()
        val quests = FakeQuestRepository()
        val sessions = FakeSessionRepository()
        val encounters = FakeEncounterRepository()
        val battleMaps = FakeBattleMapRepository()
        val context = FakeActiveContextRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T12:00:00Z") }
        private var nextId = 0
        private val ids = EntityIdFactory { "id-${++nextId}" }
        private val setActiveWorld = SetActiveWorldUseCase(worlds, campaigns, context, instant)
        private val setActiveCampaign = SetActiveCampaignUseCase(campaigns, context)
        val createOneShot = CreateOneShotUseCase(
            createWorld = CreateWorldUseCase(
                worlds,
                calendars,
                DefaultWorldCalendarFactory(ids),
                ids,
                instant,
                setActiveWorld,
            ),
            createCampaign = CreateCampaignUseCase(
                campaigns,
                context,
                ids,
                instant,
                setActiveCampaign,
            ),
            createLocation = CreateLocationUseCase(locations, context, ids, instant),
            createWorldPerson = CreateWorldPersonUseCase(worldPeople, context, ids, instant),
            createFaction = CreateFactionUseCase(factions, context, ids, instant),
            createLore = CreateLoreUseCase(lore, locations, context, ids, instant),
            createQuest = CreateQuestUseCase(
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
            ),
            createSession = CreateSessionUseCase(
                sessions,
                campaigns,
                calendars,
                context,
                ids,
                instant,
            ),
            createEncounter = CreateEncounterUseCase(
                encounters,
                campaigns,
                locations,
                battleMaps,
                worldPeople,
                campaignPeople,
                context,
                ids,
                instant,
            ),
        )
    }
}
