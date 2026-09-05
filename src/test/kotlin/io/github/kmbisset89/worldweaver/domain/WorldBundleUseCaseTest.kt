package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.time.Instant
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

internal class WorldBundleUseCaseTest {
    private val now = Instant.parse("2026-08-30T12:00:00Z")

    @Test
    fun emptyWorldExportsAndImportsAsCopy() = runTest {
        val harness = Harness()
        val source = harness.insertWorld(id = "world-1", name = "Faerun")
        val dest = File(harness.tempDir, "faerun.wwbundle")

        val exported = harness.exportWorld(source.id, dest)
        assertIs<ExportWorldBundleUseCase.Result.Written>(exported)

        val imported = harness.importWorld(dest)
        val created = assertIs<ImportWorldBundleUseCase.Result.Imported>(imported)
        assertNotEquals(source.id, created.world.id)
        assertEquals("Faerun (imported)", created.world.name)
        assertEquals(created.world.id, harness.context.get().activeWorldId)
        assertEquals(source.name, harness.worlds.getById(source.id)?.name)
        assertEquals(2, harness.worlds.all().size)
        val importedCalendar = harness.calendars.getByWorld(created.world.id)
        assertEquals(12, importedCalendar?.months?.size)
        assertTrue(harness.observances.getByWorld(created.world.id).isEmpty())
    }

    @Test
    fun richGraphRemapsIdsAndKeepsLinks() = runTest {
        val harness = Harness()
        val source = harness.insertGraph()
        val dest = File(harness.tempDir, "rich.wwbundle")

        assertIs<ExportWorldBundleUseCase.Result.Written>(harness.exportWorld(source.world.id, dest))
        val imported = assertIs<ImportWorldBundleUseCase.Result.Imported>(harness.importWorld(dest))

        assertEquals(source.world.name, harness.worlds.getById(source.world.id)?.name)
        assertTrue(harness.avatarFileStore.pathIfPresent(PersonRef.World(source.worldPerson.id)) != null)
        assertTrue(harness.battleMapFileStore.listRelativeFiles(source.battleMap.id).isNotEmpty())

        val newWorld = harness.worlds.getById(imported.world.id)!!
        assertEquals("Sword Coast (imported)", newWorld.name)
        val continent = harness.locations.getByWorld(newWorld.id).first { it.name == "Faerun" }
        val city = harness.locations.getByWorld(newWorld.id).first { it.name == "Waterdeep" }
        assertEquals(continent.id, city.parentLocationId)
        val worldPerson = harness.worldPeople.getByWorld(newWorld.id).single()
        val lore = harness.lore.getByWorld(newWorld.id).single()
        assertEquals(city.id, lore.locationId)
        assertEquals(worldPerson.id, lore.characterId)
        assertEquals("hidden", lore.secrets.single().secret)
        val campaign = harness.campaigns.getByWorld(newWorld.id).single()
        val campaignPerson = harness.campaignPeople.getByCampaign(campaign.id).single()
        assertEquals(worldPerson.id, campaignPerson.worldPersonId)
        val overlay = harness.overlays.getByCampaign(campaign.id).single()
        assertEquals(city.id, overlay.locationId)
        val session = harness.sessions.getByCampaign(campaign.id).single()
        assertEquals(campaignPerson.id, session.marchOrder.single().person.id)
        val calendar = harness.calendars.getByWorld(newWorld.id)!!
        assertEquals("DR", calendar.eraSuffix)
        assertEquals(listOf("Hammer"), calendar.months.map { it.name })
        assertEquals(calendar.months.single().id, session.inWorldDate?.monthId)
        val observance = harness.observances.getByWorld(newWorld.id).single()
        assertEquals("Midwinter", observance.name)
        assertEquals(calendar.months.single().id, observance.monthId)
        assertEquals(listOf(lore.id), observance.loreIds)
        assertNotEquals(source.observance.id, observance.id)
        assertEquals(12, session.inWorldDate?.day)
        val quest = harness.quests.getByCampaign(campaign.id).single()
        assertEquals(city.id, quest.locationId)
        assertEquals(lore.id, quest.links.single().targetId)
        val encounter = harness.encounters.getByCampaign(campaign.id).single()
        val newMap = harness.battleMaps.getByCampaign(campaign.id).single()
        assertEquals(newMap.id, encounter.battleMapId)
        assertEquals(worldPerson.id, encounter.participants.single().sourceId)
        val situation = harness.situations.getByBattleMap(newMap.id).single()
        assertEquals("Flood", situation.name)
        val relationship = harness.relationships.all().first { it.id != source.relationship.id }
        assertEquals(worldPerson.id, relationship.from.id)
        assertEquals(campaignPerson.id, relationship.to.id)
        assertTrue(harness.avatarFileStore.pathIfPresent(PersonRef.World(worldPerson.id)) != null)
        assertTrue(harness.voiceClipFileStore.pathIfPresent(VoiceClipRef.WorldPerson(worldPerson.id)) != null)
        assertTrue(harness.voiceClipFileStore.pathIfPresent(VoiceClipRef.Location(city.id)) != null)
        val mapFiles = harness.battleMapFileStore.listRelativeFiles(newMap.id)
        assertTrue(mapFiles.any { it.first == "original.png" })
        assertTrue(mapFiles.any { it.first == "situations/${situation.id}/original.png" })
        assertNotEquals(source.world.id, newWorld.id)
        assertNotEquals(source.worldPerson.id, worldPerson.id)
        assertNotEquals(source.battleMap.id, newMap.id)
    }

