package net.tactware.worldweaver.domain

internal class DiceNotationParser {
    fun parse(text: String): DiceRollRequest? {
        val match = NOTATION.matchEntire(text.trim()) ?: return null
        val count = match.groupValues[1].toIntOrNull() ?: 1
        val sides = match.groupValues[2].toInt()
        if (DieSides.fromSides(sides) == null) {
            return null
        }
        if (count !in MIN_COUNT..MAX_COUNT) {
            return null
        }
        val sign = match.groupValues[3]
        val modifierMagnitude = match.groupValues[4]
        val modifier = if (sign.isEmpty() || modifierMagnitude.isEmpty()) {
            0
        } else {
            val magnitude = modifierMagnitude.toInt()
            if (sign == "-") -magnitude else magnitude
        }
        val modeToken = match.groupValues[5].lowercase()
        val requestedMode = when (modeToken) {
            "adv", "advantage" -> RollMode.Advantage
            "dis", "disadvantage" -> RollMode.Disadvantage
            else -> RollMode.Normal
        }
        val advantageAllowed = sides == DieSides.D20.sides && count == 1
        if (requestedMode != RollMode.Normal && !advantageAllowed) {
            return null
        }
        return DiceRollRequest(
            sides = sides,
            count = count,
            modifier = modifier,
            mode = requestedMode,
        )
    }

    private companion object {
        const val MIN_COUNT = 1
        const val MAX_COUNT = 20
        val NOTATION = Regex(
            """(\d+)?\s*d\s*(\d+)\s*(?:([+-])\s*(\d+))?\s*(adv|advantage|dis|disadvantage)?""",
            RegexOption.IGNORE_CASE,
        )
    }
}
