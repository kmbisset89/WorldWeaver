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

    fun isValidObservance(
        calendar: WorldCalendar,
        monthId: String,
        day: Int,
        year: Int?,
    ): Boolean {
        if (year != null) {
            if (year < 1) {
                return false
            }
            return isValid(calendar, WorldDate(year = year, monthId = monthId, day = day))
        }
        if (day < 1) {
            return false
        }
        val month = calendar.months.firstOrNull { it.id == monthId } ?: return false
        return day <= month.days
    }

    /**
     * Formats an observance date as `12 Hammer` when [year] is omitted, or
     * `12 Hammer, 1489 DR` when a year is present.
     */
    fun formatObservance(
        calendar: WorldCalendar,
        monthId: String,
        day: Int,
        year: Int?,
    ): String? {
        if (!isValidObservance(calendar, monthId, day, year)) {
            return null
        }
        val month = calendar.months.firstOrNull { it.id == monthId } ?: return null
        val body = if (year == null) {
            "$day ${month.name}"
        } else {
            "$day ${month.name}, $year"
        }
        val suffix = calendar.eraSuffix.trim()
        return if (year == null || suffix.isEmpty()) {
            body
        } else {
            "$body $suffix"
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
