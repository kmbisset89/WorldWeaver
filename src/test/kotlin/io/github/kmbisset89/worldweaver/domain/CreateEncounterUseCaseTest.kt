package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class CreateEncounterUseCaseTest {
    @Test
    fun createRequiresActiveCampaign() = runTest {
        val harness = Harness()

        val result = harness.createEncounter(harness.draft())

        assertIs<CreateEncounterUseCase.Result.NoActiveCampaign>(result)
        assertTrue(harness.encounters.all().isEmpty())
    }

    @Test
    fun createRejectsBlankName() = runTest {
        val harness = Harness()
        harness.activateCampaign()

        val result = harness.createEncounter(harness.draft(name = "  "))

        assertIs<CreateEncounterUseCase.Result.InvalidName>(result)
    }

    @Test
    fun createStoresCampaignOwnedEncounterWithNamelessParticipant() = runTest {
        val harness = Harness()
        harness.activateCampaign()

        val result = harness.createEncounter(
            harness.draft(
                name = "Ambush",
                notes = "Roadside.",
                participants = listOf(
                    harness.nameless(name = "Goblin", groupCount = 4),
                    harness.nameless(name = "  ", groupCount = 1),
                ),
            )
        )

        val created = assertIs<CreateEncounterUseCase.Result.Created>(result)
        assertEquals("campaign-1", created.encounter.campaignId)
        assertEquals("Ambush", created.encounter.name)
        assertEquals(EncounterStatus.Planned, created.encounter.status)
        assertEquals(1, created.encounter.currentRound)
        assertEquals(1, created.encounter.participants.size)
        assertEquals("Goblin", created.encounter.participants[0].name)
        assertEquals(4, created.encounter.participants[0].groupCount)
        assertEquals(EncounterParticipantSource.Nameless, created.encounter.participants[0].source)
        assertNull(created.encounter.participants[0].sourceId)
        assertTrue(created.encounter.participants[0].id.isNotBlank())
    }

    @Test
    fun createKeepsLinkedParticipantReferenceAndDropsBrokenOnes() = runTest {
        val harness = Harness()
        harness.activateCampaign()
        val npc = harness.insertWorldPerson("Bram")
        val pc = harness.insertCampaignPerson("Aelar")

        val result = harness.createEncounter(
            harness.draft(
                participants = listOf(
                    harness.linkedWorld(npc.id, "Bram"),
                    harness.linkedWorld("missing", "Ghost"),
                    harness.linkedCampaign(pc.id, "Aelar"),
                ),
            )
        )

        val created = assertIs<CreateEncounterUseCase.Result.Created>(result)
        assertEquals(2, created.encounter.participants.size)
        assertEquals(npc.id, created.encounter.participants[0].sourceId)
        assertEquals(EncounterParticipantSource.WorldPerson, created.encounter.participants[0].source)
        assertEquals(pc.id, created.encounter.participants[1].sourceId)
    }

    @Test
    fun createRejectsLocationFromAnotherWorld() = runTest {
        val harness = Harness()
        harness.activateCampaign()
        val other = harness.insertLocation("Baldur's Gate", worldId = "world-2")

        val result = harness.createEncounter(harness.draft(locationId = other.id))

        assertIs<CreateEncounterUseCase.Result.InvalidLocation>(result)
    }

    @Test
    fun createAttachesValidLocation() = runTest {
        val harness = Harness()
        harness.activateCampaign()
        val location = harness.insertLocation("Waterdeep")

        val result = harness.createEncounter(harness.draft(locationId = location.id))

        val created = assertIs<CreateEncounterUseCase.Result.Created>(result)
        assertEquals(location.id, created.encounter.locationId)
    }

    @Test
    fun createRejectsBattleMapFromAnotherCampaign() = runTest {
        val harness = Harness()
        harness.activateCampaign()
        val other = harness.insertBattleMap(name = "Other map", campaignId = "campaign-2")

        val result = harness.createEncounter(harness.draft(battleMapId = other.id))

        assertIs<CreateEncounterUseCase.Result.InvalidBattleMap>(result)
    }

    @Test
    fun createAttachesValidBattleMap() = runTest {
        val harness = Harness()
        harness.activateCampaign()
        val battleMap = harness.insertBattleMap(name = "Cave")

        val result = harness.createEncounter(harness.draft(battleMapId = battleMap.id))

        val created = assertIs<CreateEncounterUseCase.Result.Created>(result)
        assertEquals(battleMap.id, created.encounter.battleMapId)
    }

    private class Harness {
        val encounters = FakeEncounterRepository()
        val campaigns = FakeCampaignRepository()
        val locations = FakeLocationRepository()
        val battleMaps = FakeBattleMapRepository()
        val worldPeople = FakeWorldPersonRepository()
        val campaignPeople = FakeCampaignPersonRepository()
        val context = FakeActiveContextRepository()
        private val instant = InstantProvider { Instant.parse("2026-08-29T12:00:00Z") }
        private var nextId = 0
        private val ids = EntityIdFactory { "encounter-${++nextId}" }
        val createEncounter = CreateEncounterUseCase(
            encounters,
            campaigns,
            locations,
            battleMaps,
            worldPeople,
            campaignPeople,
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
            name: String = "Fight",
            locationId: String? = null,
            battleMapId: String? = null,
            difficulty: EncounterDifficulty = EncounterDifficulty.Medium,
            notes: String = "",
            participants: List<EncounterParticipant> = emptyList(),
        ): EncounterDraft {
            return EncounterDraft(
                name = name,
                locationId = locationId,
                battleMapId = battleMapId,
                difficulty = difficulty,
                notes = notes,
                outcomeNote = "",
                participants = participants,
            )
        }

        fun nameless(
            name: String,
            groupCount: Int = 1,
        ): EncounterParticipant {
            return EncounterParticipant(
                id = "",
                name = name,
                source = EncounterParticipantSource.Nameless,
                sourceId = null,
                initiativeRoll = null,
                initiativeBonus = 0,
                armorClass = 13,
                hitPoints = 7,
                maxHitPoints = 7,
                temporaryHitPoints = 0,
                conditions = emptyList(),
                groupCount = groupCount,
                combatState = CombatState.Conscious,
            )
        }

        fun linkedWorld(sourceId: String, name: String): EncounterParticipant {
            return nameless(name).copy(
                source = EncounterParticipantSource.WorldPerson,
                sourceId = sourceId,
            )
        }

        fun linkedCampaign(sourceId: String, name: String): EncounterParticipant {
            return nameless(name).copy(
                source = EncounterParticipantSource.CampaignPerson,
                sourceId = sourceId,
            )
        }

        suspend fun insertBattleMap(
            name: String,
            campaignId: String = "campaign-1",
        ): BattleMap {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            val battleMap = BattleMap(
                id = "map-${name.lowercase()}",
                campaignId = campaignId,
                name = name,
                originalWidth = 512,
                originalHeight = 512,
                tileSizePx = 256,
                minZoom = 0,
                maxZoom = 0,
                createdAt = now,
                updatedAt = now,
            )
            battleMaps.insert(battleMap)
            return battleMap
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

        suspend fun insertWorldPerson(name: String): WorldPerson {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            val person = WorldPerson(
                id = "world-person-$name",
                worldId = "world-1",
                kind = PersonKind.Npc,
                name = name,
                description = "",
                sheet = FifthEditionSheet.empty(),
                createdAt = now,
                updatedAt = now,
            )
            worldPeople.insert(person)
            return person
        }

        suspend fun insertCampaignPerson(name: String): CampaignPerson {
            val now = Instant.parse("2026-08-29T12:00:00Z")
            val person = CampaignPerson(
                id = "campaign-person-$name",
                campaignId = "campaign-1",
                worldPersonId = null,
                kind = PersonKind.PlayerCharacter,
                name = name,
                description = "",
                sheet = FifthEditionSheet.empty(),
                overlayHitPoints = null,
                overlayNotes = "",
                createdAt = now,
                updatedAt = now,
            )
            campaignPeople.insert(person)
            return person
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
