package net.tactware.worldweaver.domain

import java.io.File
import java.time.Instant

internal class DemoWorldBundleFactory(
    private val assetsDir: File = File("fixtures/demo/assets"),
    private val now: Instant = Instant.parse("2026-08-30T15:00:00Z"),
    private val pyramidFactory: BattleMapTilePyramidFactory = BattleMapTilePyramidFactory(),
    private val situationTransformer: BattleMapSituationImageTransformer = BattleMapSituationImageTransformer(),
) {
    fun create(): WorldBundle {
        val world = world()
        val locations = locations(world.id)
        val worldPeople = worldPeople(world.id)
        val loreEntries = loreEntries(world.id, locations, worldPeople)
        val campaign = campaign(world.id)
        val campaignPeople = campaignPeople(campaign.id, worldPeople)
        val locationOverlays = overlays(campaign.id, locations)
        val sessions = sessions(campaign.id, campaignPeople)
        val plotThreads = plotThreads(campaign.id, sessions)
        val referenceDocs = referenceDocs(campaign.id, sessions)
        val quests = quests(campaign.id, locations, loreEntries, worldPeople, campaignPeople, sessions)
        val preparedMaps = battleMaps(campaign.id)
        val battleMaps = preparedMaps.map { it.map }
        val preparedSituations = situations(preparedMaps)
        val encounters = encounters(
            campaignId = campaign.id,
            worldPeople = worldPeople,
            campaignPeople = campaignPeople,
        )
        return WorldBundle(
            formatVersion = WorldBundle.FORMAT_VERSION,
            exportedAt = now,
            world = world,
            calendar = calendar(world.id),
            campaigns = listOf(campaign),
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
            battleMapSituations = preparedSituations.map { it.situation },
            encounters = encounters,
            relationships = relationships(worldPeople, campaignPeople),
            companions = companions(campaignPeople),
            avatarFiles = avatars(worldPeople, campaignPeople),
            mapFiles = mapFiles(preparedMaps, preparedSituations),
        )
    }

    private fun world(): World {
        return World(
            id = WORLD_ID,
            name = "The Shattered Expanse",
            description = "A fog-choked coastal reach where the tide has started pulling old oaths " +
                "out of the mud. The demo is one city and the roads that feed it. Nothing here " +
                "requires the next scene. Follow whatever the table leans toward.",
            defaultGameSystem = GameSystem.FifthEdition,
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun calendar(worldId: String): WorldCalendar {
        return WorldCalendar(
            id = CAL_ID,
            worldId = worldId,
            eraSuffix = "TR",
            months = listOf(
                WorldCalendarMonth(id = MONTH_SALTWAKE, name = "Saltwake", days = 30),
                WorldCalendarMonth(id = MONTH_FOGREACH, name = "Fogreach", days = 30),
                WorldCalendarMonth(id = MONTH_LOWWATCH, name = "Lowwatch", days = 30),
                WorldCalendarMonth(id = MONTH_STORMFLOOD, name = "Stormflood", days = 31),
                WorldCalendarMonth(id = MONTH_EMBERHARBOR, name = "Emberharbor", days = 30),
                WorldCalendarMonth(id = MONTH_ICEGLASS, name = "Iceglass", days = 30),
                WorldCalendarMonth(id = MONTH_THAWBELL, name = "Thawbell", days = 30),
                WorldCalendarMonth(id = MONTH_BRIGHTKEEL, name = "Brightkeel", days = 31),
                WorldCalendarMonth(id = MONTH_HARVESTIDE, name = "Harvestide", days = 30),
                WorldCalendarMonth(id = MONTH_DROWNEDMOON, name = "Drownedmoon", days = 30),
                WorldCalendarMonth(id = MONTH_OATHFALL, name = "Oathfall", days = 30),
                WorldCalendarMonth(id = MONTH_DEEPDARK, name = "Deepdark", days = 31),
            ),
            weekdays = listOf(
                WorldCalendarWeekday(id = "wd-tide", name = "Tideday"),
                WorldCalendarWeekday(id = "wd-lantern", name = "Lantern"),
                WorldCalendarWeekday(id = "wd-bell", name = "Bellday"),
                WorldCalendarWeekday(id = "wd-market", name = "Market"),
                WorldCalendarWeekday(id = "wd-oath", name = "Oathday"),
                WorldCalendarWeekday(id = "wd-fog", name = "Fogday"),
                WorldCalendarWeekday(id = "wd-rest", name = "Restday"),
            ),
            currentDate = WorldDate(year = 847, monthId = MONTH_FOGREACH, day = 8),
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun locations(worldId: String): List<Location> {
        return listOf(
            location(
                id = LOC_EXPANSE,
                worldId = worldId,
                type = LocationType.Continent,
                parentId = null,
                name = "The Shattered Expanse",
                description = "Broken coastline and inland marches left after the Sundering. " +
                    "Most folk never travel farther than the next market town.",
                climate = "Cool, wet, salt-heavy air",
                terrain = "Coast, marsh, low hills",
                government = "Patchwork crowns and harbor councils",
                landmarks = listOf("The Drowned Bell", "The inland salt roads"),
                history = "The Sundering split the old coastal kingdoms. What remains trades on " +
                    "fish, salt, and the memory of better harbors.",
                notes = "Keep the map small. One city is enough for the demo.",
            ),
            location(
                id = LOC_SALTMERE,
                worldId = worldId,
                type = LocationType.Area,
                parentId = LOC_EXPANSE,
                name = "Saltmere Coast",
                description = "A narrow inhabited strip between the fog bank and the inland hills. " +
                    "Couriers still try the old road when the weather allows.",
                climate = "Fog most mornings, hard rain at the turn of tide",
                terrain = "Mudflats, shingle beaches, peat roads",
                government = "Duskhaven harbor council claims the coast. Inland farms ignore them.",
                landmarks = listOf("The courier road", "The wreck line"),
                history = "Saltmere used to send tribute inland. The tribute stopped after the last " +
                    "heir vanished. Nobody agreed what that means.",
                notes = "Use if the party leaves the city. They do not have to.",
            ),
            location(
                id = LOC_DUSKHAVEN,
                worldId = worldId,
                type = LocationType.City,
                parentId = LOC_SALTMERE,
                name = "Duskhaven",
                description = "A working port that pretends it is still important. Lanterns stay " +
                    "lit past midnight because the fog swallows sound as well as light.",
                climate = "Damp and cold even in summer",
                terrain = "Stacked timber, wet cobble, salt-rot cellars",
                government = "Harbor council chaired by Calder Morrow. The shrine answers to Olan Ashford.",
                landmarks = listOf("Harbor Landing", "The Salted Lantern", "Tide Shrine"),
                history = "Old Crowns still have a manor on the ridge. The family has not been " +
                    "seen in public since the winter wrecks.",
                notes = "Party starts here. Current presence is the docks.",
            ),
            location(
                id = LOC_HARBOR,
                worldId = worldId,
                type = LocationType.Place,
                parentId = LOC_DUSKHAVEN,
                name = "Harbor Landing",
                description = "The only pier that still takes strangers. Crates, a crane, two " +
                    "fishing boats that never seem to leave.",
                climate = "Wind off the water",
                terrain = "Wet plank and cobble",
                government = "Harbor watch, when they bother",
                landmarks = listOf("The crane", "Calder's counting shed"),
                history = "Vessa Vale was last seen arguing with a boatman here two nights ago.",
                notes = "Session 1 cold open. Optional dockside shakedown if the table wants a fight.",
            ),
            location(
                id = LOC_LANTERN,
                worldId = worldId,
                type = LocationType.Place,
                parentId = LOC_DUSKHAVEN,
                name = "The Salted Lantern",
                description = "Jor Rook's tavern. Warm, loud, and the only place rumors are cheap.",
                climate = "Hearth-hot, beer-damp",
                terrain = "Dark boards, one stair, a kitchen that never closes",
                government = "Jor's house. His rules.",
                landmarks = listOf("The long table", "The upstairs rooms"),
                history = "Nim grew up in the back stair. Jor still pretends not to notice the lockpicks.",
                notes = "Session 2 if they look for gossip. They can sleep here.",
            ),
            location(
                id = LOC_SHRINE,
                worldId = worldId,
                type = LocationType.Place,
                parentId = LOC_DUSKHAVEN,
                name = "Tide Shrine",
                description = "An older faith house half below the waterline. The drowned bell sits " +
                    "in a pool that should not keep time with the harbor.",
                climate = "Cold, dripping, salt-sweet",
                terrain = "Worn stone, seawater channel, side alcoves",
                government = "Olan Ashford's new vestments over an old rite",
                landmarks = listOf("The drowned bell", "The offering alcoves"),
                history = "The Salt Law says the bell is rung only when a body is given back to the tide. " +
                    "Someone has been ringing it at low watch.",
                notes = "Session 3 if they follow the bell. Husks if they linger after dark.",
            ),
            location(
                id = LOC_VAULTS,
                worldId = worldId,
                type = LocationType.Place,
                parentId = LOC_DUSKHAVEN,
                name = "Old Salt Vaults",
                description = "Disused store halls under the east quay. The grate still locks. " +
                    "The channels still remember the sea.",
                climate = "Still air until the floodgates answer",
                terrain = "Vaulted stone, salt blocks, wooden walkways",
                government = "Council property. Watch keys are missing.",
                landmarks = listOf("The iron grate", "The dry channels"),
                history = "Salt was wealth here. The last shipment was never collected. Something " +
                    "else was stored in the far room.",
                notes = "Session 4 only if they find a key, a rumor, or force the grate. Flood is a situation, not a script.",
            ),
            location(
                id = LOC_MANOR,
                worldId = worldId,
                type = LocationType.Place,
                parentId = LOC_DUSKHAVEN,
                name = "Rookery Manor",
                description = "The Crown family's ridge house. Windows stay shuttered. A clerk " +
                    "in a borrowed coat sometimes answers the side door.",
                climate = "Hill wind, dry compared to the docks",
                terrain = "Stone steps, neglected garden, a side kitchen",
                government = "House Crown, in theory",
                landmarks = listOf("The shuttered hall", "The kitchen door"),
                history = "Lysa Crown is supposed to be dead or missing. The manor still buys bread.",
                notes = "Session 5 if they chase the heir thread. Do not force a finale.",
            ),
        )
    }

    private fun location(
        id: String,
        worldId: String,
        type: LocationType,
        parentId: String?,
        name: String,
        description: String,
        climate: String,
        terrain: String,
        government: String,
        landmarks: List<String>,
        history: String,
        notes: String,
    ): Location {
        return Location(
            id = id,
            worldId = worldId,
            type = type,
            parentLocationId = parentId,
            name = name,
            description = description,
            climate = climate,
            terrain = terrain,
            government = government,
            landmarks = landmarks,
            history = history,
            notes = notes,
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun worldPeople(worldId: String): List<WorldPerson> {
        return listOf(
            worldPerson(
                id = WP_CALDER,
                worldId = worldId,
                kind = PersonKind.Npc,
                name = "Calder Morrow",
                description = "Harbor master. Hired the party to find his courier, not to save the city. " +
                    "Sleeps in the counting shed. Tells the truth in short sentences.",
                sheet = npcSheet(
                    race = "Human",
                    className = "Veteran",
                    level = 3,
                    scores = AbilityScores(14, 10, 13, 12, 14, 11),
                    hitPoints = 22,
                    armorClass = 14,
                    items = listOf(InventoryItem("Harbor keys", 1, "Not the vault grate.")),
                    notes = "Will pay half now, half when the satchel is on his desk.",
                ),
            ),
            worldPerson(
                id = WP_OLAN,
                worldId = worldId,
                kind = PersonKind.Npc,
                name = "Olan Ashford",
                description = "Tide priest in new vestments. Too clean for this port. Offers charity " +
                    "and asks the party to leave the bell alone.",
                sheet = npcSheet(
                    race = "Human",
                    className = "Cleric",
                    level = 4,
                    scores = AbilityScores(10, 12, 12, 13, 16, 15),
                    hitPoints = 28,
                    armorClass = 13,
                    items = listOf(InventoryItem("Bronze bell charm", 1, "Polished yesterday.")),
                    notes = "Knows Lysa is alive. Will not say so unless cornered.",
                ),
            ),
            worldPerson(
                id = WP_VESSA,
                worldId = worldId,
                kind = PersonKind.Npc,
                name = "Vessa Vale",
                description = "Missing courier. Iria's younger sister. Last seen at the landing with " +
                    "a satchel meant for the inland road.",
                sheet = npcSheet(
                    race = "Human",
                    className = "Scout",
                    level = 2,
                    scores = AbilityScores(11, 15, 12, 12, 13, 10),
                    hitPoints = 16,
                    armorClass = 13,
                    items = listOf(InventoryItem("Courier satchel", 1, "Still sealed if they find it.")),
                    notes = "Not dead unless the table decides she is. Default: hiding in the vaults.",
                ),
            ),
            worldPerson(
                id = WP_JOR,
                worldId = worldId,
                kind = PersonKind.Npc,
                name = "Jor Rook",
                description = "Keeps the Salted Lantern. Raised Nim after the winter wrecks. Hears " +
                    "everything and sells only some of it.",
                sheet = npcSheet(
                    race = "Human",
                    className = "Commoner",
                    level = 1,
                    scores = AbilityScores(13, 10, 14, 11, 14, 13),
                    hitPoints = 10,
                    armorClass = 10,
                    items = listOf(InventoryItem("House lantern", 1, "The inn's namesake.")),
                    notes = "Points the party at Olan if they buy a round. Protects Lysa if asked kindly.",
                ),
            ),
            worldPerson(
                id = WP_LYSA,
                worldId = worldId,
                kind = PersonKind.Npc,
                name = "Lysa Crown",
                description = "Missing heir, currently a clerk in a coat that does not fit. Buys bread. " +
                    "Does not want a crown or a rescue.",
                sheet = npcSheet(
                    race = "Human",
                    className = "Noble",
                    level = 2,
                    scores = AbilityScores(9, 13, 11, 14, 12, 16),
                    hitPoints = 14,
                    armorClass = 12,
                    items = listOf(InventoryItem("Hidden signet", 1, "On a chain under the shirt.")),
                    notes = "Optional thread. She can stay a clerk.",
                ),
            ),
            worldPerson(
                id = WP_HUSK,
                worldId = worldId,
                kind = PersonKind.Monster,
                name = "Drowned Husk",
                description = "A waterlogged watchman who did not stay down. Tide-magic, not a mind.",
                sheet = monsterSheet(hitPoints = 11, armorClass = 12, notes = "Slow. Packs of two or three."),
            ),
            worldPerson(
                id = WP_GUARD,
                worldId = worldId,
                kind = PersonKind.Monster,
                name = "Salt-crusted Guard",
                description = "Former vault watch, packed in salt until something called them back.",
                sheet = monsterSheet(hitPoints = 16, armorClass = 15, notes = "Brittle. Thunder and shatter work."),
            ),
            worldPerson(
                id = WP_WIGHT,
                worldId = worldId,
                kind = PersonKind.Monster,
                name = "The Bell Wight",
                description = "A drowned noble bound to the cracked bell. Use only if the table rings it.",
                sheet = monsterSheet(hitPoints = 45, armorClass = 14, notes = "Optional. Not a required boss."),
            ),
        )
    }

    private fun worldPerson(
        id: String,
        worldId: String,
        kind: PersonKind,
        name: String,
        description: String,
        sheet: FifthEditionSheet,
    ): WorldPerson {
        return WorldPerson(
            id = id,
            worldId = worldId,
            kind = kind,
            name = name,
            description = description,
            sheet = sheet,
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun loreEntries(
        worldId: String,
        locations: List<Location>,
        worldPeople: List<WorldPerson>,
    ): List<Lore> {
        val duskhaven = locations.first { it.id == LOC_DUSKHAVEN }
        val shrine = locations.first { it.id == LOC_SHRINE }
        val olan = worldPeople.first { it.id == WP_OLAN }
        val lysa = worldPeople.first { it.id == WP_LYSA }
        return listOf(
            Lore(
                id = LORE_SUNDERING,
                worldId = worldId,
                title = "The Sundering",
                content = "A generation back the coast split. Charts still show roads that end in water. " +
                    "Folk use it as a date, not a theology. The party does not need the true cause.",
                category = LoreCategory.History,
                tags = listOf("setting", "optional"),
                relatedEntryIds = listOf(LORE_CROWNS),
                secrets = listOf(
                    LoreSecret(
                        id = "secret-sundering",
                        title = "The inland version",
                        secret = "Inland courts blame a coastal rite. Coastal priests blame an inland crown. " +
                            "Both are guessing.",
                        hints = listOf(
                            LoreHint(id = "hint-sundering-0", text = "Old charts stop at the fog line.", revealed = true),
                            LoreHint(id = "hint-sundering-1", text = "The manor still keeps a sealed survey.", revealed = false),
                        ),
                    )
                ),
                locationId = duskhaven.id,
                characterId = lysa.id,
                createdAt = now,
                updatedAt = now,
            ),
            Lore(
                id = LORE_SALT_LAW,
                worldId = worldId,
                title = "The Salt Law",
                content = "The older rite: salt on the tongue for a promise, a bell for a body, " +
                    "no coins on the shrine floor. Iria still keeps it. Olan's version is softer and newer.",
                category = LoreCategory.Religion,
                tags = listOf("faith", "iria"),
                relatedEntryIds = listOf(LORE_BELL),
                secrets = listOf(
                    LoreSecret(
                        id = "secret-salt-law",
                        title = "What the new rite dropped",
                        secret = "The Salt Law forbids ringing the bell to bind a spirit. Olan has been doing exactly that.",
                        hints = listOf(
                            LoreHint(id = "hint-salt-0", text = "Iria knows the old words by heart.", revealed = true),
                            LoreHint(id = "hint-salt-1", text = "Olan's hymn book has new pages pasted over old ones.", revealed = false),
                        ),
                    )
                ),
                locationId = shrine.id,
                characterId = olan.id,
                createdAt = now,
                updatedAt = now,
            ),
            Lore(
                id = LORE_BELL,
                worldId = worldId,
                title = "The Drowned Bell",
                content = "A cracked bronze bell in the shrine pool. It should only sound when the tide " +
                    "takes a body. Lately it sounds at low watch, and the water answers.",
                category = LoreCategory.Myth,
                tags = listOf("hook", "shrine"),
                relatedEntryIds = listOf(LORE_SALT_LAW),
                secrets = listOf(
                    LoreSecret(
                        id = "secret-bell",
                        title = "The missing clapper",
                        secret = "The original clapper is in the salt vaults. The shrine bell has a replacement " +
                            "that rings the dead instead of releasing them.",
                        hints = listOf(
                            LoreHint(id = "hint-bell-0", text = "The tone is wrong. Sailors notice.", revealed = true),
                            LoreHint(id = "hint-bell-1", text = "Vessa was carrying a wrapped bronze weight.", revealed = false),
                        ),
                    )
                ),
                locationId = shrine.id,
                characterId = olan.id,
                createdAt = now,
                updatedAt = now,
            ),
            Lore(
                id = LORE_CROWNS,
                worldId = worldId,
                title = "Old Crowns of Duskhaven",
                content = "House Crown held the ridge and the salt charter. After the winter wrecks " +
                    "the family stopped appearing. The council still uses their seal on paper.",
                category = LoreCategory.Politics,
                tags = listOf("heir", "optional"),
                relatedEntryIds = listOf(LORE_SUNDERING),
                secrets = listOf(
                    LoreSecret(
                        id = "secret-crowns",
                        title = "The clerk on the ridge",
                        secret = "Lysa Crown is alive and does not want the charter back. Olan has been using " +
                            "her seal to keep the shrine funded.",
                        hints = listOf(
                            LoreHint(id = "hint-crown-0", text = "The manor still buys bread.", revealed = true),
                            LoreHint(id = "hint-crown-1", text = "Olan's ledgers use the Crown seal.", revealed = false),
                        ),
                    )
                ),
                locationId = duskhaven.id,
                characterId = lysa.id,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    private fun campaign(worldId: String): Campaign {
        return Campaign(
            id = CAMPAIGN_ID,
            worldId = worldId,
            name = "Salt and Silence",
            description = "Find a missing courier in Duskhaven. The job is one satchel. The city " +
                "will offer a shrine mystery, a false priest, and an heir who does not want saving. " +
                "Take any of it. Leave the rest.",
            notes = "Loose prep. Sessions are bags of scenes, not a railroad. If they recover the " +
                "satchel in session 1, the rest becomes optional gossip. If they never enter the " +
                "vaults, the flood stays a rumor.",
            gameSystem = GameSystem.FifthEdition,
            status = CampaignStatus.Active,
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun campaignPeople(
        campaignId: String,
        worldPeople: List<WorldPerson>,
    ): List<CampaignPerson> {
        val pcs = listOf(
            campaignPerson(
                id = CP_BRAM,
                campaignId = campaignId,
                kind = PersonKind.PlayerCharacter,
                name = "Bram Pike",
                description = "Ex-harbor watch. Still has the limp. Took Calder's job because the " +
                    "watch would not.",
                sheet = FifthEditionSheet(
                    race = "Human",
                    classLevels = listOf(ClassLevel("Fighter", "Battle Master", 3)),
                    abilityScores = AbilityScores(16, 12, 15, 10, 13, 11),
                    hitPoints = 28,
                    maxHitPoints = 28,
                    temporaryHitPoints = 0,
                    armorClass = 16,
                    walkSpeed = 30,
                    deathSaves = DeathSaves.none(),
                    items = listOf(
                        InventoryItem("Longsword", 1, "Watch issue, grip rewrapped."),
                        InventoryItem("Rations", 4, ""),
                    ),
                    features = listOf(PersonFeature("Second Wind", "A short breath in a long fight.")),
                    spells = emptyList(),
                    notes = "Knows Calder. Distrusts Olan on sight.",
                ),
                overlayHitPoints = 28,
                overlayNotes = "Standing first watch on the landing.",
            ),
            campaignPerson(
                id = CP_SERA,
                campaignId = campaignId,
                kind = PersonKind.PlayerCharacter,
                name = "Sera Quill",
                description = "Elf scholar sent to copy tide-glyphs. The courier job is a convenient " +
                    "excuse to stay in Duskhaven.",
                sheet = FifthEditionSheet(
                    race = "High Elf",
                    classLevels = listOf(ClassLevel("Wizard", "Diviner", 3)),
                    abilityScores = AbilityScores(8, 14, 12, 16, 13, 12),
                    hitPoints = 18,
                    maxHitPoints = 18,
                    temporaryHitPoints = 0,
                    armorClass = 12,
                    walkSpeed = 30,
                    deathSaves = DeathSaves.none(),
                    items = listOf(
                        InventoryItem("Spellbook", 1, "Tide-glyph rubbings in the back."),
                        InventoryItem("Ink and charcoal", 1, ""),
                    ),
                    features = listOf(PersonFeature("Portent", "Two rolls she wrote down this morning.")),
                    spells = listOf(
                        PersonSpell("Detect Magic", 1, true),
                        PersonSpell("Identify", 1, true),
                        PersonSpell("Misty Step", 2, true),
                    ),
                    notes = "Ash the owl stays on the rail unless she sends him.",
                ),
                overlayHitPoints = 18,
                overlayNotes = "Slightly seasick. Still taking notes.",
            ),
            campaignPerson(
                id = CP_NIM,
                campaignId = campaignId,
                kind = PersonKind.PlayerCharacter,
                name = "Nim Rook",
                description = "Halfling who knows every back stair in Duskhaven. Jor still calls them " +
                    "by a childhood name they have asked him to drop.",
                sheet = FifthEditionSheet(
                    race = "Lightfoot Halfling",
                    classLevels = listOf(ClassLevel("Rogue", "Thief", 3)),
                    abilityScores = AbilityScores(10, 17, 12, 13, 11, 14),
                    hitPoints = 21,
                    maxHitPoints = 21,
                    temporaryHitPoints = 0,
                    armorClass = 14,
                    walkSpeed = 25,
                    deathSaves = DeathSaves.none(),
                    items = listOf(
                        InventoryItem("Thieves' tools", 1, "Jor pretends not to see them."),
                        InventoryItem("Shortsword", 1, ""),
                    ),
                    features = listOf(PersonFeature("Second-Story Work", "The lantern's roof is a highway.")),
                    spells = emptyList(),
                    notes = "Will steal the sermon plate if Olan is rude.",
                ),
                overlayHitPoints = 21,
                overlayNotes = "Already checked the alley behind the lantern.",
            ),
            campaignPerson(
                id = CP_IRIA,
                campaignId = campaignId,
                kind = PersonKind.PlayerCharacter,
                name = "Iria Vale",
                description = "Cleric of the Salt Law. Came for her sister Vessa. Will not take " +
                    "Olan's blessing.",
                sheet = FifthEditionSheet(
                    race = "Human",
                    classLevels = listOf(ClassLevel("Cleric", "Life", 3)),
                    abilityScores = AbilityScores(13, 10, 14, 11, 16, 13),
                    hitPoints = 24,
                    maxHitPoints = 24,
                    temporaryHitPoints = 0,
                    armorClass = 16,
                    walkSpeed = 30,
                    deathSaves = DeathSaves.none(),
                    items = listOf(
                        InventoryItem("Shell-and-iron symbol", 1, "Old rite."),
                        InventoryItem("Mace", 1, ""),
                    ),
                    features = listOf(PersonFeature("Disciple of Life", "Healing that actually sticks.")),
                    spells = listOf(
                        PersonSpell("Cure Wounds", 1, true),
                        PersonSpell("Guiding Bolt", 1, true),
                        PersonSpell("Spiritual Weapon", 2, true),
                    ),
                    notes = "Will walk into the shrine even if the others will not.",
                ),
                overlayHitPoints = 24,
                overlayNotes = "Holding the old words under her breath.",
            ),
            campaignPerson(
                id = CP_ASH,
                campaignId = campaignId,
                kind = PersonKind.Npc,
                name = "Ash",
                description = "Sera's coastal owl familiar. Copper ring on one leg. Delivers scraps " +
                    "of paper and judges everyone.",
                sheet = FifthEditionSheet.empty().copy(
                    race = "Owl",
                    hitPoints = 1,
                    maxHitPoints = 1,
                    armorClass = 12,
                    walkSpeed = 5,
                    notes = "Fly 60. Does not fight unless the table is being cute.",
                ),
                overlayHitPoints = 1,
                overlayNotes = "Perched on the landing rail.",
            ),
        )
        val overlays = listOf(WP_CALDER, WP_OLAN, WP_VESSA, WP_JOR, WP_LYSA).map { worldId ->
            val source = worldPeople.first { it.id == worldId }
            campaignPerson(
                id = "cp-$worldId",
                campaignId = campaignId,
                worldPersonId = source.id,
                kind = PersonKind.Npc,
                name = source.name,
                description = source.description,
                sheet = source.sheet,
                overlayHitPoints = source.sheet.hitPoints,
                overlayNotes = when (source.id) {
                    WP_CALDER -> "Waiting at the counting shed."
                    WP_OLAN -> "Holding evening rite whether anyone comes or not."
                    WP_VESSA -> "Missing. Do not place her until they find her."
                    WP_JOR -> "Behind the bar."
                    else -> "At the manor kitchen door if they go."
                },
            )
        }
        return pcs + overlays
    }

    private fun campaignPerson(
        id: String,
        campaignId: String,
        kind: PersonKind,
        name: String,
        description: String,
        sheet: FifthEditionSheet,
        overlayHitPoints: Int?,
        overlayNotes: String,
        worldPersonId: String? = null,
    ): CampaignPerson {
        return CampaignPerson(
            id = id,
            campaignId = campaignId,
            worldPersonId = worldPersonId,
            kind = kind,
            name = name,
            description = description,
            sheet = sheet,
            overlayHitPoints = overlayHitPoints,
            overlayNotes = overlayNotes,
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun overlays(campaignId: String, locations: List<Location>): List<LocationOverlay> {
        val byId = locations.associateBy { it.id }
        return listOf(
            LocationOverlay(
                campaignId = campaignId,
                locationId = byId.getValue(LOC_HARBOR).id,
                hasPartyPresence = true,
                notes = "Current. They just stepped off the boat.",
                updatedAt = now,
            ),
            LocationOverlay(
                campaignId = campaignId,
                locationId = byId.getValue(LOC_LANTERN).id,
                hasPartyPresence = false,
                notes = "Rooms held if they want them. Jor already set a pot on.",
                updatedAt = now,
            ),
            LocationOverlay(
                campaignId = campaignId,
                locationId = byId.getValue(LOC_SHRINE).id,
                hasPartyPresence = false,
                notes = "Bell sounded last night. No party marker yet.",
                updatedAt = now,
            ),
        )
    }

    private fun sessions(
        campaignId: String,
        campaignPeople: List<CampaignPerson>,
    ): List<Session> {
        val march = campaignPeople.filter { it.kind == PersonKind.PlayerCharacter }.mapIndexed { index, person ->
            MarchOrderEntry(
                id = "march-$index",
                person = PersonRef.Campaign(person.id),
                displayName = person.name,
            )
        }
        return listOf(
            Session(
                id = SESS_LANDING,
                campaignId = campaignId,
                name = "Session 1: Fog on the Landing",
                notes = "Cold open on the pier. Calder pays half and describes Vessa. " +
                    "If they wander, they hear the bell and the lantern. If they fight, use the " +
                    "dockside shakedown. Do not invent a deadline.",
                inWorldDate = WorldDate(year = 847, monthId = MONTH_SALTWAKE, day = 12),
                scenes = listOf(
                    SessionScene(id = "scene-1-0", title = "The counting shed", notes = "Calder, the job, half pay."),
                    SessionScene(id = "scene-1-1", title = "Dock talk", notes = "Optional. Boatmen, a stolen crate, the bell."),
                    SessionScene(id = "scene-1-2", title = "Where next", notes = "Lantern, shrine, or sleep on the boat."),
                ),
                marchOrder = march,
                createdAt = now,
                updatedAt = now,
            ),
            Session(
                id = SESS_LANTERN,
                campaignId = campaignId,
                name = "Session 2: Lanterns After Midnight",
                notes = "Only if they want gossip or a bed. Jor trades stories for coin or a favor. " +
                    "Olan may come in to preach. A cutpurse is available, not required.",
                inWorldDate = WorldDate(year = 847, monthId = MONTH_SALTWAKE, day = 14),
                scenes = listOf(
                    SessionScene(id = "scene-2-0", title = "A round for the house", notes = "Jor, rumors, Nim's old room."),
                    SessionScene(id = "scene-2-1", title = "The too-clean priest", notes = "Olan asks them to stay away from the bell."),
                    SessionScene(id = "scene-2-2", title = "After hours", notes = "Optional brawl or a stolen purse."),
                ),
                marchOrder = march,
                createdAt = now,
                updatedAt = now,
            ),
            Session(
                id = SESS_SHRINE,
                campaignId = campaignId,
                name = "Session 3: What the Tide Keeps",
                notes = "If they follow the bell or Iria's rite. The shrine can be talk, search, or " +
                    "husks after dark. Finding the wrong clapper is enough of a win.",
                inWorldDate = WorldDate(year = 847, monthId = MONTH_FOGREACH, day = 2),
                scenes = listOf(
                    SessionScene(id = "scene-3-0", title = "Daylight rite", notes = "Olan is present. The pool is quiet."),
                    SessionScene(id = "scene-3-1", title = "The pool", notes = "Bell, offerings, a wrapped bronze scrap."),
                    SessionScene(id = "scene-3-2", title = "Low watch", notes = "Optional husks. Do not spring them in daylight unless asked."),
                ),
                marchOrder = march,
                createdAt = now,
                updatedAt = now,
            ),
            Session(
                id = SESS_VAULTS,
                campaignId = campaignId,
                name = "Session 4: The Vault Door",
                notes = "Only if they found a key, a rumor, or kicked the grate. Vessa's default " +
                    "hiding place. Flood situation is a lever, not a scripted collapse.",
                inWorldDate = WorldDate(year = 847, monthId = MONTH_FOGREACH, day = 5),
                scenes = listOf(
                    SessionScene(id = "scene-4-0", title = "The grate", notes = "Locked. Nim, Calder's keys, or force."),
                    SessionScene(id = "scene-4-1", title = "Dry halls", notes = "Salt, chests, a satchel if you want it here."),
                    SessionScene(id = "scene-4-2", title = "The channels remember", notes = "Toggle flood if someone pulls the wrong chain."),
                ),
                marchOrder = march,
                createdAt = now,
                updatedAt = now,
            ),
            Session(
                id = SESS_MANOR,
                campaignId = campaignId,
                name = "Session 5: A Name in the Manor",
                notes = "Not a finale. If they chase the heir, Lysa asks them to leave her as a clerk. " +
                    "If they never come, the campaign can end at the satchel.",
                inWorldDate = WorldDate(year = 847, monthId = MONTH_FOGREACH, day = 8),
                scenes = listOf(
                    SessionScene(id = "scene-5-0", title = "Kitchen door", notes = "Lysa, bread, a refusal to be crowned."),
                    SessionScene(id = "scene-5-1", title = "What they do with that", notes = "Tell Calder, tell Olan, or keep it."),
                ),
                marchOrder = march,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    private fun plotThreads(campaignId: String, sessions: List<Session>): List<PlotThread> {
        val byId = sessions.associateBy { it.id }
        return listOf(
            PlotThread(
                id = "plot-courier",
                campaignId = campaignId,
                sessionId = byId.getValue(SESS_LANDING).id,
                title = "The courier never made the inland road",
                details = "Vessa took a sealed satchel from Calder and vanished. Default: she is " +
                    "alive in the vaults. Change that if the table needs a body.",
                status = PlotThreadStatus.InProgress,
                priority = PlotThreadPriority.High,
                createdAt = now,
                updatedAt = now,
            ),
            PlotThread(
                id = "plot-clapper",
                campaignId = campaignId,
                sessionId = byId.getValue(SESS_SHRINE).id,
                title = "The clapper is the wrong bronze",
                details = "The shrine bell rings the dead. The original clapper is wrapped in the " +
                    "satchel or sitting in the far vault. Either placement is fine.",
                status = PlotThreadStatus.Open,
                priority = PlotThreadPriority.High,
                createdAt = now,
                updatedAt = now,
            ),
            PlotThread(
                id = "plot-olan",
                campaignId = campaignId,
                sessionId = byId.getValue(SESS_LANTERN).id,
                title = "Olan is too clean",
                details = "He may be a fraud, a desperate binder, or both. Let the table decide if " +
                    "he is a villain or a man in over his head.",
                status = PlotThreadStatus.Open,
                priority = PlotThreadPriority.Medium,
                createdAt = now,
                updatedAt = now,
            ),
            PlotThread(
                id = "plot-heir",
                campaignId = campaignId,
                sessionId = byId.getValue(SESS_MANOR).id,
                title = "The clerk on the ridge",
                details = "Lysa Crown is optional. She does not want saving. Drop this if the table " +
                    "is happy with the satchel.",
                status = PlotThreadStatus.Open,
                priority = PlotThreadPriority.Low,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    private fun referenceDocs(campaignId: String, sessions: List<Session>): List<ReferenceDoc> {
        val landing = sessions.first { it.id == SESS_LANDING }
        val shrine = sessions.first { it.id == SESS_SHRINE }
        return listOf(
            ReferenceDoc(
                id = "ref-courier-letter",
                campaignId = campaignId,
                sessionId = landing.id,
                title = "Courier's last note",
                pathOrUrl = "handouts/vessa-last-note.txt",
                createdAt = now,
                updatedAt = now,
            ),
            ReferenceDoc(
                id = "ref-glyph",
                campaignId = campaignId,
                sessionId = shrine.id,
                title = "Tide-glyph rubbing",
                pathOrUrl = "handouts/tide-glyph.png",
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    private fun quests(
        campaignId: String,
        locations: List<Location>,
        loreEntries: List<Lore>,
        worldPeople: List<WorldPerson>,
        campaignPeople: List<CampaignPerson>,
        sessions: List<Session>,
    ): List<Quest> {
        val harbor = locations.first { it.id == LOC_HARBOR }
        val shrine = locations.first { it.id == LOC_SHRINE }
        val manor = locations.first { it.id == LOC_MANOR }
        val lantern = locations.first { it.id == LOC_LANTERN }
        val vessa = worldPeople.first { it.id == WP_VESSA }
        val olan = worldPeople.first { it.id == WP_OLAN }
        val lysa = worldPeople.first { it.id == WP_LYSA }
        val calderPc = campaignPeople.first { it.worldPersonId == WP_CALDER }
        val iria = campaignPeople.first { it.id == CP_IRIA }
        val landing = sessions.first { it.id == SESS_LANDING }
        val shrineSession = sessions.first { it.id == SESS_SHRINE }
        val manorSession = sessions.first { it.id == SESS_MANOR }
        val lanternSession = sessions.first { it.id == SESS_LANTERN }
        val bellLore = loreEntries.first { it.id == LORE_BELL }
        val saltLaw = loreEntries.first { it.id == LORE_SALT_LAW }
        val crowns = loreEntries.first { it.id == LORE_CROWNS }
        return listOf(
            Quest(
                id = "quest-satchel",
                campaignId = campaignId,
                title = "The Courier's Satchel",
                summary = "Calder wants the satchel on his desk. Finding Vessa is extra. The party " +
                    "can return the bag and walk away.",
                status = QuestStatus.Active,
                locationId = harbor.id,
                objectives = listOf(
                    QuestObjective(id = "obj-satchel-0", title = "Hear Calder's offer", status = QuestObjectiveStatus.Complete),
                    QuestObjective(id = "obj-satchel-1", title = "Learn Vessa's last steps", status = QuestObjectiveStatus.Open),
                    QuestObjective(id = "obj-satchel-2", title = "Recover the sealed satchel", status = QuestObjectiveStatus.Open),
                ),
                links = listOf(
                    QuestLink(id = "ql-satchel-vessa", kind = QuestLinkKind.WORLD_PERSON, targetId = vessa.id),
                    QuestLink(id = "ql-satchel-calder", kind = QuestLinkKind.CAMPAIGN_PERSON, targetId = calderPc.id),
                    QuestLink(id = "ql-satchel-sess", kind = QuestLinkKind.SESSION, targetId = landing.id),
                ),
                createdAt = now,
                updatedAt = now,
            ),
            Quest(
                id = "quest-bell",
                campaignId = campaignId,
                title = "Who Rings the Drowned Bell",
                summary = "The tone is wrong. Someone replaced the clapper. Follow only if the table " +
                    "cares about the shrine.",
                status = QuestStatus.Active,
                locationId = shrine.id,
                objectives = listOf(
                    QuestObjective(id = "obj-bell-0", title = "Hear the wrong tone", status = QuestObjectiveStatus.Open),
                    QuestObjective(id = "obj-bell-1", title = "Find the original clapper", status = QuestObjectiveStatus.Open),
                ),
                links = listOf(
                    QuestLink(id = "ql-bell-lore", kind = QuestLinkKind.LORE, targetId = bellLore.id),
                    QuestLink(id = "ql-bell-olan", kind = QuestLinkKind.WORLD_PERSON, targetId = olan.id),
                    QuestLink(id = "ql-bell-sess", kind = QuestLinkKind.SESSION, targetId = shrineSession.id),
                ),
                createdAt = now,
                updatedAt = now,
            ),
            Quest(
                id = "quest-heir",
                campaignId = campaignId,
                title = "Keep the Heir Breathing",
                summary = "Optional. Lysa is alive and does not want a throne. Completing this can " +
                    "mean leaving her alone.",
                status = QuestStatus.Active,
                locationId = manor.id,
                objectives = listOf(
                    QuestObjective(id = "obj-heir-0", title = "Confirm the manor still eats", status = QuestObjectiveStatus.Open),
                    QuestObjective(id = "obj-heir-1", title = "Speak with Lysa, or decide not to", status = QuestObjectiveStatus.Open),
                ),
                links = listOf(
                    QuestLink(id = "ql-heir-lore", kind = QuestLinkKind.LORE, targetId = crowns.id),
                    QuestLink(id = "ql-heir-lysa", kind = QuestLinkKind.WORLD_PERSON, targetId = lysa.id),
                    QuestLink(id = "ql-heir-sess", kind = QuestLinkKind.SESSION, targetId = manorSession.id),
                ),
                createdAt = now,
                updatedAt = now,
            ),
            Quest(
                id = "quest-charity",
                campaignId = campaignId,
                title = "Charity of Salt",
                summary = "Olan asked the party to take alms to the lantern poor. They did. It was " +
                    "a way to keep them away from the shrine.",
                status = QuestStatus.Completed,
                locationId = lantern.id,
                objectives = listOf(
                    QuestObjective(id = "obj-charity-0", title = "Carry Olan's alms to Jor", status = QuestObjectiveStatus.Complete),
                    QuestObjective(id = "obj-charity-1", title = "Notice the alms were already Jor's coin", status = QuestObjectiveStatus.Complete),
                ),
                links = listOf(
                    QuestLink(id = "ql-charity-lore", kind = QuestLinkKind.LORE, targetId = saltLaw.id),
                    QuestLink(id = "ql-charity-iria", kind = QuestLinkKind.CAMPAIGN_PERSON, targetId = iria.id),
                    QuestLink(id = "ql-charity-sess", kind = QuestLinkKind.SESSION, targetId = lanternSession.id),
                ),
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    private fun battleMaps(campaignId: String): List<PreparedMap> {
        return listOf(
            preparedMap(MAP_HARBOR, campaignId, "Harbor Landing", "map-harbor-landing.png"),
            preparedMap(MAP_LANTERN, campaignId, "The Salted Lantern", "map-salted-lantern.png"),
            preparedMap(MAP_SHRINE, campaignId, "Tide Shrine Crypt", "map-tide-shrine.png"),
            preparedMap(MAP_VAULTS, campaignId, "Old Salt Vaults", "map-salt-vaults.png"),
        )
    }

    private fun preparedMap(
        id: String,
        campaignId: String,
        name: String,
        assetName: String,
    ): PreparedMap {
        val pyramid = pyramidFactory.create(readAsset(assetName))
            ?: error("Could not tile battle map $assetName")
        return PreparedMap(
            map = BattleMap(
                id = id,
                campaignId = campaignId,
                name = name,
                originalWidth = pyramid.originalWidth,
                originalHeight = pyramid.originalHeight,
                tileSizePx = pyramid.tileSizePx,
                minZoom = pyramid.minZoom,
                maxZoom = pyramid.maxZoom,
                columns = 16,
                rows = 16,
                unitName = "ft",
                unitsPerTile = 5.0,
                createdAt = now,
                updatedAt = now,
            ),
            pyramid = pyramid,
        )
    }

    private fun situations(preparedMaps: List<PreparedMap>): List<PreparedSituation> {
        val vaults = preparedMaps.first { it.map.id == MAP_VAULTS }
        val floodPng = situationTransformer.transform(
            imagePng = readAsset("map-salt-vaults-flood.png"),
            targetWidth = vaults.pyramid.originalWidth,
            targetHeight = vaults.pyramid.originalHeight,
        ) ?: error("Could not fit the vault flood overlay")
        val floodPyramid = pyramidFactory.create(floodPng)
            ?: error("Could not tile the vault flood overlay")
        return listOf(
            PreparedSituation(
                situation = BattleMapSituation(
                    id = SIT_FLOOD,
                    battleMapId = vaults.map.id,
                    name = "Flooded channels",
                    visible = false,
                    sortIndex = 0,
                    createdAt = now,
                    updatedAt = now,
                ),
                pyramid = floodPyramid,
            )
        )
    }

    private fun encounters(
        campaignId: String,
        worldPeople: List<WorldPerson>,
        campaignPeople: List<CampaignPerson>,
    ): List<Encounter> {
        val party = campaignPeople.filter { it.kind == PersonKind.PlayerCharacter }
        val husk = worldPeople.first { it.id == WP_HUSK }
        val guard = worldPeople.first { it.id == WP_GUARD }
        val wight = worldPeople.first { it.id == WP_WIGHT }
        return listOf(
            Encounter(
                id = "enc-dockside",
                campaignId = campaignId,
                name = "Dockside shakedown",
                locationId = LOC_HARBOR,
                battleMapId = MAP_HARBOR,
                difficulty = EncounterDifficulty.Easy,
                notes = "Three nameless dock toughs lean on newcomers. They run if someone bleeds. " +
                    "Skip it if the table wants to talk.",
                outcomeNote = "",
                status = EncounterStatus.Planned,
                currentRound = 0,
                currentTurnIndex = 0,
                participants = partyPlaced("dock", party, startColumn = 4, row = 12) + listOf(
                    nameless("encp-dock-0", "Dock tough", 12, 9, 16, column = 10, row = 6),
                    nameless("encp-dock-1", "Dock tough", 11, 9, 16, column = 12, row = 7),
                    nameless("encp-dock-2", "Dock tough", 10, 9, 16, column = 11, row = 5),
                ),
                createdAt = now,
                updatedAt = now,
            ),
            Encounter(
                id = "enc-lantern",
                campaignId = campaignId,
                name = "Midnight in the Lantern",
                locationId = LOC_LANTERN,
                battleMapId = MAP_LANTERN,
                difficulty = EncounterDifficulty.Easy,
                notes = "A cutpurse and a drunk cousin. Jor wants furniture left standing.",
                outcomeNote = "",
                status = EncounterStatus.Planned,
                currentRound = 0,
                currentTurnIndex = 0,
                participants = partyPlaced("lantern", party, startColumn = 3, row = 10) + listOf(
                    nameless("encp-lantern-0", "Cutpurse", 14, 8, 12, column = 9, row = 6),
                    nameless("encp-lantern-1", "Drunk cousin", 8, 10, 18, column = 7, row = 4),
                ),
                createdAt = now,
                updatedAt = now,
            ),
            Encounter(
                id = "enc-shrine",
                campaignId = campaignId,
                name = "Husks after low watch",
                locationId = LOC_SHRINE,
                battleMapId = MAP_SHRINE,
                difficulty = EncounterDifficulty.Medium,
                notes = "Use after dark or if they ring the bell. Daylight can stay empty.",
                outcomeNote = "",
                status = EncounterStatus.Planned,
                currentRound = 0,
                currentTurnIndex = 0,
                participants = partyPlaced("shrine", party, startColumn = 2, row = 13) + listOf(
                    fromWorld("encp-husk-0", husk, 8, column = 8, row = 6),
                    fromWorld("encp-husk-1", husk, 7, column = 10, row = 7),
                    fromWorld("encp-husk-2", husk, 6, column = 7, row = 4),
                ),
                createdAt = now,
                updatedAt = now,
            ),
            Encounter(
                id = "enc-vaults",
                campaignId = campaignId,
                name = "When the grate gives",
                locationId = LOC_VAULTS,
                battleMapId = MAP_VAULTS,
                difficulty = EncounterDifficulty.Hard,
                notes = "Salt guards in the far hall. Flip the flood situation if someone pulls the chain.",
                outcomeNote = "",
                status = EncounterStatus.Planned,
                currentRound = 0,
                currentTurnIndex = 0,
                participants = partyPlaced("vaults", party, startColumn = 2, row = 14) + listOf(
                    fromWorld("encp-guard-0", guard, 9, column = 11, row = 5),
                    fromWorld("encp-guard-1", guard, 8, column = 13, row = 6),
                ),
                createdAt = now,
                updatedAt = now,
            ),
            Encounter(
                id = "enc-bell",
                campaignId = campaignId,
                name = "If they ring it",
                locationId = LOC_SHRINE,
                battleMapId = MAP_SHRINE,
                difficulty = EncounterDifficulty.Deadly,
                notes = "Optional. The Bell Wight only if they restore or strike the bell on purpose.",
                outcomeNote = "",
                status = EncounterStatus.Planned,
                currentRound = 0,
                currentTurnIndex = 0,
                participants = partyPlaced("bell", party, startColumn = 3, row = 12) + listOf(
                    fromWorld("encp-wight", wight, 14, column = 8, row = 5),
                ),
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    private fun partyPlaced(
        encounterKey: String,
        party: List<CampaignPerson>,
        startColumn: Int,
        row: Int,
    ): List<EncounterParticipant> {
        return party.mapIndexed { index, person ->
            EncounterParticipant(
                id = "encp-$encounterKey-${person.id}",
                name = person.name,
                source = EncounterParticipantSource.CampaignPerson,
                sourceId = person.id,
                initiativeRoll = null,
                initiativeBonus = 2,
                armorClass = person.sheet.armorClass,
                hitPoints = person.sheet.hitPoints,
                maxHitPoints = person.sheet.maxHitPoints,
                temporaryHitPoints = 0,
                conditions = emptyList(),
                groupCount = 1,
                combatState = CombatState.Conscious,
                gridColumn = startColumn + index,
                gridRow = row,
            )
        }
    }

    private fun nameless(
        id: String,
        name: String,
        initiative: Int,
        armorClass: Int,
        hitPoints: Int,
        column: Int,
        row: Int,
    ): EncounterParticipant {
        return EncounterParticipant(
            id = id,
            name = name,
            source = EncounterParticipantSource.Nameless,
            sourceId = null,
            initiativeRoll = initiative,
            initiativeBonus = 1,
            armorClass = armorClass,
            hitPoints = hitPoints,
            maxHitPoints = hitPoints,
            temporaryHitPoints = 0,
            conditions = emptyList(),
            groupCount = 1,
            combatState = CombatState.Conscious,
            gridColumn = column,
            gridRow = row,
        )
    }

    private fun fromWorld(
        id: String,
        person: WorldPerson,
        initiative: Int,
        column: Int,
        row: Int,
    ): EncounterParticipant {
        return EncounterParticipant(
            id = id,
            name = person.name,
            source = EncounterParticipantSource.WorldPerson,
            sourceId = person.id,
            initiativeRoll = initiative,
            initiativeBonus = 1,
            armorClass = person.sheet.armorClass,
            hitPoints = person.sheet.hitPoints,
            maxHitPoints = person.sheet.maxHitPoints,
            temporaryHitPoints = 0,
            conditions = emptyList(),
            groupCount = 1,
            combatState = CombatState.Conscious,
            gridColumn = column,
            gridRow = row,
        )
    }

    private fun relationships(
        worldPeople: List<WorldPerson>,
        campaignPeople: List<CampaignPerson>,
    ): List<PersonRelationship> {
        val iria = campaignPeople.first { it.id == CP_IRIA }
        val nim = campaignPeople.first { it.id == CP_NIM }
        val bram = campaignPeople.first { it.id == CP_BRAM }
        val vessa = worldPeople.first { it.id == WP_VESSA }
        val jor = worldPeople.first { it.id == WP_JOR }
        val calder = worldPeople.first { it.id == WP_CALDER }
        val olan = worldPeople.first { it.id == WP_OLAN }
        val lysa = worldPeople.first { it.id == WP_LYSA }
        return listOf(
            PersonRelationship(
                id = "rel-iria-vessa",
                from = PersonRef.Campaign(iria.id),
                to = PersonRef.World(vessa.id),
                type = RelationshipType.Sibling,
                description = "Iria came to Duskhaven for Vessa. She will take the satchel second.",
                factionLean = "Salt Law",
            ),
            PersonRelationship(
                id = "rel-jor-nim",
                from = PersonRef.World(jor.id),
                to = PersonRef.Campaign(nim.id),
                type = RelationshipType.Other,
                description = "Jor raised Nim after the wrecks. Not blood. Still family.",
                factionLean = "The Lantern",
            ),
            PersonRelationship(
                id = "rel-calder-vessa",
                from = PersonRef.World(calder.id),
                to = PersonRef.World(vessa.id),
                type = RelationshipType.Ally,
                description = "Calder hired her. He wants the bag more than he wants to admit he likes her.",
                factionLean = "Harbor council",
            ),
            PersonRelationship(
                id = "rel-bram-calder",
                from = PersonRef.Campaign(bram.id),
                to = PersonRef.World(calder.id),
                type = RelationshipType.Ally,
                description = "Old watch. Bram still limps from a job Calder sent him on.",
                factionLean = "Harbor watch",
            ),
            PersonRelationship(
                id = "rel-olan-lysa",
                from = PersonRef.World(olan.id),
                to = PersonRef.World(lysa.id),
                type = RelationshipType.Other,
                description = "Olan knows she lives. He has been using the Crown seal for shrine coin.",
                factionLean = "Tide shrine",
            ),
        )
    }

    private fun companions(campaignPeople: List<CampaignPerson>): List<PersonCompanion> {
        val sera = campaignPeople.first { it.id == CP_SERA }
        val ash = campaignPeople.first { it.id == CP_ASH }
        return listOf(
            PersonCompanion(
                id = "comp-ash",
                owner = PersonRef.Campaign(sera.id),
                companion = PersonRef.Campaign(ash.id),
                kind = CompanionKind.Familiar,
            )
        )
    }

    private fun avatars(
        worldPeople: List<WorldPerson>,
        campaignPeople: List<CampaignPerson>,
    ): List<WorldBundle.AvatarFile> {
        val worldFiles = worldPeople.map { person ->
            WorldBundle.AvatarFile(
                ref = PersonRef.World(person.id),
                png = readAsset(WORLD_AVATARS.getValue(person.id)),
            )
        }
        val campaignFiles = campaignPeople.mapNotNull { person ->
            val asset = CAMPAIGN_AVATARS[person.id]
                ?: WORLD_AVATARS[person.worldPersonId]
                ?: return@mapNotNull null
            WorldBundle.AvatarFile(
                ref = PersonRef.Campaign(person.id),
                png = readAsset(asset),
            )
        }
        return worldFiles + campaignFiles
    }

    private fun mapFiles(
        preparedMaps: List<PreparedMap>,
        preparedSituations: List<PreparedSituation>,
    ): List<WorldBundle.MapFile> {
        val originals = preparedMaps.flatMap { prepared ->
            filesForPyramid(prepared.map.id, prepared.pyramid, relativePrefix = "")
        }
        val situations = preparedSituations.flatMap { prepared ->
            filesForPyramid(
                battleMapId = prepared.situation.battleMapId,
                pyramid = prepared.pyramid,
                relativePrefix = "situations/${prepared.situation.id}/",
            )
        }
        return originals + situations
    }

    private fun filesForPyramid(
        battleMapId: String,
        pyramid: BattleMapTilePyramid,
        relativePrefix: String,
    ): List<WorldBundle.MapFile> {
        val original = WorldBundle.MapFile(
            battleMapId = battleMapId,
            relativePath = "${relativePrefix}original.png",
            bytes = pyramid.originalPng,
        )
        val tiles = pyramid.tiles.map { tile ->
            WorldBundle.MapFile(
                battleMapId = battleMapId,
                relativePath = "${relativePrefix}tiles/${tile.zoom}/${tile.x}_${tile.y}.png",
                bytes = tile.imagePng,
            )
        }
        return listOf(original) + tiles
    }

    private fun npcSheet(
        race: String,
        className: String,
        level: Int,
        scores: AbilityScores,
        hitPoints: Int,
        armorClass: Int,
        items: List<InventoryItem>,
        notes: String,
    ): FifthEditionSheet {
        return FifthEditionSheet(
            race = race,
            classLevels = listOf(ClassLevel(className, "", level)),
            abilityScores = scores,
            hitPoints = hitPoints,
            maxHitPoints = hitPoints,
            temporaryHitPoints = 0,
            armorClass = armorClass,
            walkSpeed = 30,
            deathSaves = DeathSaves.none(),
            items = items,
            features = emptyList(),
            spells = emptyList(),
            notes = notes,
        )
    }

    private fun monsterSheet(hitPoints: Int, armorClass: Int, notes: String): FifthEditionSheet {
        return FifthEditionSheet.empty().copy(
            race = "Undead",
            hitPoints = hitPoints,
            maxHitPoints = hitPoints,
            armorClass = armorClass,
            notes = notes,
        )
    }

    private fun readAsset(fileName: String): ByteArray {
        val file = File(assetsDir, fileName)
        check(file.isFile) { "Missing demo asset: ${file.absolutePath}" }
        return file.readBytes()
    }

    private data class PreparedMap(
        val map: BattleMap,
        val pyramid: BattleMapTilePyramid,
    )

    private data class PreparedSituation(
        val situation: BattleMapSituation,
        val pyramid: BattleMapTilePyramid,
    )

    private companion object {
        const val WORLD_ID = "world-saltmere"
        const val CAL_ID = "cal-saltmere"
        const val MONTH_SALTWAKE = "month-saltwake"
        const val MONTH_FOGREACH = "month-fogreach"
        const val MONTH_LOWWATCH = "month-lowwatch"
        const val MONTH_STORMFLOOD = "month-stormflood"
        const val MONTH_EMBERHARBOR = "month-emberharbor"
        const val MONTH_ICEGLASS = "month-iceglass"
        const val MONTH_THAWBELL = "month-thawbell"
        const val MONTH_BRIGHTKEEL = "month-brightkeel"
        const val MONTH_HARVESTIDE = "month-harvestide"
        const val MONTH_DROWNEDMOON = "month-drownedmoon"
        const val MONTH_OATHFALL = "month-oathfall"
        const val MONTH_DEEPDARK = "month-deepdark"
        const val CAMPAIGN_ID = "camp-salt-and-silence"
        const val LOC_EXPANSE = "loc-expanse"
        const val LOC_SALTMERE = "loc-saltmere"
        const val LOC_DUSKHAVEN = "loc-duskhaven"
        const val LOC_HARBOR = "loc-harbor"
        const val LOC_LANTERN = "loc-lantern"
        const val LOC_SHRINE = "loc-shrine"
        const val LOC_VAULTS = "loc-vaults"
        const val LOC_MANOR = "loc-manor"
        const val WP_CALDER = "wp-calder"
        const val WP_OLAN = "wp-olan"
        const val WP_VESSA = "wp-vessa"
        const val WP_JOR = "wp-jor"
        const val WP_LYSA = "wp-lysa"
        const val WP_HUSK = "wp-husk"
        const val WP_GUARD = "wp-guard"
        const val WP_WIGHT = "wp-wight"
        const val CP_BRAM = "cp-bram"
        const val CP_SERA = "cp-sera"
        const val CP_NIM = "cp-nim"
        const val CP_IRIA = "cp-iria"
        const val CP_ASH = "cp-ash"
        const val SESS_LANDING = "sess-landing"
        const val SESS_LANTERN = "sess-lantern"
        const val SESS_SHRINE = "sess-shrine"
        const val SESS_VAULTS = "sess-vaults"
        const val SESS_MANOR = "sess-manor"
        const val MAP_HARBOR = "map-harbor"
        const val MAP_LANTERN = "map-lantern"
        const val MAP_SHRINE = "map-shrine"
        const val MAP_VAULTS = "map-vaults"
        const val SIT_FLOOD = "sit-vault-flood"
        const val LORE_SUNDERING = "lore-sundering"
        const val LORE_SALT_LAW = "lore-salt-law"
        const val LORE_BELL = "lore-bell"
        const val LORE_CROWNS = "lore-crowns"

        val WORLD_AVATARS = mapOf(
            WP_CALDER to "avatar-calder-morrow.png",
            WP_OLAN to "avatar-olan-ashford.png",
            WP_VESSA to "avatar-vessa-vale.png",
            WP_JOR to "avatar-jor-rook.png",
            WP_LYSA to "avatar-lysa-crown.png",
            WP_HUSK to "avatar-drowned-husk.png",
            WP_GUARD to "avatar-salt-guard.png",
            WP_WIGHT to "avatar-bell-wight.png",
        )
        val CAMPAIGN_AVATARS = mapOf(
            CP_BRAM to "avatar-bram-pike.png",
            CP_SERA to "avatar-sera-quill.png",
            CP_NIM to "avatar-nim-rook.png",
            CP_IRIA to "avatar-iria-vale.png",
            CP_ASH to "avatar-ash.png",
        )
    }
}
