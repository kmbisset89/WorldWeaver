package net.tactware.worldweaver.domain

internal sealed interface PersonSheet {
    val hitPoints: Int
    val maxHitPoints: Int
    val temporaryHitPoints: Int
    val armorClass: Int
    val abilityScores: AbilityScores

    fun gameSystem(): GameSystem

    fun movementSpeed(): Int

    fun totalLevel(): Int

    fun lineageLabel(): String

    fun creatureSize(): CreatureSize
}