    @Test
    fun encounterParticipantRecordDefaultsMissingEconomyFields() {
        val json = Json { ignoreUnknownKeys = true }
        val record = json.decodeFromString(
            WorldBundle.EncounterParticipantRecord.serializer(),
            """
            {
              "id": "p-1",
              "name": "Goblin",
              "source": "Nameless",
              "sourceId": null,
              "initiativeRoll": null,
              "initiativeBonus": 0,
              "armorClass": 13,
              "hitPoints": 7,
              "maxHitPoints": 7,
              "temporaryHitPoints": 0,
              "conditions": [],
              "groupCount": 1,
              "combatState": "Conscious",
              "gridColumn": null,
              "gridRow": null
            }
            """.trimIndent(),
        )
        val participant = record.toDomain()
        assertEquals(1, participant.attacksAllowed)
        assertEquals(0, participant.attacksUsed)
        assertEquals(false, participant.bonusActionUsed)
        assertEquals(false, participant.reactionUsed)
        assertEquals(true, participant.visibleToPlayers)
    }

    @Test
    fun missingPersonSheetSystemDecodesAsFifthEdition() {
        val json = Json { ignoreUnknownKeys = true }
        val record = json.decodeFromString(
            WorldBundle.WorldPersonRecord.serializer(),
            """
            {
              "id": "wp-1",
              "worldId": "world-1",
              "kind": "Npc",
              "name": "Bram",
              "description": "",
              "sheet": {
                "race": "Human",
                "classLevels": [],
                "abilityScores": {
                  "strength": 10,
                  "dexterity": 10,
                  "constitution": 10,
                  "intelligence": 10,
                  "wisdom": 10,
                  "charisma": 10
                },
                "hitPoints": 10,
                "maxHitPoints": 10,
                "temporaryHitPoints": 0,
                "armorClass": 10,
                "walkSpeed": 30,
                "deathSaves": { "successes": 0, "failures": 0 },
                "items": [],
                "features": [],
                "spells": [],
                "notes": ""
              },
              "createdAtEpochMillis": 1756560000000,
              "updatedAtEpochMillis": 1756560000000
            }
            """.trimIndent(),
        )
        val person = record.toDomain()
        val sheet = assertIs<FifthEditionSheet>(person.sheet)
        assertEquals("Human", sheet.race)
        assertEquals(GameSystem.FifthEdition, sheet.gameSystem())
    }

    @Test
    fun pathfinderPersonSheetRoundTripsThroughBundleRecord() {
        val person = WorldPerson(
            id = "wp-pf2e",
            worldId = "world-1",
            kind = PersonKind.Npc,
            name = "Harsk",
            description = "",
            sheet = Pathfinder2ESheet.empty().copy(
                ancestry = "Dwarf",
                className = "Ranger",
                level = 4,
            ),
            createdAt = now,
            updatedAt = now,
        )

        val decoded = WorldBundle.WorldPersonRecord.from(person).toDomain()
        val sheet = assertIs<Pathfinder2ESheet>(decoded.sheet)
        assertEquals("Dwarf", sheet.ancestry)
        assertEquals("Ranger", sheet.className)
        assertEquals(4, sheet.level)
    }

