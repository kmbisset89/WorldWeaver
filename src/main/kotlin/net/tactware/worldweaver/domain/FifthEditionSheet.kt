package net.tactware.worldweaver.domain

internal data class FifthEditionSheet(
    val race: String,
    val classLevels: List<ClassLevel>,
    val abilityScores: AbilityScores,
    val hitPoints: Int,
    val maxHitPoints: Int,
    val temporaryHitPoints: Int,
    val armorClass: Int,
    val walkSpeed: Int = 30,
    val deathSaves: DeathSaves,
    val items: List<InventoryItem>,
    val features: List<PersonFeature>,
    val spells: List<PersonSpell>,
    val notes: String,
) {
    fun totalLevel(): Int {
        val sum = classLevels.sumOf { it.level }
        return if (sum > 0) sum else 1
    }

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
            )
        }
    }
}
