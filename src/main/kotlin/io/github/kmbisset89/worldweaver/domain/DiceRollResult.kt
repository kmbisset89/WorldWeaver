package io.github.kmbisset89.worldweaver.domain

internal data class DiceRollResult(
    val sides: Int,
    val count: Int,
    val modifier: Int,
    val mode: RollMode,
    val faces: List<Int>,
    val keptFaces: List<Int>,
    val total: Int,
    val source: DiceRollSource = DiceRollSource.Automated,
)
