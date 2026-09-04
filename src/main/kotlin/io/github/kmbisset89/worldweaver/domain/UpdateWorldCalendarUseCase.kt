package io.github.kmbisset89.worldweaver.domain

internal class UpdateWorldCalendarUseCase(
    private val worldCalendarRepository: WorldCalendarRepository,
    private val findSessionMonthIds: FindSessionCalendarMonthIdsForWorldUseCase,
    private val observanceRepository: WorldCalendarObservanceRepository,
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
        data object InvalidObservanceDate : Result
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
        val observances = observanceRepository.getByWorld(existing.worldId)
        val keptMonthIds = months.map { it.id }.toSet()
        val referenced = referencedMonthIds(existing, currentDate, observances)
        if (referenced.any { it !in keptMonthIds }) {
            return Result.MonthReferenced
        }
        val daysByMonthId = months.associate { it.id to it.days }
        val observancesFit = observances.all { observance ->
            val days = daysByMonthId[observance.monthId] ?: return@all false
            observance.day in 1..days
        }
        if (!observancesFit) {
            return Result.InvalidObservanceDate
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
        observances: List<WorldCalendarObservance>,
    ): Set<String> {
        val fromSessions = findSessionMonthIds(existing.worldId)
        val fromObservances = observances.map { it.monthId }
        val fromCurrent = listOfNotNull(currentDate?.monthId)
        return fromSessions + fromObservances + fromCurrent
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
