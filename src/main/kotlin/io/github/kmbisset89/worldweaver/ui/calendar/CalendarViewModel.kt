package io.github.kmbisset89.worldweaver.ui.calendar

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import io.github.kmbisset89.worldweaver.core.AppCoroutineScope
import io.github.kmbisset89.worldweaver.domain.ActiveContextDetails
import io.github.kmbisset89.worldweaver.domain.FindSessionCalendarMonthIdsForWorldUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveActiveContextDetailsUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveWorldCalendarForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateWorldCalendarUseCase
import io.github.kmbisset89.worldweaver.domain.WorldCalendar
import io.github.kmbisset89.worldweaver.domain.WorldCalendarDraft
import io.github.kmbisset89.worldweaver.domain.WorldCalendarMonth
import io.github.kmbisset89.worldweaver.domain.WorldCalendarWeekday
import io.github.kmbisset89.worldweaver.domain.WorldDate
import io.github.kmbisset89.worldweaver.domain.WorldDateFormatter

internal class CalendarViewModel(
    private val appScope: AppCoroutineScope,
    private val observeActiveContextDetails: ObserveActiveContextDetailsUseCase,
    private val observeCalendar: ObserveWorldCalendarForActiveWorldUseCase,
    private val findSessionMonthIds: FindSessionCalendarMonthIdsForWorldUseCase,
    private val updateCalendar: UpdateWorldCalendarUseCase,
    private val dateFormatter: WorldDateFormatter = WorldDateFormatter(),
) {
    private val _state = MutableStateFlow<CalendarViewState>(CalendarViewState.Loading)
    val state: StateFlow<CalendarViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<CalendarViewEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<CalendarViewEffect> = _effects.asSharedFlow()

    private var observeJob: Job? = null
    private var latestCalendar: WorldCalendar? = null
    private var latestWorldId: String? = null
    private var latestWorldName: String = ""
    private var referencedMonthIds: Set<String> = emptySet()
    private var dirty = false

    init {
        observe()
    }

    fun onInteraction(interaction: CalendarInteraction) {
        when (interaction) {
            CalendarInteraction.ScreenStarted -> Unit
            CalendarInteraction.RetrySelected -> observe()
            CalendarInteraction.CreateWorldSelected -> {
                _effects.tryEmit(CalendarViewEffect.OpenWorlds)
            }
            is CalendarInteraction.EraSuffixChanged -> updateContent { content ->
                content.copy(eraSuffix = interaction.eraSuffix, saveError = null)
            }
            CalendarInteraction.MonthAdded -> updateContent { content ->
                content.copy(
                    months = content.months + CalendarViewState.MonthEditor(
                        id = "",
                        name = "",
                        daysText = "30",
                    ),
                    monthsError = null,
                    saveError = null,
                )
            }
            is CalendarInteraction.MonthRemoved -> updateContent { content ->
                if (content.months.size <= 1) {
                    return@updateContent content
                }
                val month = content.months.getOrNull(interaction.index) ?: return@updateContent content
                if (month.id.isNotEmpty() && month.id in content.referencedMonthIds) {
                    return@updateContent content.copy(
                        saveError = "That month is used by a session or the current date.",
                    )
                }
                content.copy(
                    months = content.months.filterIndexed { index, _ -> index != interaction.index },
                    currentMonthId = content.currentMonthId.takeIf { it != month.id },
                    monthsError = null,
                    saveError = null,
                )
            }
            is CalendarInteraction.MonthMoved -> updateContent { content ->
                content.copy(months = move(content.months, interaction.index, interaction.delta))
            }
            is CalendarInteraction.MonthNameChanged -> updateContent { content ->
                content.copy(
                    months = content.months.mapIndexed { index, month ->
                        if (index == interaction.index) month.copy(name = interaction.name) else month
                    },
                    monthsError = null,
                    saveError = null,
                )
            }
            is CalendarInteraction.MonthDaysChanged -> updateContent { content ->
                content.copy(
                    months = content.months.mapIndexed { index, month ->
                        if (index == interaction.index) month.copy(daysText = interaction.days) else month
                    },
                    monthsError = null,
                    saveError = null,
                )
            }
            CalendarInteraction.WeekdayAdded -> updateContent { content ->
                content.copy(
                    weekdays = content.weekdays + CalendarViewState.WeekdayEditor(id = "", name = ""),
                    weekdaysError = null,
                    saveError = null,
                )
            }
            is CalendarInteraction.WeekdayRemoved -> updateContent { content ->
                content.copy(
                    weekdays = content.weekdays.filterIndexed { index, _ -> index != interaction.index },
                    weekdaysError = null,
                    saveError = null,
                )
            }
            is CalendarInteraction.WeekdayMoved -> updateContent { content ->
                content.copy(weekdays = move(content.weekdays, interaction.index, interaction.delta))
            }
            is CalendarInteraction.WeekdayNameChanged -> updateContent { content ->
                content.copy(
                    weekdays = content.weekdays.mapIndexed { index, weekday ->
                        if (index == interaction.index) weekday.copy(name = interaction.name) else weekday
                    },
                    weekdaysError = null,
                    saveError = null,
                )
            }
            is CalendarInteraction.CurrentYearChanged -> updateContent { content ->
                content.copy(currentYear = interaction.year, currentDateError = null, saveError = null)
            }
            is CalendarInteraction.CurrentMonthSelected -> updateContent { content ->
                content.copy(currentMonthId = interaction.monthId, currentDateError = null, saveError = null)
            }
            is CalendarInteraction.CurrentDayChanged -> updateContent { content ->
                content.copy(currentDay = interaction.day, currentDateError = null, saveError = null)
            }
            CalendarInteraction.CurrentDateCleared -> updateContent { content ->
                content.copy(
                    currentYear = "",
                    currentMonthId = null,
                    currentDay = "",
                    currentDateError = null,
                    saveError = null,
                )
            }
            CalendarInteraction.Saved -> save()
        }
    }

    private fun observe() {
        observeJob?.cancel()
        _state.value = CalendarViewState.Loading
        dirty = false
        observeJob = appScope.scope.launch {
            combine(
                observeActiveContextDetails(),
                observeCalendar(),
            ) { details, calendar ->
                details to calendar
            }
                .catch { error ->
                    _state.value = CalendarViewState.Error(
                        message = error.message ?: "Could not load the calendar",
                        canRetry = true,
                    )
                }
                .collect { (details, calendar) ->
                    applyLoaded(details, calendar)
                }
        }
    }

    private suspend fun applyLoaded(
        details: ActiveContextDetails,
        calendar: WorldCalendar?,
    ) {
        val world = details.world
        if (world == null) {
            latestCalendar = null
            latestWorldId = null
            latestWorldName = ""
            referencedMonthIds = emptySet()
            dirty = false
            _state.value = CalendarViewState.NoActiveWorld
            return
        }
        val worldChanged = latestWorldId != world.id
        latestWorldId = world.id
        latestWorldName = world.name
        latestCalendar = calendar
        referencedMonthIds = findSessionMonthIds(world.id) + setOfNotNull(calendar?.currentDate?.monthId)
        if (calendar == null) {
            dirty = false
            _state.value = CalendarViewState.Error(
                message = "This world is missing a calendar.",
                canRetry = true,
            )
            return
        }
        if (worldChanged || !dirty) {
            dirty = false
            _state.value = contentFrom(calendar)
        } else {
            updateContent { content ->
                content.copy(
                    worldName = world.name,
                    referencedMonthIds = referencedMonthIds,
                    preview = previewFor(content),
                )
            }
        }
    }

    private fun save() {
        val content = _state.value as? CalendarViewState.Content ?: return
        val months = content.months.map { month ->
            WorldCalendarMonth(
                id = month.id,
                name = month.name,
                days = month.daysText.trim().toIntOrNull() ?: 0,
            )
        }
        if (months.isEmpty() || months.any { it.name.isBlank() || it.days < 1 }) {
            updateContent { current ->
                current.copy(monthsError = "Each month needs a name and at least 1 day.")
            }
            return
        }
        val weekdays = content.weekdays.map { weekday ->
            WorldCalendarWeekday(id = weekday.id, name = weekday.name)
        }
        if (weekdays.any { it.name.isBlank() }) {
            updateContent { current ->
                current.copy(weekdaysError = "Each weekday needs a name.")
            }
            return
        }
        val currentDate = parsedCurrentDate(content)
        if (currentDate is DateParse.Invalid) {
            updateContent { current ->
                current.copy(currentDateError = "Enter a year, month, and day, or clear the current date.")
            }
            return
        }
        appScope.scope.launch {
            val result = updateCalendar(
                content.calendarId,
                WorldCalendarDraft(
                    eraSuffix = content.eraSuffix,
                    months = months,
                    weekdays = weekdays,
                    currentDate = (currentDate as? DateParse.Found)?.date,
                ),
            )
            when (result) {
                UpdateWorldCalendarUseCase.Result.Updated -> {
                    dirty = false
                }
                UpdateWorldCalendarUseCase.Result.InvalidMonths -> updateContent { current ->
                    current.copy(monthsError = "Each month needs a name and at least 1 day.")
                }
                UpdateWorldCalendarUseCase.Result.InvalidWeekdays -> updateContent { current ->
                    current.copy(weekdaysError = "Each weekday needs a name.")
                }
                UpdateWorldCalendarUseCase.Result.InvalidCurrentDate -> updateContent { current ->
                    current.copy(currentDateError = "That date is not valid on this calendar.")
                }
                UpdateWorldCalendarUseCase.Result.MonthReferenced -> updateContent { current ->
                    current.copy(saveError = "That month is used by a session or the current date.")
                }
                UpdateWorldCalendarUseCase.Result.NotFound -> updateContent { current ->
                    current.copy(saveError = "Calendar was not found.")
                }
            }
        }
    }

    private fun contentFrom(calendar: WorldCalendar): CalendarViewState.Content {
        val current = calendar.currentDate
        return CalendarViewState.Content(
            worldName = latestWorldName,
            calendarId = calendar.id,
            eraSuffix = calendar.eraSuffix,
            months = calendar.months.map { month ->
                CalendarViewState.MonthEditor(
                    id = month.id,
                    name = month.name,
                    daysText = month.days.toString(),
                )
            },
            weekdays = calendar.weekdays.map { weekday ->
                CalendarViewState.WeekdayEditor(id = weekday.id, name = weekday.name)
            },
            currentYear = current?.year?.toString().orEmpty(),
            currentMonthId = current?.monthId,
            currentDay = current?.day?.toString().orEmpty(),
            preview = current?.let { dateFormatter.format(calendar, it) },
            referencedMonthIds = referencedMonthIds,
            monthsError = null,
            weekdaysError = null,
            currentDateError = null,
            saveError = null,
        )
    }

    private fun previewFor(content: CalendarViewState.Content): String? {
        val calendar = latestCalendar ?: return null
        val date = (parsedCurrentDate(content) as? DateParse.Found)?.date ?: return null
        val draftCalendar = calendar.copy(
            eraSuffix = content.eraSuffix,
            months = content.months.mapNotNull { month ->
                val days = month.daysText.trim().toIntOrNull() ?: return@mapNotNull null
                WorldCalendarMonth(id = month.id.ifBlank { month.name }, name = month.name, days = days)
            },
            weekdays = content.weekdays.map { weekday ->
                WorldCalendarWeekday(id = weekday.id.ifBlank { weekday.name }, name = weekday.name)
            },
        )
        return dateFormatter.format(draftCalendar, date)
    }

    private fun parsedCurrentDate(content: CalendarViewState.Content): DateParse {
        val yearText = content.currentYear.trim()
        val dayText = content.currentDay.trim()
        if (yearText.isEmpty() && dayText.isEmpty() && content.currentMonthId == null) {
            return DateParse.None
        }
        val year = yearText.toIntOrNull()
        val day = dayText.toIntOrNull()
        val monthId = content.currentMonthId
        if (year == null || day == null || monthId.isNullOrBlank()) {
            return DateParse.Invalid
        }
        return DateParse.Found(WorldDate(year = year, monthId = monthId, day = day))
    }

    private fun updateContent(transform: (CalendarViewState.Content) -> CalendarViewState.Content) {
        val current = _state.value
        if (current !is CalendarViewState.Content) {
            return
        }
        dirty = true
        val next = transform(current)
        _state.value = next.copy(preview = previewFor(next))
    }

    private fun <T> move(items: List<T>, index: Int, delta: Int): List<T> {
        val target = index + delta
        if (index !in items.indices || target !in items.indices) {
            return items
        }
        val mutable = items.toMutableList()
        val item = mutable.removeAt(index)
        mutable.add(target, item)
        return mutable
    }

    private sealed interface DateParse {
        data object None : DateParse
        data object Invalid : DateParse
        data class Found(val date: WorldDate) : DateParse
    }
}
