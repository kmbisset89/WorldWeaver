package net.tactware.worldweaver.domain

internal class UpdateWorldCalendarUseCase(
    private val worldCalendarRepository: WorldCalendarRepository,
    private val findSessionMonthIds: FindSessionCalendarMonthIdsForWorldUseCase,
    private val entityIdFactory: EntityIdFactory,
    private val instantProvider: InstantProvider,
    private val dateFormatter: WorldDateFormatter = WorldDateFormatter(),
) {
    sealed interface Result {
        data object Updated : Result
        data object NotFound : Result
        data object InvalidMonths : Result
        data object InvalidWeekdays : Result
        data object InvalidCurrentDate : Result
        data object MonthReferenced : Result
    }

    suspend operator fun invoke(
        calendarId: String,
        draft: WorldCalendarDraft,
    ): Result {
        val existing = worldCalendarRepository.getById(calendarId) ?: return Result.NotFound
        val months = assignMonths(draft.months)
        if (months.isEmpty() || months.any { it.name.isEmpty() || it.days < 1 }) {
            return Result.InvalidMonths
        }
        val weekdays = assignWeekdays(draft.weekdays)
        if (weekdays.any { it.name.isEmpty() }) {
            return Result.InvalidWeekdays
        }
        val calendarForValidation = existing.copy(months = months, weekdays = weekdays)
        val currentDate = draft.currentDate
        if (currentDate != null && !dateFormatter.isValid(calendarForValidation, currentDate)) {
            return Result.InvalidCurrentDate
        }
        val keptMonthIds = months.map { it.id }.toSet()
        val referenced = referencedMonthIds(existing, currentDate)
        if (referenced.any { it !in keptMonthIds }) {
            return Result.MonthReferenced
        }
        worldCalendarRepository.update(
            existing.copy(
                eraSuffix = draft.eraSuffix.trim(),
                months = months,
                weekdays = weekdays,
                currentDate = currentDate,
                updatedAt = instantProvider.now(),
            )
        )
        return Result.Updated
    }

    private suspend fun referencedMonthIds(
        existing: WorldCalendar,
        currentDate: WorldDate?,
    ): Set<String> {
        val fromSessions = findSessionMonthIds(existing.worldId)
        val fromCurrent = listOfNotNull(currentDate?.monthId)
        return fromSessions + fromCurrent
    }

    private fun assignMonths(months: List<WorldCalendarMonth>): List<WorldCalendarMonth> {
        return months.map { month ->
            month.copy(
                id = month.id.ifBlank { entityIdFactory.create() },
                name = month.name.trim(),
                days = month.days,
            )
        }
    }

    private fun assignWeekdays(weekdays: List<WorldCalendarWeekday>): List<WorldCalendarWeekday> {
        return weekdays.map { weekday ->
            weekday.copy(
                id = weekday.id.ifBlank { entityIdFactory.create() },
                name = weekday.name.trim(),
            )
        }
    }
}
