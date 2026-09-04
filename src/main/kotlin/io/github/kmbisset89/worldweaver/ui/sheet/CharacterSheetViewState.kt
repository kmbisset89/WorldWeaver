package io.github.kmbisset89.worldweaver.ui.sheet

import io.github.kmbisset89.worldweaver.domain.DeathSaves
import io.github.kmbisset89.worldweaver.domain.PersonKind
import io.github.kmbisset89.worldweaver.ui.characters.PersonMembership

internal sealed class CharacterSheetViewState {
    data object Hidden : CharacterSheetViewState()

    data object Loading : CharacterSheetViewState()

    data class Unavailable(
        val message: String,
    ) : CharacterSheetViewState()

    data class Error(
        val message: String,
        val canRetry: Boolean,
    ) : CharacterSheetViewState()

    data class Content(
        val key: PersonKey,
        val name: String,
        val kind: PersonKind,
        val identityLine: String,
        val experiencePoints: Int?,
        val systemBadge: String,
        val avatarPath: String?,
        val abilityScores: List<AbilityScoreTile>,
        val proficiencyBonus: Int?,
        val initiativeBonus: Int,
        val vitals: Vitals,
        val body: SheetBody,
    ) : CharacterSheetViewState()

    data class PersonKey(
        val membership: PersonMembership,
        val id: String,
    )

    data class AbilityScoreTile(
        val label: String,
        val score: Int,
        val modifier: Int,
    )

    data class Vitals(
        val hitPoints: Int,
        val maxHitPoints: Int,
        val temporaryHitPoints: Int,
        val armorClass: Int,
        val speed: Int,
        val usesOverlayHitPoints: Boolean,
        val fifthEdition: FifthEditionVitals?,
        val pathfinder: PathfinderVitals?,
    )

    data class FifthEditionVitals(
        val deathSaves: DeathSaves,
        val writable: Boolean,
    )

    data class PathfinderVitals(
        val perception: Int,
        val dying: Int,
        val wounded: Int,
        val writable: Boolean,
    )

    sealed class SheetBody {
        data class FifthEdition(
            val skills: List<SkillRow>,
            val skillsCaption: String,
            val concentratingSpell: String,
            val spellSlots: List<SpellSlotRow>,
            val features: List<NamedText>,
            val spells: List<SpellGroup>,
            val items: List<ItemRow>,
            val notes: String,
        ) : SheetBody()

        data class Pathfinder(
            val skills: List<PathfinderSkillRow>,
            val feats: List<NamedText>,
            val spells: List<SpellGroup>,
            val notes: String,
        ) : SheetBody()
    }

    data class SkillRow(
        val name: String,
        val ability: String,
        val modifier: Int,
        val proficient: Boolean,
    )

    data class SpellSlotRow(
        val level: Int,
        val remaining: Int,
        val maximum: Int,
    )

    data class PathfinderSkillRow(
        val name: String,
        val rank: String,
    )

    data class SpellGroup(
        val heading: String,
        val spells: List<SpellRow>,
    )

    data class SpellRow(
        val name: String,
        val prepared: Boolean,
    )

    data class ItemRow(
        val name: String,
        val quantity: Int,
        val notes: String,
    )

    data class NamedText(
        val name: String,
        val description: String,
    )
}
