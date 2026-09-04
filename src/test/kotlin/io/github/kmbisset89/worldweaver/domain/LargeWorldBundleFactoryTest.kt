package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class LargeWorldBundleFactoryTest {
    @Test
    fun minimalBundleWritesAndReads() {
        val bundle = LargeWorldBundleFactory(LargeWorldBundleFactory.Scale.Minimal).create()
        val dest = Files.createTempFile("ww-minimal", ".wwbundle").toFile()
        val converter = WorldBundleArchiveConverter()

        converter.write(bundle, dest)
        val read = converter.read(dest)

        val ready = assertIs<WorldBundleArchiveConverter.ReadResult.Ready>(read)
        assertEquals("The Shattered Expanse", ready.bundle.world.name)
        assertEquals(WorldBundle.FORMAT_VERSION, ready.bundle.formatVersion)
        assertTrue(ready.bundle.campaigns.isEmpty())
        dest.delete()
    }

    @Test
    fun largeBundleWritesReadsAndImports() = runTest {
        val source = LargeWorldBundleFactory().create()
        val dest = fixtureFile()
        val converter = WorldBundleArchiveConverter()
        converter.write(source, dest)

        val read = assertIs<WorldBundleArchiveConverter.ReadResult.Ready>(converter.read(dest))
        assertEquals(source.campaigns.size, read.bundle.campaigns.size)
        assertEquals(source.locations.size, read.bundle.locations.size)
        assertEquals(source.loreEntries.size, read.bundle.loreEntries.size)
        assertEquals(source.worldPeople.size, read.bundle.worldPeople.size)
        assertEquals(source.campaignPeople.size, read.bundle.campaignPeople.size)
        assertEquals(source.quests.size, read.bundle.quests.size)
        assertEquals(source.sessions.size, read.bundle.sessions.size)
        assertEquals(source.encounters.size, read.bundle.encounters.size)
        assertEquals(source.battleMaps.size, read.bundle.battleMaps.size)
        assertEquals(source.avatarFiles.size, read.bundle.avatarFiles.size)
        assertEquals(source.mapFiles.size, read.bundle.mapFiles.size)
        assertTrue(source.locations.size > 300)
        assertTrue(source.campaigns.size >= 4)

        val harness = ImportHarness()
        val existing = World(
            id = "already-here",
            name = "Keep Me",
            description = "",
            defaultGameSystem = GameSystem.FifthEdition,
            createdAt = java.time.Instant.parse("2026-08-30T15:00:00Z"),
            updatedAt = java.time.Instant.parse("2026-08-30T15:00:00Z"),
        )
        harness.worlds.insert(existing)
        val imported = harness.importWorld(dest)
        val created = assertIs<ImportWorldBundleUseCase.Result.Imported>(imported)
        assertEquals("Keep Me", harness.worlds.getById("already-here")?.name)
        assertEquals("The Shattered Expanse (imported)", created.world.name)
        assertEquals(source.locations.size, harness.locations.getByWorld(created.world.id).size)
        assertEquals(source.worldPeople.size, harness.worldPeople.getByWorld(created.world.id).size)
        assertEquals(source.loreEntries.size, harness.lore.getByWorld(created.world.id).size)
        assertEquals(source.factions.size, harness.factions.getByWorld(created.world.id).size)
        assertEquals(source.memberships.size, harness.memberships.all().size)
        assertEquals(source.campaigns.size, harness.campaigns.getByWorld(created.world.id).size)
        assertEquals(source.quests.size, harness.quests.all().size)
        assertEquals(source.sessions.size, harness.sessions.all().size)
        assertEquals(source.encounters.size, harness.encounters.all().size)
        assertEquals(source.battleMaps.size, harness.battleMaps.all().size)
        assertEquals(source.relationships.size, harness.relationships.all().size)
        assertTrue(harness.avatarFileStore.pathIfPresent(PersonRef.World(harness.worldPeople.all().first().id)) != null)
        assertTrue(dest.length() > 10_000)
    }

    private fun fixtureFile(): File {
        val dir = File("fixtures")
        dir.mkdirs()
        return File(dir, "large-campaign.wwbundle")
    }

    private class ImportHarness {
        val tempDir: File = Files.createTempDirectory("ww-large-import").toFile()
        val worlds = FakeWorldRepository()
        val campaigns = FakeCampaignRepository()
        val locations = FakeLocationRepository()
        val lore = FakeLoreRepository()
        val observances = FakeWorldCalendarObservanceRepository()
        val factions = FakeFactionRepository()
        val memberships = FakeFactionMembershipRepository()
        val worldPeople = FakeWorldPersonRepository()
        val campaignPeople = FakeCampaignPersonRepository()
        val overlays = FakeLocationOverlayRepository()
        val quests = FakeQuestRepository()
        val sessions = FakeSessionRepository()
        val plotThreads = FakePlotThreadRepository()
        val referenceDocs = FakeReferenceDocRepository()
        val battleMaps = FakeBattleMapRepository()
        val situations = FakeBattleMapSituationRepository()
        val encounters = FakeEncounterRepository()
        val relationships = FakePersonRelationshipRepository()
        val companions = FakePersonCompanionRepository()
        val context = FakeActiveContextRepository()
        val avatarFileStore = PersonAvatarFileStore(File(tempDir, "avatars"))
        val battleMapFileStore = BattleMapFileStore(File(tempDir, "maps"))
        val worldMaps = FakeWorldMapRepository()
        val worldMapFileStore = WorldMapFileStore(File(tempDir, "world_maps"))
        val voiceClipFileStore = VoiceClipFileStore(File(tempDir, "voices"))
        private val instant = InstantProvider { java.time.Instant.parse("2026-08-30T15:00:00Z") }
        private var nextId = 0
        private val importWorldUseCase = ImportWorldBundleUseCase(
            archiveConverter = WorldBundleArchiveConverter(),
            idRemapper = WorldBundleIdRemapper(EntityIdFactory { "imp-${++nextId}" }),
            transactionRunner = object : TransactionRunner {
                override suspend fun <T> run(block: suspend () -> T): T = block()
            },
            worldRepository = worlds,
            worldCalendarRepository = FakeWorldCalendarRepository(),
            defaultCalendarFactory = DefaultWorldCalendarFactory(EntityIdFactory { "cal-${++nextId}" }),
            observanceRepository = observances,
            campaignRepository = campaigns,
            locationRepository = locations,
            loreRepository = lore,
            factionRepository = factions,
            factionMembershipRepository = memberships,
            worldPersonRepository = worldPeople,
            campaignPersonRepository = campaignPeople,
            locationOverlayRepository = overlays,
            questRepository = quests,
            sessionRepository = sessions,
            plotThreadRepository = plotThreads,
            referenceDocRepository = referenceDocs,
            battleMapRepository = battleMaps,
            battleMapSituationRepository = situations,
            encounterRepository = encounters,
            personRelationshipRepository = relationships,
            personCompanionRepository = companions,
            avatarFileStore = avatarFileStore,
            battleMapFileStore = battleMapFileStore,
            worldMapRepository = worldMaps,
            worldMapFileStore = worldMapFileStore,
            voiceClipFileStore = voiceClipFileStore,
            setActiveWorld = SetActiveWorldUseCase(worlds, campaigns, context, instant),
        )

        suspend fun importWorld(file: File): ImportWorldBundleUseCase.Result {
            return importWorldUseCase(file)
        }
    }
}
