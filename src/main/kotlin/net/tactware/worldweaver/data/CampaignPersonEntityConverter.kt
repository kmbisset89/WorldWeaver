package net.tactware.worldweaver.data

import net.tactware.worldweaver.domain.CampaignPerson
import net.tactware.worldweaver.domain.PersonKind
import java.time.Instant

internal class CampaignPersonEntityConverter(
    private val sheetConverter: FifthEditionSheetConverter,
) {
    fun toPerson(entity: CampaignPersonEntity): CampaignPerson {
        return CampaignPerson(
            id = entity.id,
            campaignId = entity.campaignId,
            worldPersonId = entity.worldPersonId,
            kind = PersonKind.fromStorage(entity.kind),
            name = entity.name,
            description = entity.description,
            sheet = sheetConverter.decode(encodedSheet(entity)),
            overlayHitPoints = entity.overlayHitPoints,
            overlayNotes = entity.overlayNotes,
            createdAt = Instant.ofEpochMilli(entity.createdAtEpochMillis),
            updatedAt = Instant.ofEpochMilli(entity.updatedAtEpochMillis),
        )
    }

    fun toEntity(person: CampaignPerson): CampaignPersonEntity {
        val encoded = sheetConverter.encode(person.sheet)
        return CampaignPersonEntity(
            id = person.id,
            campaignId = person.campaignId,
            worldPersonId = person.worldPersonId,
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
            overlayHitPoints = person.overlayHitPoints,
            overlayNotes = person.overlayNotes,
            createdAtEpochMillis = person.createdAt.toEpochMilli(),
            updatedAtEpochMillis = person.updatedAt.toEpochMilli(),
        )
    }

    private fun encodedSheet(entity: CampaignPersonEntity): FifthEditionSheetConverter.EncodedSheet {
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
