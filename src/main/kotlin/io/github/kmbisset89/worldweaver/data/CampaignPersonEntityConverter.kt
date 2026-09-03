package io.github.kmbisset89.worldweaver.data

import io.github.kmbisset89.worldweaver.domain.CampaignPerson
import io.github.kmbisset89.worldweaver.domain.PersonKind
import java.time.Instant

internal class CampaignPersonEntityConverter(
    private val sheetConverter: PersonSheetEntityConverter,
) {
    fun toPerson(entity: CampaignPersonEntity): CampaignPerson {
        return CampaignPerson(
            id = entity.id,
            campaignId = entity.campaignId,
            worldPersonId = entity.worldPersonId,
            kind = PersonKind.fromStorage(entity.kind),
            name = entity.name,
            description = entity.description,
            sheet = sheetConverter.decode(
                sheetSystem = entity.sheetSystem,
                encodedFifthEdition = encodedFifthEdition(entity),
                pf2ePayload = entity.pf2ePayload,
            ),
            overlayHitPoints = entity.overlayHitPoints,
            overlayNotes = entity.overlayNotes,
            createdAt = Instant.ofEpochMilli(entity.createdAtEpochMillis),
            updatedAt = Instant.ofEpochMilli(entity.updatedAtEpochMillis),
        )
    }

    fun toEntity(person: CampaignPerson): CampaignPersonEntity {
        val encoded = sheetConverter.encode(person.sheet)
        val fifth = encoded.fifthEdition
        return CampaignPersonEntity(
            id = person.id,
            campaignId = person.campaignId,
            worldPersonId = person.worldPersonId,
            kind = person.kind.name,
            name = person.name,
            description = person.description,
            race = fifth.race,
            classLevels = fifth.classLevels,
            abilities = fifth.abilities,
            hitPoints = fifth.hitPoints,
            maxHitPoints = fifth.maxHitPoints,
            temporaryHitPoints = fifth.temporaryHitPoints,
            armorClass = fifth.armorClass,
            walkSpeed = fifth.walkSpeed,
            deathSaves = fifth.deathSaves,
            items = fifth.items,
            features = fifth.features,
            spells = fifth.spells,
            notes = fifth.notes,
            overlayHitPoints = person.overlayHitPoints,
            overlayNotes = person.overlayNotes,
            skills = fifth.skills,
            spellSlots = fifth.spellSlots,
            concentratingSpell = fifth.concentratingSpell,
            creatureSize = fifth.creatureSize,
            sheetSystem = encoded.sheetSystem,
            pf2ePayload = encoded.pf2ePayload,
            createdAtEpochMillis = person.createdAt.toEpochMilli(),
            updatedAtEpochMillis = person.updatedAt.toEpochMilli(),
        )
    }

    private fun encodedFifthEdition(
        entity: CampaignPersonEntity,
    ): FifthEditionSheetConverter.EncodedSheet {
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
            skills = entity.skills,
            spellSlots = entity.spellSlots,
            concentratingSpell = entity.concentratingSpell,
            creatureSize = entity.creatureSize,
        )
    }
}
