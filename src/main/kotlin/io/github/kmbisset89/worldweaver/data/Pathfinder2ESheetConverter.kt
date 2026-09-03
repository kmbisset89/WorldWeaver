package io.github.kmbisset89.worldweaver.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import io.github.kmbisset89.worldweaver.domain.AbilityScores
import io.github.kmbisset89.worldweaver.domain.CreatureSize
import io.github.kmbisset89.worldweaver.domain.Pathfinder2EFeat
import io.github.kmbisset89.worldweaver.domain.Pathfinder2ESheet
import io.github.kmbisset89.worldweaver.domain.Pathfinder2ESkill
import io.github.kmbisset89.worldweaver.domain.Pathfinder2ESkillRank
import io.github.kmbisset89.worldweaver.domain.Pathfinder2ESpell

internal class Pathfinder2ESheetConverter {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(sheet: Pathfinder2ESheet): String {
        return json.encodeToString(Pathfinder2ESheetPayload.serializer(), toPayload(sheet))
    }

    fun decode(payload: String): Pathfinder2ESheet {
        if (payload.isBlank()) {
            return Pathfinder2ESheet.empty()
        }
        return fromPayload(json.decodeFromString(Pathfinder2ESheetPayload.serializer(), payload))
    }

    private fun toPayload(sheet: Pathfinder2ESheet): Pathfinder2ESheetPayload {
        return Pathfinder2ESheetPayload(
            ancestry = sheet.ancestry,
            heritage = sheet.heritage,
            background = sheet.background,
            className = sheet.className,
            subclass = sheet.subclass,
            level = sheet.level,
            strength = sheet.abilityScores.strength,
            dexterity = sheet.abilityScores.dexterity,
            constitution = sheet.abilityScores.constitution,
            intelligence = sheet.abilityScores.intelligence,
            wisdom = sheet.abilityScores.wisdom,
            charisma = sheet.abilityScores.charisma,
            hitPoints = sheet.hitPoints,
            maxHitPoints = sheet.maxHitPoints,
            temporaryHitPoints = sheet.temporaryHitPoints,
            armorClass = sheet.armorClass,
            perception = sheet.perception,
            landSpeed = sheet.landSpeed,
            skills = sheet.skills.map { skill ->
                Pathfinder2ESkillPayload(name = skill.name, rank = skill.rank.name)
            },
            feats = sheet.feats.map { feat ->
                Pathfinder2EFeatPayload(
                    name = feat.name,
                    type = feat.type,
                    description = feat.description,
                )
            },
            spells = sheet.spells.map { spell ->
                Pathfinder2ESpellPayload(
                    name = spell.name,
                    rank = spell.rank,
                    prepared = spell.prepared,
                )
            },
            notes = sheet.notes,
            dying = sheet.dying,
            wounded = sheet.wounded,
            creatureSize = sheet.creatureSize.name,
        )
    }

    private fun fromPayload(payload: Pathfinder2ESheetPayload): Pathfinder2ESheet {
        return Pathfinder2ESheet(
            ancestry = payload.ancestry,
            heritage = payload.heritage,
            background = payload.background,
            className = payload.className,
            subclass = payload.subclass,
            level = payload.level.coerceAtLeast(1),
            abilityScores = AbilityScores(
                strength = payload.strength,
                dexterity = payload.dexterity,
                constitution = payload.constitution,
                intelligence = payload.intelligence,
                wisdom = payload.wisdom,
                charisma = payload.charisma,
            ),
            hitPoints = payload.hitPoints,
            maxHitPoints = payload.maxHitPoints,
            temporaryHitPoints = payload.temporaryHitPoints,
            armorClass = payload.armorClass,
            perception = payload.perception,
            landSpeed = payload.landSpeed,
            skills = payload.skills.map { skill ->
                Pathfinder2ESkill(
                    name = skill.name,
                    rank = Pathfinder2ESkillRank.fromStorage(skill.rank),
                )
            },
            feats = payload.feats.map { feat ->
                Pathfinder2EFeat(
                    name = feat.name,
                    type = feat.type,
                    description = feat.description,
                )
            },
            spells = payload.spells.map { spell ->
                Pathfinder2ESpell(
                    name = spell.name,
                    rank = spell.rank.coerceIn(0, 10),
                    prepared = spell.prepared,
                )
            },
            notes = payload.notes,
            dying = payload.dying.coerceAtLeast(0),
            wounded = payload.wounded.coerceAtLeast(0),
            creatureSize = CreatureSize.fromStorage(payload.creatureSize),
        )
    }
}

@Serializable
private data class Pathfinder2ESheetPayload(
    val ancestry: String,
    val heritage: String,
    val background: String,
    val className: String,
    val subclass: String,
    val level: Int,
    val strength: Int,
    val dexterity: Int,
    val constitution: Int,
    val intelligence: Int,
    val wisdom: Int,
    val charisma: Int,
    val hitPoints: Int,
    val maxHitPoints: Int,
    val temporaryHitPoints: Int,
    val armorClass: Int,
    val perception: Int,
    val landSpeed: Int,
    val skills: List<Pathfinder2ESkillPayload> = emptyList(),
    val feats: List<Pathfinder2EFeatPayload> = emptyList(),
    val spells: List<Pathfinder2ESpellPayload> = emptyList(),
    val notes: String,
    val dying: Int = 0,
    val wounded: Int = 0,
    val creatureSize: String = "Medium",
)

@Serializable
private data class Pathfinder2ESkillPayload(
    val name: String,
    val rank: String,
)

@Serializable
private data class Pathfinder2EFeatPayload(
    val name: String,
    val type: String,
    val description: String,
)

@Serializable
private data class Pathfinder2ESpellPayload(
    val name: String,
    val rank: Int,
    val prepared: Boolean,
)
