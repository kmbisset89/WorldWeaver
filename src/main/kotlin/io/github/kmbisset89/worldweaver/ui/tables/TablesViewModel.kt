package io.github.kmbisset89.worldweaver.ui.tables

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
import io.github.kmbisset89.worldweaver.domain.CreateRandomTableUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteRandomTableUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveActiveContextDetailsUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveRandomTablesForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.RandomTable
import io.github.kmbisset89.worldweaver.domain.RandomTableDraft
import io.github.kmbisset89.worldweaver.domain.RandomTableRoll
import io.github.kmbisset89.worldweaver.domain.RandomTableRowDraft
import io.github.kmbisset89.worldweaver.domain.RollRandomTableUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateRandomTableUseCase

internal class TablesViewModel(
    private val appScope: AppCoroutineScope,
    private val observeActiveContextDetails: ObserveActiveContextDetailsUseCase,
    private val observeTables: ObserveRandomTablesForActiveWorldUseCase,
    private val createTable: CreateRandomTableUseCase,
    private val updateTable: UpdateRandomTableUseCase,
    private val deleteTable: DeleteRandomTableUseCase,
    private val rollTable: RollRandomTableUseCase,
) {
    private val _state = MutableStateFlow<TablesViewState>(TablesViewState.Loading)
    val state: StateFlow<TablesViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<TablesViewEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<TablesViewEffect> = _effects.asSharedFlow()

    private var observeJob: Job? = null
    private var openCreateOnNextLoad = false
    private var selectedTableId: String? = null
    private var latestTables: List<RandomTable> = emptyList()
    private var latestWorldName: String = ""
    private var lastRoll: RandomTableRoll? = null
    private var rowKeySequence = 0

    init {
        observe()
    }

    fun onInteraction(interaction: TablesInteraction) {
        when (interaction) {
            TablesInteraction.ScreenStarted -> Unit
            TablesInteraction.RetrySelected -> observe()
            TablesInteraction.CreateWorldSelected -> {
                _effects.tryEmit(TablesViewEffect.OpenWorlds)
            }
            TablesInteraction.NewTableSelected -> openCreateEditor()
            is TablesInteraction.TableSelected,
            is TablesInteraction.TableOpened,
            -> selectTable(selectedId(interaction))
            is TablesInteraction.EditTableSelected -> openEditEditor(interaction.tableId)
            is TablesInteraction.DeleteTableSelected -> requestDelete(interaction.tableId)
            TablesInteraction.DeleteConfirmed -> confirmDelete()
            TablesInteraction.DeleteCancelled -> updatePendingDelete(null)
            is TablesInteraction.RollSelected -> roll(interaction.tableId)
            is TablesInteraction.EditorNameChanged -> updateEditor { editor ->
                editor?.copy(name = interaction.name, nameError = null)
            }
            is TablesInteraction.EditorNotesChanged -> updateEditor { editor ->
                editor?.copy(notes = interaction.notes)
            }
            TablesInteraction.EditorRowAdded -> updateEditor { editor ->
                editor?.copy(
                    rows = editor.rows + emptyRow(),
                    rowsError = null,
                )
            }
            is TablesInteraction.EditorRowRemoved -> updateEditor { editor ->
                editor?.copy(
                    rows = editor.rows.filterNot { it.key == interaction.key }.ifEmpty { listOf(emptyRow()) },
                    rowsError = null,
                )
            }
            is TablesInteraction.EditorRowLabelChanged -> updateEditor { editor ->
                editor?.copy(
                    rows = editor.rows.map { row ->
                        if (row.key == interaction.key) row.copy(label = interaction.label) else row
                    },
                    rowsError = null,
                )
            }
            is TablesInteraction.EditorRowWeightChanged -> updateEditor { editor ->
                editor?.copy(
                    rows = editor.rows.map { row ->
                        if (row.key == interaction.key) {
                            row.copy(weightText = interaction.weight, weightError = null)
                        } else {
                            row
                        }
                    },
                    rowsError = null,
                )
            }
            is TablesInteraction.EditorRowNestedSelected -> updateEditor { editor ->
                editor?.copy(
                    rows = editor.rows.map { row ->
                        if (row.key == interaction.key) {
                            row.copy(nestedTableId = interaction.nestedTableId)
                        } else {
                            row
                        }
                    },
                )
            }
            TablesInteraction.EditorSaved -> saveEditor()
            TablesInteraction.EditorDismissed -> updateEditor { null }
        }
    }

    private fun observe() {
        observeJob?.cancel()
        _state.value = TablesViewState.Loading
        observeJob = appScope.scope.launch {
            combine(
                observeActiveContextDetails(),
                observeTables(),
            ) { details, tables ->
                details to tables
            }
                .catch { error ->
                    _state.value = TablesViewState.Error(
                        message = error.message ?: "Could not load tables",
                        canRetry = true,
                    )
                }
                .collect { (details, tables) ->
                    applyLoaded(details, tables)
                }
        }
    }

    private fun applyLoaded(
        details: ActiveContextDetails,
        tables: List<RandomTable>,
    ) {
        val world = details.world
        if (world == null) {
            latestTables = emptyList()
            selectedTableId = null
            lastRoll = null
            _state.value = TablesViewState.NoActiveWorld
            return
        }
        latestTables = tables
        latestWorldName = world.name
        val current = _state.value
        val editor = if (openCreateOnNextLoad) {
            openCreateOnNextLoad = false
            createEditor()
        } else {
            editorFrom(current)?.copy(nestedOptions = nestedOptions(editorFrom(current)?.tableId))
        }
        if (tables.isEmpty()) {
            selectedTableId = null
            lastRoll = null
            _state.value = TablesViewState.Empty(
                worldName = world.name,
                editor = editor,
            )
            return
        }
        val selected = selectedFrom(tables) ?: tables.first()
        selectedTableId = selected.id
        if (lastRoll?.tableId != selected.id) {
            lastRoll = null
        }
        _state.value = contentState(
            selected = selected,
            editor = editor,
            pendingDelete = pendingDeleteFrom(current),
        )
    }

    private fun refreshContent() {
        val current = _state.value
        if (current !is TablesViewState.Content) {
            return
        }
        val selected = selectedFrom(latestTables) ?: latestTables.firstOrNull()
        _state.value = contentState(
            selected = selected,
            editor = current.editor,
            pendingDelete = current.pendingDelete,
        )
    }

    private fun contentState(
        selected: RandomTable?,
        editor: TablesViewState.TableEditorState?,
        pendingDelete: TablesViewState.PendingDelete?,
    ): TablesViewState.Content {
        return TablesViewState.Content(
            worldName = latestWorldName,
            tables = latestTables,
            selectedTable = selected,
            lastRoll = lastRoll,
            editor = editor,
            pendingDelete = pendingDelete,
        )
    }

    private fun selectTable(tableId: String) {
        if (tableId.isEmpty() || selectedTableId == tableId) {
            return
        }
        selectedTableId = tableId
        lastRoll = null
        refreshContent()
    }

    private fun openCreateEditor() {
        when (val current = _state.value) {
            is TablesViewState.Empty -> {
                _state.value = current.copy(editor = createEditor())
            }
            is TablesViewState.Content -> {
                _state.value = current.copy(editor = createEditor())
            }
            TablesViewState.Loading, is TablesViewState.Error -> {
                openCreateOnNextLoad = true
            }
            TablesViewState.NoActiveWorld -> Unit
        }
    }

    private fun openEditEditor(tableId: String) {
        val table = latestTables.firstOrNull { it.id == tableId } ?: return
        val editor = TablesViewState.TableEditorState(
            tableId = table.id,
            name = table.name,
            notes = table.notes,
            rows = table.rows.map { row ->
                TablesViewState.EditorRow(
                    key = nextRowKey(),
                    id = row.id,
                    label = row.label,
                    weightText = row.weight.toString(),
                    nestedTableId = row.nestedTableId,
                    weightError = null,
                )
            }.ifEmpty { listOf(emptyRow()) },
            nestedOptions = nestedOptions(table.id),
            nameError = null,
            rowsError = null,
        )
        when (val current = _state.value) {
            is TablesViewState.Content -> _state.value = current.copy(editor = editor)
            is TablesViewState.Empty -> _state.value = current.copy(editor = editor)
            else -> Unit
        }
    }

    private fun createEditor(): TablesViewState.TableEditorState {
        return TablesViewState.TableEditorState(
            tableId = null,
            name = "",
            notes = "",
            rows = listOf(emptyRow(), emptyRow()),
            nestedOptions = nestedOptions(null),
            nameError = null,
            rowsError = null,
        )
    }

    private fun saveEditor() {
        val editor = editorFrom(_state.value) ?: return
        val parsed = parseRows(editor.rows)
        if (parsed == null) {
            updateEditor { current ->
                current?.copy(
                    rows = current.rows.map { row ->
                        val weight = row.weightText.trim().toIntOrNull()
                        row.copy(
                            weightError = if (row.label.isBlank() || (weight != null && weight >= 1)) {
                                null
                            } else {
                                "Weight must be 1 or more"
                            },
                        )
                    },
                    rowsError = "Add at least one labeled row with a weight of 1 or more",
                )
            }
            return
        }
        val draft = RandomTableDraft(
            name = editor.name,
            notes = editor.notes,
            rows = parsed,
        )
        appScope.scope.launch {
            val result = if (editor.tableId == null) {
                when (val created = createTable(draft)) {
                    is CreateRandomTableUseCase.Result.Created -> {
                        selectedTableId = created.table.id
                        SaveResult.Saved
                    }
                    CreateRandomTableUseCase.Result.InvalidName -> SaveResult.InvalidName
                    CreateRandomTableUseCase.Result.InvalidRows -> SaveResult.InvalidRows
                    CreateRandomTableUseCase.Result.DuplicateName -> SaveResult.DuplicateName
                    CreateRandomTableUseCase.Result.NoActiveWorld -> SaveResult.Failed
                }
            } else {
                when (updateTable(editor.tableId, draft)) {
                    UpdateRandomTableUseCase.Result.Updated -> SaveResult.Saved
                    UpdateRandomTableUseCase.Result.InvalidName -> SaveResult.InvalidName
                    UpdateRandomTableUseCase.Result.InvalidRows -> SaveResult.InvalidRows
                    UpdateRandomTableUseCase.Result.DuplicateName -> SaveResult.DuplicateName
                    UpdateRandomTableUseCase.Result.NotFound -> SaveResult.Failed
                }
            }
            when (result) {
                SaveResult.Saved -> updateEditor { null }
                SaveResult.InvalidName -> updateEditor { current ->
                    current?.copy(nameError = "Name is required")
                }
                SaveResult.DuplicateName -> updateEditor { current ->
                    current?.copy(nameError = "A table with that name already exists")
                }
                SaveResult.InvalidRows -> updateEditor { current ->
                    current?.copy(rowsError = "Add at least one labeled row with a weight of 1 or more")
                }
                SaveResult.Failed -> updateEditor { current ->
                    current?.copy(nameError = "Could not save that table")
                }
            }
        }
    }

    private fun parseRows(rows: List<TablesViewState.EditorRow>): List<RandomTableRowDraft>? {
        val parsed = rows.mapNotNull { row ->
            val label = row.label.trim()
            if (label.isEmpty()) {
                return@mapNotNull null
            }
            val weight = row.weightText.trim().toIntOrNull() ?: return null
            if (weight < 1) {
                return null
            }
            RandomTableRowDraft(
                id = row.id,
                label = label,
                weight = weight,
                nestedTableId = row.nestedTableId,
            )
        }
        return parsed.takeIf { it.isNotEmpty() }
    }

    private fun requestDelete(tableId: String) {
        val table = latestTables.firstOrNull { it.id == tableId } ?: return
        updatePendingDelete(
            TablesViewState.PendingDelete(
                tableId = table.id,
                tableName = table.name,
            )
        )
    }

    private fun confirmDelete() {
        val pending = pendingDeleteFrom(_state.value) ?: return
        appScope.scope.launch {
            when (deleteTable(pending.tableId)) {
                DeleteRandomTableUseCase.Result.Deleted -> {
                    if (selectedTableId == pending.tableId) {
                        selectedTableId = null
                    }
                    if (lastRoll?.tableId == pending.tableId) {
                        lastRoll = null
                    }
                    updatePendingDelete(null)
                }
                DeleteRandomTableUseCase.Result.NotFound -> updatePendingDelete(null)
            }
        }
    }

    private fun roll(tableId: String) {
        appScope.scope.launch {
            when (val result = rollTable(tableId)) {
                is RollRandomTableUseCase.Result.Rolled -> {
                    lastRoll = result.roll
                    selectedTableId = tableId
                    refreshContent()
                }
                RollRandomTableUseCase.Result.Empty,
                RollRandomTableUseCase.Result.NotFound,
                -> Unit
            }
        }
    }

    private fun nestedOptions(excludeId: String?): List<TablesViewState.NestedOption> {
        return latestTables
            .filter { table -> table.id != excludeId }
            .map { table ->
                TablesViewState.NestedOption(tableId = table.id, name = table.name)
            }
    }

    private fun emptyRow(): TablesViewState.EditorRow {
        return TablesViewState.EditorRow(
            key = nextRowKey(),
            id = null,
            label = "",
            weightText = "1",
            nestedTableId = null,
            weightError = null,
        )
    }

    private fun nextRowKey(): String {
        rowKeySequence += 1
        return "row-$rowKeySequence"
    }

    private fun selectedId(interaction: TablesInteraction): String {
        return when (interaction) {
            is TablesInteraction.TableSelected -> interaction.tableId
            is TablesInteraction.TableOpened -> interaction.tableId
            else -> ""
        }
    }

    private fun selectedFrom(tables: List<RandomTable>): RandomTable? {
        return selectedTableId?.let { id -> tables.firstOrNull { it.id == id } }
    }

    private fun editorFrom(state: TablesViewState): TablesViewState.TableEditorState? {
        return when (state) {
            is TablesViewState.Empty -> state.editor
            is TablesViewState.Content -> state.editor
            else -> null
        }
    }

    private fun pendingDeleteFrom(state: TablesViewState): TablesViewState.PendingDelete? {
        return (state as? TablesViewState.Content)?.pendingDelete
    }

    private fun updateEditor(
        transform: (TablesViewState.TableEditorState?) -> TablesViewState.TableEditorState?,
    ) {
        when (val current = _state.value) {
            is TablesViewState.Empty -> {
                _state.value = current.copy(editor = transform(current.editor))
            }
            is TablesViewState.Content -> {
                _state.value = current.copy(editor = transform(current.editor))
            }
            else -> Unit
        }
    }

    private fun updatePendingDelete(pending: TablesViewState.PendingDelete?) {
        val current = _state.value as? TablesViewState.Content ?: return
        _state.value = current.copy(pendingDelete = pending)
    }

    private enum class SaveResult {
        Saved,
        InvalidName,
        InvalidRows,
        DuplicateName,
        Failed,
    }
}
