package io.github.kmbisset89.worldweaver.domain

/**
 * Static chips and default prose for the one-shot wizard.
 */
internal class OneShotTemplateCatalog {
    data class Chip(
        val id: String,
        val label: String,
    )

    data class Genre(
        val id: String,
        val label: String,
        val worldDescription: String,
        val realmName: String,
        val regionName: String,
        val settlementName: String,
        val climate: String,
        val terrain: String,
        val logline: String,
    )

    data class Tone(
        val id: String,
        val label: String,
        val flavor: String,
    )

    data class Hook(
        val id: String,
        val label: String,
        val text: String,
        val questTitle: String,
        val stakes: String,
        val objective: String,
    )

    data class SiteType(
        val id: String,
        val label: String,
        val openingName: String,
        val openingDescription: String,
        val middleName: String,
        val middleDescription: String,
        val climaxName: String,
        val climaxDescription: String,
    )

    data class VillainType(
        val id: String,
        val label: String,
        val name: String,
        val description: String,
        val kind: PersonKind,
        val factionName: String,
        val factionDescription: String,
    )

    data class Objective(
        val id: String,
        val label: String,
    )

    fun genreChips(): List<Chip> = genres.map { Chip(it.id, it.label) }

    fun toneChips(): List<Chip> = tones.map { Chip(it.id, it.label) }

    fun hookChips(): List<Chip> = hooks.map { Chip(it.id, it.label) }

    fun siteTypeChips(): List<Chip> = siteTypes.map { Chip(it.id, it.label) }

    fun villainTypeChips(): List<Chip> = villainTypes.map { Chip(it.id, it.label) }

    fun objectiveChips(): List<Chip> = objectives.map { Chip(it.id, it.label) }

    fun genre(id: String): Genre? = genres.firstOrNull { it.id == id }

    fun tone(id: String): Tone? = tones.firstOrNull { it.id == id }

    fun hook(id: String): Hook? = hooks.firstOrNull { it.id == id }

    fun siteType(id: String): SiteType? = siteTypes.firstOrNull { it.id == id }

    fun villainType(id: String): VillainType? = villainTypes.firstOrNull { it.id == id }

    fun objective(id: String): Objective? = objectives.firstOrNull { it.id == id }

    private val genres = listOf(
        Genre(
            id = "high_fantasy",
            label = "High fantasy",
            worldDescription = "A land of old oaths, bright banners, and magic that still answers when called.",
            realmName = "The Crownlands",
            regionName = "Silvervale",
            settlementName = "Brightwater",
            climate = "Temperate",
            terrain = "Rolling hills and river valleys",
            logline = "A border realm where a small company can still change the fate of a kingdom.",
        ),
        Genre(
            id = "grimdark",
            label = "Grimdark",
            worldDescription = "A worn world of ash, bargains, and victories that never come clean.",
            realmName = "The Shattered Marches",
            regionName = "Ashfen",
            settlementName = "Hollowford",
            climate = "Cold and wet",
            terrain = "Bogs, broken walls, and blackened fields",
            logline = "Hope is a rumor, and tonight it has a price.",
        ),
        Genre(
            id = "mystery",
            label = "Mystery",
            worldDescription = "Fog, closed doors, and a town that remembers more than it admits.",
            realmName = "The Fog Coast",
            regionName = "Mirewatch",
            settlementName = "Lamplight",
            climate = "Damp and cool",
            terrain = "Marshes, cliffs, and cobbled lanes",
            logline = "Someone is lying, and the truth will not stay buried until dawn.",
        ),
        Genre(
            id = "heist",
            label = "Heist",
            worldDescription = "Free cities, gilded vaults, and crews who work in the space between laws.",
            realmName = "The Free Cities",
            regionName = "Goldward",
            settlementName = "Port Fortune",
            climate = "Mild",
            terrain = "Crowded docks, rooftops, and counting houses",
            logline = "The prize is in sight. Getting out with it is the adventure.",
        ),
        Genre(
            id = "pirate",
            label = "Pirate",
            worldDescription = "A broken sea of rival flags, hidden coves, and storms that take names.",
            realmName = "The Shattered Sea",
            regionName = "Widow's Reach",
            settlementName = "Saltmere",
            climate = "Salt wind and sudden rain",
            terrain = "Coves, reefs, and cliffside jetties",
            logline = "A tide is turning, and whoever rides it will own the coast.",
        ),
        Genre(
            id = "gothic",
            label = "Gothic",
            worldDescription = "Pale manors, family curses, and nights that last longer than they should.",
            realmName = "The Pale Provinces",
            regionName = "Ravenshade",
            settlementName = "Duskwick",
            climate = "Perpetual autumn",
            terrain = "Moors, iron gates, and candlelit halls",
            logline = "An old house has woken, and it remembers every guest.",
        ),
    )

