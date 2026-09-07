package io.github.kmbisset89.worldweaver.ui.tables

internal sealed interface TablesInteraction {
    data object ScreenStarted : TablesInteraction
    data object RetrySelected : TablesInteraction
    data object CreateWorldSelected : TablesInteraction
    data object NewTableSelected : TablesInteraction
    data class TableSelected(val tableId: String) : TablesInteraction
    data class TableOpened(val tableId: String) : TablesInteraction
    data class EditTableSelected(val tableId: String) : TablesInteraction
    data class DeleteTableSelected(val tableId: String) : TablesInteraction
    data object DeleteConfirmed : TablesInteraction
    data object DeleteCancelled : TablesInteraction
    data class RollSelected(val tableId: String) : TablesInteraction
    data class EditorNameChanged(val name: String) : TablesInteraction
    data class EditorNotesChanged(val notes: String) : TablesInteraction
    data object EditorRowAdded : TablesInteraction
    data class EditorRowRemoved(val key: String) : TablesInteraction
    data class EditorRowLabelChanged(val key: String, val label: String) : TablesInteraction
    data class EditorRowWeightChanged(val key: String, val weight: String) : TablesInteraction
    data class EditorRowNestedSelected(val key: String, val nestedTableId: String?) : TablesInteraction
    data object EditorSaved : TablesInteraction
    data object EditorDismissed : TablesInteraction
}
