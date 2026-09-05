package io.github.kmbisset89.worldweaver.ui.calendar

import io.github.kmbisset89.worldweaver.domain.WorldCalendarObservanceKind

internal sealed interface CalendarInteraction {
    data object ScreenStarted : CalendarInteraction
    data object RetrySelected : CalendarInteraction
    data object CreateWorldSelected : CalendarInteraction
    data class EraSuffixChanged(val eraSuffix: String) : CalendarInteraction
    data object MonthAdded : CalendarInteraction
    data class MonthRemoved(val index: Int) : CalendarInteraction
    data class MonthMoved(val index: Int, val delta: Int) : CalendarInteraction
    data class MonthNameChanged(val index: Int, val name: String) : CalendarInteraction
    data class MonthDaysChanged(val index: Int, val days: String) : CalendarInteraction
    data object WeekdayAdded : CalendarInteraction
    data class WeekdayRemoved(val index: Int) : CalendarInteraction
    data class WeekdayMoved(val index: Int, val delta: Int) : CalendarInteraction
    data class WeekdayNameChanged(val index: Int, val name: String) : CalendarInteraction
    data class CurrentYearChanged(val year: String) : CalendarInteraction
    data class CurrentMonthSelected(val monthId: String?) : CalendarInteraction
    data class CurrentDayChanged(val day: String) : CalendarInteraction
    data object CurrentDateCleared : CalendarInteraction
    data object Saved : CalendarInteraction
    data object NewObservanceSelected : CalendarInteraction
    data class ObservanceSelected(val observanceId: String) : CalendarInteraction
    data class ObservanceOpened(val observanceId: String) : CalendarInteraction
    data class EditObservanceSelected(val observanceId: String) : CalendarInteraction
    data class DeleteObservanceSelected(val observanceId: String) : CalendarInteraction
    data object DeleteConfirmed : CalendarInteraction
    data object DeleteCancelled : CalendarInteraction
    data class LinkedLoreSelected(val loreId: String) : CalendarInteraction
    data class EditorNameChanged(val name: String) : CalendarInteraction
    data class EditorNotesChanged(val notes: String) : CalendarInteraction
    data class EditorKindSelected(val kind: WorldCalendarObservanceKind) : CalendarInteraction
    data class EditorMonthSelected(val monthId: String) : CalendarInteraction
    data class EditorDayChanged(val day: String) : CalendarInteraction
    data class EditorYearChanged(val year: String) : CalendarInteraction
    data class EditorLoreToggled(val loreId: String) : CalendarInteraction
    data object EditorSaved : CalendarInteraction
    data object EditorDismissed : CalendarInteraction
}
