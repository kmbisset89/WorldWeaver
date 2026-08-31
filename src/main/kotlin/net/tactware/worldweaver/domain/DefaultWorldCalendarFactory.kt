package net.tactware.worldweaver.domain

import java.time.Instant

internal class DefaultWorldCalendarFactory(
    private val entityIdFactory: EntityIdFactory,
) {
    fun create(worldId: String, now: Instant): WorldCalendar {
        return WorldCalendar(
            id = entityIdFactory.create(),
            worldId = worldId,
            eraSuffix = "",
            months = DEFAULT_MONTHS.map { (name, days) ->
                WorldCalendarMonth(
                    id = entityIdFactory.create(),
                    name = name,
                    days = days,
                )
            },
            weekdays = DEFAULT_WEEKDAYS.map { name ->
                WorldCalendarWeekday(
                    id = entityIdFactory.create(),
                    name = name,
                )
            },
            currentDate = null,
            createdAt = now,
            updatedAt = now,
        )
    }

    private companion object {
        val DEFAULT_MONTHS = listOf(
            "January" to 31,
            "February" to 28,
            "March" to 31,
            "April" to 30,
            "May" to 31,
            "June" to 30,
            "July" to 31,
            "August" to 31,
            "September" to 30,
            "October" to 31,
            "November" to 30,
            "December" to 31,
        )
        val DEFAULT_WEEKDAYS = listOf(
            "Sunday",
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday",
        )
    }
}
