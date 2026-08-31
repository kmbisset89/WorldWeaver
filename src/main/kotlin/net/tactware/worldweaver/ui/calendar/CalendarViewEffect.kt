package net.tactware.worldweaver.ui.calendar

internal sealed interface CalendarViewEffect {
    data object OpenWorlds : CalendarViewEffect
}
