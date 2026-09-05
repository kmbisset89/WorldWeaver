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
import io.github.kmbisset89.worldweaver.domain.CreateWorldCalendarObservanceUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteWorldCalendarObservanceUseCase
import io.github.kmbisset89.worldweaver.domain.FindSessionCalendarMonthIdsForWorldUseCase
import io.github.kmbisset89.worldweaver.domain.Lore
import io.github.kmbisset89.worldweaver.domain.ObserveActiveContextDetailsUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveLoreForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveWorldCalendarForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveWorldCalendarObservancesForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateWorldCalendarObservanceUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateWorldCalendarUseCase
import io.github.kmbisset89.worldweaver.domain.WorldCalendar
import io.github.kmbisset89.worldweaver.domain.WorldCalendarDraft
import io.github.kmbisset89.worldweaver.domain.WorldCalendarMonth
import io.github.kmbisset89.worldweaver.domain.WorldCalendarObservance
import io.github.kmbisset89.worldweaver.domain.WorldCalendarObservanceDraft
import io.github.kmbisset89.worldweaver.domain.WorldCalendarObservanceKind
import io.github.kmbisset89.worldweaver.domain.WorldCalendarWeekday
import io.github.kmbisset89.worldweaver.domain.WorldDate
import io.github.kmbisset89.worldweaver.domain.WorldDateFormatter

