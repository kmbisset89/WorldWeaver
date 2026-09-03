package io.github.kmbisset89.worldweaver.domain

internal data class Pathfinder2ESheet(
    val ancestry: String,
    val heritage: String,
    val background: String,
    val className: String,
    val subclass: String,
    val level: Int,
    override val abilityScores: AbilityScores,
    override val hitPoints: Int,
    override val maxHitPoints: Int,
    override val temporaryHitPoints: Int,
    override val armorClass: Int,
    val perception: Int,
    val landSpeed: Int,
    val skills: List<Pathfinder2ESkill>,
    val feats: List<Pathfinder2EFeat>,
    val spells: List<Pathfinder2ESpell>,
    val notes: String,
    val dying: Int,
    val wounded: Int,
    val creatureSize: CreatureSize = CreatureSize.Medium,
) : PersonSheet {
    override fun gameSystem(): GameSystem = GameSystem.Pathfinder2E

    override fun movementSpeed(): Int = landSpeed

    override fun totalLevel(): Int = level.coerceAtLeast(1)

    override fun lineageLabel(): String = ancestry

    override fun creatureSize(): CreatureSize = creatureSize

    companion object {
        fun empty(): Pathfinder2ESheet {
            return Pathfinder2ESheet(
                ancestry = "",
                heritage = "",
                background = "",
                className = "",
                subclass = "",
                level = 1,
                abilityScores = AbilityScores.average(),
                hitPoints = 16,
                maxHitPoints = 16,
                temporaryHitPoints = 0,
                armorClass = 15,
                perception = 3,
                landSpeed = 25,
                skills = emptyList(),
                feats = emptyList(),
                spells = emptyList(),
                notes = "",
                dying = 0,
                wounded = 0,
            )
        }
    }
}
