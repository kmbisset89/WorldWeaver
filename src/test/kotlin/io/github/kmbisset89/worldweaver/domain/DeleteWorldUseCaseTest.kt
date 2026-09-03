package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class DeleteWorldUseCaseTest {
    @Test
    fun deleteIsBlockedWhenCampaignsExist() = runTest {
        val harness = Harness()
        val world = harness.insertWorld("Faerun")
        harness.insertCampaign(world.id, "Icewind Dale")

        val result = harness.deleteWorld(world.id)

        val blocked = assertIs<DeleteWorldUseCase.Result.Blocked>(result)
        assertEquals(1, blocked.campaignCount)
        assertEquals(world.id, harness.worlds.getById(world.id)?.id)
        assertEquals(1, harness.campaigns.all().size)
    }

    @Test
    fun deleteRemovesWorldWithoutCampaignsAndClearsContext() = runTest {
        val harness = Harness()
        val world = harness.insertWorld("Faerun")
        harness.context.setActiveWorldId(world.id)

        val result = harness.deleteWorld(world.id)

        assertIs<DeleteWorldUseCase.Result.Deleted>(result)
        assertTrue(harness.worlds.all().isEmpty())
        assertNull(harness.context.get().activeWorldId)
        assertNull(harness.context.get().activeCampaignId)
    }

    @Test
    fun deleteRemovesWorldScopedPeopleFactionsAndLinks() = runTest {
        val harness = Harness()
        val world = harness.insertWorld("Faerun")
        val person = harness.insertWorldPerson(world.id, "Bram")
        val other = harness.insertWorldPerson(world.id, "Cora")
        val faction = harness.insertFaction(world.id, "Harpers")
        harness.memberships.insert(
            FactionMembership(
                id = "mem-1",
                person = PersonRef.World(person.id),
                factionId = faction.id,
                role = "Agent",
                notes = "",
                createdAt = harness.now,
            )
        )
        harness.relationships.insert(
            PersonRelationship(
                id = "rel-1",
                from = PersonRef.World(person.id),
                to = PersonRef.World(other.id),
                type = RelationshipType.Ally,
                description = "",
                factionId = faction.id,
            )
        )
        harness.companions.insert(
            PersonCompanion(
                id = "companion-1",
                owner = PersonRef.World(person.id),
                companion = PersonRef.World(other.id),
                kind = CompanionKind.Familiar,
            )
        )

        val result = harness.deleteWorld(world.id)

        assertIs<DeleteWorldUseCase.Result.Deleted>(result)
        assertTrue(harness.worlds.all().isEmpty())
        assertTrue(harness.worldPeople.all().isEmpty())
        assertTrue(harness.factions.all().isEmpty())
        assertTrue(harness.memberships.all().isEmpty())
        assertTrue(harness.relationships.all().isEmpty())
        assertTrue(harness.companions.all().isEmpty())
    }

    @Test
    fun deleteRemovesOrphanFactionLinksAfterPeopleAreGone() = runTest {
        val harness = Harness()
        val world = harness.insertWorld("Faerun")
        val faction = harness.insertFaction(world.id, "Harpers")
        harness.memberships.insert(
            FactionMembership(
                id = "mem-orphan",
                person = PersonRef.Campaign("deleted-campaign-person"),
                factionId = faction.id,
                role = "Agent",
                notes = "",
                createdAt = harness.now,
            )
        )
        harness.relationships.insert(
            PersonRelationship(
                id = "rel-orphan",
                from = PersonRef.Campaign("a"),
                to = PersonRef.Campaign("b"),
                type = RelationshipType.Ally,
                description = "",
                factionId = faction.id,
            )
        )

        val result = harness.deleteWorld(world.id)

        assertIs<DeleteWorldUseCase.Result.Deleted>(result)
        assertTrue(harness.memberships.all().isEmpty())
        assertTrue(harness.relationships.all().isEmpty())
        assertTrue(harness.factions.all().isEmpty())
    }

    @Test
    fun deleteRemovesWorldPersonAndLocationSidecarFiles() = runTest {
        val harness = Harness()
        val world = harness.insertWorld("Faerun")
        val person = harness.insertWorldPerson(world.id, "Bram")
        val location = Location(
            id = "loc-1",
            worldId = world.id,
            type = LocationType.Continent,
            parentLocationId = null,
            name = "Sword Coast",
            description = "",
            climate = "",
            terrain = "",
            government = "",
            landmarks = emptyList(),
            history = "",
            notes = "",
            createdAt = harness.now,
            updatedAt = harness.now,
        )
        harness.locations.insert(location)
        harness.avatarFileStore.write(PersonRef.World(person.id), byteArrayOf(1, 2))
        harness.voiceClipFileStore.write(VoiceClipRef.WorldPerson(person.id), byteArrayOf(3, 4))
        harness.voiceClipFileStore.write(VoiceClipRef.Location(location.id), byteArrayOf(5, 6))

        val result = harness.deleteWorld(world.id)

        assertIs<DeleteWorldUseCase.Result.Deleted>(result)
        assertTrue(harness.avatarFileStore.pathIfPresent(PersonRef.World(person.id)) == null)
        assertTrue(harness.voiceClipFileStore.pathIfPresent(VoiceClipRef.WorldPerson(person.id)) == null)
        assertTrue(harness.voiceClipFileStore.pathIfPresent(VoiceClipRef.Location(location.id)) == null)
    }

    private class Harness {
        val worlds = FakeWorldRepository()
        val campaigns = FakeCampaignRepository()
        val worldPeople = FakeWorldPersonRepository()
        val locations = FakeLocationRepository()
        val factions = FakeFactionRepository()
        val memberships = FakeFactionMembershipRepository()
        val relationships = FakePersonRelationshipRepository()
        val companions = FakePersonCompanionRepository()
        val context = FakeActiveContextRepository()
        val now = Instant.parse("2026-08-29T12:00:00Z")
        private var nextPersonId = 0
        val avatarFileStore = PersonAvatarFileStore(Files.createTempDirectory("ww-avatars").toFile())
        val voiceClipFileStore = VoiceClipFileStore(Files.createTempDirectory("ww-voices").toFile())
        private val deleteWorldPerson = DeleteWorldPersonUseCase(
            worldPeople,
            FakeCampaignPersonRepository(),
            relationships,
            memberships,
            companions,
            FakeQuestRepository(),
            avatarFileStore,
            voiceClipFileStore,
        )
        val deleteWorld = DeleteWorldUseCase(
            worlds,
            campaigns,
            worldPeople,
            locations,
            factions,
            memberships,
            relationships,
            deleteWorldPerson,
            voiceClipFileStore,
            context,
        )

        suspend fun insertWorld(name: String): World {
            val world = World(
                id = "world-1",
                name = name,
                description = "",
                defaultGameSystem = GameSystem.FifthEdition,
                createdAt = now,
                updatedAt = now,
            )
            worlds.insert(world)
            return world
        }

        suspend fun insertCampaign(worldId: String, name: String): Campaign {
            val campaign = Campaign(
                id = "campaign-1",
                worldId = worldId,
                name = name,
                description = "",
                notes = "",
                gameSystem = null,
                status = CampaignStatus.Active,
                createdAt = now,
                updatedAt = now,
            )
            campaigns.insert(campaign)
            return campaign
        }

        suspend fun insertWorldPerson(worldId: String, name: String): WorldPerson {
            val person = WorldPerson(
                id = "world-person-${++nextPersonId}",
                worldId = worldId,
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

        suspend fun insertFaction(worldId: String, name: String): Faction {
            val faction = Faction(
                id = "faction-1",
                worldId = worldId,
                name = name,
                description = "",
                goals = "",
                notes = "",
                createdAt = now,
                updatedAt = now,
            )
            factions.insert(faction)
            return faction
        }
    }
}
