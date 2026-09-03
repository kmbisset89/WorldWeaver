package io.github.kmbisset89.worldweaver.ui.calendar

internal sealed interface CalendarViewEffect {
    data object OpenWorlds : CalendarViewEffect
}
