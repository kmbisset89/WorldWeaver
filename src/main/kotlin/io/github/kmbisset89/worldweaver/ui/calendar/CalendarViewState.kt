package io.github.kmbisset89.worldweaver.ui.calendar

import io.github.kmbisset89.worldweaver.domain.Lore
import io.github.kmbisset89.worldweaver.domain.WorldCalendarObservanceKind

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
        val todayObservances: List<ObservanceLine>,
        val observances: List<ObservanceLine>,
        val selectedObservanceId: String?,
        val referencedMonthIds: Set<String>,
        val monthsError: String?,
        val weekdaysError: String?,
        val currentDateError: String?,
        val saveError: String?,
        val editor: ObservanceEditorState?,
        val pendingDelete: PendingDelete?,
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

    data class ObservanceLine(
        val id: String,
        val name: String,
        val kindLabel: String,
        val dateLabel: String,
        val notes: String,
        val loreLinks: List<LoreLink>,
    )

    data class LoreLink(
        val loreId: String,
        val title: String,
    )

    data class ObservanceEditorState(
        val observanceId: String?,
        val name: String,
        val notes: String,
        val kind: WorldCalendarObservanceKind,
        val monthId: String,
        val dayText: String,
        val yearText: String,
        val loreIds: List<String>,
        val loreOptions: List<Lore>,
        val nameError: String?,
        val dateError: String?,
        val saveError: String?,
    )

    data class PendingDelete(
        val observanceId: String,
        val name: String,
    )
}
