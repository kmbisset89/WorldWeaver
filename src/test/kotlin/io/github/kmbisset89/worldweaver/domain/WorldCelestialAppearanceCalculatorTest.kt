package io.github.kmbisset89.worldweaver.domain

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class WorldCelestialAppearanceCalculatorTest {
    private val calculator = WorldCelestialAppearanceCalculator()
    private val now = Instant.parse("2026-09-05T12:00:00Z")

    @Test
    fun yearOneFirstDayIsCycleDayZeroWithNoOffset() {
        val appearance = calculator.appearance(
            calendar = calendar(),
            date = WorldDate(year = 1, monthId = "m-1", day = 1),
            body = body(periodDays = 10, epochOffsetDays = 0),
        )

        assertEquals(0, appearance?.cycleDay)
        assertEquals(0.0, appearance?.fraction)
        assertEquals("Day 1 of 10 (0%)", appearance?.label())
        assertNull(appearance?.moonPhase)
    }

    @Test
    fun offsetShiftsTheCycleAndWraps() {
        val appearance = calculator.appearance(
            calendar = calendar(),
            date = WorldDate(year = 1, monthId = "m-1", day = 1),
            body = body(periodDays = 10, epochOffsetDays = 3),
        )

        assertEquals(7, appearance?.cycleDay)
        assertEquals("Day 8 of 10 (70%)", appearance?.label())
    }

    @Test
    fun moonMapsEqualEighthsToNamedPhases() {
        val calendar = calendar()
        val newMoon = calculator.appearance(
            calendar = calendar,
            date = WorldDate(year = 1, monthId = "m-1", day = 1),
            body = moon(periodDays = 8, epochOffsetDays = 0),
        )
        val fullMoon = calculator.appearance(
            calendar = calendar,
            date = WorldDate(year = 1, monthId = "m-1", day = 5),
            body = moon(periodDays = 8, epochOffsetDays = 0),
        )

        assertEquals(CelestialMoonPhase.New, newMoon?.moonPhase)
        assertEquals("New (1 / 8)", newMoon?.label())
        assertEquals(CelestialMoonPhase.Full, fullMoon?.moonPhase)
        assertEquals("Full (5 / 8)", fullMoon?.label())
    }

    @Test
    fun negativeOffsetWrapsForward() {
        val appearance = calculator.appearance(
            calendar = calendar(),
            date = WorldDate(year = 1, monthId = "m-1", day = 1),
            body = body(periodDays = 10, epochOffsetDays = -1),
        )

        assertEquals(1, appearance?.cycleDay)
    }

    @Test
    fun invalidDateReturnsNull() {
        val appearance = calculator.appearance(
            calendar = calendar(),
            date = WorldDate(year = 1, monthId = "m-1", day = 40),
            body = body(periodDays = 10, epochOffsetDays = 0),
        )

        assertNull(appearance)
    }

    @Test
    fun invalidPeriodReturnsNull() {
        val appearance = calculator.appearance(
            calendar = calendar(),
            date = WorldDate(year = 1, monthId = "m-1", day = 1),
            body = body(periodDays = 0, epochOffsetDays = 0),
        )

        assertNull(appearance)
    }

    @Test
    fun dayIndexCrossesMonthsAndYears() {
        val formatter = WorldDateFormatter()
        val calendar = calendar()

        assertEquals(0, formatter.dayIndex(calendar, WorldDate(year = 1, monthId = "m-1", day = 1)))
        assertEquals(30, formatter.dayIndex(calendar, WorldDate(year = 1, monthId = "m-2", day = 1)))
        assertEquals(60, formatter.dayIndex(calendar, WorldDate(year = 2, monthId = "m-1", day = 1)))
        assertTrue(formatter.dayIndex(calendar, WorldDate(year = 1, monthId = "missing", day = 1)) == null)
    }

    private fun calendar(): WorldCalendar {
        return WorldCalendar(
            id = "cal-1",
            worldId = "world-1",
            eraSuffix = "",
            months = listOf(
                WorldCalendarMonth(id = "m-1", name = "Hammer", days = 30),
                WorldCalendarMonth(id = "m-2", name = "Alturiak", days = 30),
            ),
            weekdays = emptyList(),
            currentDate = null,
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun body(
        periodDays: Int,
        epochOffsetDays: Int,
        kind: CelestialBodyKind = CelestialBodyKind.Sun,
    ): WorldCelestialBody {
        return WorldCelestialBody(
            id = "body-1",
            worldId = "world-1",
            name = "The Sun",
            notes = "",
            kind = kind,
            periodDays = periodDays,
            epochOffsetDays = epochOffsetDays,
            sortIndex = 0,
            createdAt = now,
            updatedAt = now,
        )
    }

    private fun moon(periodDays: Int, epochOffsetDays: Int): WorldCelestialBody {
        return body(periodDays = periodDays, epochOffsetDays = epochOffsetDays, kind = CelestialBodyKind.Moon)
            .copy(name = "The Moon")
    }
}
