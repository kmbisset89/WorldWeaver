package io.github.kmbisset89.worldweaver.domain

internal data class FifthEditionPickerCatalog(
    val races: List<String>,
    val classes: List<String>,
    val spells: List<String>,
    val familiars: List<String>,
    val animalCompanions: List<String>,
    val monsters: List<SrdMonsterEntry>,
    val spellLevelsByName: Map<String, Int>,
    private val subclassesByClass: Map<String, List<String>>,
) {
    fun subclassesFor(className: String): List<String> {
        return subclassesByClass[className].orEmpty()
    }

    fun spellLevelFor(name: String): Int? {
        val exact = spellLevelsByName[name]
        if (exact != null) {
            return exact
        }
        return spellLevelsByName.entries.firstOrNull { entry ->
            entry.key.equals(name, ignoreCase = true)
        }?.value
    }

    fun monsterNamed(name: String): SrdMonsterEntry? {
        return monsters.firstOrNull { monster ->
            monster.name.equals(name, ignoreCase = true)
        }
    }
}
