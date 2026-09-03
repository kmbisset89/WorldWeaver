package io.github.kmbisset89.worldweaver.ui.sessions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun SessionEditorDialog(
    editor: SessionsViewState.SessionEditorState,
    onInteraction: (SessionsInteraction) -> Unit,
) {
    val isCreate = editor.sessionId == null
    AlertDialog(
        onDismissRequest = { onInteraction(SessionsInteraction.EditorDismissed) },
        title = {
            Text(if (isCreate) "New session" else "Edit session")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = editor.name,
                    onValueChange = { onInteraction(SessionsInteraction.EditorNameChanged(it)) },
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
                    onValueChange = { onInteraction(SessionsInteraction.EditorNotesChanged(it)) },
                    label = { Text("Notes") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                if (editor.months.isEmpty()) {
                    Text("Set months on the Calendar screen to stamp an in-world date.")
                } else {
                    OutlinedTextField(
                        value = editor.yearText,
                        onValueChange = { onInteraction(SessionsInteraction.EditorYearChanged(it)) },
                        label = { Text("Year") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editor.dayText,
                        onValueChange = { onInteraction(SessionsInteraction.EditorDayChanged(it)) },
                        label = { Text("Day") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    editor.months.forEach { month ->
                        TextButton(
                            onClick = { onInteraction(SessionsInteraction.EditorMonthSelected(month.id)) }
                        ) {
                            Text(
                                if (editor.monthId == month.id) "• ${month.name}" else month.name
                            )
                        }
                    }
                    TextButton(onClick = { onInteraction(SessionsInteraction.EditorDateCleared) }) {
                        Text("Clear date")
                    }
                    editor.datePreview?.let { preview ->
                        Text(preview)
                    }
                    editor.dateError?.let { error ->
                        Text(error)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onInteraction(SessionsInteraction.EditorSaved) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = { onInteraction(SessionsInteraction.EditorDismissed) }) {
                Text("Cancel")
            }
        }
    )
}
