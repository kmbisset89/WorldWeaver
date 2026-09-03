package net.tactware.worldweaver.domain

import kotlinx.serialization.Serializable

@Serializable
internal data class SrdMonsterPayload(
    val name: String,
    val creatureType: String = "",
    val challengeRating: String = "",
    val hitPoints: Int,
    val armorClass: Int,
    val walkSpeed: Int = 30,
)
