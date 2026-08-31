package net.tactware.worldweaver.data

import net.tactware.worldweaver.domain.PersonKind
import net.tactware.worldweaver.domain.WorldPerson
import java.time.Instant

internal class WorldPersonEntityConverter(
    private val sheetConverter: FifthEditionSheetConverter,
) {
    fun toPerson(entity: WorldPersonEntity): WorldPerson {
        return WorldPerson(
            id = entity.id,
            worldId = entity.worldId,
            kind = PersonKind.fromStorage(entity.kind),
            name = entity.name,
            description = entity.description,
            sheet = sheetConverter.decode(encodedSheet(entity)),
            createdAt = Instant.ofEpochMilli(entity.createdAtEpochMillis),
            updatedAt = Instant.ofEpochMilli(entity.updatedAtEpochMillis),
        )
    }

    fun toEntity(person: WorldPerson): WorldPersonEntity {
        val encoded = sheetConverter.encode(person.sheet)
        return WorldPersonEntity(
            id = person.id,
            worldId = person.worldId,
            kind = person.kind.name,
            name = person.name,
            description = person.description,
            race = encoded.race,
            classLevels = encoded.classLevels,
            abilities = encoded.abilities,
            hitPoints = encoded.hitPoints,
            maxHitPoints = encoded.maxHitPoints,
            temporaryHitPoints = encoded.temporaryHitPoints,
            armorClass = encoded.armorClass,
            walkSpeed = encoded.walkSpeed,
            deathSaves = encoded.deathSaves,
            items = encoded.items,
            features = encoded.features,
            spells = encoded.spells,
            notes = encoded.notes,
            createdAtEpochMillis = person.createdAt.toEpochMilli(),
            updatedAtEpochMillis = person.updatedAt.toEpochMilli(),
        )
    }

    private fun encodedSheet(entity: WorldPersonEntity): FifthEditionSheetConverter.EncodedSheet {
        return FifthEditionSheetConverter.EncodedSheet(
            race = entity.race,
            classLevels = entity.classLevels,
            abilities = entity.abilities,
            hitPoints = entity.hitPoints,
            maxHitPoints = entity.maxHitPoints,
            temporaryHitPoints = entity.temporaryHitPoints,
            armorClass = entity.armorClass,
            walkSpeed = entity.walkSpeed,
            deathSaves = entity.deathSaves,
            items = entity.items,
            features = entity.features,
            spells = entity.spells,
            notes = entity.notes,
        )
    }
}
