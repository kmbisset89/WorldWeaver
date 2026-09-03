package net.tactware.worldweaver.domain

internal class FifthEditionPickerCatalogResolver {
    fun resolve(imported: SrdCatalog?): FifthEditionPickerCatalog {
        val importedRaces = imported?.races.orEmpty()
        val importedClasses = imported?.classes.orEmpty()
        val importedSpells = imported?.spells.orEmpty()
        val subclassesByClass = mergedSubclasses(importedClasses)
        val spellLevels = importedSpells.associate { spell -> spell.name to spell.level }
        return FifthEditionPickerCatalog(
            races = mergeNames(importedRaces, FifthEditionReference.races),
            classes = mergeNames(
                importedClasses.map { it.name },
                FifthEditionReference.classes,
            ),
            spells = mergeNames(
                importedSpells.map { it.name },
                FifthEditionReference.spells,
            ),
            familiars = FifthEditionReference.familiars,
            animalCompanions = FifthEditionReference.animalCompanions,
            monsters = imported?.monsters.orEmpty(),
            spellLevelsByName = spellLevels,
            subclassesByClass = subclassesByClass,
        )
    }

    private fun mergedSubclasses(
        importedClasses: List<SrdClassEntry>,
    ): Map<String, List<String>> {
        val importedByName = importedClasses.associateBy { it.name.lowercase() }
        val names = mergeNames(
            importedClasses.map { it.name },
            FifthEditionReference.classes,
        )
        return names.associateWith { className ->
            val imported = importedByName[className.lowercase()]?.subclasses.orEmpty()
            val bundled = FifthEditionReference.subclassesFor(className)
            mergeNames(imported, bundled)
        }
    }

    private fun mergeNames(preferred: List<String>, fallback: List<String>): List<String> {
        val seen = mutableSetOf<String>()
        return (preferred + fallback).filter { name ->
            name.isNotBlank() && seen.add(name.lowercase())
        }
    }
}
