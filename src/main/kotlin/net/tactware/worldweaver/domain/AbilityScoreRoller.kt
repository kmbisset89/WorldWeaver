package net.tactware.worldweaver.domain

internal class AbilityScoreRoller(
    private val diceRoller: DiceRoller,
) {
    fun roll(method: AbilityScoreMethod): AbilityScores {
        return AbilityScores(
            strength = rollScore(method),
            dexterity = rollScore(method),
            constitution = rollScore(method),
            intelligence = rollScore(method),
            wisdom = rollScore(method),
            charisma = rollScore(method),
        )
    }

    private fun rollScore(method: AbilityScoreMethod): Int {
        return when (method) {
            AbilityScoreMethod.ThreeD6 -> {
                diceRoller.roll(DiceRollRequest(sides = DieSides.D6.sides, count = 3)).total
            }
            AbilityScoreMethod.FourD6DropLowest -> {
                val faces = diceRoller.roll(
                    DiceRollRequest(sides = DieSides.D6.sides, count = 4),
                ).faces
                faces.sum() - (faces.minOrNull() ?: 1)
            }
        }
    }
}