    @Test
    fun oldRelationshipLeanCreatesFactionOnRead() {
        val world = World(
            id = "world-1",
            name = "Faerun",
            description = "",
            defaultGameSystem = GameSystem.FifthEdition,
            createdAt = now,
            updatedAt = now,
        )
        val bram = WorldPerson(
            id = "wp-1",
            worldId = world.id,
            kind = PersonKind.Npc,
            name = "Bram",
            description = "",
            sheet = FifthEditionSheet.empty(),
            createdAt = now,
            updatedAt = now,
        )
        val cora = bram.copy(id = "wp-2", name = "Cora")
        val payload = WorldBundle.Payload(
            world = WorldBundle.WorldRecord.from(world),
            campaigns = emptyList(),
            locations = emptyList(),
            loreEntries = emptyList(),
            worldPeople = listOf(
                WorldBundle.WorldPersonRecord.from(bram),
                WorldBundle.WorldPersonRecord.from(cora),
            ),
            campaignPeople = emptyList(),
            locationOverlays = emptyList(),
            quests = emptyList(),
            sessions = emptyList(),
            plotThreads = emptyList(),
            referenceDocs = emptyList(),
            battleMaps = emptyList(),
            battleMapSituations = emptyList(),
            encounters = emptyList(),
            relationships = listOf(
                WorldBundle.PersonRelationshipRecord(
                    id = "rel-1",
                    from = WorldBundle.PersonRefRecord.from(PersonRef.World(bram.id)),
                    to = WorldBundle.PersonRefRecord.from(PersonRef.World(cora.id)),
                    type = "Ally",
                    description = "",
                    factionLean = "Harpers",
                )
            ),
            companions = emptyList(),
        )
        val bundle = WorldBundle.fromRecords(
            manifest = WorldBundle.Manifest(1, now.toEpochMilli(), "Faerun"),
            payload = payload,
            avatarFiles = emptyList(),
            mapFiles = emptyList(),
        )
        assertEquals(1, bundle.factions.size)
        assertEquals("Harpers", bundle.factions.single().name)
        assertEquals(bundle.factions.single().id, bundle.relationships.single().factionId)
    }

    @Test
    fun unknownFormatVersionIsRejected() = runTest {
        val harness = Harness()
        val source = harness.insertWorld(id = "world-1", name = "Faerun")
        val dest = File(harness.tempDir, "future.wwbundle")
        writeUnsupportedArchive(dest)

        val result = harness.importWorld(dest)

        assertIs<ImportWorldBundleUseCase.Result.UnsupportedVersion>(result)
        assertEquals(1, harness.worlds.all().size)
        assertEquals(source.id, harness.worlds.all().single().id)
    }

    @Test
    fun exportMissingWorldFails() = runTest {
        val harness = Harness()
        val dest = File(harness.tempDir, "missing.wwbundle")

        val result = harness.exportWorld("missing", dest)

        assertIs<ExportWorldBundleUseCase.Result.WorldNotFound>(result)
        assertTrue(!dest.exists())
    }

