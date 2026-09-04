package io.github.kmbisset89.worldweaver.data

import io.github.kmbisset89.worldweaver.domain.AbilityScores
import io.github.kmbisset89.worldweaver.domain.Pathfinder2EFeat
import io.github.kmbisset89.worldweaver.domain.Pathfinder2ESheet
import io.github.kmbisset89.worldweaver.domain.Pathfinder2ESkill
import io.github.kmbisset89.worldweaver.domain.Pathfinder2ESkillRank
import io.github.kmbisset89.worldweaver.domain.Pathfinder2ESpell
import kotlin.test.Test
import kotlin.test.assertEquals

internal class Pathfinder2ESheetConverterTest {
    private val converter = Pathfinder2ESheetConverter()

    @Test
    fun encodeAndDecodeRoundTrip() {
        val sheet = Pathfinder2ESheet.empty().copy(
            ancestry = "Elf",
            heritage = "Seer",
            background = "Scholar",
            className = "Wizard",
            subclass = "Universalist",
            level = 3,
            abilityScores = AbilityScores(10, 14, 12, 18, 12, 10),
            hitPoints = 28,
            maxHitPoints = 28,
            armorClass = 16,
            perception = 7,
            landSpeed = 30,
            skills = listOf(
                Pathfinder2ESkill("Arcana", Pathfinder2ESkillRank.Expert),
                Pathfinder2ESkill("Society", Pathfinder2ESkillRank.Trained),
            ),
            feats = listOf(
                Pathfinder2EFeat("Recognize Spell", "Skill", "Identify a spell as it is cast."),
            ),
            spells = listOf(
                Pathfinder2ESpell("Magic Missile", rank = 1, prepared = true),
            ),
            notes = "Prepared caster",
            dying = 1,
            wounded = 2,
            currentXp = 1200,
        )

        val decoded = converter.decode(converter.encode(sheet))

        assertEquals(sheet, decoded)
    }

    @Test
    fun blankPayloadDecodesAsEmptySheet() {
        val decoded = converter.decode("")

        assertEquals(Pathfinder2ESheet.empty(), decoded)
    }
}
