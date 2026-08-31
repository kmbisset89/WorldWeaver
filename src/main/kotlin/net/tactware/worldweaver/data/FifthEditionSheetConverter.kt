package net.tactware.worldweaver.data

import net.tactware.worldweaver.domain.AbilityScores
import net.tactware.worldweaver.domain.ClassLevel
import net.tactware.worldweaver.domain.DeathSaves
import net.tactware.worldweaver.domain.FifthEditionSheet
import net.tactware.worldweaver.domain.InventoryItem
import net.tactware.worldweaver.domain.PersonFeature
import net.tactware.worldweaver.domain.PersonSpell

internal class FifthEditionSheetConverter {
    fun encode(sheet: FifthEditionSheet): EncodedSheet {
        return EncodedSheet(
            race = sheet.race,
            classLevels = sheet.classLevels.joinToString(RECORD) { level ->
                listOf(level.className, level.subclass, level.level.toString()).joinToString(FIELD)
            },
            abilities = listOf(
                sheet.abilityScores.strength,
                sheet.abilityScores.dexterity,
                sheet.abilityScores.constitution,
                sheet.abilityScores.intelligence,
                sheet.abilityScores.wisdom,
                sheet.abilityScores.charisma,
            ).joinToString(FIELD),
            hitPoints = sheet.hitPoints,
            maxHitPoints = sheet.maxHitPoints,
            temporaryHitPoints = sheet.temporaryHitPoints,
            armorClass = sheet.armorClass,
            walkSpeed = sheet.walkSpeed,
            deathSaves = "${sheet.deathSaves.successes}$FIELD${sheet.deathSaves.failures}",
            items = sheet.items.joinToString(RECORD) { item ->
                listOf(item.name, item.quantity.toString(), item.notes).joinToString(FIELD)
            },
            features = sheet.features.joinToString(RECORD) { feature ->
                listOf(feature.name, feature.description).joinToString(FIELD)
            },
            spells = sheet.spells.joinToString(RECORD) { spell ->
                listOf(spell.name, spell.level.toString(), spell.prepared.toString()).joinToString(FIELD)
            },
            notes = sheet.notes,
        )
    }

    fun decode(encoded: EncodedSheet): FifthEditionSheet {
        val abilityParts = encoded.abilities.split(FIELD)
        val deathParts = encoded.deathSaves.split(FIELD)
        return FifthEditionSheet(
            race = encoded.race,
            classLevels = decodeRecords(encoded.classLevels) { parts ->
                ClassLevel(
                    className = parts.getOrElse(0) { "" },
                    subclass = parts.getOrElse(1) { "" },
                    level = parts.getOrElse(2) { "1" }.toIntOrNull() ?: 1,
                )
            },
            abilityScores = AbilityScores(
                strength = abilityParts.getOrElse(0) { "10" }.toIntOrNull() ?: 10,
                dexterity = abilityParts.getOrElse(1) { "10" }.toIntOrNull() ?: 10,
                constitution = abilityParts.getOrElse(2) { "10" }.toIntOrNull() ?: 10,
                intelligence = abilityParts.getOrElse(3) { "10" }.toIntOrNull() ?: 10,
                wisdom = abilityParts.getOrElse(4) { "10" }.toIntOrNull() ?: 10,
                charisma = abilityParts.getOrElse(5) { "10" }.toIntOrNull() ?: 10,
            ),
            hitPoints = encoded.hitPoints,
            maxHitPoints = encoded.maxHitPoints,
            temporaryHitPoints = encoded.temporaryHitPoints,
            armorClass = encoded.armorClass,
            walkSpeed = encoded.walkSpeed.takeIf { it > 0 } ?: 30,
            deathSaves = DeathSaves(
                successes = deathParts.getOrElse(0) { "0" }.toIntOrNull() ?: 0,
                failures = deathParts.getOrElse(1) { "0" }.toIntOrNull() ?: 0,
            ),
            items = decodeRecords(encoded.items) { parts ->
                InventoryItem(
                    name = parts.getOrElse(0) { "" },
                    quantity = parts.getOrElse(1) { "1" }.toIntOrNull() ?: 1,
                    notes = parts.getOrElse(2) { "" },
                )
            },
            features = decodeRecords(encoded.features) { parts ->
                PersonFeature(
                    name = parts.getOrElse(0) { "" },
                    description = parts.getOrElse(1) { "" },
                )
            },
            spells = decodeRecords(encoded.spells) { parts ->
                PersonSpell(
                    name = parts.getOrElse(0) { "" },
                    level = parts.getOrElse(1) { "0" }.toIntOrNull() ?: 0,
                    prepared = parts.getOrElse(2) { "false" }.toBooleanStrictOrNull() ?: false,
                )
            },
            notes = encoded.notes,
        )
    }

    private fun <T> decodeRecords(value: String, transform: (List<String>) -> T): List<T> {
        if (value.isEmpty()) {
            return emptyList()
        }
        return value.split(RECORD).filter { it.isNotEmpty() }.map { record ->
            transform(record.split(FIELD))
        }
    }

    data class EncodedSheet(
        val race: String,
        val classLevels: String,
        val abilities: String,
        val hitPoints: Int,
        val maxHitPoints: Int,
        val temporaryHitPoints: Int,
        val armorClass: Int,
        val walkSpeed: Int = 30,
        val deathSaves: String,
        val items: String,
        val features: String,
        val spells: String,
        val notes: String,
    )

    private companion object {
        const val FIELD = "\u001f"
        const val RECORD = "\n"
    }
}
