package io.github.kmbisset89.worldweaver.data

import io.github.kmbisset89.worldweaver.domain.AbilityScores
import io.github.kmbisset89.worldweaver.domain.DeathSaves
import io.github.kmbisset89.worldweaver.domain.FifthEditionSheet
import kotlin.test.Test
import kotlin.test.assertEquals

internal class FifthEditionSheetConverterTest {
    private val converter = FifthEditionSheetConverter()

    @Test
    fun encodeAndDecodePreserveWalkSpeed() {
        val sheet = FifthEditionSheet.empty().copy(
            armorClass = 16,
            walkSpeed = 40,
        )

        val decoded = converter.decode(converter.encode(sheet))

        assertEquals(40, decoded.walkSpeed)
        assertEquals(16, decoded.armorClass)
    }

    @Test
    fun missingWalkSpeedDecodesAsThirty() {
        val encoded = FifthEditionSheetConverter.EncodedSheet(
            race = "",
            classLevels = "",
            abilities = "10\u001f10\u001f10\u001f10\u001f10\u001f10",
            hitPoints = 10,
            maxHitPoints = 10,
            temporaryHitPoints = 0,
            armorClass = 12,
            deathSaves = "0\u001f0",
            items = "",
            features = "",
            spells = "",
            notes = "",
        )

        val sheet = converter.decode(encoded)

        assertEquals(30, sheet.walkSpeed)
        assertEquals(12, sheet.armorClass)
    }

    @Test
    fun nonPositiveWalkSpeedDecodesAsThirty() {
        val encoded = converter.encode(FifthEditionSheet.empty()).copy(walkSpeed = 0)

        assertEquals(30, converter.decode(encoded).walkSpeed)
    }

    @Test
    fun emptySheetUsesDefaultAbilityScores() {
        val decoded = converter.decode(converter.encode(FifthEditionSheet.empty()))

        assertEquals(AbilityScores.average(), decoded.abilityScores)
        assertEquals(DeathSaves.none(), decoded.deathSaves)
        assertEquals(30, decoded.walkSpeed)
    }
}