    private val tones = listOf(
        Tone(id = "heroic", label = "Heroic", flavor = "Play it bold: the table should feel like a legend starting."),
        Tone(id = "grim", label = "Grim", flavor = "Keep the victories costly and the choices sharp."),
        Tone(id = "whimsical", label = "Whimsical", flavor = "Let strange details charm before they bite."),
        Tone(id = "tense", label = "Tense", flavor = "Clock, pressure, and no safe pause."),
        Tone(id = "tragic", label = "Tragic", flavor = "Someone will lose something they cannot replace."),
        Tone(id = "pulpy", label = "Pulpy", flavor = "Fast scenes, big swings, and a grin between disasters."),
    )

    private val hooks = listOf(
        Hook(
            id = "monster_hunt",
            label = "Monster hunt",
            text = "Something is killing people on the road, and the town will pay anyone who can end it.",
            questTitle = "Hunt the beast",
            stakes = "If the hunters fail, the next victim is someone the town cannot spare.",
            objective = "Track and defeat the creature",
        ),
        Hook(
            id = "missing_person",
            label = "Missing person",
            text = "A local has vanished, and the clues point somewhere the watch will not go.",
            questTitle = "Find the missing",
            stakes = "Every hour lost makes a rescue less likely.",
            objective = "Discover what happened to the missing person",
        ),
        Hook(
            id = "heist",
            label = "Heist",
            text = "A prize sits behind locks, guards, and a timetable that will not wait.",
            questTitle = "Take the prize",
            stakes = "Miss the window and the prize leaves town forever.",
            objective = "Steal the target and get out",
        ),
        Hook(
            id = "ritual",
            label = "Ritual",
            text = "A rite is already in motion. Stop it, or live with what it summons.",
            questTitle = "Stop the ritual",
            stakes = "If the last verse is spoken, the site will not belong to the living.",
            objective = "Interrupt the ritual before it completes",
        ),
        Hook(
            id = "mystery",
            label = "Mystery",
            text = "A crime, a rumor, and three stories that cannot all be true.",
            questTitle = "Uncover the truth",
            stakes = "The wrong accusation will ruin an innocent and protect the guilty.",
            objective = "Identify who is responsible",
        ),
        Hook(
            id = "escort",
            label = "Escort",
            text = "Someone needs to reach a place they will not survive alone.",
            questTitle = "See them through",
            stakes = "If the charge falls, the journey's purpose dies with them.",
            objective = "Deliver the charge to safety",
        ),
    )

