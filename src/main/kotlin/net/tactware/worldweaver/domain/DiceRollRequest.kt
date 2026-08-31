package net.tactware.worldweaver.domain

internal data class DiceRollRequest(
    val sides: Int,
    val count: Int = 1,
    val modifier: Int = 0,
    val mode: RollMode = RollMode.Normal,
)
