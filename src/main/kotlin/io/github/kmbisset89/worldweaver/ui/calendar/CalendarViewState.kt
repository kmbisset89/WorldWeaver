package io.github.kmbisset89.worldweaver.ui.calendar

internal sealed class CalendarViewState {
    data object Loading : CalendarViewState()

    data class Error(
        val message: String,
        val canRetry: Boolean,
    ) : CalendarViewState()

    data object NoActiveWorld : CalendarViewState()

    data class Content(
        val worldName: String,
        val calendarId: String,
        val eraSuffix: String,
        val months: List<MonthEditor>,
        val weekdays: List<WeekdayEditor>,
        val currentYear: String,
        val currentMonthId: String?,
        val currentDay: String,
        val preview: String?,
        val referencedMonthIds: Set<String>,
        val monthsError: String?,
        val weekdaysError: String?,
        val currentDateError: String?,
        val saveError: String?,
    ) : CalendarViewState()

    data class MonthEditor(
        val id: String,
        val name: String,
        val daysText: String,
    )

    data class WeekdayEditor(
        val id: String,
        val name: String,
    )
}
