package io.github.kmbisset89.worldweaver.ui.dice

import io.github.kmbisset89.worldweaver.domain.DiceRollResult
import io.github.kmbisset89.worldweaver.domain.DiceRollSource
import io.github.kmbisset89.worldweaver.domain.DieSides
import io.github.kmbisset89.worldweaver.domain.RollMode

internal fun diceRollNotation(result: DiceRollResult): String {
    val countPrefix = if (result.count == 1) "" else result.count.toString()
    val modifierPart = when {
        result.modifier > 0 -> "+${result.modifier}"
        result.modifier < 0 -> result.modifier.toString()
        else -> ""
    }
    val modePart = when (result.mode) {
        RollMode.Normal -> ""
        RollMode.Advantage -> " adv"
        RollMode.Disadvantage -> " dis"
    }
    return "${countPrefix}d${result.sides}$modifierPart$modePart"
}

internal fun formatDiceRollFaces(result: DiceRollResult): String {
    val facesLabel = when (result.mode) {
        RollMode.Normal -> result.keptFaces.joinToString("+")
        RollMode.Advantage -> "${result.faces.joinToString(" / ")}, adv"
        RollMode.Disadvantage -> "${result.faces.joinToString(" / ")}, dis"
    }
    val modifierLabel = when {
        result.modifier > 0 -> "+${result.modifier}"
        result.modifier < 0 -> result.modifier.toString()
        else -> ""
    }
    return "$facesLabel$modifierLabel"
}

internal fun isDiscardedFace(result: DiceRollResult, face: Int): Boolean {
    if (result.mode == RollMode.Normal) {
        return false
    }
    if (result.faces.distinct().size <= 1) {
        return false
    }
    return face != result.keptFaces.single()
}

internal fun isTableRoll(result: DiceRollResult): Boolean {
    return result.source == DiceRollSource.Manual
}

internal fun isNaturalTwenty(result: DiceRollResult): Boolean {
    return result.sides == 20 && result.keptFaces.contains(20)
}

internal fun isNaturalOne(result: DiceRollResult): Boolean {
    return result.sides == 20 &&
        result.keptFaces.contains(1) &&
        !result.keptFaces.contains(20)
}

internal fun isNaturalTwentyFace(die: DieSides, face: Int?): Boolean {
    return die == DieSides.D20 && face == 20
}

internal fun isNaturalOneFace(die: DieSides, face: Int?): Boolean {
    return die == DieSides.D20 && face == 1
}