internal class CalendarViewModel(
    private val appScope: AppCoroutineScope,
    private val observeActiveContextDetails: ObserveActiveContextDetailsUseCase,
    private val observeCalendar: ObserveWorldCalendarForActiveWorldUseCase,
    private val observeObservances: ObserveWorldCalendarObservancesForActiveWorldUseCase,
    private val observeLore: ObserveLoreForActiveWorldUseCase,
    private val findSessionMonthIds: FindSessionCalendarMonthIdsForWorldUseCase,
    private val updateCalendar: UpdateWorldCalendarUseCase,
    private val createObservance: CreateWorldCalendarObservanceUseCase,
    private val updateObservance: UpdateWorldCalendarObservanceUseCase,
    private val deleteObservance: DeleteWorldCalendarObservanceUseCase,
    private val dateFormatter: WorldDateFormatter = WorldDateFormatter(),
) {
    private val _state = MutableStateFlow<CalendarViewState>(CalendarViewState.Loading)
    val state: StateFlow<CalendarViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<CalendarViewEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<CalendarViewEffect> = _effects.asSharedFlow()

    private var observeJob: Job? = null
    private var latestCalendar: WorldCalendar? = null
    private var latestObservances: List<WorldCalendarObservance> = emptyList()
    private var latestLore: List<Lore> = emptyList()
    private var latestWorldId: String? = null
    private var latestWorldName: String = ""
    private var referencedMonthIds: Set<String> = emptySet()
    private var selectedObservanceId: String? = null
    private var pendingOpenObservanceId: String? = null
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
                        saveError = "That month is used by a session, holiday, or the current date.",
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
            CalendarInteraction.NewObservanceSelected -> openCreateEditor()
            is CalendarInteraction.ObservanceSelected,
            is CalendarInteraction.ObservanceOpened,
            -> selectObservance(selectedId(interaction))
            is CalendarInteraction.EditObservanceSelected -> openEditEditor(interaction.observanceId)
            is CalendarInteraction.DeleteObservanceSelected -> requestDelete(interaction.observanceId)
            CalendarInteraction.DeleteConfirmed -> confirmDelete()
            CalendarInteraction.DeleteCancelled -> updatePendingDelete(null)
            is CalendarInteraction.LinkedLoreSelected -> {
                _effects.tryEmit(CalendarViewEffect.OpenLore(interaction.loreId))
            }
            is CalendarInteraction.EditorNameChanged -> updateEditor { editor ->
                editor.copy(name = interaction.name, nameError = null, saveError = null)
            }
            is CalendarInteraction.EditorNotesChanged -> updateEditor { editor ->
                editor.copy(notes = interaction.notes)
            }
            is CalendarInteraction.EditorKindSelected -> updateEditor { editor ->
                editor.copy(kind = interaction.kind)
            }
            is CalendarInteraction.EditorMonthSelected -> updateEditor { editor ->
                editor.copy(monthId = interaction.monthId, dateError = null, saveError = null)
            }
            is CalendarInteraction.EditorDayChanged -> updateEditor { editor ->
                editor.copy(dayText = interaction.day, dateError = null, saveError = null)
            }
            is CalendarInteraction.EditorYearChanged -> updateEditor { editor ->
                editor.copy(yearText = interaction.year, dateError = null, saveError = null)
            }
            is CalendarInteraction.EditorLoreToggled -> updateEditor { editor ->
                val loreIds = if (interaction.loreId in editor.loreIds) {
                    editor.loreIds - interaction.loreId
                } else {
                    editor.loreIds + interaction.loreId
                }
                editor.copy(loreIds = loreIds)
            }
            CalendarInteraction.EditorSaved -> saveEditor()
            CalendarInteraction.EditorDismissed -> updateEditor { null }
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
                observeObservances(),
                observeLore(),
            ) { details, calendar, observances, lore ->
                LoadedSnapshot(details, calendar, observances, lore)
            }
                .catch { error ->
                    _state.value = CalendarViewState.Error(
                        message = error.message ?: "Could not load the calendar",
                        canRetry = true,
                    )
                }
                .collect { snapshot ->
                    applyLoaded(snapshot.details, snapshot.calendar, snapshot.observances, snapshot.lore)
                }
        }
    }

    private suspend fun applyLoaded(
        details: ActiveContextDetails,
        calendar: WorldCalendar?,
        observances: List<WorldCalendarObservance>,
        lore: List<Lore>,
    ) {
        val world = details.world
        if (world == null) {
            latestCalendar = null
            latestObservances = emptyList()
            latestLore = emptyList()
            latestWorldId = null
            latestWorldName = ""
            referencedMonthIds = emptySet()
            selectedObservanceId = null
            dirty = false
            _state.value = CalendarViewState.NoActiveWorld
            return
        }
        val worldChanged = latestWorldId != world.id
        latestWorldId = world.id
        latestWorldName = world.name
        latestCalendar = calendar
        latestObservances = observances
        latestLore = lore
        referencedMonthIds = findSessionMonthIds(world.id) +
            observances.map { it.monthId } +
            setOfNotNull(calendar?.currentDate?.monthId)
        if (calendar == null) {
            dirty = false
            _state.value = CalendarViewState.Error(
                message = "This world is missing a calendar.",
                canRetry = true,
            )
            return
        }
        pendingOpenObservanceId?.let { openId ->
            if (observances.any { it.id == openId }) {
                selectedObservanceId = openId
                pendingOpenObservanceId = null
            }
        }
        if (selectedObservanceId != null && observances.none { it.id == selectedObservanceId }) {
            selectedObservanceId = null
        }
        if (worldChanged || !dirty) {
            dirty = false
            _state.value = contentFrom(calendar, currentEditor(), currentPendingDelete())
        } else {
            updateContent(markDirty = false) { content ->
                content.copy(
                    worldName = world.name,
                    todayObservances = todayLines(calendar, observances),
                    observances = observanceLines(calendar, observances),
                    selectedObservanceId = selectedObservanceId,
                    referencedMonthIds = referencedMonthIds,
                    preview = previewFor(content),
                    editor = content.editor?.copy(loreOptions = lore),
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
                UpdateWorldCalendarUseCase.Result.InvalidObservanceDate -> updateContent { current ->
                    current.copy(saveError = "A holiday or important day would fall outside that month.")
                }
                UpdateWorldCalendarUseCase.Result.MonthReferenced -> updateContent { current ->
                    current.copy(saveError = "That month is used by a session, holiday, or the current date.")
                }
                UpdateWorldCalendarUseCase.Result.NotFound -> updateContent { current ->
                    current.copy(saveError = "Calendar was not found.")
                }
            }
        }
    }

    private fun contentFrom(
        calendar: WorldCalendar,
        editor: CalendarViewState.ObservanceEditorState?,
        pendingDelete: CalendarViewState.PendingDelete?,
    ): CalendarViewState.Content {
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
            todayObservances = todayLines(calendar, latestObservances),
            observances = observanceLines(calendar, latestObservances),
            selectedObservanceId = selectedObservanceId,
            referencedMonthIds = referencedMonthIds,
            monthsError = null,
            weekdaysError = null,
            currentDateError = null,
            saveError = null,
            editor = editor,
            pendingDelete = pendingDelete,
        )
    }

    private fun todayLines(
        calendar: WorldCalendar,
        observances: List<WorldCalendarObservance>,
    ): List<CalendarViewState.ObservanceLine> {
        val today = calendar.currentDate ?: return emptyList()
        return observanceLines(calendar, observances.filter { it.matches(today) })
    }

    private fun observanceLines(
        calendar: WorldCalendar,
        observances: List<WorldCalendarObservance>,
    ): List<CalendarViewState.ObservanceLine> {
        val monthOrder = calendar.months.mapIndexed { index, month -> month.id to index }.toMap()
        val loreTitles = latestLore.associate { it.id to it.title }
        return observances
            .sortedWith(
                compareBy<WorldCalendarObservance> { monthOrder[it.monthId] ?: Int.MAX_VALUE }
                    .thenBy { it.day }
                    .thenBy { it.name.lowercase() }
            )
            .map { observance ->
                CalendarViewState.ObservanceLine(
                    id = observance.id,
                    name = observance.name,
                    kindLabel = observance.kind.displayName,
                    dateLabel = dateFormatter.formatObservance(
                        calendar,
                        observance.monthId,
                        observance.day,
                        observance.year,
                    ) ?: "${observance.day}",
                    notes = observance.notes,
                    loreLinks = observance.loreIds.map { loreId ->
                        CalendarViewState.LoreLink(
                            loreId = loreId,
                            title = loreTitles[loreId] ?: "Missing lore",
                        )
                    },
                )
            }
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

    private fun selectObservance(observanceId: String) {
        val exists = latestObservances.any { it.id == observanceId }
        if (!exists) {
            pendingOpenObservanceId = observanceId
            return
        }
        selectedObservanceId = observanceId
        updateContent(markDirty = false) { content ->
            content.copy(selectedObservanceId = observanceId)
        }
    }

    private fun openCreateEditor() {
        val calendar = latestCalendar ?: return
        val firstMonthId = calendar.months.firstOrNull()?.id.orEmpty()
        updateEditor {
            CalendarViewState.ObservanceEditorState(
                observanceId = null,
                name = "",
                notes = "",
                kind = WorldCalendarObservanceKind.Holiday,
                monthId = firstMonthId,
                dayText = "1",
                yearText = "",
                loreIds = emptyList(),
                loreOptions = latestLore,
                nameError = null,
                dateError = null,
                saveError = null,
            )
        }
    }

    private fun openEditEditor(observanceId: String) {
        val observance = latestObservances.firstOrNull { it.id == observanceId } ?: return
        selectedObservanceId = observanceId
        updateEditor {
            CalendarViewState.ObservanceEditorState(
                observanceId = observance.id,
                name = observance.name,
                notes = observance.notes,
                kind = observance.kind,
                monthId = observance.monthId,
                dayText = observance.day.toString(),
                yearText = observance.year?.toString().orEmpty(),
                loreIds = observance.loreIds,
                loreOptions = latestLore,
                nameError = null,
                dateError = null,
                saveError = null,
            )
        }
    }

    private fun requestDelete(observanceId: String) {
        val observance = latestObservances.firstOrNull { it.id == observanceId } ?: return
        updatePendingDelete(
            CalendarViewState.PendingDelete(
                observanceId = observance.id,
                name = observance.name,
            )
        )
    }

    private fun confirmDelete() {
        val pending = currentPendingDelete() ?: return
        appScope.scope.launch {
            when (deleteObservance(pending.observanceId)) {
                DeleteWorldCalendarObservanceUseCase.Result.Deleted -> {
                    if (selectedObservanceId == pending.observanceId) {
                        selectedObservanceId = null
                    }
                    updatePendingDelete(null)
                }
                DeleteWorldCalendarObservanceUseCase.Result.NotFound -> updatePendingDelete(null)
            }
        }
    }

    private fun saveEditor() {
        val content = _state.value as? CalendarViewState.Content ?: return
        val editor = content.editor ?: return
        val name = editor.name.trim()
        if (name.isEmpty()) {
            updateEditor { current -> current.copy(nameError = "Name is required.") }
            return
        }
        val day = editor.dayText.trim().toIntOrNull()
        val yearText = editor.yearText.trim()
        val year = if (yearText.isEmpty()) {
            null
        } else {
            yearText.toIntOrNull()
        }
        if (day == null || editor.monthId.isBlank() || (yearText.isNotEmpty() && year == null)) {
            updateEditor { current ->
                current.copy(dateError = "Enter a month and day. Year is optional.")
            }
            return
        }
        val draft = WorldCalendarObservanceDraft(
            name = editor.name,
            notes = editor.notes,
            kind = editor.kind,
            monthId = editor.monthId,
            day = day,
            year = year,
            loreIds = editor.loreIds,
        )
        appScope.scope.launch {
            if (editor.observanceId == null) {
                when (val result = createObservance(draft)) {
                    is CreateWorldCalendarObservanceUseCase.Result.Created -> {
                        selectedObservanceId = result.observance.id
                        updateEditor { null }
                    }
                    CreateWorldCalendarObservanceUseCase.Result.InvalidName -> updateEditor { current ->
                        current.copy(nameError = "Name is required.")
                    }
                    CreateWorldCalendarObservanceUseCase.Result.DuplicateName -> updateEditor { current ->
                        current.copy(nameError = "That name is already used.")
                    }
                    CreateWorldCalendarObservanceUseCase.Result.InvalidDate -> updateEditor { current ->
                        current.copy(dateError = "That date is not valid on this calendar.")
                    }
                    CreateWorldCalendarObservanceUseCase.Result.NoActiveWorld,
                    CreateWorldCalendarObservanceUseCase.Result.MissingCalendar,
                    -> updateEditor { current ->
                        current.copy(saveError = "Could not save this day.")
                    }
                }
            } else {
                when (updateObservance(editor.observanceId, draft)) {
                    UpdateWorldCalendarObservanceUseCase.Result.Updated -> updateEditor { null }
                    UpdateWorldCalendarObservanceUseCase.Result.InvalidName -> updateEditor { current ->
                        current.copy(nameError = "Name is required.")
                    }
                    UpdateWorldCalendarObservanceUseCase.Result.DuplicateName -> updateEditor { current ->
                        current.copy(nameError = "That name is already used.")
                    }
                    UpdateWorldCalendarObservanceUseCase.Result.InvalidDate -> updateEditor { current ->
                        current.copy(dateError = "That date is not valid on this calendar.")
                    }
                    UpdateWorldCalendarObservanceUseCase.Result.NotFound,
                    UpdateWorldCalendarObservanceUseCase.Result.MissingCalendar,
                    -> updateEditor { current ->
                        current.copy(saveError = "Could not save this day.")
                    }
                }
            }
        }
    }

    private fun selectedId(interaction: CalendarInteraction): String {
        return when (interaction) {
            is CalendarInteraction.ObservanceSelected -> interaction.observanceId
            is CalendarInteraction.ObservanceOpened -> interaction.observanceId
            else -> ""
        }
    }

    private fun currentEditor(): CalendarViewState.ObservanceEditorState? {
        return (_state.value as? CalendarViewState.Content)?.editor
    }

    private fun currentPendingDelete(): CalendarViewState.PendingDelete? {
        return (_state.value as? CalendarViewState.Content)?.pendingDelete
    }

    private fun updateEditor(transform: (CalendarViewState.ObservanceEditorState) -> CalendarViewState.ObservanceEditorState?) {
        updateContent(markDirty = false) { content ->
            val editor = content.editor
            val next = if (editor == null) {
                transform(
                    CalendarViewState.ObservanceEditorState(
                        observanceId = null,
                        name = "",
                        notes = "",
                        kind = WorldCalendarObservanceKind.Holiday,
                        monthId = content.months.firstOrNull()?.id.orEmpty(),
                        dayText = "1",
                        yearText = "",
                        loreIds = emptyList(),
                        loreOptions = latestLore,
                        nameError = null,
                        dateError = null,
                        saveError = null,
                    )
                )
            } else {
                transform(editor)
            }
            content.copy(editor = next)
        }
    }

    private fun updatePendingDelete(pending: CalendarViewState.PendingDelete?) {
        updateContent(markDirty = false) { content ->
            content.copy(pendingDelete = pending)
        }
    }

    private fun updateContent(
        markDirty: Boolean = true,
        transform: (CalendarViewState.Content) -> CalendarViewState.Content,
    ) {
        val current = _state.value
        if (current !is CalendarViewState.Content) {
            return
        }
        if (markDirty) {
            dirty = true
        }
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

    private data class LoadedSnapshot(
        val details: ActiveContextDetails,
        val calendar: WorldCalendar?,
        val observances: List<WorldCalendarObservance>,
        val lore: List<Lore>,
    )

    private sealed interface DateParse {
        data object None : DateParse
        data object Invalid : DateParse
        data class Found(val date: WorldDate) : DateParse
    }
}
