package io.github.kmbisset89.worldweaver.domain

internal class WorldCelestialAppearanceCalculator(
    private val dateFormatter: WorldDateFormatter = WorldDateFormatter(),
) {
    fun appearance(
        calendar: WorldCalendar,
        date: WorldDate,
        body: WorldCelestialBody,
    ): WorldCelestialAppearance? {
        if (body.periodDays < 1) {
            return null
        }
        val dayIndex = dateFormatter.dayIndex(calendar, date) ?: return null
        val cycleDay = Math.floorMod(
            dayIndex - body.epochOffsetDays.toLong(),
            body.periodDays.toLong(),
        ).toInt()
        val fraction = cycleDay.toDouble() / body.periodDays.toDouble()
        val moonPhase = if (body.kind == CelestialBodyKind.Moon) {
            phaseFor(fraction)
        } else {
            null
        }
        return WorldCelestialAppearance(
            cycleDay = cycleDay,
            periodDays = body.periodDays,
            fraction = fraction,
            moonPhase = moonPhase,
        )
    }

    private fun phaseFor(fraction: Double): CelestialMoonPhase {
        val bucket = (fraction * PHASE_COUNT).toInt().coerceIn(0, PHASE_COUNT - 1)
        return CelestialMoonPhase.entries[bucket]
    }

    private companion object {
        const val PHASE_COUNT = 8
    }
}
