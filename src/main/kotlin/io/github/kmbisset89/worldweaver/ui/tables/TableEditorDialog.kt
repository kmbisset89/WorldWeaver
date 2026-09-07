package io.github.kmbisset89.worldweaver.ui.tables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kmbisset89.worldweaver.ui.components.ActionIconButtonComposeWidget
import io.github.kmbisset89.worldweaver.ui.theme.ErrorRed

@Composable
internal fun TableEditorDialog(
    editor: TablesViewState.TableEditorState,
    onInteraction: (TablesInteraction) -> Unit,
) {
    val isCreate = editor.tableId == null
    AlertDialog(
        onDismissRequest = { onInteraction(TablesInteraction.EditorDismissed) },
        title = {
            Text(if (isCreate) "New table" else "Edit table")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = editor.name,
                    onValueChange = { onInteraction(TablesInteraction.EditorNameChanged(it)) },
                    label = { Text("Name") },
                    isError = editor.nameError != null,
                    supportingText = editor.nameError?.let { error ->
                        { Text(error) }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editor.notes,
                    onValueChange = { onInteraction(TablesInteraction.EditorNotesChanged(it)) },
                    label = { Text("Notes") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                editor.rowsError?.let { error ->
                    Text(error)
                }
                editor.rows.forEach { row ->
                    EditorRowFields(
                        row = row,
                        nestedOptions = editor.nestedOptions,
                        onInteraction = onInteraction,
                    )
                }
                TextButton(onClick = { onInteraction(TablesInteraction.EditorRowAdded) }) {
                    Text("Add row")
                }
            }
        },
        confirmButton = {
            ActionIconButtonComposeWidget(
                icon = Icons.Default.Save,
                tooltip = "Save",
                onClick = { onInteraction(TablesInteraction.EditorSaved) },
            )
        },
        dismissButton = {
            TextButton(onClick = { onInteraction(TablesInteraction.EditorDismissed) }) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun EditorRowFields(
    row: TablesViewState.EditorRow,
    nestedOptions: List<TablesViewState.NestedOption>,
    onInteraction: (TablesInteraction) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = row.label,
                onValueChange = {
                    onInteraction(TablesInteraction.EditorRowLabelChanged(row.key, it))
                },
                label = { Text("Result") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = row.weightText,
                onValueChange = {
                    onInteraction(TablesInteraction.EditorRowWeightChanged(row.key, it))
                },
                label = { Text("Weight") },
                isError = row.weightError != null,
                supportingText = row.weightError?.let { error ->
                    { Text(error) }
                },
                singleLine = true,
                modifier = Modifier.weight(0.4f)
            )
            ActionIconButtonComposeWidget(
                icon = Icons.Default.Close,
                tooltip = "Remove",
                tint = ErrorRed,
                onClick = { onInteraction(TablesInteraction.EditorRowRemoved(row.key)) },
            )
        }
        if (nestedOptions.isNotEmpty()) {
            TextButton(
                onClick = {
                    onInteraction(TablesInteraction.EditorRowNestedSelected(row.key, null))
                }
            ) {
                Text(if (row.nestedTableId == null) "• No nested table" else "No nested table")
            }
            nestedOptions.forEach { option ->
                TextButton(
                    onClick = {
                        onInteraction(
                            TablesInteraction.EditorRowNestedSelected(row.key, option.tableId)
                        )
                    }
                ) {
                    Text(
                        if (row.nestedTableId == option.tableId) {
                            "• ${option.name}"
                        } else {
                            option.name
                        }
                    )
                }
            }
        }
    }
}
