package io.github.kmbisset89.worldweaver.ui.tables

import io.github.kmbisset89.worldweaver.domain.RandomTable
import io.github.kmbisset89.worldweaver.domain.RandomTableRoll

internal sealed class TablesViewState {
    data object Loading : TablesViewState()

    data class Error(
        val message: String,
        val canRetry: Boolean,
    ) : TablesViewState()

    data object NoActiveWorld : TablesViewState()

    data class Empty(
        val worldName: String,
        val editor: TableEditorState?,
    ) : TablesViewState()

    data class Content(
        val worldName: String,
        val tables: List<RandomTable>,
        val selectedTable: RandomTable?,
        val lastRoll: RandomTableRoll?,
        val editor: TableEditorState?,
        val pendingDelete: PendingDelete?,
    ) : TablesViewState()

    data class TableEditorState(
        val tableId: String?,
        val name: String,
        val notes: String,
        val rows: List<EditorRow>,
        val nestedOptions: List<NestedOption>,
        val nameError: String?,
        val rowsError: String?,
    )

    data class EditorRow(
        val key: String,
        val id: String?,
        val label: String,
        val weightText: String,
        val nestedTableId: String?,
        val weightError: String?,
    )

    data class NestedOption(
        val tableId: String,
        val name: String,
    )

    data class PendingDelete(
        val tableId: String,
        val tableName: String,
    )
}
