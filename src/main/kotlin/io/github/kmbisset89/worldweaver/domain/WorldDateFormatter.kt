package io.github.kmbisset89.worldweaver.domain

internal class WorldDateFormatter {
    fun isValid(calendar: WorldCalendar, date: WorldDate): Boolean {
        return monthOf(calendar, date) != null
    }

    fun format(calendar: WorldCalendar, date: WorldDate): String? {
        val month = monthOf(calendar, date) ?: return null
        val weekday = weekdayName(calendar, date)
        val body = "${date.day} ${month.name}, ${date.year}"
        val withWeekday = if (weekday == null) {
            body
        } else {
            "$weekday, $body"
        }
        val suffix = calendar.eraSuffix.trim()
        return if (suffix.isEmpty()) {
            withWeekday
        } else {
            "$withWeekday $suffix"
        }
    }

    fun weekdayName(calendar: WorldCalendar, date: WorldDate): String? {
        val weekdays = calendar.weekdays
        if (weekdays.isEmpty()) {
            return null
        }
        val index = weekdayIndex(calendar, date) ?: return null
        return weekdays[index].name
    }

    fun weekdayIndex(calendar: WorldCalendar, date: WorldDate): Int? {
        val month = monthOf(calendar, date) ?: return null
        val weekdays = calendar.weekdays
        if (weekdays.isEmpty()) {
            return null
        }
        val yearLength = calendar.months.sumOf { it.days }
        if (yearLength <= 0) {
            return null
        }
        val monthOffset = calendar.months
            .takeWhile { it.id != month.id }
            .sumOf { it.days }
        val dayOffset = (date.year - 1L) * yearLength + monthOffset + (date.day - 1)
        return Math.floorMod(dayOffset, weekdays.size.toLong()).toInt()
    }

    private fun monthOf(calendar: WorldCalendar, date: WorldDate): WorldCalendarMonth? {
        if (date.day < 1) {
            return null
        }
        val month = calendar.months.firstOrNull { it.id == date.monthId } ?: return null
        if (date.day > month.days) {
            return null
        }
        return month
    }
}
