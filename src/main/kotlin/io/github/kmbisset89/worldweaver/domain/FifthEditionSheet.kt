package io.github.kmbisset89.worldweaver.domain

internal data class FifthEditionSheet(
    val race: String,
    val classLevels: List<ClassLevel>,
    override val abilityScores: AbilityScores,
    override val hitPoints: Int,
    override val maxHitPoints: Int,
    override val temporaryHitPoints: Int,
    override val armorClass: Int,
    val walkSpeed: Int = 30,
    val deathSaves: DeathSaves,
    val items: List<InventoryItem>,
    val features: List<PersonFeature>,
    val spells: List<PersonSpell>,
    val notes: String,
    val skills: List<FifthEditionSkill> = emptyList(),
    val spellSlots: List<FifthEditionSpellSlot> = emptyList(),
    val concentratingSpell: String = "",
    val creatureSize: CreatureSize = CreatureSize.Medium,
    override val currentXp: Int = 0,
) : PersonSheet {
    override fun gameSystem(): GameSystem = GameSystem.FifthEdition

    override fun movementSpeed(): Int = walkSpeed

    override fun totalLevel(): Int {
        val sum = classLevels.sumOf { it.level }
        return if (sum > 0) sum else 1
    }

    override fun lineageLabel(): String = race

    override fun creatureSize(): CreatureSize = creatureSize

    companion object {
        fun empty(): FifthEditionSheet {
            return FifthEditionSheet(
                race = "",
                classLevels = emptyList(),
                abilityScores = AbilityScores.average(),
                hitPoints = 10,
                maxHitPoints = 10,
                temporaryHitPoints = 0,
                armorClass = 10,
                walkSpeed = 30,
                deathSaves = DeathSaves.none(),
                items = emptyList(),
                features = emptyList(),
                spells = emptyList(),
                notes = "",
                skills = emptyList(),
                spellSlots = emptyList(),
                concentratingSpell = "",
                creatureSize = CreatureSize.Medium,
            )
        }
    }
}
