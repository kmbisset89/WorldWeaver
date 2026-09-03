package io.github.kmbisset89.worldweaver.domain

import kotlin.math.floor

internal data class AbilityScores(
    val strength: Int,
    val dexterity: Int,
    val constitution: Int,
    val intelligence: Int,
    val wisdom: Int,
    val charisma: Int,
) {
    fun modifierFor(score: Int): Int {
        return floor((score - 10) / 2.0).toInt()
    }

    companion object {
        fun average(): AbilityScores {
            return AbilityScores(
                strength = 10,
                dexterity = 10,
                constitution = 10,
                intelligence = 10,
                wisdom = 10,
                charisma = 10,
            )
        }
    }
}
