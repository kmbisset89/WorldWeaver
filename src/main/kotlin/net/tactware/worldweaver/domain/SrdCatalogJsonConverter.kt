package net.tactware.worldweaver.domain

import kotlinx.serialization.json.Json
import java.time.Instant

internal class SrdCatalogJsonConverter {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = false
    }

    fun decode(jsonText: String): SrdCatalogPayload {
        return json.decodeFromString(SrdCatalogPayload.serializer(), jsonText)
    }

    fun encode(payload: SrdCatalogPayload): String {
        return json.encodeToString(SrdCatalogPayload.serializer(), payload)
    }

    fun toCatalog(payload: SrdCatalogPayload, importedAt: Instant): SrdCatalog {
        return SrdCatalog(
            formatVersion = payload.formatVersion,
            sourceLabel = payload.sourceLabel.trim(),
            importedAt = importedAt,
            races = uniqueNames(payload.races),
            classes = uniqueClasses(payload.classes),
            spells = uniqueSpells(payload.spells),
            monsters = uniqueMonsters(payload.monsters),
        )
    }

    fun toPayload(catalog: SrdCatalog): SrdCatalogPayload {
        return SrdCatalogPayload(
            formatVersion = catalog.formatVersion,
            sourceLabel = catalog.sourceLabel,
            importedAtEpochMillis = catalog.importedAt.toEpochMilli(),
            races = catalog.races,
            classes = catalog.classes.map { entry ->
                SrdClassPayload(name = entry.name, subclasses = entry.subclasses)
            },
            spells = catalog.spells.map { entry ->
                SrdSpellPayload(name = entry.name, level = entry.level)
            },
            monsters = catalog.monsters.map { entry ->
                SrdMonsterPayload(
                    name = entry.name,
                    creatureType = entry.creatureType,
                    challengeRating = entry.challengeRating,
                    hitPoints = entry.hitPoints,
                    armorClass = entry.armorClass,
                    walkSpeed = entry.walkSpeed,
                )
            },
        )
    }

    private fun uniqueNames(values: List<String>): List<String> {
        val seen = mutableSetOf<String>()
        return values.map { it.trim() }.filter { name ->
            name.isNotEmpty() && seen.add(name.lowercase())
        }
    }

    private fun uniqueClasses(values: List<SrdClassPayload>): List<SrdClassEntry> {
        val seen = mutableSetOf<String>()
        return values.mapNotNull { payload ->
            val name = payload.name.trim()
            if (name.isEmpty() || !seen.add(name.lowercase())) {
                null
            } else {
                SrdClassEntry(name = name, subclasses = uniqueNames(payload.subclasses))
            }
        }
    }

    private fun uniqueSpells(values: List<SrdSpellPayload>): List<SrdSpellEntry> {
        val seen = mutableSetOf<String>()
        return values.mapNotNull { payload ->
            val name = payload.name.trim()
            if (name.isEmpty() || !seen.add(name.lowercase())) {
                null
            } else {
                SrdSpellEntry(name = name, level = payload.level.coerceIn(0, 9))
            }
        }
    }

    private fun uniqueMonsters(values: List<SrdMonsterPayload>): List<SrdMonsterEntry> {
        val seen = mutableSetOf<String>()
        return values.mapNotNull { payload ->
            val name = payload.name.trim()
            if (name.isEmpty() || !seen.add(name.lowercase())) {
                null
            } else {
                SrdMonsterEntry(
                    name = name,
                    creatureType = payload.creatureType.trim(),
                    challengeRating = payload.challengeRating.trim(),
                    hitPoints = payload.hitPoints.coerceAtLeast(1),
                    armorClass = payload.armorClass.coerceAtLeast(1),
                    walkSpeed = payload.walkSpeed.coerceAtLeast(0),
                )
            }
        }
    }
}
