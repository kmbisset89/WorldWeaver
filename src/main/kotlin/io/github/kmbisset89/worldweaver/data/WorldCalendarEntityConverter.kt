package io.github.kmbisset89.worldweaver.data

import io.github.kmbisset89.worldweaver.domain.WorldCalendar
import io.github.kmbisset89.worldweaver.domain.WorldCalendarMonth
import io.github.kmbisset89.worldweaver.domain.WorldCalendarWeekday
import io.github.kmbisset89.worldweaver.domain.WorldDate
import java.time.Instant

internal class WorldCalendarEntityConverter {
    fun toCalendar(
        entity: WorldCalendarEntity,
        months: List<WorldCalendarMonth>,
        weekdays: List<WorldCalendarWeekday>,
    ): WorldCalendar {
        return WorldCalendar(
            id = entity.id,
            worldId = entity.worldId,
            eraSuffix = entity.eraSuffix,
            months = months,
            weekdays = weekdays,
            currentDate = toCurrentDate(entity),
            createdAt = Instant.ofEpochMilli(entity.createdAtEpochMillis),
            updatedAt = Instant.ofEpochMilli(entity.updatedAtEpochMillis),
        )
    }

    fun toEntity(calendar: WorldCalendar): WorldCalendarEntity {
        val current = calendar.currentDate
        return WorldCalendarEntity(
            id = calendar.id,
            worldId = calendar.worldId,
            eraSuffix = calendar.eraSuffix,
            currentYear = current?.year,
            currentMonthId = current?.monthId,
            currentDay = current?.day,
            createdAtEpochMillis = calendar.createdAt.toEpochMilli(),
            updatedAtEpochMillis = calendar.updatedAt.toEpochMilli(),
        )
    }

    fun toMonthEntities(calendar: WorldCalendar): List<WorldCalendarMonthEntity> {
        return calendar.months.mapIndexed { index, month ->
            WorldCalendarMonthEntity(
                id = month.id,
                calendarId = calendar.id,
                name = month.name,
                days = month.days,
                sortIndex = index,
            )
        }
    }

    fun toWeekdayEntities(calendar: WorldCalendar): List<WorldCalendarWeekdayEntity> {
        return calendar.weekdays.mapIndexed { index, weekday ->
            WorldCalendarWeekdayEntity(
                id = weekday.id,
                calendarId = calendar.id,
                name = weekday.name,
                sortIndex = index,
            )
        }
    }

    fun toMonths(entities: List<WorldCalendarMonthEntity>): List<WorldCalendarMonth> {
        return entities.map { entity ->
            WorldCalendarMonth(
                id = entity.id,
                name = entity.name,
                days = entity.days,
            )
        }
    }

    fun toWeekdays(entities: List<WorldCalendarWeekdayEntity>): List<WorldCalendarWeekday> {
        return entities.map { entity ->
            WorldCalendarWeekday(
                id = entity.id,
                name = entity.name,
            )
        }
    }

    private fun toCurrentDate(entity: WorldCalendarEntity): WorldDate? {
        val year = entity.currentYear ?: return null
        val monthId = entity.currentMonthId ?: return null
        val day = entity.currentDay ?: return null
        return WorldDate(year = year, monthId = monthId, day = day)
    }
}
