package io.github.kmbisset89.worldweaver.domain

import kotlin.math.roundToInt

internal data class WorldCelestialAppearance(
    val cycleDay: Int,
    val periodDays: Int,
    val fraction: Double,
    val moonPhase: CelestialMoonPhase?,
) {
    /**
     * A short label for this appearance, such as `Waxing gibbous (18 / 29)`
     * or `Day 41 of 88 (47%)`.
     */
    fun label(): String {
        return if (moonPhase != null) {
            "${moonPhase.displayName} (${cycleDay + 1} / $periodDays)"
        } else {
            val percent = (fraction * 100.0).roundToInt()
            "Day ${cycleDay + 1} of $periodDays ($percent%)"
        }
    }
}
