package net.tactware.worldweaver.domain

internal object Pathfinder2EReference {
    val ancestries: List<String> = listOf(
        "Dwarf",
        "Elf",
        "Gnome",
        "Goblin",
        "Halfling",
        "Human",
        "Leshy",
        "Orc",
    )

    val backgrounds: List<String> = listOf(
        "Acolyte",
        "Acrobat",
        "Farmhand",
        "Guard",
        "Hunter",
        "Merchant",
        "Nomad",
        "Scholar",
        "Scout",
        "Warrior",
    )

    val classes: List<String> = listOf(
        "Alchemist",
        "Barbarian",
        "Bard",
        "Champion",
        "Cleric",
        "Druid",
        "Fighter",
        "Monk",
        "Ranger",
        "Rogue",
        "Sorcerer",
        "Wizard",
    )

    val skills: List<String> = listOf(
        "Acrobatics",
        "Arcana",
        "Athletics",
        "Crafting",
        "Deception",
        "Diplomacy",
        "Intimidation",
        "Lore",
        "Medicine",
        "Nature",
        "Occultism",
        "Performance",
        "Religion",
        "Society",
        "Stealth",
        "Survival",
        "Thievery",
    )

    val feats: List<String> = listOf(
        "Toughness",
        "Diehard",
        "Incredible Initiative",
        "Battle Medicine",
        "Assurance",
        "Power Attack",
        "Sudden Charge",
        "Shield Block",
        "Combat Climber",
        "Quick Repair",
    )

    val spells: List<String> = listOf(
        "Shield",
        "Heal",
        "Magic Missile",
        "Fear",
        "Grease",
        "Hydraulic Push",
        "Heal Animal",
        "Burning Hands",
        "Invisibility",
        "Fireball",
        "Haste",
        "Fly",
    )

    fun heritagesFor(ancestry: String): List<String> {
        return heritageByAncestry[ancestry].orEmpty()
    }

    fun subclassesFor(className: String): List<String> {
        return subclassByClass[className].orEmpty()
    }

    private val heritageByAncestry: Map<String, List<String>> = mapOf(
        "Dwarf" to listOf("Ancient-Blooded", "Forge", "Rock", "Strong-Blooded"),
        "Elf" to listOf("Ancient", "Arctic", "Cavern", "Seer", "Whisper"),
        "Gnome" to listOf("Chameleon", "Fey-Touched", "Sensate", "Umbral"),
        "Goblin" to listOf("Charhide", "Irongut", "Razortooth", "Snow"),
        "Halfling" to listOf("Gutsy", "Hillock", "Nomadic", "Twilight"),
        "Human" to listOf("Skilled", "Versatile"),
        "Leshy" to listOf("Fungus", "Gourd", "Leaf", "Vine"),
        "Orc" to listOf("Badlands", "Deep", "Hold-Scarred", "Rainfall"),
    )

    private val subclassByClass: Map<String, List<String>> = mapOf(
        "Alchemist" to listOf("Bomber", "Chirurgeon", "Mutagenist"),
        "Barbarian" to listOf("Animal", "Dragon", "Fury", "Giant"),
        "Bard" to listOf("Enigma", "Maestro", "Polymath"),
        "Champion" to listOf("Paladin", "Redeemer", "Liberator"),
        "Cleric" to listOf("Cloistered", "Warpriest"),
        "Druid" to listOf("Animal", "Leaf", "Storm", "Wild"),
        "Fighter" to emptyList(),
        "Monk" to emptyList(),
        "Ranger" to listOf("Flurry", "Outwit", "Precision"),
        "Rogue" to listOf("Ruffian", "Scoundrel", "Thief"),
        "Sorcerer" to listOf("Draconic", "Elemental", "Imperial"),
        "Wizard" to listOf("Universalist", "School of Evocation", "School of Illusion"),
    )
}