    private fun writeUnsupportedArchive(dest: File) {
        ZipOutputStream(dest.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry("manifest.json"))
            zip.write(
                Json.encodeToString(
                    WorldBundle.Manifest.serializer(),
                    WorldBundle.Manifest(
                        formatVersion = 99,
                        exportedAtEpochMillis = now.toEpochMilli(),
                        originalWorldName = "Future",
                    ),
                ).encodeToByteArray(),
            )
            zip.closeEntry()
            zip.putNextEntry(ZipEntry("bundle.json"))
            zip.write("{}".encodeToByteArray())
            zip.closeEntry()
        }
    }

    private inner class Harness {
        val tempDir: File = Files.createTempDirectory("ww-bundle").toFile()
        val worlds = FakeWorldRepository()
        val calendars = FakeWorldCalendarRepository()
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
        private val instant = InstantProvider { now }
        private var nextId = 0
        private val ids = EntityIdFactory { "new-${++nextId}" }
        private val setActiveWorld = SetActiveWorldUseCase(worlds, campaigns, context, instant)
        private val snapshotFactory = WorldBundleSnapshotFactory(
            worldRepository = worlds,
            worldCalendarRepository = calendars,
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
            instantProvider = instant,
        )
        private val archiveConverter = WorldBundleArchiveConverter()
        val exportWorld = ExportWorldBundleUseCase(snapshotFactory, archiveConverter)
        val importWorld = ImportWorldBundleUseCase(
            archiveConverter = archiveConverter,
            idRemapper = WorldBundleIdRemapper(ids),
            transactionRunner = ImmediateTransactionRunner(),
            worldRepository = worlds,
            worldCalendarRepository = calendars,
            defaultCalendarFactory = DefaultWorldCalendarFactory(ids),
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
            setActiveWorld = setActiveWorld,
        )

        suspend fun insertWorld(id: String, name: String): World {
            val world = World(
                id = id,
                name = name,
                description = "A setting",
                defaultGameSystem = GameSystem.FifthEdition,
                createdAt = now,
                updatedAt = now,
            )
            worlds.insert(world)
            return world
        }

        suspend fun insertGraph(): SourceGraph {
            val world = insertWorld(id = "world-1", name = "Sword Coast")
            val continent = Location(
                id = "loc-continent",
                worldId = world.id,
                type = LocationType.Continent,
                parentLocationId = null,
                name = "Faerun",
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
            val city = continent.copy(
                id = "loc-city",
                type = LocationType.City,
                parentLocationId = continent.id,
                name = "Waterdeep",
            )
            locations.insert(continent)
            locations.insert(city)
            val worldPerson = WorldPerson(
                id = "wp-1",
                worldId = world.id,
                kind = PersonKind.Npc,
                name = "Volo",
                description = "Guide",
                sheet = FifthEditionSheet.empty(),
                createdAt = now,
                updatedAt = now,
            )
            worldPeople.insert(worldPerson)
            calendars.insert(
                WorldCalendar(
                    id = "cal-1",
                    worldId = world.id,
                    eraSuffix = "DR",
                    months = listOf(WorldCalendarMonth(id = "m-hammer", name = "Hammer", days = 30)),
                    weekdays = listOf(WorldCalendarWeekday(id = "w-1", name = "Moonday")),
                    currentDate = WorldDate(year = 1492, monthId = "m-hammer", day = 1),
                    createdAt = now,
                    updatedAt = now,
                )
            )
            avatarFileStore.write(PersonRef.World(worldPerson.id), byteArrayOf(1, 2, 3, 4))
            voiceClipFileStore.write(
                VoiceClipRef.WorldPerson(worldPerson.id),
                VoiceClipWavFormat.wrapPcm(ByteArray(80)),
            )
            voiceClipFileStore.write(
                VoiceClipRef.Location(city.id),
                VoiceClipWavFormat.wrapPcm(ByteArray(40)),
            )
            val loreEntry = Lore(
                id = "lore-1",
                worldId = world.id,
                title = "Open Lord",
                content = "Piergeiron",
                category = LoreCategory.Politics,
                tags = listOf("city"),
                relatedEntryIds = emptyList(),
                secrets = listOf(
                    LoreSecret(
                        id = "secret-1",
                        title = "Mask",
                        secret = "hidden",
                        hints = listOf(LoreHint(id = "hint-1", text = "a clue", revealed = false)),
                    )
                ),
                locationId = city.id,
                characterId = worldPerson.id,
                createdAt = now,
                updatedAt = now,
            )
            lore.insert(loreEntry)
            val observance = WorldCalendarObservance(
                id = "obs-1",
                worldId = world.id,
                name = "Midwinter",
                notes = "Lamps",
                kind = WorldCalendarObservanceKind.Holiday,
                monthId = "m-hammer",
                day = 1,
                year = null,
                loreIds = listOf(loreEntry.id),
                createdAt = now,
                updatedAt = now,
            )
            observances.insert(observance)
            val campaign = Campaign(
                id = "camp-1",
                worldId = world.id,
                name = "Dragon Heist",
                description = "",
                notes = "",
                gameSystem = null,
                status = CampaignStatus.Active,
                createdAt = now,
                updatedAt = now,
            )
            campaigns.insert(campaign)
            val campaignPerson = CampaignPerson(
                id = "cp-1",
                campaignId = campaign.id,
                worldPersonId = worldPerson.id,
                kind = PersonKind.PlayerCharacter,
                name = "Tav",
                description = "",
                sheet = FifthEditionSheet.empty(),
                overlayHitPoints = 12,
                overlayNotes = "",
                createdAt = now,
                updatedAt = now,
            )
            campaignPeople.insert(campaignPerson)
            overlays.upsert(
                LocationOverlay(
                    campaignId = campaign.id,
                    locationId = city.id,
                    hasPartyPresence = true,
                    notes = "Inn",
                    updatedAt = now,
                )
            )
            val session = Session(
                id = "sess-1",
                campaignId = campaign.id,
                name = "Session 1",
                notes = "",
                inWorldDate = WorldDate(year = 1492, monthId = "m-hammer", day = 12),
                scenes = listOf(SessionScene(id = "scene-1", title = "Tavern", notes = "")),
                marchOrder = listOf(
                    MarchOrderEntry(
                        id = "march-1",
                        person = PersonRef.Campaign(campaignPerson.id),
                        displayName = campaignPerson.name,
                    )
                ),
                createdAt = now,
                updatedAt = now,
            )
            sessions.insert(session)
            quests.insert(
                Quest(
                    id = "quest-1",
                    campaignId = campaign.id,
                    title = "Find the stone",
                    summary = "",
                    status = QuestStatus.Active,
                    locationId = city.id,
                    objectives = listOf(
                        QuestObjective(id = "obj-1", title = "Ask Volo", status = QuestObjectiveStatus.Open)
                    ),
                    links = listOf(
                        QuestLink(id = "link-1", kind = QuestLinkKind.LORE, targetId = loreEntry.id)
                    ),
                    createdAt = now,
                    updatedAt = now,
                )
            )
            val battleMap = BattleMap(
                id = "map-1",
                campaignId = campaign.id,
                name = "Sewer",
                originalWidth = 64,
                originalHeight = 64,
                tileSizePx = 256,
                minZoom = 0,
                maxZoom = 0,
                createdAt = now,
                updatedAt = now,
            )
            battleMaps.insert(battleMap)
            val situation = BattleMapSituation(
                id = "sit-1",
                battleMapId = battleMap.id,
                name = "Flood",
                visible = false,
                sortIndex = 0,
                createdAt = now,
                updatedAt = now,
            )
            situations.insert(situation)
            battleMapFileStore.writeRelativeFiles(
                battleMap.id,
                listOf(
                    "original.png" to byteArrayOf(9, 8, 7),
                    "situations/${situation.id}/original.png" to byteArrayOf(6, 5, 4),
                ),
            )
            encounters.insert(
                Encounter(
                    id = "enc-1",
                    campaignId = campaign.id,
                    name = "Ambush",
                    locationId = city.id,
                    battleMapId = battleMap.id,
                    difficulty = EncounterDifficulty.Medium,
                    notes = "",
                    outcomeNote = "",
                    status = EncounterStatus.Planned,
                    currentRound = 0,
                    currentTurnIndex = 0,
                    participants = listOf(
                        EncounterParticipant(
                            id = "part-1",
                            name = "Volo",
                            source = EncounterParticipantSource.WorldPerson,
                            sourceId = worldPerson.id,
                            initiativeRoll = null,
                            initiativeBonus = 1,
                            armorClass = 12,
                            hitPoints = 8,
                            maxHitPoints = 8,
                            temporaryHitPoints = 0,
                            conditions = emptyList(),
                            groupCount = 1,
                            combatState = CombatState.Conscious,
                        )
                    ),
                    createdAt = now,
                    updatedAt = now,
                )
            )
            val relationship = PersonRelationship(
                id = "rel-1",
                from = PersonRef.World(worldPerson.id),
                to = PersonRef.Campaign(campaignPerson.id),
                type = RelationshipType.Ally,
                description = "Friend",
                factionId = null,
            )
            relationships.insert(relationship)
            return SourceGraph(
                world = world,
                worldPerson = worldPerson,
                battleMap = battleMap,
                relationship = relationship,
                observance = observance,
            )
        }
    }

    private data class SourceGraph(
        val world: World,
        val worldPerson: WorldPerson,
        val battleMap: BattleMap,
        val relationship: PersonRelationship,
        val observance: WorldCalendarObservance,
    )

    private class ImmediateTransactionRunner : TransactionRunner {
        override suspend fun <T> run(block: suspend () -> T): T {
            return block()
        }
    }
}