    private val siteTypes = listOf(
        SiteType(
            id = "tavern",
            label = "Tavern",
            openingName = "The Crooked Lantern",
            openingDescription = "A packed common room where news, jobs, and trouble arrive together.",
            middleName = "The Back Rooms",
            middleDescription = "Private booths, a locked cellar door, and a landlord who hears everything.",
            climaxName = "The Cellar Vault",
            climaxDescription = "Barrels, a hidden hatch, and barely enough room to swing a blade.",
        ),
        SiteType(
            id = "shrine",
            label = "Shrine",
            openingName = "Pilgrim's Yard",
            openingDescription = "Offerings, worn steps, and a keeper who knows which prayers are recent.",
            middleName = "The Inner Chapel",
            middleDescription = "Icons, incense, and a door the faithful are told not to open.",
            climaxName = "The Whispering Sanctum",
            climaxDescription = "A sealed chamber where the shrine's true work is done.",
        ),
        SiteType(
            id = "dungeon",
            label = "Dungeon",
            openingName = "The Broken Gate",
            openingDescription = "Collapsed masonry and a trail that someone else used first.",
            middleName = "The Guarded Halls",
            middleDescription = "Traps, side passages, and evidence of a camp that was abandoned in a hurry.",
            climaxName = "The Deep Chamber",
            climaxDescription = "The reason the place was sealed, still waiting in the dark.",
        ),
        SiteType(
            id = "manor",
            label = "Manor",
            openingName = "The Front Hall",
            openingDescription = "Portraits, a nervous steward, and a house that watches its guests.",
            middleName = "The Family Wing",
            middleDescription = "Locked rooms, old letters, and a floor that creaks on purpose.",
            climaxName = "The Master's Study",
            climaxDescription = "The secret the house was built to keep.",
        ),
        SiteType(
            id = "docks",
            label = "Docks",
            openingName = "Harbor Row",
            openingDescription = "Warehouses, night crews, and ships that do not list their cargo.",
            middleName = "The Tide Warehouse",
            middleDescription = "Stacked crates, a bribed clerk, and a door to the water.",
            climaxName = "Berth Nine",
            climaxDescription = "A ship ready to leave, if nobody stops it.",
        ),
        SiteType(
            id = "wilderness",
            label = "Wilderness",
            openingName = "The Trailhead",
            openingDescription = "A marker stone and a path the locals no longer use after dark.",
            middleName = "The Hidden Camp",
            middleDescription = "Ash, tracks, and a choice of two trails.",
            climaxName = "The Standing Stones",
            climaxDescription = "A clearing where the land itself feels like a participant.",
        ),
    )

    private val villainTypes = listOf(
        VillainType(
            id = "bandit_captain",
            label = "Bandit captain",
            name = "Rook Venn",
            description = "A road-captain who treats the region as a toll they invented.",
            kind = PersonKind.Npc,
            factionName = "The Tollmen",
            factionDescription = "A crew that taxes travelers and silences anyone who reports them.",
        ),
        VillainType(
            id = "cult_leader",
            label = "Cult leader",
            name = "Sister Vale",
            description = "A preacher with a kind voice and a rite that needs one more name.",
            kind = PersonKind.Npc,
            factionName = "The Pale Choir",
            factionDescription = "A circle that believes the world must be remade by sacrifice.",
        ),
        VillainType(
            id = "rival_noble",
            label = "Rival noble",
            name = "Lord Cindermere",
            description = "A landed rival who smiles in public and collects debts in private.",
            kind = PersonKind.Npc,
            factionName = "House Cindermere",
            factionDescription = "A house that buys loyalty and buries witnesses.",
        ),
        VillainType(
            id = "monster",
            label = "Monster",
            name = "The Hollow Stag",
            description = "A hunting horror that wears a crown of antlers and leftover vows.",
            kind = PersonKind.Monster,
            factionName = "",
            factionDescription = "",
        ),
        VillainType(
            id = "corrupt_official",
            label = "Corrupt official",
            name = "Magistrate Helms",
            description = "The person who should investigate this is the person who arranged it.",
            kind = PersonKind.Npc,
            factionName = "The Harbor Office",
            factionDescription = "A desk-bound machine that stamps whatever it is paid to stamp.",
        ),
    )

    private val objectives = listOf(
        Objective(id = "rescue", label = "Rescue the captive"),
        Objective(id = "relic", label = "Recover the relic"),
        Objective(id = "ritual", label = "Stop the ritual"),
        Objective(id = "truth", label = "Uncover the truth"),
        Objective(id = "survive", label = "Survive until dawn"),
        Objective(id = "escape", label = "Escape the site"),
    )
}
