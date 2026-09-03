package net.tactware.worldweaver.domain

internal data class SrdMonsterEntry(
    val name: String,
    val creatureType: String,
    val challengeRating: String,
    val hitPoints: Int,
    val armorClass: Int,
    val walkSpeed: Int,
)
