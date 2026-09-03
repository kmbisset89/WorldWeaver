package io.github.kmbisset89.worldweaver.domain

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class WorldDateFormatterTest {
    private val formatter = WorldDateFormatter()
    private val now = Instant.parse("2026-08-31T12:00:00Z")

    @Test
    fun formatsWeekdayMonthYearAndEra() {
        val calendar = calendar()
        val date = WorldDate(year = 1, monthId = "m-7", day = 1)

        assertEquals("Moonday, 1 Flamerule, 1 DR", formatter.format(calendar, date))
        assertEquals(0, formatter.weekdayIndex(calendar, date))
    }

    @Test
    fun omitsWeekdayAndEraWhenEmpty() {
        val calendar = calendar().copy(eraSuffix = "", weekdays = emptyList())
        val date = WorldDate(year = 1492, monthId = "m-7", day = 12)

        assertEquals("12 Flamerule, 1492", formatter.format(calendar, date))
        assertNull(formatter.weekdayIndex(calendar, date))
    }

    @Test
    fun yearOneFirstDayIsFirstWeekday() {
        val calendar = calendar()
        val date = WorldDate(year = 1, monthId = "m-1", day = 1)

        assertEquals(0, formatter.weekdayIndex(calendar, date))
        assertTrue(formatter.isValid(calendar, date))
    }

    @Test
    fun rejectsDayOutsideMonth() {
        val calendar = calendar()
        val date = WorldDate(year = 1492, monthId = "m-2", day = 31)

        assertFalse(formatter.isValid(calendar, date))
        assertNull(formatter.format(calendar, date))
    }

    private fun calendar(): WorldCalendar {
        return WorldCalendar(
            id = "cal-1",
            worldId = "world-1",
            eraSuffix = "DR",
            months = listOf(
                WorldCalendarMonth(id = "m-1", name = "Hammer", days = 30),
                WorldCalendarMonth(id = "m-2", name = "Alturiak", days = 30),
                WorldCalendarMonth(id = "m-3", name = "Ches", days = 30),
                WorldCalendarMonth(id = "m-4", name = "Tarsakh", days = 30),
                WorldCalendarMonth(id = "m-5", name = "Mirtul", days = 30),
                WorldCalendarMonth(id = "m-6", name = "Kythorn", days = 30),
                WorldCalendarMonth(id = "m-7", name = "Flamerule", days = 30),
            ),
            weekdays = listOf(
                WorldCalendarWeekday(id = "w-1", name = "Moonday"),
                WorldCalendarWeekday(id = "w-2", name = "Treeday"),
            ),
            currentDate = null,
            createdAt = now,
            updatedAt = now,
        )
    }
}
