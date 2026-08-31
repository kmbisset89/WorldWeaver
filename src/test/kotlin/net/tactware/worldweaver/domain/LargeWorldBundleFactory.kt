package net.tactware.worldweaver.domain

import java.time.Instant

internal class LargeWorldBundleFactory(
    private val scale: Scale = Scale.Large,
    private val now: Instant = Instant.parse("2026-08-30T15:00:00Z"),
) {
    data class Scale(
        val campaignCount: Int,
        val continents: Int,
        val areasPerContinent: Int,
        val citiesPerArea: Int,
        val placesPerCity: Int,
        val worldPeople: Int,
        val loreEntries: Int,
        val pcsPerCampaign: Int,
        val campaignNpcsPerCampaign: Int,
        val questsPerCampaign: Int,
        val sessionsPerCampaign: Int,
        val plotThreadsPerCampaign: Int,
        val referenceDocsPerCampaign: Int,
        val encountersPerCampaign: Int,
        val mapsPerCampaign: Int,
        val situationsPerMap: Int,
        val overlaysPerCampaign: Int,
        val relationships: Int,
        val companions: Int,
        val avatarCount: Int,
    ) {
        companion object {
            val Minimal = Scale(
                campaignCount = 0,
                continents = 0,
                areasPerContinent = 0,
                citiesPerArea = 0,
                placesPerCity = 0,
                worldPeople = 0,
                loreEntries = 0,
                pcsPerCampaign = 0,
                campaignNpcsPerCampaign = 0,
                questsPerCampaign = 0,
                sessionsPerCampaign = 0,
                plotThreadsPerCampaign = 0,
                referenceDocsPerCampaign = 0,
                encountersPerCampaign = 0,
                mapsPerCampaign = 0,
                situationsPerMap = 0,
                overlaysPerCampaign = 0,
                relationships = 0,
                companions = 0,
                avatarCount = 0,
            )
            val Large = Scale(
                campaignCount = 4,
                continents = 5,
                areasPerContinent = 4,
                citiesPerArea = 4,
                placesPerCity = 3,
                worldPeople = 180,
                loreEntries = 120,
                pcsPerCampaign = 6,
                campaignNpcsPerCampaign = 18,
                questsPerCampaign = 24,
                sessionsPerCampaign = 16,
                plotThreadsPerCampaign = 12,
                referenceDocsPerCampaign = 8,
                encountersPerCampaign = 18,
                mapsPerCampaign = 6,
                situationsPerMap = 2,
                overlaysPerCampaign = 40,
                relationships = 80,
                companions = 30,
                avatarCount = 50,
            )
        }
    }

    fun create(): WorldBundle {
        val world = world()
        val locations = locations(world.id)
        val worldPeople = worldPeople(world.id)
        val loreEntries = loreEntries(world.id, locations, worldPeople)
        val campaigns = campaigns(world.id)
        val campaignPeople = campaignPeople(campaigns, worldPeople)
        val locationOverlays = overlays(campaigns, locations)
        val sessions = sessions(campaigns, campaignPeople)
        val plotThreads = plotThreads(campaigns, sessions)
        val referenceDocs = referenceDocs(campaigns, sessions)
        val quests = quests(campaigns, locations, loreEntries, worldPeople, campaignPeople, sessions)
        val battleMaps = battleMaps(campaigns)
        val situations = situations(battleMaps)
        val encounters = encounters(campaigns, locations, battleMaps, worldPeople, campaignPeople)
        val relationships = relationships(worldPeople, campaignPeople)
        val companions = companions(worldPeople, campaignPeople)
        return WorldBundle(
            formatVersion = WorldBundle.FORMAT_VERSION,
            exportedAt = now,
            world = world,
            campaigns = campaigns,
            locations = locations,
            loreEntries = loreEntries,
            worldPeople = worldPeople,
            campaignPeople = campaignPeople,
            locationOverlays = locationOverlays,
            quests = quests,
            sessions = sessions,
            plotThreads = plotThreads,
            referenceDocs = referenceDocs,
            battleMaps = battleMaps,
            battleMapSituations = situations,
            encounters = encounters,
            relationships = relationships,
            companions = companions,
            avatarFiles = avatars(worldPeople, campaignPeople),
            mapFiles = mapFiles(battleMaps, situations),
        )
    }

    private fun world(): World {
        return World(
            id = "world-shattered",
            name = "The Shattered Expanse",
            description = "A continent-spanning setting built to stress world-bundle import: " +
                "layered locations, a large NPC library, and four parallel campaigns.",
            defaultGameSystem = GameSystem.FifthEdition,
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun locations(worldId: String): List<Location> {
        val result = mutableListOf<Location>()
        repeat(scale.continents) { continentIndex ->
            val continentId = "loc-cont-$continentIndex"
            result += location(
                id = continentId,
                worldId = worldId,
                type = LocationType.Continent,
                parentId = null,
                name = "${CONTINENTS[continentIndex % CONTINENTS.size]} $continentIndex",
            )
            repeat(scale.areasPerContinent) { areaIndex ->
                val areaId = "loc-area-$continentIndex-$areaIndex"
                result += location(
                    id = areaId,
                    worldId = worldId,
                    type = LocationType.Area,
                    parentId = continentId,
                    name = "${AREAS[areaIndex % AREAS.size]} $continentIndex-$areaIndex",
                )
                repeat(scale.citiesPerArea) { cityIndex ->
                    val cityId = "loc-city-$continentIndex-$areaIndex-$cityIndex"
                    result += location(
                        id = cityId,
                        worldId = worldId,
                        type = LocationType.City,
                        parentId = areaId,
                        name = "${CITIES[cityIndex % CITIES.size]} $continentIndex-$areaIndex-$cityIndex",
                    )
                    repeat(scale.placesPerCity) { placeIndex ->
                        result += location(
                            id = "loc-place-$continentIndex-$areaIndex-$cityIndex-$placeIndex",
                            worldId = worldId,
                            type = LocationType.Place,
                            parentId = cityId,
                            name = "${PLACES[placeIndex % PLACES.size]} $continentIndex-$areaIndex-$cityIndex-$placeIndex",
                        )
                    }
                }
            }
        }
        return result
    }

    private fun location(
        id: String,
        worldId: String,
        type: LocationType,
        parentId: String?,
        name: String,
    ): Location {
        return Location(
            id = id,
            worldId = worldId,
            type = type,
            parentLocationId = parentId,
            name = name,
            description = "Generated $type used for import stress testing.",
            climate = "Temperate",
            terrain = "Mixed",
            government = "Local council",
            landmarks = listOf("Market", "Watchtower"),
            history = "Layered history for $name.",
            notes = "Fixture note",
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun worldPeople(worldId: String): List<WorldPerson> {
        return List(scale.worldPeople) { index ->
            val kind = if (index % 7 == 0) PersonKind.Monster else PersonKind.Npc
            WorldPerson(
                id = "wp-$index",
                worldId = worldId,
                kind = kind,
                name = "${FIRST_NAMES[index % FIRST_NAMES.size]} ${SURNAMES[index % SURNAMES.size]}",
                description = "World library ${kind.displayName} #$index.",
                sheet = sheetFor(index, kind),
                createdAt = now,
                updatedAt = now,
            )
        }
    }

    private fun loreEntries(
        worldId: String,
        locations: List<Location>,
        worldPeople: List<WorldPerson>,
    ): List<Lore> {
        val cities = locations.filter { it.type == LocationType.City }
        return List(scale.loreEntries) { index ->
            val previous = if (index > 0) listOf("lore-${index - 1}") else emptyList()
            Lore(
                id = "lore-$index",
                worldId = worldId,
                title = "Lore ${LORE_TITLES[index % LORE_TITLES.size]} $index",
                content = "Long-form setting text for entry $index. It exists to bulk out import.",
                category = LoreCategory.entries[index % LoreCategory.entries.size],
                tags = listOf("generated", "stress"),
                relatedEntryIds = previous,
                secrets = listOf(
                    LoreSecret(
                        id = "secret-$index",
                        title = "Secret $index",
                        secret = "The truth behind lore $index.",
                        hints = listOf(
                            LoreHint(id = "hint-$index-0", text = "A whispered rumor.", revealed = false),
                            LoreHint(id = "hint-$index-1", text = "A torn page.", revealed = index % 4 == 0),
                        ),
                    )
                ),
                locationId = cities.getOrNull(index % cities.size.coerceAtLeast(1))?.id,
                characterId = worldPeople.getOrNull(index % worldPeople.size.coerceAtLeast(1))?.id,
                createdAt = now,
                updatedAt = now,
            )
        }
    }

    private fun campaigns(worldId: String): List<Campaign> {
        return List(scale.campaignCount) { index ->
            Campaign(
                id = "camp-$index",
                worldId = worldId,
                name = CAMPAIGN_NAMES[index % CAMPAIGN_NAMES.size],
                description = "Play-through $index of the Shattered Expanse.",
                notes = "Prep notes for campaign $index.",
                gameSystem = null,
                status = if (index == scale.campaignCount - 1) {
                    CampaignStatus.Archived
                } else {
                    CampaignStatus.Active
                },
                createdAt = now,
                updatedAt = now,
            )
        }
    }

    private fun campaignPeople(
        campaigns: List<Campaign>,
        worldPeople: List<WorldPerson>,
    ): List<CampaignPerson> {
        val people = mutableListOf<CampaignPerson>()
        campaigns.forEachIndexed { campaignIndex, campaign ->
            repeat(scale.pcsPerCampaign) { index ->
                people += CampaignPerson(
                    id = "cp-$campaignIndex-pc-$index",
                    campaignId = campaign.id,
                    worldPersonId = null,
                    kind = PersonKind.PlayerCharacter,
                    name = "PC ${FIRST_NAMES[index % FIRST_NAMES.size]} $campaignIndex-$index",
                    description = "Party member $index.",
                    sheet = sheetFor(index + 20, PersonKind.PlayerCharacter),
                    overlayHitPoints = 18 + index,
                    overlayNotes = "At the table",
                    createdAt = now,
                    updatedAt = now,
                )
            }
            repeat(scale.campaignNpcsPerCampaign) { index ->
                val worldPerson = worldPeople.getOrNull((campaignIndex * 17 + index) % worldPeople.size.coerceAtLeast(1))
                people += CampaignPerson(
                    id = "cp-$campaignIndex-npc-$index",
                    campaignId = campaign.id,
                    worldPersonId = worldPerson?.id,
                    kind = PersonKind.Npc,
                    name = worldPerson?.name ?: "NPC $campaignIndex-$index",
                    description = "Campaign overlay NPC $index.",
                    sheet = worldPerson?.sheet ?: FifthEditionSheet.empty(),
                    overlayHitPoints = 10,
                    overlayNotes = "Met in session ${index % 4}",
                    createdAt = now,
                    updatedAt = now,
                )
            }
        }
        return people
    }

    private fun overlays(
        campaigns: List<Campaign>,
        locations: List<Location>,
    ): List<LocationOverlay> {
        val places = locations.filter { it.type == LocationType.Place }
        if (places.isEmpty()) {
            return emptyList()
        }
        return campaigns.flatMapIndexed { campaignIndex, campaign ->
            List(scale.overlaysPerCampaign.coerceAtMost(places.size)) { index ->
                LocationOverlay(
                    campaignId = campaign.id,
                    locationId = places[(campaignIndex + index) % places.size].id,
                    hasPartyPresence = index % 3 == 0,
                    notes = "Party marker $campaignIndex-$index",
                    updatedAt = now,
                )
            }
        }
    }

    private fun sessions(
        campaigns: List<Campaign>,
        campaignPeople: List<CampaignPerson>,
    ): List<Session> {
        return campaigns.flatMapIndexed { campaignIndex, campaign ->
            val party = campaignPeople.filter {
                it.campaignId == campaign.id && it.kind == PersonKind.PlayerCharacter
            }
            List(scale.sessionsPerCampaign) { index ->
                Session(
                    id = "sess-$campaignIndex-$index",
                    campaignId = campaign.id,
                    name = "Session ${index + 1}: ${SESSION_TITLES[index % SESSION_TITLES.size]}",
                    notes = "Recap and beats for session ${index + 1}.",
                    scenes = listOf(
                        SessionScene(
                            id = "scene-$campaignIndex-$index-0",
                            title = "Cold open",
                            notes = "Hook",
                        ),
                        SessionScene(
                            id = "scene-$campaignIndex-$index-1",
                            title = "Main conflict",
                            notes = "Pressure",
                        ),
                    ),
                    marchOrder = party.mapIndexed { personIndex, person ->
                        MarchOrderEntry(
                            id = "march-$campaignIndex-$index-$personIndex",
                            person = PersonRef.Campaign(person.id),
                            displayName = person.name,
                        )
                    },
                    createdAt = now,
                    updatedAt = now,
                )
            }
        }
    }

    private fun plotThreads(
        campaigns: List<Campaign>,
        sessions: List<Session>,
    ): List<PlotThread> {
        return campaigns.flatMapIndexed { campaignIndex, campaign ->
            val campaignSessions = sessions.filter { it.campaignId == campaign.id }
            List(scale.plotThreadsPerCampaign) { index ->
                PlotThread(
                    id = "plot-$campaignIndex-$index",
                    campaignId = campaign.id,
                    sessionId = campaignSessions.getOrNull(index % campaignSessions.size.coerceAtLeast(1))?.id,
                    title = "Thread ${PLOT_TITLES[index % PLOT_TITLES.size]} $index",
                    details = "Ongoing thread $index.",
                    status = PlotThreadStatus.entries[index % PlotThreadStatus.entries.size],
                    priority = PlotThreadPriority.entries[index % PlotThreadPriority.entries.size],
                    createdAt = now,
                    updatedAt = now,
                )
            }
        }
    }

    private fun referenceDocs(
        campaigns: List<Campaign>,
        sessions: List<Session>,
    ): List<ReferenceDoc> {
        return campaigns.flatMapIndexed { campaignIndex, campaign ->
            val campaignSessions = sessions.filter { it.campaignId == campaign.id }
            List(scale.referenceDocsPerCampaign) { index ->
                ReferenceDoc(
                    id = "ref-$campaignIndex-$index",
                    campaignId = campaign.id,
                    sessionId = campaignSessions.getOrNull(index % campaignSessions.size.coerceAtLeast(1))?.id,
                    title = "Handout $index",
                    pathOrUrl = "https://example.invalid/handout-$campaignIndex-$index",
                    createdAt = now,
                    updatedAt = now,
                )
            }
        }
    }

    private fun quests(
        campaigns: List<Campaign>,
        locations: List<Location>,
        loreEntries: List<Lore>,
        worldPeople: List<WorldPerson>,
        campaignPeople: List<CampaignPerson>,
        sessions: List<Session>,
    ): List<Quest> {
        val cities = locations.filter { it.type == LocationType.City }
        return campaigns.flatMapIndexed { campaignIndex, campaign ->
            val people = campaignPeople.filter { it.campaignId == campaign.id }
            val campaignSessions = sessions.filter { it.campaignId == campaign.id }
            List(scale.questsPerCampaign) { index ->
                Quest(
                    id = "quest-$campaignIndex-$index",
                    campaignId = campaign.id,
                    title = "Quest ${QUEST_TITLES[index % QUEST_TITLES.size]} $index",
                    summary = "Objective chain $index for campaign $campaignIndex.",
                    status = if (index % 5 == 0) QuestStatus.Completed else QuestStatus.Active,
                    locationId = cities.getOrNull((campaignIndex + index) % cities.size.coerceAtLeast(1))?.id,
                    objectives = listOf(
                        QuestObjective(
                            id = "obj-$campaignIndex-$index-0",
                            title = "Learn the rumor",
                            status = QuestObjectiveStatus.Complete,
                        ),
                        QuestObjective(
                            id = "obj-$campaignIndex-$index-1",
                            title = "Travel to the site",
                            status = QuestObjectiveStatus.Open,
                        ),
                    ),
                    links = listOfNotNull(
                        loreEntries.getOrNull(index)?.let { lore ->
                            QuestLink(id = "ql-$campaignIndex-$index-lore", kind = QuestLinkKind.LORE, targetId = lore.id)
                        },
                        worldPeople.getOrNull(index)?.let { person ->
                            QuestLink(
                                id = "ql-$campaignIndex-$index-wp",
                                kind = QuestLinkKind.WORLD_PERSON,
                                targetId = person.id,
                            )
                        },
                        people.getOrNull(index)?.let { person ->
                            QuestLink(
                                id = "ql-$campaignIndex-$index-cp",
                                kind = QuestLinkKind.CAMPAIGN_PERSON,
                                targetId = person.id,
                            )
                        },
                        campaignSessions.getOrNull(index)?.let { session ->
                            QuestLink(
                                id = "ql-$campaignIndex-$index-sess",
                                kind = QuestLinkKind.SESSION,
                                targetId = session.id,
                            )
                        },
                    ),
                    createdAt = now,
                    updatedAt = now,
                )
            }
        }
    }

    private fun battleMaps(campaigns: List<Campaign>): List<BattleMap> {
        return campaigns.flatMapIndexed { campaignIndex, campaign ->
            List(scale.mapsPerCampaign) { index ->
                BattleMap(
                    id = "map-$campaignIndex-$index",
                    campaignId = campaign.id,
                    name = "Map ${MAP_NAMES[index % MAP_NAMES.size]} $index",
                    originalWidth = 64,
                    originalHeight = 64,
                    tileSizePx = 256,
                    minZoom = 0,
                    maxZoom = 0,
                    columns = 8,
                    rows = 8,
                    createdAt = now,
                    updatedAt = now,
                )
            }
        }
    }

    private fun situations(battleMaps: List<BattleMap>): List<BattleMapSituation> {
        return battleMaps.flatMap { map ->
            List(scale.situationsPerMap) { index ->
                BattleMapSituation(
                    id = "sit-${map.id}-$index",
                    battleMapId = map.id,
                    name = if (index == 0) "Flood" else "Collapse",
                    visible = index == 0,
                    sortIndex = index,
                    createdAt = now,
                    updatedAt = now,
                )
            }
        }
    }

    private fun encounters(
        campaigns: List<Campaign>,
        locations: List<Location>,
        battleMaps: List<BattleMap>,
        worldPeople: List<WorldPerson>,
        campaignPeople: List<CampaignPerson>,
    ): List<Encounter> {
        val places = locations.filter { it.type == LocationType.Place }
        return campaigns.flatMapIndexed { campaignIndex, campaign ->
            val maps = battleMaps.filter { it.campaignId == campaign.id }
            val party = campaignPeople.filter {
                it.campaignId == campaign.id && it.kind == PersonKind.PlayerCharacter
            }
            List(scale.encountersPerCampaign) { index ->
                Encounter(
                    id = "enc-$campaignIndex-$index",
                    campaignId = campaign.id,
                    name = "Encounter ${ENCOUNTER_NAMES[index % ENCOUNTER_NAMES.size]} $index",
                    locationId = places.getOrNull((campaignIndex + index) % places.size.coerceAtLeast(1))?.id,
                    battleMapId = maps.getOrNull(index % maps.size.coerceAtLeast(1))?.id,
                    difficulty = EncounterDifficulty.entries[index % EncounterDifficulty.entries.size],
                    notes = "Table notes $index",
                    outcomeNote = "",
                    status = if (index % 6 == 0) EncounterStatus.Ended else EncounterStatus.Planned,
                    currentRound = 0,
                    currentTurnIndex = 0,
                    participants = listOfNotNull(
                        worldPeople.getOrNull(index)?.let { person ->
                            participant("encp-$campaignIndex-$index-wp", person.name, EncounterParticipantSource.WorldPerson, person.id)
                        },
                        party.getOrNull(index % party.size.coerceAtLeast(1))?.let { person ->
                            participant("encp-$campaignIndex-$index-cp", person.name, EncounterParticipantSource.CampaignPerson, person.id)
                        },
                        EncounterParticipant(
                            id = "encp-$campaignIndex-$index-mob",
                            name = "Nameless guard",
                            source = EncounterParticipantSource.Nameless,
                            sourceId = null,
                            initiativeRoll = 10,
                            initiativeBonus = 1,
                            armorClass = 13,
                            hitPoints = 9,
                            maxHitPoints = 9,
                            temporaryHitPoints = 0,
                            conditions = emptyList(),
                            groupCount = 3,
                            combatState = CombatState.Conscious,
                        ),
                    ),
                    createdAt = now,
                    updatedAt = now,
                )
            }
        }
    }

    private fun participant(
        id: String,
        name: String,
        source: EncounterParticipantSource,
        sourceId: String,
    ): EncounterParticipant {
        return EncounterParticipant(
            id = id,
            name = name,
            source = source,
            sourceId = sourceId,
            initiativeRoll = 12,
            initiativeBonus = 2,
            armorClass = 14,
            hitPoints = 20,
            maxHitPoints = 20,
            temporaryHitPoints = 0,
            conditions = emptyList(),
            groupCount = 1,
            combatState = CombatState.Conscious,
        )
    }

    private fun relationships(
        worldPeople: List<WorldPerson>,
        campaignPeople: List<CampaignPerson>,
    ): List<PersonRelationship> {
        if (worldPeople.size < 2) {
            return emptyList()
        }
        return List(scale.relationships) { index ->
            val from = worldPeople[index % worldPeople.size]
            val toPerson = campaignPeople.getOrNull(index)
            PersonRelationship(
                id = "rel-$index",
                from = PersonRef.World(from.id),
                to = if (toPerson != null) {
                    PersonRef.Campaign(toPerson.id)
                } else {
                    PersonRef.World(worldPeople[(index + 1) % worldPeople.size].id)
                },
                type = RelationshipType.entries[index % RelationshipType.entries.size],
                description = "Generated relationship $index",
                factionLean = if (index % 2 == 0) "Crown" else "Guild",
            )
        }
    }

    private fun companions(
        worldPeople: List<WorldPerson>,
        campaignPeople: List<CampaignPerson>,
    ): List<PersonCompanion> {
        val owners = worldPeople.take(scale.companions)
        if (owners.isEmpty()) {
            return emptyList()
        }
        return owners.mapIndexed { index, owner ->
            val companion = campaignPeople.getOrNull(index) ?: return@mapIndexed null
            PersonCompanion(
                id = "comp-$index",
                owner = PersonRef.World(owner.id),
                companion = PersonRef.Campaign(companion.id),
                kind = if (index % 2 == 0) CompanionKind.Familiar else CompanionKind.AnimalCompanion,
            )
        }.filterNotNull()
    }

    private fun avatars(
        worldPeople: List<WorldPerson>,
        campaignPeople: List<CampaignPerson>,
    ): List<WorldBundle.AvatarFile> {
        val png = BattleMapPngFixture.pngBytes(32, 32)
        val worldAvatars = worldPeople.take(scale.avatarCount / 2).map { person ->
            WorldBundle.AvatarFile(ref = PersonRef.World(person.id), png = png)
        }
        val campaignAvatars = campaignPeople.take(scale.avatarCount - worldAvatars.size).map { person ->
            WorldBundle.AvatarFile(ref = PersonRef.Campaign(person.id), png = png)
        }
        return worldAvatars + campaignAvatars
    }

    private fun mapFiles(
        battleMaps: List<BattleMap>,
        situations: List<BattleMapSituation>,
    ): List<WorldBundle.MapFile> {
        val png = BattleMapPngFixture.pngBytes(64, 64)
        val originals = battleMaps.map { map ->
            WorldBundle.MapFile(battleMapId = map.id, relativePath = "original.png", bytes = png)
        }
        val tiles = battleMaps.map { map ->
            WorldBundle.MapFile(battleMapId = map.id, relativePath = "tiles/0/0_0.png", bytes = png)
        }
        val situationFiles = situations.flatMap { situation ->
            listOf(
                WorldBundle.MapFile(
                    battleMapId = situation.battleMapId,
                    relativePath = "situations/${situation.id}/original.png",
                    bytes = png,
                ),
                WorldBundle.MapFile(
                    battleMapId = situation.battleMapId,
                    relativePath = "situations/${situation.id}/tiles/0/0_0.png",
                    bytes = png,
                ),
            )
        }
        return originals + tiles + situationFiles
    }

    private fun sheetFor(index: Int, kind: PersonKind): FifthEditionSheet {
        if (kind == PersonKind.Monster) {
            return FifthEditionSheet.empty().copy(
                race = "Goblin",
                hitPoints = 7,
                maxHitPoints = 7,
                armorClass = 13,
            )
        }
        return FifthEditionSheet(
            race = RACES[index % RACES.size],
            classLevels = listOf(
                ClassLevel(
                    className = CLASSES[index % CLASSES.size],
                    subclass = "",
                    level = 1 + (index % 8),
                )
            ),
            abilityScores = AbilityScores.average(),
            hitPoints = 12 + index % 20,
            maxHitPoints = 12 + index % 20,
            temporaryHitPoints = 0,
            armorClass = 12 + index % 6,
            deathSaves = DeathSaves.none(),
            items = listOf(InventoryItem(name = "Rations", quantity = 5, notes = "")),
            features = listOf(PersonFeature(name = "Feature", description = "Generated")),
            spells = emptyList(),
            notes = "",
        )
    }

    private companion object {
        val CONTINENTS = listOf("Auralis", "Kethar", "Veld", "Orryn", "Nimbar")
        val AREAS = listOf("Highlands", "Coast", "March", "Wilds")
        val CITIES = listOf("Thornwall", "Silverport", "Duskhaven", "Redhollow")
        val PLACES = listOf("Inn", "Temple", "Sewer", "Manor")
        val FIRST_NAMES = listOf("Iria", "Calder", "Nim", "Bram", "Sera", "Olan", "Vessa", "Jor")
        val SURNAMES = listOf("Vale", "Ashford", "Quill", "Morrow", "Pike", "Rook")
        val LORE_TITLES = listOf("The Sundering", "Old Crowns", "Night Roads", "Salt Law")
        val CAMPAIGN_NAMES = listOf(
            "Crownfall",
            "Salt and Silence",
            "The Ember Compact",
            "Ashen Tide",
        )
        val SESSION_TITLES = listOf("Arrival", "Bargain", "Ambush", "Aftermath")
        val PLOT_TITLES = listOf("Missing heir", "Stolen relic", "False priest", "Border war")
        val QUEST_TITLES = listOf("Recover", "Escort", "Investigate", "Defend")
        val MAP_NAMES = listOf("Tavern", "Crypt", "Harbor", "Ruins", "Bridge", "Keep")
        val ENCOUNTER_NAMES = listOf("Watch", "Bandits", "Beast", "Court")
        val RACES = listOf("Human", "Elf", "Dwarf", "Halfling")
        val CLASSES = listOf("Fighter", "Wizard", "Rogue", "Cleric")
    }
}
