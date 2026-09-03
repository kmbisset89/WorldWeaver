package io.github.kmbisset89.worldweaver.domain

import java.time.Instant

internal class ShatteredAccordWorldBundleFactory(
    private val now: Instant = Instant.parse("2026-08-31T18:00:00Z"),
) {
    fun create(): WorldBundle {
        val world = world()
        val locations = locations(world.id)
        val worldPeople = worldPeople(world.id)
        val loreEntries = loreEntries(world.id, locations, worldPeople)
        val campaign = campaign(world.id)
        val campaignPeople = campaignPeople(campaign.id, worldPeople)
        val locationOverlays = overlays(campaign.id)
        val sessions = sessions(campaign.id, campaignPeople)
        val plotThreads = plotThreads(campaign.id, sessions)
        val quests = quests(campaign.id, locations, loreEntries, worldPeople, campaignPeople, sessions)
        return WorldBundle(
            formatVersion = WorldBundle.FORMAT_VERSION,
            exportedAt = now,
            world = world,
            calendar = calendar(world.id),
            campaigns = listOf(campaign),
            locations = locations,
            loreEntries = loreEntries,
            factions = factions(world.id),
            memberships = memberships(worldPeople, campaignPeople),
            worldPeople = worldPeople,
            campaignPeople = campaignPeople,
            locationOverlays = locationOverlays,
            quests = quests,
            sessions = sessions,
            plotThreads = plotThreads,
            referenceDocs = emptyList(),
            battleMaps = emptyList(),
            battleMapSituations = emptyList(),
            encounters = emptyList(),
            relationships = relationships(worldPeople, campaignPeople),
            companions = companions(worldPeople, campaignPeople),
            avatarFiles = emptyList(),
            mapFiles = emptyList(),
        )
    }

    private fun world(): World {
        return World(
            id = WORLD_ID,
            name = "The Shattered Accord",
            description = "A Gaelic-inspired high-fantasy world in which dragons and mortals are " +
                "connected through two traditions: Dragon Riders, who are chosen by dragons, and " +
                "Dragon Warriors, who wield power through dragon gems. A hidden enemy is exploiting " +
                "the mistrust between the two groups while rediscovering the forbidden art of " +
                "smashing dragon gems and forging weapons from their remains.",
            defaultGameSystem = GameSystem.FifthEdition,
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun calendar(worldId: String): WorldCalendar {
        return WorldCalendar(
            id = CAL_ID,
            worldId = worldId,
            eraSuffix = "AA",
            months = listOf(
                WorldCalendarMonth(id = MONTH_CEO, name = "Ceòmhìos", days = 30),
                WorldCalendarMonth(id = MONTH_AONTACHD, name = "Aontachd", days = 30),
                WorldCalendarMonth(id = MONTH_GLEANNACH, name = "Gleannach", days = 31),
                WorldCalendarMonth(id = MONTH_TEINE, name = "Teine", days = 30),
                WorldCalendarMonth(id = MONTH_UISGE, name = "Uisge", days = 30),
                WorldCalendarMonth(id = MONTH_GAOTH, name = "Gaoth", days = 30),
                WorldCalendarMonth(id = MONTH_COILLEACH, name = "Coilleach", days = 31),
                WorldCalendarMonth(id = MONTH_STOIRM, name = "Stoirm", days = 30),
                WorldCalendarMonth(id = MONTH_FOGHAR, name = "Foghar", days = 30),
                WorldCalendarMonth(id = MONTH_REOTHADH, name = "Reothadh", days = 30),
                WorldCalendarMonth(id = MONTH_GEAMHRADH, name = "Geamhradh", days = 31),
                WorldCalendarMonth(id = MONTH_DIOMHAIR, name = "Dìomhair", days = 30),
            ),
            weekdays = listOf(
                WorldCalendarWeekday(id = "wd-dragon", name = "Latha Dràgon"),
                WorldCalendarWeekday(id = "wd-clach", name = "Latha Clach"),
                WorldCalendarWeekday(id = "wd-gaoth", name = "Latha Gaoth"),
                WorldCalendarWeekday(id = "wd-coille", name = "Latha Coille"),
                WorldCalendarWeekday(id = "wd-cuan", name = "Latha Cuan"),
                WorldCalendarWeekday(id = "wd-teine", name = "Latha Teine"),
                WorldCalendarWeekday(id = "wd-tamh", name = "Latha Tàmh"),
            ),
            currentDate = WorldDate(year = 412, monthId = MONTH_GLEANNACH, day = 8),
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun locations(worldId: String): List<Location> {
        return listOf(
            location(
                id = LOC_LANDS,
                worldId = worldId,
                type = LocationType.Continent,
                parentId = null,
                name = "The Accord Lands",
                description = "Mist-covered islands, ancient forests, storm-beaten coasts, high " +
                    "valleys, and dragon-haunted mountains. Old kingdoms still claim legitimacy " +
                    "through their relationship with dragons.",
                climate = "Cool, wet, and changeable. Fog is common near the sea.",
                terrain = "Archipelagos, deep forest, mountain-walled valleys, storm coasts",
                government = "Rival traditions: Rider councils and Warrior monarchies, plus a " +
                    "neutral city that hosts both.",
                landmarks = listOf("Eilean na Dràgon", "Ard-Gleann", "Dùn na Sìth"),
                history = "The Age of Ceò gave way to Rider bonds and Warrior gems. Periods of " +
                    "harmony and Dragon Wars left a peace that is real, but fragile.",
                notes = "The campaign map is the five realms. Do not invent a sixth unless the table asks.",
            ),
            location(
                id = LOC_EILEAN,
                worldId = worldId,
                type = LocationType.Area,
                parentId = LOC_LANDS,
                name = "Eilean na Dràgon",
                description = "Island of Dragons. A sprawling archipelago of jagged cliffs, " +
                    "mist-filled valleys, hidden caves, waterfalls, and high nesting grounds.",
                climate = "Frequent fog. Navigation is difficult even for locals.",
                terrain = "Sea cliffs, wind-cut stone, nesting ledges, hidden caves",
                government = "Dràgonan Aontachd dominant. Gormshùil Sentinels present.",
                landmarks = listOf("Aontachd Haven", "Rider Gathering Grounds", "Sapphire Falls"),
                history = "Dragon Riders are embedded in local life. Sailors, navigators, and " +
                    "dragon-watchers treat the islands as home rather than a fortress.",
                notes = "Opening Rider table. The gathering happens here while Daibhidh dies inland.",
            ),
            location(
                id = LOC_COILLE,
                worldId = worldId,
                type = LocationType.Area,
                parentId = LOC_LANDS,
                name = "Coille Mhòr",
                description = "The Great Forest. Giant oaks, pines, hidden glens, magical " +
                    "clearings, and old ruins where daylight barely reaches the floor.",
                climate = "Cool shade, thin boundary between natural and magical life",
                terrain = "Ancient woodland, deep glens, ruin-choked hollows",
                government = "Liath Fàidhean dominant. Fìor-Bhèist Hunters present.",
                landmarks = listOf("Heartwood Steading", "Whispering Woods", "Misty Vale"),
                history = "Known for woodworking, herbalism, hunting, druidic traditions, and " +
                    "old elven communities.",
                notes = "Aeliana's home range. The Shadowthorn Whip later turns part of this forest.",
            ),
            location(
                id = LOC_ARD,
                worldId = worldId,
                type = LocationType.Area,
                parentId = LOC_LANDS,
                name = "Ard-Gleann",
                description = "High Valley. A fertile mountain-walled land of farms, a great " +
                    "central lake, mineral-rich peaks, and fortified passes.",
                climate = "Clear high-valley air, hard winters on the surrounding peaks",
                terrain = "Farmland, lake country, mountain passes, fortified roads",
                government = "Cloch Custodians and the royal House of Ailín. Ceardaichean present.",
                landmarks = listOf("Gleann Seat", "the central lake", "the mountain passes"),
                history = "Heartland of inherited gems, martial contests, and royal legitimacy.",
                notes = "Opening Warrior table. The succession crisis begins here.",
            ),
            location(
                id = LOC_NEUTRAL,
                worldId = worldId,
                type = LocationType.Area,
                parentId = LOC_LANDS,
                name = "The Neutral March",
                description = "Dry high plateau and the trade roads that feed Dùn na Sìth. " +
                    "No faction officially dominates.",
                climate = "Dry, bright, excellent sight lines",
                terrain = "Plateau, irrigation works, major trade roads",
                government = "Representatives from multiple factions share the city below.",
                landmarks = listOf("Dùn na Sìth", "the plateau walls"),
                history = "Built as a place where Riders and Warriors could meet without a crown.",
                notes = "Use later for diplomacy. Not required for the opening.",
            ),
            location(
                id = LOC_CUAN,
                worldId = worldId,
                type = LocationType.Area,
                parentId = LOC_LANDS,
                name = "Cuan Stoirm",
                description = "Storm Bay. Violent coastline of cliffs, dangerous currents, " +
                    "sheltered harbors, and rocky islands.",
                climate = "Frequent storms. Ships live or die by the weather.",
                terrain = "Sea cliffs, rocky islands, sheltered harbors",
                government = "Gormshùil Sentinels dominant. Dràgonan Aontachd present.",
                landmarks = listOf("Stormharbor", "the wreck line"),
                history = "Famous for shipbuilding, sailing, storytelling, and surviving weather " +
                    "that should have ended them.",
                notes = "Gormshùil intelligence often starts here.",
            ),
            location(
                id = LOC_HAVEN,
                worldId = worldId,
                type = LocationType.City,
                parentId = LOC_EILEAN,
                name = "Aontachd Haven",
                description = "The inhabited harbor of the Island of Dragons. Riders, sailors, " +
                    "and dragon-watchers share the same docks.",
                climate = "Fog most mornings, wind off the cliffs",
                terrain = "Stone quays, cliff stairs, wind-cut courtyards",
                government = "Seanair Council of the Dràgonan Aontachd",
                landmarks = listOf("The gathering terrace", "the watch eyries"),
                history = "Grew around the council's need for a place mortals and dragons could " +
                    "both reach.",
                notes = "Party can sleep here if the gathering runs long.",
            ),
            location(
                id = LOC_GLEANN_SEAT,
                worldId = worldId,
                type = LocationType.City,
                parentId = LOC_ARD,
                name = "Gleann Seat",
                description = "Royal city of Ard-Gleann. Honor, family, and inherited gems are " +
                    "visible in every banner.",
                climate = "Crisp valley air, lake fog at dawn",
                terrain = "Fortified streets, lake shore, mountain-gate roads",
                government = "House of Ailín and the Cloch Custodians",
                landmarks = listOf("Royal residence", "castle garden", "the lake road"),
                history = "Possession of famous gems became almost inseparable from the right to rule.",
                notes = "Caelum comes here seeking Daibhidh. The death happens inside the residence.",
            ),
            location(
                id = LOC_DUN,
                worldId = worldId,
                type = LocationType.City,
                parentId = LOC_NEUTRAL,
                name = "Dùn na Sìth",
                description = "Fortress of Peace. A walled city on a high plateau used for " +
                    "diplomacy, trade, scholarship, and inter-faction negotiation.",
                climate = "Dry, windy, clear",
                terrain = "Massive walls, irrigation channels, trade yards",
                government = "Shared. No faction officially dominates.",
                landmarks = listOf("The negotiation halls", "the scholar quarter"),
                history = "Built so that neither tradition would have to kneel on the other's soil.",
                notes = "Later diplomacy. Keep it empty in the opening unless the table flees there.",
            ),
            location(
                id = LOC_STORMHARBOR,
                worldId = worldId,
                type = LocationType.City,
                parentId = LOC_CUAN,
                name = "Stormharbor",
                description = "The sheltered port that still takes ships when the bay is trying " +
                    "to kill them.",
                climate = "Salt wind, sudden rain",
                terrain = "Stone moles, shipyards, cliff stairs",
                government = "Gormshùil Sentinels and local shipwrights",
                landmarks = listOf("The inner mole", "Fionnlagh's training yard"),
                history = "Survived by building better ships than the weather.",
                notes = "Optional Gormshùil introduction.",
            ),
            location(
                id = LOC_WARDEN,
                worldId = worldId,
                type = LocationType.City,
                parentId = LOC_COILLE,
                name = "Heartwood Steading",
                description = "A warden town among giant oaks. Hunters, seers, and old elven " +
                    "households share the same paths.",
                climate = "Cool shade, damp undergrowth",
                terrain = "Living wood halls, hidden glens, ruin trails",
                government = "Liath Fàidhean counsel and hunter election custom",
                landmarks = listOf("The seer grove", "the hunter stones"),
                history = "Older than most kingdoms. The trees remember more than the banners.",
                notes = "Aeliana's home if the table asks where she came from.",
            ),
            location(
                id = LOC_GATHERING,
                worldId = worldId,
                type = LocationType.Place,
                parentId = LOC_HAVEN,
                name = "Rider Gathering Grounds",
                description = "Open terrace and nesting ledges where the Aontachd meets when " +
                    "rumor turns into politics.",
                climate = "Wind and fog. Dragons hear the weather first.",
                terrain = "Stone terrace, drop to the sea, high ledges",
                government = "Seanair Council when it is in session",
                landmarks = listOf("The speaking stone", "the horizon line toward Ard-Gleann"),
                history = "Used for counsel, not ceremony. Younger Riders demand action here. " +
                    "Elders counsel restraint.",
                notes = "Session 5. The distant pulse happens during an argument, not a scripted speech.",
            ),
            location(
                id = LOC_SAPPHIRE,
                worldId = worldId,
                type = LocationType.Place,
                parentId = LOC_HAVEN,
                name = "Sapphire Falls",
                description = "A gathering place for dragons and Riders around a great waterfall. " +
                    "The Veil later uses the Abyssal Trident here, believing a hoard may sit beneath.",
                climate = "Spray, cold stone, roaring water",
                terrain = "Waterfall basin, wet ledges, hidden plunge pools",
                government = "Rider custom, not a garrison",
                landmarks = listOf("The falls", "the deep pool"),
                history = "A place of meeting before it becomes a battlefield.",
                notes = "Later attack site. Do not spring the trident in the opening.",
            ),
            location(
                id = LOC_CLOUDTOP,
                worldId = worldId,
                type = LocationType.Place,
                parentId = LOC_HAVEN,
                name = "Cloudtop Eyrie",
                description = "A fortified Rider stronghold in the high mountains. The Veil later " +
                    "employs the Stormbreaker Hammer to make flight impossible.",
                climate = "Thin air, sudden storms",
                terrain = "Cliff fort, nesting towers, narrow stairs",
                government = "Aontachd garrison",
                landmarks = listOf("The flight towers", "the storm wall"),
                history = "Built to watch the sea and the inland passes at once.",
                notes = "Later attack. Artificial storms are the clue, not a set-piece yet.",
            ),
            location(
                id = LOC_MISTY,
                worldId = worldId,
                type = LocationType.Place,
                parentId = LOC_WARDEN,
                name = "Misty Vale",
                description = "A secluded valley occupied by bonded dragons and Riders. The Veil " +
                    "later searches hoards here with the Frostfire Lance.",
                climate = "Standing mist, cool even in summer",
                terrain = "Hidden valley, hoard caves, narrow approaches",
                government = "Bonded households, not a town",
                landmarks = listOf("The vale mouth", "the hoard caves"),
                history = "The Riders learn someone will attack a bonded dragon simply because " +
                    "it might possess a gem.",
                notes = "Later attack. The lesson is the motive, not the loot.",
            ),
            location(
                id = LOC_WHISPER,
                worldId = worldId,
                type = LocationType.Place,
                parentId = LOC_WARDEN,
                name = "Whispering Woods",
                description = "A forest sanctuary used by Riders. The Shadowthorn Whip later " +
                    "turns the trees themselves into a weapon.",
                climate = "Deep shade, living quiet",
                terrain = "Old growth, sanctuary clearings, root-paths",
                government = "Warden custom",
                landmarks = listOf("The sanctuary ring", "the listening oaks"),
                history = "A place meant for rest. That is why the later attack lands so hard.",
                notes = "Later attack. Plant-warping weapons are a Veil fingerprint.",
            ),
            location(
                id = LOC_RESIDENCE,
                worldId = worldId,
                type = LocationType.Place,
                parentId = LOC_GLEANN_SEAT,
                name = "Royal Residence",
                description = "The castle of House Ailín. Daibhidh returns here injured and " +
                    "without his gem. He dies in these halls when the Emerald Heart is smashed elsewhere.",
                climate = "Stone-cold, lake-damp",
                terrain = "Great halls, family apartments, a high sickroom",
                government = "King Ailín",
                landmarks = listOf("The throne hall", "the prince's rooms"),
                history = "Gems have hung on these walls longer than most family names.",
                notes = "Session 4. Healing cannot fix what is being ripped out of him.",
            ),
            location(
                id = LOC_GARDEN,
                worldId = worldId,
                type = LocationType.Place,
                parentId = LOC_GLEANN_SEAT,
                name = "Castle Garden",
                description = "Where a young Eirlys tested a mechanical bird and Daibhidh brought " +
                    "her wildflowers. The memory should land immediately before she learns he has returned.",
                climate = "Sheltered, lake breeze",
                terrain = "Garden paths, workbench stone, flower beds",
                government = "House Ailín, in practice Eirlys's workshop",
                landmarks = listOf("The mechanical-bird bench", "the flower walk"),
                history = "Daibhidh asked her to remain herself no matter what the house demanded.",
                notes = "Session 3. Make the death personal before it becomes political.",
            ),
            location(
                id = LOC_AMBUSH,
                worldId = worldId,
                type = LocationType.Place,
                parentId = LOC_GLEANN_SEAT,
                name = "The Ambush Road",
                description = "A mountain-gate road that becomes unnaturally quiet. A deliberate " +
                    "obstruction. A coordinated group takes Daibhidh's Emerald Heart Gem and leaves.",
                climate = "Still air that should not be still",
                terrain = "Fortified pass road, choke point, tree line",
                government = "Royal road. The watch is late.",
                landmarks = listOf("The obstruction", "the tree line"),
                history = "The attackers are far more disciplined than the thieves who took Caelum's gem.",
                notes = "Session 2. Their target is the prince, not Caelum. The two thefts are unrelated.",
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
                id = WP_MORCANT,
                worldId = worldId,
                kind = PersonKind.Npc,
                name = "Morcant",
                description = "The Shadowed Sovereign. Leader of the Veil of Thorns. Condemns " +
                    "traditional gem use while secretly possessing the Lumina Shard.",
                sheet = npcSheet(
                    race = "Human",
                    className = "Warlock",
                    level = 12,
                    scores = AbilityScores(12, 14, 14, 16, 15, 18),
                    hitPoints = 88,
                    armorClass = 16,
                    items = listOf(InventoryItem("Lumina Shard", 1, "GM secret. Light, revelation, radiant chains.")),
                    notes = "Do not present him as common knowledge. His hypocrisy is the point.",
                ),
            ),
            worldPerson(
                id = WP_EIRA,
                worldId = worldId,
                kind = PersonKind.Npc,
                name = "Eira Shadowweaver",
                description = "Morcant's lieutenant. Publicly opposes Dragon Riders while secretly " +
                    "bonded to Lir, a Mistral Shadow Dragon.",
                sheet = npcSheet(
                    race = "Human",
                    className = "Ranger",
                    level = 8,
                    scores = AbilityScores(12, 16, 13, 14, 16, 14),
                    hitPoints = 58,
                    armorClass = 15,
                    items = listOf(InventoryItem("Veil tokens", 1, "Nothing that names Lir.")),
                    notes = "Hidden from much of the Veil. Why Lir chose her should matter.",
                ),
            ),
            worldPerson(
                id = WP_AILIN,
                worldId = worldId,
                kind = PersonKind.Npc,
                name = "King Ailín",
                description = "King of Ard-Gleann. Silver hair, piercing blue eyes, the visible " +
                    "weight of long rule. Bearer of the Emerald Storm Gem.",
                sheet = npcSheet(
                    race = "Human",
                    className = "Fighter",
                    level = 10,
                    scores = AbilityScores(14, 12, 14, 13, 16, 16),
                    hitPoints = 84,
                    armorClass = 17,
                    items = listOf(InventoryItem("Emerald Storm Gem", 1, "Wind, storm calling, gentle weather as well as gales.")),
                    notes = "Must grieve as a father while appearing unbreakable as a king.",
                ),
            ),
            worldPerson(
                id = WP_DAIBHIDH,
                worldId = worldId,
                kind = PersonKind.Npc,
                name = "Prince Daibhidh Ironfist",
                description = "Prince of Ard-Gleann and brother of Eirlys. Bearer of the Emerald " +
                    "Heart Gem. Dies when that gem is deliberately destroyed.",
                sheet = npcSheet(
                    race = "Human",
                    className = "Paladin",
                    level = 6,
                    scores = AbilityScores(16, 12, 14, 11, 13, 16),
                    hitPoints = 52,
                    armorClass = 18,
                    items = listOf(InventoryItem("Emerald Heart Gem", 1, "Stolen in the ambush. Destroyed elsewhere.")),
                    notes = "Keep him alive through sessions 1–3. His last words are to Eirlys as a brother.",
                ),
            ),
            worldPerson(
                id = WP_FIONNLAGH,
                worldId = worldId,
                kind = PersonKind.Npc,
                name = "Fionnlagh the Stormblade",
                description = "Gormshùil swordsman associated with lightning-fast strikes and storm magic.",
                sheet = npcSheet(
                    race = "Human",
                    className = "Fighter",
                    level = 8,
                    scores = AbilityScores(14, 18, 14, 12, 13, 14),
                    hitPoints = 68,
                    armorClass = 16,
                    items = listOf(InventoryItem("Stormblade", 1, "Lightning along the edge when he wants it seen.")),
                    notes = "Meritocrat. Useful if the table wants a Sentinel contact.",
                ),
            ),
            worldPerson(
                id = WP_BRIGHDE,
                worldId = worldId,
                kind = PersonKind.Npc,
                name = "Archseer Brìghde",
                description = "Leader of the Liath Fàidhean. Oligarch of seers, diviners, and mages.",
                sheet = npcSheet(
                    race = "Elf",
                    className = "Wizard",
                    level = 11,
                    scores = AbilityScores(8, 14, 12, 18, 16, 14),
                    hitPoints = 62,
                    armorClass = 12,
                    items = listOf(InventoryItem("Seer circlet", 1, "")),
                    notes = "Sells counsel. Does not give the Veil away for free.",
                ),
            ),
            worldPerson(
                id = WP_CALUM,
                worldId = worldId,
                kind = PersonKind.Npc,
                name = "Seer Calum",
                description = "Liath Fàidhean prophet known for visions and powerful illusions.",
                sheet = npcSheet(
                    race = "Human",
                    className = "Wizard",
                    level = 7,
                    scores = AbilityScores(9, 13, 12, 16, 16, 14),
                    hitPoints = 40,
                    armorClass = 12,
                    items = listOf(InventoryItem("Illusion dust", 1, "")),
                    notes = "Visions can be true and still point at the wrong enemy.",
                ),
            ),
            worldPerson(
                id = WP_DAIGH,
                worldId = worldId,
                kind = PersonKind.Npc,
                name = "Daigh the Forgeheart",
                description = "Master Craftsman of the Ceardaichean. Guild leader for weapons, " +
                    "armor, artifacts, and engineering.",
                sheet = npcSheet(
                    race = "Dwarf",
                    className = "Artificer",
                    level = 9,
                    scores = AbilityScores(14, 12, 16, 16, 13, 12),
                    hitPoints = 72,
                    armorClass = 16,
                    items = listOf(InventoryItem("Forgeheart hammer", 1, "")),
                    notes = "May later recognize gemforged metal and refuse to say so in public.",
                ),
            ),
            worldPerson(
                id = WP_EIRIC,
                worldId = worldId,
                kind = PersonKind.Npc,
                name = "Eiric the Alchemist",
                description = "Ceardaichean alchemist. Potions, elixirs, and unusual transmutations.",
                sheet = npcSheet(
                    race = "Human",
                    className = "Wizard",
                    level = 5,
                    scores = AbilityScores(10, 14, 13, 16, 12, 11),
                    hitPoints = 32,
                    armorClass = 12,
                    items = listOf(InventoryItem("Unlabeled vials", 6, "Ask before drinking.")),
                    notes = "Useful for analysis of gem fragments later.",
                ),
            ),
            worldPerson(
                id = WP_MORAG,
                worldId = worldId,
                kind = PersonKind.Npc,
                name = "Mòrag the Dragonbane",
                description = "Elected leader of the Fìor-Bhèist Hunters. Tracking, wilderness " +
                    "survival, and frontier protection.",
                sheet = npcSheet(
                    race = "Human",
                    className = "Ranger",
                    level = 9,
                    scores = AbilityScores(14, 16, 14, 11, 16, 12),
                    hitPoints = 70,
                    armorClass = 15,
                    items = listOf(InventoryItem("Dragonbane spear", 1, "Reputation more than a trophy.")),
                    notes = "Hunts monsters. Does not automatically hunt Riders.",
                ),
            ),
            worldPerson(
                id = WP_TORCALL,
                worldId = worldId,
                kind = PersonKind.Npc,
                name = "Torcall the Tracker",
                description = "Hunter famous for extraordinary quarry and handling wild creatures.",
                sheet = npcSheet(
                    race = "Half-Elf",
                    className = "Ranger",
                    level = 6,
                    scores = AbilityScores(12, 16, 13, 12, 16, 13),
                    hitPoints = 46,
                    armorClass = 14,
                    items = listOf(InventoryItem("Tracking kit", 1, "")),
                    notes = "Can follow a gemforged strike if the table brings him a trail.",
                ),
            ),
            worldPerson(
                id = WP_SERENA,
                worldId = worldId,
                kind = PersonKind.Npc,
                name = "Serena of the Whisperwind",
                description = "Historical Gemforging victim. Bearer of Zephyr's Whisper. Her " +
                    "homeland was torn by violent weather when the gem was destroyed.",
                sheet = historicalSheet("Wind-whisper records. Dead."),
            ),
            worldPerson(
                id = WP_BROGAN,
                worldId = worldId,
                kind = PersonKind.Npc,
                name = "Brogan Ironheart",
                description = "Historical Gemforging victim. Bearer of the Coreforge Gem. " +
                    "Tremors and collapsing mines followed the destruction.",
                sheet = historicalSheet("Earth-and-metal records. Dead."),
            ),
            worldPerson(
                id = WP_LILIANA,
                worldId = worldId,
                kind = PersonKind.Npc,
                name = "Liliana of the Moonshade",
                description = "Historical Gemforging victim. Bearer of Lunar Gleam. Her homeland " +
                    "became unnaturally dark.",
                sheet = historicalSheet("Light-and-shadow records. Dead."),
            ),
            worldPerson(
                id = WP_EWAN,
                worldId = worldId,
                kind = PersonKind.Npc,
                name = "Ewan Stormrider",
                description = "Historical Gemforging victim. Bearer of Tempest's Heart. Coastal " +
                    "weather and marine life never recovered.",
                sheet = historicalSheet("Storm records. Dead."),
            ),
            worldPerson(
                id = WP_GWENDOLYN,
                worldId = worldId,
                kind = PersonKind.Npc,
                name = "Gwendolyn of the Verdant Vale",
                description = "Historical Gemforging victim. Bearer of Bloomheart. Fertile " +
                    "regions withered after the gem was smashed.",
                sheet = historicalSheet("Healing-growth records. Dead."),
            ),
            worldPerson(
                id = WP_KAEL,
                worldId = worldId,
                kind = PersonKind.Npc,
                name = "Thane Kael of the Emberpeak",
                description = "Historical Gemforging victim. Bearer of the Inferno Shard. The " +
                    "region cooled and lost its forges. Earlier lore tied him to Caelum's ancestry; " +
                    "keep or drop that as needed.",
                sheet = historicalSheet("Fire records. Dead. Ancestry link optional."),
            ),
            worldPerson(
                id = WP_LIR,
                worldId = worldId,
                kind = PersonKind.Monster,
                name = "Lir",
                description = "The Mist Warden. Mistral Shadow Dragon bonded to Eira. Silver-grey " +
                    "scales, violet eyes, mistlike wings. Calm, intelligent, protective.",
                sheet = monsterSheet(
                    race = "Mistral Shadow Dragon",
                    scores = AbilityScores(16, 18, 16, 16, 16, 14),
                    hitPoints = 96,
                    armorClass = 17,
                    walkSpeed = 40,
                    features = listOf(
                        PersonFeature("Mist Manipulation", "Creates and controls heavy fog."),
                        PersonFeature("Shadow Camouflage", "Blends into darkness and low light."),
                        PersonFeature("Ethereal Passage", "Briefly moves through barriers as mist."),
                        PersonFeature("Bond of the Mist", "Unusually deep connection with Eira."),
                    ),
                    notes = "Title: The Mist Warden. Why he chose Eira is an open question.",
                ),
            ),
            worldPerson(
                id = WP_NYTHENDRA,
                worldId = worldId,
                kind = PersonKind.Monster,
                name = "Nythendra",
                description = "The Timeless Sage. Near-eternal ancient dragon who answers Eirlys. " +
                    "Twilight scales, constellation-like wings. Scholar, not a mount.",
                sheet = monsterSheet(
                    race = "Near-eternal dragon",
                    scores = AbilityScores(22, 12, 22, 20, 22, 18),
                    hitPoints = 320,
                    armorClass = 21,
                    walkSpeed = 40,
                    features = listOf(
                        PersonFeature("Temporal Insight", "Perceives time in ways mortals cannot."),
                        PersonFeature("Arcane Reservoir", "Can empower artifice and magical devices."),
                        PersonFeature("Wisdom of Ages", "History, dragons, magic, lost civilizations."),
                    ),
                    notes = "Mentor and companion to Eirlys. Not ownership. Not obedience.",
                ),
            ),
            worldPerson(
                id = WP_LYSANTHIR,
                worldId = worldId,
                kind = PersonKind.Monster,
                name = "Lysanthir",
                description = "Shadow-Veil Forest Dragon bonded to Aeliana. Reluctant hoarder who " +
                    "keeps objects with meaning. May one day give Aurora's Heart to Caelum.",
                sheet = monsterSheet(
                    race = "Shadow-Veil Forest Dragon",
                    scores = AbilityScores(18, 16, 16, 14, 16, 14),
                    hitPoints = 85,
                    armorClass = 16,
                    walkSpeed = 40,
                    features = listOf(
                        PersonFeature("Shadowmeld Glide", "Advantage on Stealth in dim light or forest."),
                        PersonFeature("Bonded Awareness", "Sharpens Aeliana's awareness when near."),
                        PersonFeature("Veil Breath", "Recharge 5–6. Cone of mist that can blind and halt."),
                        PersonFeature("Protect the Rider", "Reaction. Interferes with attacks against Aeliana."),
                    ),
                    notes = "Large, Neutral Good. Fly 80, climb 30. Gem: Aurora's Heart. Bite +7 2d10+4, claw +7 2d6+4.",
                ),
            ),
            worldPerson(
                id = WP_PYRAETHUS,
                worldId = worldId,
                kind = PersonKind.Monster,
                name = "Pyraethus",
                description = "The Crimson Flame. Ember-red scales like a newly opened furnace. " +
                    "Bonded to Fianna because each recognized the other's honesty and enthusiasm.",
                sheet = monsterSheet(
                    race = "Crimson flame dragon",
                    scores = AbilityScores(20, 14, 18, 12, 13, 16),
                    hitPoints = 110,
                    armorClass = 17,
                    walkSpeed = 40,
                    features = listOf(
                        PersonFeature("Hearth and Furnace", "Warmth or catastrophic fire, depending on mood."),
                    ),
                    notes = "They encourage one another. This is occasionally a problem.",
                ),
            ),
            worldPerson(
                id = WP_MELODIS,
                worldId = worldId,
                kind = PersonKind.Monster,
                name = "Melodis",
                description = "The Celestial Harmony. Dawn-and-twilight scales. Bonded to " +
                    "Seraphina after her song healed him. Magic interacts through sound.",
                sheet = monsterSheet(
                    race = "Celestial harmony dragon",
                    scores = AbilityScores(16, 16, 16, 14, 16, 20),
                    hitPoints = 90,
                    armorClass = 16,
                    walkSpeed = 40,
                    features = listOf(
                        PersonFeature("Songbond", "Seraphina's music strengthens in his presence."),
                    ),
                    notes = "Gentle, curious, drawn to emotional harmony rather than dominance.",
                ),
            ),
            worldPerson(
                id = WP_VIRENTIA,
                worldId = worldId,
                kind = PersonKind.Monster,
                name = "Virentia",
                description = "The Verdant Whisper. Green dragon of living ecosystems, not an " +
                    "evil chromatic stereotype. Bonded to Liora. May later give her a gem.",
                sheet = monsterSheet(
                    race = "Green dragon",
                    scores = AbilityScores(18, 16, 18, 16, 16, 16),
                    hitPoints = 120,
                    armorClass = 18,
                    walkSpeed = 40,
                    features = listOf(
                        PersonFeature("Forest Communication", "Speaks with living terrain."),
                        PersonFeature("Plant Manipulation", "Camouflage and entanglement."),
                        PersonFeature("Life-energy Awareness", "Senses nearby living creatures."),
                    ),
                    notes = "Color does not determine morality. Future gem gift should feel like trust, not loot.",
                ),
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
        val ard = locations.first { it.id == LOC_ARD }
        val eilean = locations.first { it.id == LOC_EILEAN }
        val residence = locations.first { it.id == LOC_RESIDENCE }
        val gathering = locations.first { it.id == LOC_GATHERING }
        val morcant = worldPeople.first { it.id == WP_MORCANT }
        val ailin = worldPeople.first { it.id == WP_AILIN }
        val daibhidh = worldPeople.first { it.id == WP_DAIBHIDH }
        val nythendra = worldPeople.first { it.id == WP_NYTHENDRA }
        return listOf(
            lore(
                id = LORE_CEO,
                worldId = worldId,
                title = "The Age of Ceò",
                content = "The oldest remembered age is the Age of Mist. The world was wild, " +
                    "saturated with magic, and dominated by dragons. Mortals lived beneath their " +
                    "shadow until some were chosen and others found power in hoard gemstones.",
                category = LoreCategory.History,
                tags = listOf("history", "origins"),
                relatedIds = listOf(LORE_HARMONY, LORE_BONDS),
                locationId = ard.id,
            ),
            lore(
                id = LORE_HARMONY,
                worldId = worldId,
                title = "The Old Harmony",
                content = "There were periods when Riders and Warriors operated together. Riders " +
                    "became scouts, emissaries, and guardians of wild places. Warriors became " +
                    "defenders, champions, rulers, and keepers of heirlooms. Neither tradition " +
                    "was inherently good or evil.",
                category = LoreCategory.History,
                tags = listOf("history", "politics"),
                relatedIds = listOf(LORE_WARS, LORE_CEO),
                locationId = locations.first { it.id == LOC_DUN }.id,
            ),
            lore(
                id = LORE_WARS,
                worldId = worldId,
                title = "The Dragon Wars",
                content = "Control of gems, questions of legitimacy, old injuries, and competing " +
                    "philosophies brought periods of open conflict. Borders moved. Both traditions " +
                    "learned to suspect the other. The modern peace is real, but fragile.",
                category = LoreCategory.History,
                tags = listOf("history", "wars"),
                relatedIds = listOf(LORE_HARMONY, LORE_MISUNDERSTANDING),
                locationId = ard.id,
            ),
            lore(
                id = LORE_GEMFORGING,
                worldId = worldId,
                title = "Gemforging",
                content = "An old and nearly forbidden legend claims a dragon gem can be smashed " +
                    "and its remains forged into weapons, armor, or artifacts. Most treat it as " +
                    "myth, taboo, or wartime propaganda.",
                category = LoreCategory.Magic,
                tags = listOf("magic", "taboo"),
                relatedIds = listOf(LORE_WEEPING, LORE_WEAPONS, LORE_VEIL),
                secrets = listOf(
                    LoreSecret(
                        id = "secret-gemforging",
                        title = "It is real",
                        secret = "When a true gem is deliberately destroyed, the connected dragon " +
                            "dies, the bearer dies, a violent discharge is released, and the " +
                            "fragments retain a corrupted form of the original power. The Veil is " +
                            "harvesting gems, not merely stealing them.",
                        hints = listOf(
                            LoreHint(id = "hint-forge-0", text = "Old memorials name pairs who died together.", revealed = true),
                            LoreHint(id = "hint-forge-1", text = "Daibhidh's death is the first modern proof.", revealed = false),
                        ),
                    )
                ),
                locationId = ard.id,
                characterId = daibhidh.id,
            ),
            lore(
                id = LORE_WEEPING,
                worldId = worldId,
                title = "The Weeping Sky",
                content = "As more gems are destroyed, strange phenomena appear: spectral lights " +
                    "like tears, unstable magic, shifting ley lines, unseasonable storms, restless " +
                    "dragons, and shared nightmares among Riders.",
                category = LoreCategory.Magic,
                tags = listOf("omen", "magic"),
                relatedIds = listOf(LORE_GEMFORGING),
                locationId = eilean.id,
            ),
            lore(
                id = LORE_BONDS,
                worldId = worldId,
                title = "Dragon Bonds and Gem Inheritance",
                content = "A Rider bond cannot be demanded. Circumstances vary: courage, kindness, " +
                    "shared danger, music, healing, or something known only to the dragon. Warrior " +
                    "gems may be taken from a hoard, given voluntarily, inherited, or entrusted by " +
                    "a temple or crown.",
                category = LoreCategory.Magic,
                tags = listOf("riders", "warriors"),
                relatedIds = listOf(LORE_CEO, LORE_COVENANT),
                locationId = eilean.id,
            ),
            lore(
                id = LORE_COVENANT,
                worldId = worldId,
                title = "The Order of the Draconic Covenant",
                content = "A major faith for clerics, paladins, and pilgrims. Dragons embody " +
                    "ancient forces and should not be treated merely as weapons. A voluntary bond " +
                    "is the ideal relationship between power and responsibility. Paladins swear to " +
                    "protect sacred sites, defend dragons, recover stolen gems, and prevent desecration.",
                category = LoreCategory.Religion,
                tags = listOf("faith", "paladins"),
                relatedIds = listOf(LORE_BONDS),
                locationId = locations.first { it.id == LOC_DUN }.id,
            ),
            lore(
                id = LORE_HOUSE,
                worldId = worldId,
                title = "House of Ailín",
                content = "The ruling house of Ard-Gleann and the visible face of the Cloch " +
                    "Custodians. King Ailín bears the Emerald Storm Gem. Prince Daibhidh bears " +
                    "the Emerald Heart. Princess Eirlys inherited the Eclipse Shard and has also " +
                    "earned Nythendra's companionship.",
                category = LoreCategory.Politics,
                tags = listOf("royalty", "succession"),
                relatedIds = listOf(LORE_EMERALD, LORE_ECLIPSE, LORE_MISUNDERSTANDING),
                secrets = listOf(
                    LoreSecret(
                        id = "secret-house",
                        title = "The succession is not a script",
                        secret = "Daibhidh's death creates a succession crisis, pressure on Eirlys, " +
                            "and an opening for enemies. Who rules if he dies before inheriting is " +
                            "still an open question.",
                        hints = listOf(
                            LoreHint(id = "hint-house-0", text = "The court already watches Eirlys.", revealed = true),
                            LoreHint(id = "hint-house-1", text = "Ailín must choose grief or the crown's face.", revealed = false),
                        ),
                    )
                ),
                locationId = residence.id,
                characterId = ailin.id,
            ),
            lore(
                id = LORE_MISUNDERSTANDING,
                worldId = worldId,
                title = "The Central Misunderstanding",
                content = "Warriors see missing gems, attacks involving dragons, and Rider " +
                    "opposition to inherited ownership. Riders see Warriors weaponizing dragon " +
                    "power and political leaders demanding retaliation. Both interpretations are " +
                    "reasonable. Both are incomplete.",
                category = LoreCategory.Politics,
                tags = listOf("plot", "opening"),
                relatedIds = listOf(LORE_VEIL, LORE_HOUSE),
                secrets = listOf(
                    LoreSecret(
                        id = "secret-blame",
                        title = "Interpretation is the weapon",
                        secret = "The Veil's greatest early weapon is not a forged sword. It is " +
                            "leaving just enough evidence for each faction to believe the other is guilty.",
                        hints = listOf(
                            LoreHint(id = "hint-blame-0", text = "Each side has better questions, not answers.", revealed = true),
                            LoreHint(id = "hint-blame-1", text = "Do not let every clue point at a cult.", revealed = false),
                        ),
                    )
                ),
                locationId = gathering.id,
            ),
            lore(
                id = LORE_LUMINA,
                worldId = worldId,
                title = "The Lumina Shard",
                content = "A gem associated with light, clarity, revelation, purification, and " +
                    "radiant magic. Public histories do not name a current bearer.",
                category = LoreCategory.Other,
                tags = listOf("gem", "secret"),
                relatedIds = listOf(LORE_VEIL, LORE_GEMFORGING),
                secrets = listOf(
                    LoreSecret(
                        id = "secret-lumina",
                        title = "Morcant holds it",
                        secret = "Morcant secretly possesses the Lumina Shard. He uses illumination, " +
                            "purifying flame, insight, healing reversal, and radiant chains. His " +
                            "evil is what he chooses to do with it.",
                        hints = listOf(
                            LoreHint(id = "hint-lumina-0", text = "Someone is bending light at Veil sites.", revealed = false),
                        ),
                    )
                ),
                characterId = morcant.id,
            ),
            lore(
                id = LORE_EMERALD,
                worldId = worldId,
                title = "The Emerald Heart Gem",
                content = "Prince Daibhidh's inherited gem. Distinct from Caelum's Azure Flame. " +
                    "Its exact power is still an open question.",
                category = LoreCategory.Other,
                tags = listOf("gem", "royalty"),
                relatedIds = listOf(LORE_HOUSE, LORE_GEMFORGING),
                locationId = residence.id,
                characterId = daibhidh.id,
            ),
            lore(
                id = LORE_AZURE,
                worldId = worldId,
                title = "The Azure Flame Gem",
                content = "Caelum Ironfist's gem, associated with water, ice, and intensely cold " +
                    "blue flame. Stolen by ordinary thieves before the campaign opens.",
                category = LoreCategory.Other,
                tags = listOf("gem", "hook"),
                relatedIds = listOf(LORE_EMERALD),
                secrets = listOf(
                    LoreSecret(
                        id = "secret-azure",
                        title = "Ordinary thieves",
                        secret = "The Azure Flame theft is not initially connected to the Veil. " +
                            "The table will assume otherwise. That assumption is the point.",
                        hints = listOf(
                            LoreHint(id = "hint-azure-0", text = "The thieves were sloppy. The ambush was not.", revealed = false),
                        ),
                    )
                ),
            ),
            lore(
                id = LORE_ECLIPSE,
                worldId = worldId,
                title = "The Eclipse Shard",
                content = "Inherited through Eirlys's lineage. She used it to study animals, " +
                    "dragon behavior, and ancient patterns, which helped her find Nythendra.",
                category = LoreCategory.Other,
                tags = listOf("gem", "eirlys"),
                relatedIds = listOf(LORE_HOUSE, LORE_BONDS),
                characterId = nythendra.id,
            ),
            lore(
                id = LORE_AURORA,
                worldId = worldId,
                title = "Aurora's Heart",
                content = "Lysanthir's gem. He has little interest in traditional wealth. If he " +
                    "ever gives it to Caelum, it should cost him and feel like trust, not a loot upgrade.",
                category = LoreCategory.Other,
                tags = listOf("gem", "future"),
                relatedIds = listOf(LORE_AZURE, LORE_BONDS),
                characterId = worldPeople.first { it.id == WP_LYSANTHIR }.id,
            ),
            lore(
                id = LORE_CELESTIAL,
                worldId = worldId,
                title = "The Celestial Shard",
                content = "A temple gem awarded every five years to the most worthy monk who " +
                    "does not already hold it. The current bearer must surrender it and compete " +
                    "without its power if they wish to reclaim it. Kaelan is the present bearer.",
                category = LoreCategory.Other,
                tags = listOf("gem", "temple"),
                relatedIds = listOf(LORE_BONDS),
            ),
            lore(
                id = LORE_WEAPONS,
                worldId = worldId,
                title = "Gemforged Weapons",
                content = "Frostfire Lance, Stormbreaker Hammer, Shadowthorn Whip, and Abyssal " +
                    "Trident. Their powers resemble known lost gems. They appear in later attacks " +
                    "on Rider territory.",
                category = LoreCategory.Other,
                tags = listOf("weapons", "clues"),
                relatedIds = listOf(LORE_GEMFORGING, LORE_VEIL),
            ),
            lore(
                id = LORE_ECOLOGY,
                worldId = worldId,
                title = "Dragon Ecology",
                content = "The world holds hoarding dragons (Gleann Òir, Cloch Mara), bonding " +
                    "breeds (Coille Dhubh, Neamhnaid), rare ancients (Dràgon Teine-èisg, Aois " +
                    "Draoidheil), and common regional dragons from Meadowwing to Moonshadow. A " +
                    "dragon may keep a gem without telling its Rider.",
                category = LoreCategory.Culture,
                tags = listOf("dragons", "types"),
                relatedIds = listOf(LORE_BONDS),
                locationId = eilean.id,
            ),
            lore(
                id = LORE_VEIL,
                worldId = worldId,
                title = "The Veil of Thorns",
                content = "Almost no public knowledge. At the start of the campaign, neither " +
                    "great faction recognizes a third party as the cause of the escalating conflict.",
                category = LoreCategory.Politics,
                tags = listOf("secret", "antagonist"),
                relatedIds = listOf(LORE_GEMFORGING, LORE_MISUNDERSTANDING, LORE_LUMINA),
                secrets = listOf(
                    LoreSecret(
                        id = "secret-veil",
                        title = "GM secret — do not present early",
                        secret = "The Veil steals gems, destroys selected ones, forges artifacts, " +
                            "searches hoards, attacks Rider territories, and leaves evidence that " +
                            "encourages each side to blame the other. Morcant leads. Eira serves " +
                            "while secretly bonded to Lir.",
                        hints = listOf(
                            LoreHint(id = "hint-veil-0", text = "Someone benefits from the blame.", revealed = true),
                            LoreHint(id = "hint-veil-1", text = "The ambush and the Azure Flame theft do not match.", revealed = false),
                        ),
                    )
                ),
                characterId = morcant.id,
            ),
            lore(
                id = LORE_AURAXITHAR,
                worldId = worldId,
                title = "Origin of the Dragon Gems",
                content = "Public theology disagrees about whether gems are part of dragons, " +
                    "magically created objects, or something older. Do not settle this at the table yet.",
                category = LoreCategory.Myth,
                tags = listOf("optional", "secret"),
                relatedIds = listOf(LORE_GEMFORGING, LORE_CEO),
                secrets = listOf(
                    LoreSecret(
                        id = "secret-auraxithar",
                        title = "Optional — not required canon",
                        secret = "An earlier concept claimed all gems are fragments of Auraxithar, " +
                            "an ancient celestial dragon shattered by others. Keep this secret or " +
                            "non-canon until you decide the campaign should revolve around it.",
                        hints = listOf(
                            LoreHint(id = "hint-aura-0", text = "Some priests say gems feel alive.", revealed = true),
                        ),
                    )
                ),
            ),
        )
    }

    private fun lore(
        id: String,
        worldId: String,
        title: String,
        content: String,
        category: LoreCategory,
        tags: List<String>,
        relatedIds: List<String>,
        secrets: List<LoreSecret> = emptyList(),
        locationId: String? = null,
        characterId: String? = null,
    ): Lore {
        return Lore(
            id = id,
            worldId = worldId,
            title = title,
            content = content,
            category = category,
            tags = tags,
            relatedEntryIds = relatedIds,
            secrets = secrets,
            locationId = locationId,
            characterId = characterId,
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun campaign(worldId: String): Campaign {
        return Campaign(
            id = CAMPAIGN_ID,
            worldId = worldId,
            name = "The Shattered Accord",
            description = "The story should first feel like it is about who is attacking whom. " +
                "It should gradually become about who benefits from making both sides believe that.",
            notes = "Start amid political suspicion, not open knowledge of the Veil. The Riders " +
                "and Warriors each have reasonable evidence. Let discoveries create better " +
                "questions. Dragons have their own opinions. Loose prep: the opening is two " +
                "tables that share one pulse of magic.",
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
                id = CP_CAELUM,
                campaignId = campaignId,
                kind = PersonKind.PlayerCharacter,
                name = "Caelum Ironfist",
                description = "Human barbarian of a fallen Warrior house. Broad-shouldered, " +
                    "scarred, and dangerous without boasting. His Azure Flame Gem was stolen by " +
                    "ordinary thieves. He seeks Daibhidh's help.",
                sheet = FifthEditionSheet(
                    race = "Human",
                    classLevels = listOf(ClassLevel("Barbarian", "Ancestral Guardian", 3)),
                    abilityScores = AbilityScores(18, 14, 16, 10, 12, 12),
                    hitPoints = 38,
                    maxHitPoints = 38,
                    temporaryHitPoints = 0,
                    armorClass = 15,
                    walkSpeed = 30,
                    deathSaves = DeathSaves.none(),
                    items = listOf(
                        InventoryItem("Greataxe", 1, ""),
                        InventoryItem("Azure Flame Gem", 0, "Stolen before the campaign opens."),
                    ),
                    features = listOf(PersonFeature("Rage", "Tied to family, grief, and the fear of failing people who trust him.")),
                    spells = emptyList(),
                    notes = "Neutral Good. Ancestral Guardian. The theft is not the Veil.",
                ),
                overlayHitPoints = 38,
                overlayNotes = "On the road to Gleann Seat, asking a prince for help.",
            ),
            campaignPerson(
                id = CP_AELIANA,
                campaignId = campaignId,
                kind = PersonKind.PlayerCharacter,
                name = "Aeliana Shadowglen",
                description = "Wood elf Gloom Stalker and warden of Coille Mhòr. Quiet, precise, " +
                    "and easy to mistake for cold. Bonded to Lysanthir. Intended love interest for Caelum.",
                sheet = FifthEditionSheet(
                    race = "Wood Elf",
                    classLevels = listOf(ClassLevel("Ranger", "Gloom Stalker", 3)),
                    abilityScores = AbilityScores(12, 18, 14, 12, 16, 14),
                    hitPoints = 28,
                    maxHitPoints = 28,
                    temporaryHitPoints = 0,
                    armorClass = 16,
                    walkSpeed = 35,
                    deathSaves = DeathSaves.none(),
                    items = listOf(
                        InventoryItem("Longbow", 1, ""),
                        InventoryItem("Shortsword", 1, ""),
                    ),
                    features = listOf(PersonFeature("Dread Ambusher", "First-round extra attack and movement.")),
                    spells = listOf(
                        PersonSpell("Hunter's Mark", 1, true),
                        PersonSpell("Cure Wounds", 1, true),
                    ),
                    notes = "Neutral Good. Outlander / Warden of Coille Mhòr.",
                ),
                overlayHitPoints = 28,
                overlayNotes = "At the Rider gathering. Watching the edges of the terrace.",
            ),
            campaignPerson(
                id = CP_FIANNA,
                campaignId = campaignId,
                kind = PersonKind.PlayerCharacter,
                name = "Fianna Blazeheart",
                description = "Sweet, charismatic, and slightly too happy to solve problems with " +
                    "fire. Genuinely kind. Openly dangerous. Bonded to Pyraethus.",
                sheet = FifthEditionSheet(
                    race = "Human",
                    classLevels = listOf(ClassLevel("Sorcerer", "Draconic Bloodline", 3)),
                    abilityScores = AbilityScores(10, 14, 14, 12, 12, 18),
                    hitPoints = 20,
                    maxHitPoints = 20,
                    temporaryHitPoints = 0,
                    armorClass = 13,
                    walkSpeed = 30,
                    deathSaves = DeathSaves.none(),
                    items = listOf(InventoryItem("Arcane focus", 1, "A warm scale from Pyraethus.")),
                    features = listOf(PersonFeature("Draconic Resilience", "Scales when she wants them seen.")),
                    spells = listOf(
                        PersonSpell("Fire Bolt", 0, true),
                        PersonSpell("Burning Hands", 1, true),
                        PersonSpell("Scorching Ray", 2, true),
                    ),
                    notes = "The joke is not that she is secretly dangerous.",
                ),
                overlayHitPoints = 20,
                overlayNotes = "At the gathering, already offering to fly somewhere and look.",
            ),
            campaignPerson(
                id = CP_SERAPHINA,
                campaignId = campaignId,
                kind = PersonKind.PlayerCharacter,
                name = "Seraphina Songweaver",
                description = "Half-elf bard whose song accidentally healed Melodis. Empathetic, " +
                    "joyful, and diplomatic. The bond came looking for her.",
                sheet = FifthEditionSheet(
                    race = "Half-Elf",
                    classLevels = listOf(ClassLevel("Bard", "College of Lore", 3)),
                    abilityScores = AbilityScores(10, 14, 12, 16, 12, 18),
                    hitPoints = 21,
                    maxHitPoints = 21,
                    temporaryHitPoints = 0,
                    armorClass = 13,
                    walkSpeed = 30,
                    deathSaves = DeathSaves.none(),
                    items = listOf(InventoryItem("Lute", 1, "The instrument from the overlook.")),
                    features = listOf(PersonFeature("Cutting Words", "She would rather talk someone down.")),
                    spells = listOf(
                        PersonSpell("Healing Word", 1, true),
                        PersonSpell("Charm Person", 1, true),
                        PersonSpell("Shatter", 2, true),
                    ),
                    notes = "College of Lore. Music is how Melodis found her.",
                ),
                overlayHitPoints = 21,
                overlayNotes = "At the gathering. Trying to keep the younger Riders from starting a war.",
            ),
            campaignPerson(
                id = CP_KAELAN,
                campaignId = campaignId,
                kind = PersonKind.PlayerCharacter,
                name = "Kaelan Windwalker",
                description = "Temple monk and current bearer of the Celestial Shard. Orphaned, " +
                    "raised to believe the warrior should not depend on the gem to prove themselves.",
                sheet = FifthEditionSheet(
                    race = "Human",
                    classLevels = listOf(ClassLevel("Monk", "Way of the Open Hand", 3)),
                    abilityScores = AbilityScores(14, 18, 14, 12, 16, 10),
                    hitPoints = 24,
                    maxHitPoints = 24,
                    temporaryHitPoints = 0,
                    armorClass = 16,
                    walkSpeed = 40,
                    deathSaves = DeathSaves.none(),
                    items = listOf(InventoryItem("Celestial Shard", 1, "Temple trust for five years. Wind, leaps, warding.")),
                    features = listOf(PersonFeature("Open Hand Technique", "Knocks, pushes, or denies reactions.")),
                    spells = emptyList(),
                    notes = "Cloch Custodians — temple sect. Power should prove the warrior.",
                ),
                overlayHitPoints = 24,
                overlayNotes = "With the Warrior table, or arriving after the pulse if you split the cast.",
            ),
            campaignPerson(
                id = CP_LIORA,
                campaignId = campaignId,
                kind = PersonKind.PlayerCharacter,
                name = "Liora Shadowgleam",
                description = "Half-elf rogue, slightly overpowered on purpose. Paired daggers " +
                    "are part of her identity. Bonded to Virentia. Independent, later Aontachd.",
                sheet = FifthEditionSheet(
                    race = "Half-Elf",
                    classLevels = listOf(ClassLevel("Rogue", "Thief", 4)),
                    abilityScores = AbilityScores(12, 20, 14, 14, 12, 16),
                    hitPoints = 31,
                    maxHitPoints = 31,
                    temporaryHitPoints = 0,
                    armorClass = 15,
                    walkSpeed = 30,
                    deathSaves = DeathSaves.none(),
                    items = listOf(
                        InventoryItem("Paired daggers", 2, "Keep them visible even when she outgrows them."),
                        InventoryItem("Thieves' tools", 1, ""),
                    ),
                    features = listOf(PersonFeature("Sneak Attack", "Already a little too good at this.")),
                    spells = emptyList(),
                    notes = "Quick-witted, charming, intensely loyal once trust is earned.",
                ),
                overlayHitPoints = 31,
                overlayNotes = "At the gathering, already bored of speeches.",
            ),
            campaignPerson(
                id = CP_EIRLYS,
                campaignId = campaignId,
                kind = PersonKind.PlayerCharacter,
                name = "Eirlys Ironfist",
                description = "Princess, artificer, and Dragon Warrior by lineage. Inherited the " +
                    "Eclipse Shard and earned Nythendra. Curious, compassionate, and more interested " +
                    "in understanding power than displaying it.",
                sheet = FifthEditionSheet(
                    race = "Human",
                    classLevels = listOf(ClassLevel("Artificer", "Battle Smith", 3)),
                    abilityScores = AbilityScores(10, 14, 13, 16, 14, 12),
                    hitPoints = 24,
                    maxHitPoints = 24,
                    temporaryHitPoints = 0,
                    armorClass = 14,
                    walkSpeed = 30,
                    deathSaves = DeathSaves.none(),
                    items = listOf(
                        InventoryItem("Eclipse Shard", 1, "Lineage gem. Used for study more than war."),
                        InventoryItem("Tinker's tools", 1, "The mechanical bird started here."),
                    ),
                    features = listOf(PersonFeature("Artifice", "Magical engineering and animal handling together.")),
                    spells = listOf(
                        PersonSpell("Mending", 0, true),
                        PersonSpell("Identify", 1, true),
                        PersonSpell("Heat Metal", 2, true),
                    ),
                    notes = "Her brother's last request is that she remain herself.",
                ),
                overlayHitPoints = 24,
                overlayNotes = "In the castle garden immediately before Daibhidh returns.",
            ),
        )
        val overlayIds = listOf(
            WP_AILIN to "Holding court. Do not tell him the Veil exists.",
            WP_DAIBHIDH to "Alive through the ambush. Dead only after session 4.",
            WP_EIRA to "Not publicly present. Do not place her unless someone is looking for the wrong trail.",
            WP_LIR to "Hidden with Eira. Dragons at the gathering may feel him without naming him.",
            WP_NYTHENDRA to "Near Eirlys if she calls. Otherwise a presence in the high air.",
            WP_LYSANTHIR to "With Aeliana at the gathering.",
            WP_PYRAETHUS to "With Fianna. Already impatient.",
            WP_MELODIS to "With Seraphina. Listening more than speaking.",
            WP_VIRENTIA to "With Liora. Watching the forest line of the terrace.",
        )
        val overlays = overlayIds.map { (worldId, notes) ->
            val source = worldPeople.first { it.id == worldId }
            campaignPerson(
                id = "cp-$worldId",
                campaignId = campaignId,
                worldPersonId = source.id,
                kind = source.kind,
                name = source.name,
                description = source.description,
                sheet = source.sheet,
                overlayHitPoints = source.sheet.hitPoints,
                overlayNotes = notes,
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
        sheet: PersonSheet,
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

    private fun overlays(campaignId: String): List<LocationOverlay> {
        return listOf(
            LocationOverlay(
                campaignId = campaignId,
                locationId = LOC_RESIDENCE,
                hasPartyPresence = true,
                notes = "Warrior opening. Caelum and Eirlys are in Gleann Seat.",
                updatedAt = now,
            ),
            LocationOverlay(
                campaignId = campaignId,
                locationId = LOC_GATHERING,
                hasPartyPresence = true,
                notes = "Rider opening. The gathering is in progress when the pulse hits.",
                updatedAt = now,
            ),
            LocationOverlay(
                campaignId = campaignId,
                locationId = LOC_AMBUSH,
                hasPartyPresence = false,
                notes = "Session 2 road. Empty until Caelum and Daibhidh travel.",
                updatedAt = now,
            ),
            LocationOverlay(
                campaignId = campaignId,
                locationId = LOC_GARDEN,
                hasPartyPresence = false,
                notes = "Session 3 memory. Use immediately before the return.",
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
                id = SESS_AZURE,
                campaignId = campaignId,
                name = "Session 1: Caelum's Stolen Flame",
                notes = "Caelum's Azure Flame Gem is gone. Ordinary thieves took it. He seeks " +
                    "Daibhidh. If the table wants to hunt the thieves first, let them. Do not " +
                    "connect this theft to the Veil.",
                inWorldDate = WorldDate(year = 412, monthId = MONTH_GLEANNACH, day = 4),
                scenes = listOf(
                    SessionScene(id = "scene-1-0", title = "The empty setting", notes = "The gem is gone. The thieves were sloppy."),
                    SessionScene(id = "scene-1-1", title = "A prince will hear him", notes = "Daibhidh agrees to help. Two missing gems is not yet a pattern."),
                    SessionScene(id = "scene-1-2", title = "Where next", notes = "Ride with Daibhidh, chase the thieves, or send word to Eirlys."),
                ),
                marchOrder = march,
                createdAt = now,
                updatedAt = now,
            ),
            Session(
                id = SESS_AMBUSH,
                campaignId = campaignId,
                name = "Session 2: The Ambush Road",
                notes = "The road goes quiet. A deliberate obstruction. A coordinated group " +
                    "takes the Emerald Heart and leaves. They were not after Caelum. If the " +
                    "table captures someone, they still should not name the Veil.",
                inWorldDate = WorldDate(year = 412, monthId = MONTH_GLEANNACH, day = 6),
                scenes = listOf(
                    SessionScene(id = "scene-2-0", title = "Unnatural quiet", notes = "Birds stop. The obstruction is placed, not fallen."),
                    SessionScene(id = "scene-2-1", title = "The take", notes = "Disciplined. They want Daibhidh's gem."),
                    SessionScene(id = "scene-2-2", title = "Why not Caelum's", notes = "The mystery is the mismatch. The thefts are unrelated."),
                ),
                marchOrder = march,
                createdAt = now,
                updatedAt = now,
            ),
            Session(
                id = SESS_MEMORY,
                campaignId = campaignId,
                name = "Session 3: Wildflowers in the Garden",
                notes = "Before Daibhidh returns, Eirlys remembers the mechanical bird and the " +
                    "promise to remain herself. Use it immediately before she learns he is injured. " +
                    "If she is not at the table, someone else can walk the garden.",
                inWorldDate = WorldDate(year = 412, monthId = MONTH_GLEANNACH, day = 7),
                scenes = listOf(
                    SessionScene(id = "scene-3-0", title = "The mechanical bird", notes = "Childhood. He praised her mind, not her future as a warrior."),
                    SessionScene(id = "scene-3-1", title = "The promise", notes = "Remain yourself, no matter what the house demands."),
                    SessionScene(id = "scene-3-2", title = "Word from the gate", notes = "He is back. He does not have the gem."),
                ),
                marchOrder = march,
                createdAt = now,
                updatedAt = now,
            ),
            Session(
                id = SESS_DEATH,
                campaignId = campaignId,
                name = "Session 4: The Heart Breaks",
                notes = "Daibhidh and Caelum return. Before a recovery effort can start, the " +
                    "stolen gem is destroyed elsewhere. Healing cannot repair it. His last words " +
                    "are to Eirlys as her brother. If they never come home, the pulse still happens.",
                inWorldDate = WorldDate(year = 412, monthId = MONTH_GLEANNACH, day = 7),
                scenes = listOf(
                    SessionScene(id = "scene-4-0", title = "The return", notes = "Injured, gemless, still himself."),
                    SessionScene(id = "scene-4-1", title = "The shock", notes = "Something fundamental is ripped out. It is not a wound."),
                    SessionScene(id = "scene-4-2", title = "Don't break that promise", notes = "Brother first. Prince second. Permission, not vengeance."),
                ),
                marchOrder = march,
                createdAt = now,
                updatedAt = now,
            ),
            Session(
                id = SESS_GATHERING,
                campaignId = campaignId,
                name = "Session 5: The Gathering Feels It",
                notes = "Same moment, Eilean na Dràgon. A tense gathering about accusations, " +
                    "not stolen gems. Then dragons scream, the air stills, a green-white pulse " +
                    "crosses the sky. The Riders do not know what died. If they want to fly inland, let them.",
                inWorldDate = WorldDate(year = 412, monthId = MONTH_GLEANNACH, day = 7),
                scenes = listOf(
                    SessionScene(id = "scene-5-0", title = "Rumors from Ard-Gleann", notes = "Younger Riders want action. Elders want restraint."),
                    SessionScene(id = "scene-5-1", title = "The pulse", notes = "Dragons turn to one horizon. Some Riders feel a heartbeat that is not theirs stop."),
                    SessionScene(id = "scene-5-2", title = "What they decide", notes = "Investigate, arm, send envoys, or wait. Do not invent a deadline."),
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
                id = "plot-azure",
                campaignId = campaignId,
                sessionId = byId.getValue(SESS_AZURE).id,
                title = "Ordinary thieves took the Azure Flame",
                details = "Caelum's gem is gone. Default: ordinary thieves. Do not fold this " +
                    "into the Veil unless the table later makes that collision interesting.",
                status = PlotThreadStatus.InProgress,
                priority = PlotThreadPriority.High,
                createdAt = now,
                updatedAt = now,
            ),
            PlotThread(
                id = "plot-emerald",
                campaignId = campaignId,
                sessionId = byId.getValue(SESS_AMBUSH).id,
                title = "The Emerald Heart is taken and destroyed",
                details = "A coordinated ambush steals Daibhidh's gem. It is smashed elsewhere. " +
                    "He dies. This is the first modern proof that Gemforging is real.",
                status = PlotThreadStatus.InProgress,
                priority = PlotThreadPriority.Critical,
                createdAt = now,
                updatedAt = now,
            ),
            PlotThread(
                id = "plot-promise",
                campaignId = campaignId,
                sessionId = byId.getValue(SESS_MEMORY).id,
                title = "Eirlys's promise",
                details = "Daibhidh asked her to remain herself. His death gives her permission, " +
                    "not a command to avenge him. Succession pressure comes from everyone else.",
                status = PlotThreadStatus.Open,
                priority = PlotThreadPriority.High,
                createdAt = now,
                updatedAt = now,
            ),
            PlotThread(
                id = "plot-blame",
                campaignId = campaignId,
                sessionId = byId.getValue(SESS_GATHERING).id,
                title = "Each side has reasonable evidence",
                details = "Warriors will blame Riders. Riders will blame Warrior gem-magic. " +
                    "Both readings work. Neither is the whole pattern.",
                status = PlotThreadStatus.Open,
                priority = PlotThreadPriority.High,
                createdAt = now,
                updatedAt = now,
            ),
            PlotThread(
                id = "plot-veil",
                campaignId = campaignId,
                sessionId = byId.getValue(SESS_DEATH).id,
                title = "Someone benefits from the blame",
                details = "GM only. The Veil of Thorns is harvesting gems and leaving evidence. " +
                    "Do not let every clue point at a mysterious cult.",
                status = PlotThreadStatus.Open,
                priority = PlotThreadPriority.Medium,
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
        val gleann = locations.first { it.id == LOC_GLEANN_SEAT }
        val ambush = locations.first { it.id == LOC_AMBUSH }
        val residence = locations.first { it.id == LOC_RESIDENCE }
        val gathering = locations.first { it.id == LOC_GATHERING }
        val daibhidh = worldPeople.first { it.id == WP_DAIBHIDH }
        val caelum = campaignPeople.first { it.id == CP_CAELUM }
        val eirlys = campaignPeople.first { it.id == CP_EIRLYS }
        val aeliana = campaignPeople.first { it.id == CP_AELIANA }
        val azure = loreEntries.first { it.id == LORE_AZURE }
        val emerald = loreEntries.first { it.id == LORE_EMERALD }
        val blame = loreEntries.first { it.id == LORE_MISUNDERSTANDING }
        val sessAzure = sessions.first { it.id == SESS_AZURE }
        val sessAmbush = sessions.first { it.id == SESS_AMBUSH }
        val sessDeath = sessions.first { it.id == SESS_DEATH }
        val sessGathering = sessions.first { it.id == SESS_GATHERING }
        return listOf(
            Quest(
                id = "quest-azure",
                campaignId = campaignId,
                title = "Recover the Azure Flame",
                summary = "Caelum wants his gem back. The thieves were ordinary. The party can " +
                    "return the stone and still walk into a larger war.",
                status = QuestStatus.Active,
                locationId = gleann.id,
                objectives = listOf(
                    QuestObjective(id = "obj-azure-0", title = "Tell Daibhidh what was taken", status = QuestObjectiveStatus.Open),
                    QuestObjective(id = "obj-azure-1", title = "Find the thieves, or decide not to", status = QuestObjectiveStatus.Open),
                    QuestObjective(id = "obj-azure-2", title = "Recover the Azure Flame Gem", status = QuestObjectiveStatus.Open),
                ),
                links = listOf(
                    QuestLink(id = "ql-azure-lore", kind = QuestLinkKind.LORE, targetId = azure.id),
                    QuestLink(id = "ql-azure-caelum", kind = QuestLinkKind.CAMPAIGN_PERSON, targetId = caelum.id),
                    QuestLink(id = "ql-azure-sess", kind = QuestLinkKind.SESSION, targetId = sessAzure.id),
                ),
                createdAt = now,
                updatedAt = now,
            ),
            Quest(
                id = "quest-ambushers",
                campaignId = campaignId,
                title = "Identify the Ambushers",
                summary = "Whoever took the Emerald Heart was organized. Finding them is not the " +
                    "same as naming the Veil.",
                status = QuestStatus.Active,
                locationId = ambush.id,
                objectives = listOf(
                    QuestObjective(id = "obj-ambush-0", title = "Survive the road", status = QuestObjectiveStatus.Open),
                    QuestObjective(id = "obj-ambush-1", title = "Learn who wanted the prince's gem", status = QuestObjectiveStatus.Open),
                ),
                links = listOf(
                    QuestLink(id = "ql-ambush-lore", kind = QuestLinkKind.LORE, targetId = emerald.id),
                    QuestLink(id = "ql-ambush-daibhidh", kind = QuestLinkKind.WORLD_PERSON, targetId = daibhidh.id),
                    QuestLink(id = "ql-ambush-sess", kind = QuestLinkKind.SESSION, targetId = sessAmbush.id),
                ),
                createdAt = now,
                updatedAt = now,
            ),
            Quest(
                id = "quest-fallout",
                campaignId = campaignId,
                title = "Survive the Political Fallout",
                summary = "Daibhidh's death will be read as Rider work unless someone makes a " +
                    "better case. Completing this can mean keeping Eirlys herself, not crowning her.",
                status = QuestStatus.Active,
                locationId = residence.id,
                objectives = listOf(
                    QuestObjective(id = "obj-fallout-0", title = "Witness what the gem's destruction does", status = QuestObjectiveStatus.Open),
                    QuestObjective(id = "obj-fallout-1", title = "Stand with Eirlys, or leave her to the court", status = QuestObjectiveStatus.Open),
                ),
                links = listOf(
                    QuestLink(id = "ql-fallout-eirlys", kind = QuestLinkKind.CAMPAIGN_PERSON, targetId = eirlys.id),
                    QuestLink(id = "ql-fallout-sess", kind = QuestLinkKind.SESSION, targetId = sessDeath.id),
                ),
                createdAt = now,
                updatedAt = now,
            ),
            Quest(
                id = "quest-gathering",
                campaignId = campaignId,
                title = "Rider Gathering Diplomacy",
                summary = "The Riders are worried about being blamed, not about missing gems. " +
                    "The pulse will interrupt whatever they decide.",
                status = QuestStatus.Active,
                locationId = gathering.id,
                objectives = listOf(
                    QuestObjective(id = "obj-gather-0", title = "Hear the accusations", status = QuestObjectiveStatus.Open),
                    QuestObjective(id = "obj-gather-1", title = "Choose restraint, action, or a third path", status = QuestObjectiveStatus.Open),
                ),
                links = listOf(
                    QuestLink(id = "ql-gather-lore", kind = QuestLinkKind.LORE, targetId = blame.id),
                    QuestLink(id = "ql-gather-aeliana", kind = QuestLinkKind.CAMPAIGN_PERSON, targetId = aeliana.id),
                    QuestLink(id = "ql-gather-sess", kind = QuestLinkKind.SESSION, targetId = sessGathering.id),
                ),
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    private fun relationships(
        worldPeople: List<WorldPerson>,
        campaignPeople: List<CampaignPerson>,
    ): List<PersonRelationship> {
        val ailin = worldPeople.first { it.id == WP_AILIN }
        val daibhidh = worldPeople.first { it.id == WP_DAIBHIDH }
        val eirlys = campaignPeople.first { it.id == CP_EIRLYS }
        val caelum = campaignPeople.first { it.id == CP_CAELUM }
        val aeliana = campaignPeople.first { it.id == CP_AELIANA }
        val fianna = campaignPeople.first { it.id == CP_FIANNA }
        val seraphina = campaignPeople.first { it.id == CP_SERAPHINA }
        val liora = campaignPeople.first { it.id == CP_LIORA }
        val morcant = worldPeople.first { it.id == WP_MORCANT }
        val eira = worldPeople.first { it.id == WP_EIRA }
        val lir = worldPeople.first { it.id == WP_LIR }
        val nythendra = worldPeople.first { it.id == WP_NYTHENDRA }
        val lysanthir = worldPeople.first { it.id == WP_LYSANTHIR }
        val pyraethus = worldPeople.first { it.id == WP_PYRAETHUS }
        val melodis = worldPeople.first { it.id == WP_MELODIS }
        val virentia = worldPeople.first { it.id == WP_VIRENTIA }
        val eiraCampaign = campaignPeople.first { it.worldPersonId == WP_EIRA }
        return listOf(
            PersonRelationship(
                id = "rel-ailin-daibhidh",
                from = PersonRef.World(ailin.id),
                to = PersonRef.World(daibhidh.id),
                type = RelationshipType.Parent,
                description = "King and heir. Ailín must later grieve as a father while remaining a king.",
                factionId = FAC_CLOCH,
            ),
            PersonRelationship(
                id = "rel-ailin-eirlys",
                from = PersonRef.World(ailin.id),
                to = PersonRef.Campaign(eirlys.id),
                type = RelationshipType.Parent,
                description = "He loves her mind and still needs a public face for the house.",
                factionId = FAC_CLOCH,
            ),
            PersonRelationship(
                id = "rel-daibhidh-eirlys",
                from = PersonRef.World(daibhidh.id),
                to = PersonRef.Campaign(eirlys.id),
                type = RelationshipType.Sibling,
                description = "He asked her to remain herself. His last words return to that promise.",
                factionId = FAC_CLOCH,
            ),
            PersonRelationship(
                id = "rel-aeliana-caelum",
                from = PersonRef.Campaign(aeliana.id),
                to = PersonRef.Campaign(caelum.id),
                type = RelationshipType.Other,
                description = "Intended love interest. The bond is not established yet. Let the table earn it.",
                factionId = null,
            ),
            PersonRelationship(
                id = "rel-morcant-eira",
                from = PersonRef.World(morcant.id),
                to = PersonRef.World(eira.id),
                type = RelationshipType.Ally,
                description = "Lieutenant and enforcer. He does not know she is bonded to Lir unless you decide he does.",
                factionId = FAC_VEIL,
            ),
            PersonRelationship(
                id = "rel-eira-lir",
                from = PersonRef.Campaign(eiraCampaign.id),
                to = PersonRef.World(lir.id),
                type = RelationshipType.Ally,
                description = "Hidden Rider bond. Lir chose her. Why should matter.",
                factionId = FAC_VEIL,
            ),
            PersonRelationship(
                id = "rel-eirlys-nythendra",
                from = PersonRef.Campaign(eirlys.id),
                to = PersonRef.World(nythendra.id),
                type = RelationshipType.Mentor,
                description = "Scholar and ancient mentor who happen to care for one another. Not ownership.",
                factionId = FAC_CLOCH,
            ),
            PersonRelationship(
                id = "rel-aeliana-lysanthir",
                from = PersonRef.Campaign(aeliana.id),
                to = PersonRef.World(lysanthir.id),
                type = RelationshipType.Ally,
                description = "Chosen Rider bond. He may one day give Aurora's Heart to Caelum.",
                factionId = FAC_AONTACHD,
            ),
            PersonRelationship(
                id = "rel-fianna-pyraethus",
                from = PersonRef.Campaign(fianna.id),
                to = PersonRef.World(pyraethus.id),
                type = RelationshipType.Ally,
                description = "They encourage one another. This is occasionally a problem.",
                factionId = FAC_AONTACHD,
            ),
            PersonRelationship(
                id = "rel-seraphina-melodis",
                from = PersonRef.Campaign(seraphina.id),
                to = PersonRef.World(melodis.id),
                type = RelationshipType.Ally,
                description = "He came looking for the person behind the song.",
                factionId = FAC_AONTACHD,
            ),
            PersonRelationship(
                id = "rel-liora-virentia",
                from = PersonRef.Campaign(liora.id),
                to = PersonRef.World(virentia.id),
                type = RelationshipType.Ally,
                description = "Green dragon of living ecosystems. A later gem gift should feel like trust.",
                factionId = FAC_AONTACHD,
            ),
        )
    }

    private fun factions(worldId: String): List<Faction> {
        return listOf(
            faction(
                id = FAC_AONTACHD,
                worldId = worldId,
                name = "Dràgonan Aontachd",
                description = "Dragon Unity. The most influential political and cultural organization of Dragon Riders.",
                goals = "Treat dragon bonds as sacred. Defend Rider territories and the idea that dragons are persons, not mounts.",
                notes = "Led by the Seanair Council of elder Riders. As Warrior gems vanish, suspicion falls on them.",
            ),
            faction(
                id = FAC_CLOCH,
                worldId = worldId,
                name = "Cloch Custodians",
                description = "The principal Dragon Warrior tradition. Guardians, bearers, and inheritors of dragon gems.",
                goals = "Keep gems as sacred trusts, family legacies, and weapons of last resort.",
                notes = "Homeland Ard-Gleann. Royal House of Ailín. Daibhidh's death makes them look vulnerable.",
            ),
            faction(
                id = FAC_GORMSHUIL,
                worldId = worldId,
                name = "Gormshùil Sentinels",
                description = "Blue Eye Sentinels. Meritocracy of scouts, sailors, and intelligence officers.",
                goals = "Watch the coasts, gather intelligence, and survive Cuan Stoirm.",
                notes = "Notable: Fionnlagh the Stormblade.",
            ),
            faction(
                id = FAC_LIATH,
                worldId = worldId,
                name = "Liath Fàidhean",
                description = "Grey Seers. Oligarchy of diviners, illusionists, and political counselors.",
                goals = "Keep secret knowledge and sell counsel without being owned by either great tradition.",
                notes = "Leader: Archseer Brìghde. Notable: Seer Calum.",
            ),
            faction(
                id = FAC_CEARDAICHEAN,
                worldId = worldId,
                name = "Ceardaichean",
                description = "The Craftsmen. Guild of weapons, armor, artifacts, engineering, and alchemy.",
                goals = "Make the objects both traditions cannot live without.",
                notes = "Leader: Daigh the Forgeheart. Notable: Eiric the Alchemist.",
            ),
            faction(
                id = FAC_HUNTERS,
                worldId = worldId,
                name = "Fìor-Bhèist Hunters",
                description = "True Beast Hunters. Elected by hunting achievement. Frontier protection.",
                goals = "Track extraordinary quarry and keep settlements alive at the edge of dragon country.",
                notes = "Leader: Mòrag the Dragonbane. Notable: Torcall the Tracker.",
            ),
            faction(
                id = FAC_COVENANT,
                worldId = worldId,
                name = "Order of the Draconic Covenant",
                description = "Major faith of clerics, paladins, and pilgrims who refuse to treat dragons as weapons.",
                goals = "Protect sacred sites, recover stolen gems, and prevent magical desecration.",
                notes = "Useful for religious PCs and NPCs. Not a combat faction in the opening.",
            ),
            faction(
                id = FAC_VEIL,
                worldId = worldId,
                name = "Veil of Thorns",
                description = "GM secret. Hidden force exploiting Rider-Warrior mistrust while rediscovering Gemforging.",
                goals = "Steal gems, destroy selected ones, forge artifacts, and keep both sides blaming each other.",
                notes = "Do not present as common player knowledge. Morcant leads. Eira serves while secretly bonded to Lir.",
            ),
        )
    }

    private fun faction(
        id: String,
        worldId: String,
        name: String,
        description: String,
        goals: String,
        notes: String,
    ): Faction {
        return Faction(
            id = id,
            worldId = worldId,
            name = name,
            description = description,
            goals = goals,
            notes = notes,
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun memberships(
        worldPeople: List<WorldPerson>,
        campaignPeople: List<CampaignPerson>,
    ): List<FactionMembership> {
        val ailin = worldPeople.first { it.id == WP_AILIN }
        val daibhidh = worldPeople.first { it.id == WP_DAIBHIDH }
        val morcant = worldPeople.first { it.id == WP_MORCANT }
        val eira = worldPeople.first { it.id == WP_EIRA }
        val fionnlagh = worldPeople.first { it.id == WP_FIONNLAGH }
        val brighde = worldPeople.first { it.id == WP_BRIGHDE }
        val calum = worldPeople.first { it.id == WP_CALUM }
        val daigh = worldPeople.first { it.id == WP_DAIGH }
        val eiric = worldPeople.first { it.id == WP_EIRIC }
        val morag = worldPeople.first { it.id == WP_MORAG }
        val torcall = worldPeople.first { it.id == WP_TORCALL }
        val caelum = campaignPeople.first { it.id == CP_CAELUM }
        val eirlys = campaignPeople.first { it.id == CP_EIRLYS }
        val kaelan = campaignPeople.first { it.id == CP_KAELAN }
        val aeliana = campaignPeople.first { it.id == CP_AELIANA }
        val fianna = campaignPeople.first { it.id == CP_FIANNA }
        val seraphina = campaignPeople.first { it.id == CP_SERAPHINA }
        val liora = campaignPeople.first { it.id == CP_LIORA }
        return listOf(
            membership("mem-ailin-cloch", PersonRef.World(ailin.id), FAC_CLOCH, "King", "Bearer of the Emerald Storm Gem."),
            membership("mem-daibhidh-cloch", PersonRef.World(daibhidh.id), FAC_CLOCH, "Prince", "Bearer of the Emerald Heart until the ambush."),
            membership("mem-eirlys-cloch", PersonRef.Campaign(eirlys.id), FAC_CLOCH, "Princess / artificer", "Warrior by lineage. Also companioned by Nythendra."),
            membership("mem-caelum-cloch", PersonRef.Campaign(caelum.id), FAC_CLOCH, "Gem bearer", "Azure Flame stolen before play begins."),
            membership("mem-kaelan-cloch", PersonRef.Campaign(kaelan.id), FAC_CLOCH, "Celestial Shard bearer", "Temple sect. Five-year trust, not a dynasty."),
            membership("mem-aeliana-aontachd", PersonRef.Campaign(aeliana.id), FAC_AONTACHD, "Rider", "Bonded to Lysanthir."),
            membership("mem-fianna-aontachd", PersonRef.Campaign(fianna.id), FAC_AONTACHD, "Rider", "Bonded to Pyraethus."),
            membership("mem-seraphina-aontachd", PersonRef.Campaign(seraphina.id), FAC_AONTACHD, "Rider", "Bonded to Melodis."),
            membership("mem-liora-aontachd", PersonRef.Campaign(liora.id), FAC_AONTACHD, "Rider", "Initially independent. Later associated with the Aontachd."),
            membership("mem-morcant-veil", PersonRef.World(morcant.id), FAC_VEIL, "Leader", "Secret bearer of the Lumina Shard."),
            membership("mem-eira-veil", PersonRef.World(eira.id), FAC_VEIL, "Lieutenant", "Publicly anti-Rider. Secretly bonded to Lir."),
            membership("mem-fionnlagh-gorm", PersonRef.World(fionnlagh.id), FAC_GORMSHUIL, "Stormblade", ""),
            membership("mem-brighde-liath", PersonRef.World(brighde.id), FAC_LIATH, "Archseer", ""),
            membership("mem-calum-liath", PersonRef.World(calum.id), FAC_LIATH, "Seer", ""),
            membership("mem-daigh-ceard", PersonRef.World(daigh.id), FAC_CEARDAICHEAN, "Master Craftsman", ""),
            membership("mem-eiric-ceard", PersonRef.World(eiric.id), FAC_CEARDAICHEAN, "Alchemist", ""),
            membership("mem-morag-hunt", PersonRef.World(morag.id), FAC_HUNTERS, "Dragonbane", "Elected leader."),
            membership("mem-torcall-hunt", PersonRef.World(torcall.id), FAC_HUNTERS, "Tracker", ""),
        )
    }

    private fun membership(
        id: String,
        person: PersonRef,
        factionId: String,
        role: String,
        notes: String,
    ): FactionMembership {
        return FactionMembership(
            id = id,
            person = person,
            factionId = factionId,
            role = role,
            notes = notes,
            createdAt = now,
        )
    }

    private fun companions(
        worldPeople: List<WorldPerson>,
        campaignPeople: List<CampaignPerson>,
    ): List<PersonCompanion> {
        val eirlys = campaignPeople.first { it.id == CP_EIRLYS }
        val aeliana = campaignPeople.first { it.id == CP_AELIANA }
        val fianna = campaignPeople.first { it.id == CP_FIANNA }
        val seraphina = campaignPeople.first { it.id == CP_SERAPHINA }
        val liora = campaignPeople.first { it.id == CP_LIORA }
        val eira = campaignPeople.first { it.worldPersonId == WP_EIRA }
        val nythendra = worldPeople.first { it.id == WP_NYTHENDRA }
        val lysanthir = worldPeople.first { it.id == WP_LYSANTHIR }
        val pyraethus = worldPeople.first { it.id == WP_PYRAETHUS }
        val melodis = worldPeople.first { it.id == WP_MELODIS }
        val virentia = worldPeople.first { it.id == WP_VIRENTIA }
        val lir = worldPeople.first { it.id == WP_LIR }
        return listOf(
            PersonCompanion(
                id = "comp-eirlys-nythendra",
                owner = PersonRef.Campaign(eirlys.id),
                companion = PersonRef.World(nythendra.id),
                kind = CompanionKind.AnimalCompanion,
            ),
            PersonCompanion(
                id = "comp-aeliana-lysanthir",
                owner = PersonRef.Campaign(aeliana.id),
                companion = PersonRef.World(lysanthir.id),
                kind = CompanionKind.AnimalCompanion,
            ),
            PersonCompanion(
                id = "comp-fianna-pyraethus",
                owner = PersonRef.Campaign(fianna.id),
                companion = PersonRef.World(pyraethus.id),
                kind = CompanionKind.AnimalCompanion,
            ),
            PersonCompanion(
                id = "comp-seraphina-melodis",
                owner = PersonRef.Campaign(seraphina.id),
                companion = PersonRef.World(melodis.id),
                kind = CompanionKind.AnimalCompanion,
            ),
            PersonCompanion(
                id = "comp-liora-virentia",
                owner = PersonRef.Campaign(liora.id),
                companion = PersonRef.World(virentia.id),
                kind = CompanionKind.AnimalCompanion,
            ),
            PersonCompanion(
                id = "comp-eira-lir",
                owner = PersonRef.Campaign(eira.id),
                companion = PersonRef.World(lir.id),
                kind = CompanionKind.AnimalCompanion,
            ),
        )
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

    private fun historicalSheet(notes: String): FifthEditionSheet {
        return FifthEditionSheet.empty().copy(
            race = "Human",
            hitPoints = 0,
            maxHitPoints = 0,
            notes = notes,
        )
    }

    private fun monsterSheet(
        race: String,
        scores: AbilityScores,
        hitPoints: Int,
        armorClass: Int,
        walkSpeed: Int,
        features: List<PersonFeature>,
        notes: String,
    ): FifthEditionSheet {
        return FifthEditionSheet(
            race = race,
            classLevels = emptyList(),
            abilityScores = scores,
            hitPoints = hitPoints,
            maxHitPoints = hitPoints,
            temporaryHitPoints = 0,
            armorClass = armorClass,
            walkSpeed = walkSpeed,
            deathSaves = DeathSaves.none(),
            items = emptyList(),
            features = features,
            spells = emptyList(),
            notes = notes,
        )
    }

    private companion object {
        const val WORLD_ID = "world-accord"
        const val CAL_ID = "cal-accord"
        const val MONTH_CEO = "month-ceo"
        const val MONTH_AONTACHD = "month-aontachd"
        const val MONTH_GLEANNACH = "month-gleannach"
        const val MONTH_TEINE = "month-teine"
        const val MONTH_UISGE = "month-uisge"
        const val MONTH_GAOTH = "month-gaoth"
        const val MONTH_COILLEACH = "month-coilleach"
        const val MONTH_STOIRM = "month-stoirm"
        const val MONTH_FOGHAR = "month-foghar"
        const val MONTH_REOTHADH = "month-reothadh"
        const val MONTH_GEAMHRADH = "month-geamhradh"
        const val MONTH_DIOMHAIR = "month-diomhair"
        const val CAMPAIGN_ID = "camp-accord"
        const val LOC_LANDS = "loc-accord-lands"
        const val LOC_EILEAN = "loc-eilean"
        const val LOC_COILLE = "loc-coille"
        const val LOC_ARD = "loc-ard-gleann"
        const val LOC_NEUTRAL = "loc-neutral-march"
        const val LOC_CUAN = "loc-cuan-stoirm"
        const val LOC_HAVEN = "loc-aontachd-haven"
        const val LOC_GLEANN_SEAT = "loc-gleann-seat"
        const val LOC_DUN = "loc-dun-na-sith"
        const val LOC_STORMHARBOR = "loc-stormharbor"
        const val LOC_WARDEN = "loc-heartwood"
        const val LOC_GATHERING = "loc-gathering"
        const val LOC_SAPPHIRE = "loc-sapphire-falls"
        const val LOC_CLOUDTOP = "loc-cloudtop"
        const val LOC_MISTY = "loc-misty-vale"
        const val LOC_WHISPER = "loc-whispering-woods"
        const val LOC_RESIDENCE = "loc-royal-residence"
        const val LOC_GARDEN = "loc-castle-garden"
        const val LOC_AMBUSH = "loc-ambush-road"
        const val FAC_AONTACHD = "fac-aontachd"
        const val FAC_CLOCH = "fac-cloch"
        const val FAC_GORMSHUIL = "fac-gormshuil"
        const val FAC_LIATH = "fac-liath"
        const val FAC_CEARDAICHEAN = "fac-ceardaichean"
        const val FAC_HUNTERS = "fac-hunters"
        const val FAC_COVENANT = "fac-covenant"
        const val FAC_VEIL = "fac-veil"
        const val WP_MORCANT = "wp-morcant"
        const val WP_EIRA = "wp-eira"
        const val WP_AILIN = "wp-ailin"
        const val WP_DAIBHIDH = "wp-daibhidh"
        const val WP_FIONNLAGH = "wp-fionnlagh"
        const val WP_BRIGHDE = "wp-brighde"
        const val WP_CALUM = "wp-calum"
        const val WP_DAIGH = "wp-daigh"
        const val WP_EIRIC = "wp-eiric"
        const val WP_MORAG = "wp-morag"
        const val WP_TORCALL = "wp-torcall"
        const val WP_SERENA = "wp-serena"
        const val WP_BROGAN = "wp-brogan"
        const val WP_LILIANA = "wp-liliana"
        const val WP_EWAN = "wp-ewan"
        const val WP_GWENDOLYN = "wp-gwendolyn"
        const val WP_KAEL = "wp-kael"
        const val WP_LIR = "wp-lir"
        const val WP_NYTHENDRA = "wp-nythendra"
        const val WP_LYSANTHIR = "wp-lysanthir"
        const val WP_PYRAETHUS = "wp-pyraethus"
        const val WP_MELODIS = "wp-melodis"
        const val WP_VIRENTIA = "wp-virentia"
        const val CP_CAELUM = "cp-caelum"
        const val CP_AELIANA = "cp-aeliana"
        const val CP_FIANNA = "cp-fianna"
        const val CP_SERAPHINA = "cp-seraphina"
        const val CP_KAELAN = "cp-kaelan"
        const val CP_LIORA = "cp-liora"
        const val CP_EIRLYS = "cp-eirlys"
        const val SESS_AZURE = "sess-azure"
        const val SESS_AMBUSH = "sess-ambush"
        const val SESS_MEMORY = "sess-memory"
        const val SESS_DEATH = "sess-death"
        const val SESS_GATHERING = "sess-gathering"
        const val LORE_CEO = "lore-ceo"
        const val LORE_HARMONY = "lore-harmony"
        const val LORE_WARS = "lore-wars"
        const val LORE_GEMFORGING = "lore-gemforging"
        const val LORE_WEEPING = "lore-weeping"
        const val LORE_BONDS = "lore-bonds"
        const val LORE_COVENANT = "lore-covenant"
        const val LORE_HOUSE = "lore-house"
        const val LORE_MISUNDERSTANDING = "lore-misunderstanding"
        const val LORE_LUMINA = "lore-lumina"
        const val LORE_EMERALD = "lore-emerald"
        const val LORE_AZURE = "lore-azure"
        const val LORE_ECLIPSE = "lore-eclipse"
        const val LORE_AURORA = "lore-aurora"
        const val LORE_CELESTIAL = "lore-celestial"
        const val LORE_WEAPONS = "lore-weapons"
        const val LORE_ECOLOGY = "lore-ecology"
        const val LORE_VEIL = "lore-veil"
        const val LORE_AURAXITHAR = "lore-auraxithar"
    }
}
