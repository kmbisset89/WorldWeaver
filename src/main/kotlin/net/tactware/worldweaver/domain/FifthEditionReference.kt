package net.tactware.worldweaver.domain

internal object FifthEditionReference {
    val races: List<String> = listOf(
        "Dwarf (Hill)",
        "Dwarf (Mountain)",
        "Elf (High)",
        "Elf (Wood)",
        "Elf (Drow)",
        "Halfling (Lightfoot)",
        "Halfling (Stout)",
        "Human",
        "Dragonborn",
        "Gnome (Forest)",
        "Gnome (Rock)",
        "Half-Elf",
        "Half-Orc",
        "Tiefling",
    )

    val classes: List<String> = listOf(
        "Barbarian",
        "Bard",
        "Cleric",
        "Druid",
        "Fighter",
        "Monk",
        "Paladin",
        "Ranger",
        "Rogue",
        "Sorcerer",
        "Warlock",
        "Wizard",
    )

    val generatorRaces: List<String> = listOf(
        "Human",
        "Elf (High)",
        "Dwarf (Hill)",
        "Halfling (Lightfoot)",
        "Gnome (Rock)",
        "Half-Orc",
        "Tiefling",
    )

    val npcNames: List<String> = listOf(
        "Aelar", "Bram", "Cora", "Dain", "Elara", "Fen", "Greta", "Hale",
        "Ilya", "Joss", "Kael", "Lira", "Mira", "Nash", "Orin", "Pia",
        "Quinn", "Rook", "Sable", "Tamsin", "Ulric", "Vera", "Wren", "Yara",
        "Aldric", "Briar", "Cedric", "Dara", "Ewan", "Faye", "Garrick", "Hester",
    )

    val spells: List<String> = listOf(
        "Acid Splash",
        "Fire Bolt",
        "Light",
        "Mage Hand",
        "Prestidigitation",
        "Ray of Frost",
        "Sacred Flame",
        "Spare the Dying",
        "Vicious Mockery",
        "Bless",
        "Cure Wounds",
        "Detect Magic",
        "Guiding Bolt",
        "Healing Word",
        "Magic Missile",
        "Shield",
        "Sleep",
        "Thunderwave",
        "Hold Person",
        "Invisibility",
        "Misty Step",
        "Scorching Ray",
        "Spiritual Weapon",
        "Counterspell",
        "Fireball",
        "Fly",
        "Revivify",
        "Dimension Door",
        "Greater Invisibility",
        "Hold Monster",
        "Cone of Cold",
        "Greater Restoration",
        "Heal",
        "Disintegrate",
        "Finger of Death",
        "Teleport",
        "Power Word Kill",
        "Wish",
    )

    val familiars: List<String> = listOf(
        "Bat",
        "Cat",
        "Crab",
        "Frog",
        "Hawk",
        "Lizard",
        "Octopus",
        "Owl",
        "Poisonous Snake",
        "Fish",
        "Rat",
        "Raven",
        "Sea Horse",
        "Spider",
        "Weasel",
    )

    val animalCompanions: List<String> = listOf(
        "Wolf",
        "Hawk",
        "Panther",
        "Brown Bear",
        "Boar",
        "Giant Frog",
        "Giant Badger",
        "Giant Wolf Spider",
        "Giant Poisonous Snake",
        "Giant Eagle",
        "Dire Wolf",
        "Black Bear",
    )

    fun subclassesFor(className: String): List<String> {
        return subclassByClass[className].orEmpty()
    }

    private val subclassByClass: Map<String, List<String>> = mapOf(
        "Barbarian" to listOf(
            "Path of the Berserker",
            "Path of the Totem Warrior",
            "Path of the Zealot",
        ),
        "Bard" to listOf(
            "College of Lore",
            "College of Valor",
            "College of Swords",
        ),
        "Cleric" to listOf(
            "Life Domain",
            "Light Domain",
            "Trickery Domain",
            "War Domain",
        ),
        "Druid" to listOf(
            "Circle of the Land",
            "Circle of the Moon",
        ),
        "Fighter" to listOf(
            "Champion",
            "Battle Master",
            "Eldritch Knight",
        ),
        "Monk" to listOf(
            "Way of the Open Hand",
            "Way of Shadow",
            "Way of the Four Elements",
        ),
        "Paladin" to listOf(
            "Oath of Devotion",
            "Oath of the Ancients",
            "Oath of Vengeance",
        ),
        "Ranger" to listOf(
            "Hunter",
            "Beast Master",
            "Gloom Stalker",
        ),
        "Rogue" to listOf(
            "Thief",
            "Assassin",
            "Arcane Trickster",
        ),
        "Sorcerer" to listOf(
            "Draconic Bloodline",
            "Wild Magic",
            "Divine Soul",
        ),
        "Warlock" to listOf(
            "The Fiend",
            "The Archfey",
            "The Great Old One",
        ),
        "Wizard" to listOf(
            "School of Evocation",
            "School of Abjuration",
            "School of Divination",
            "School of Illusion",
        ),
    )
}
