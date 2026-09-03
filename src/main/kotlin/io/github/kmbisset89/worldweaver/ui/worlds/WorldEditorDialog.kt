package io.github.kmbisset89.worldweaver.ui.worlds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kmbisset89.worldweaver.domain.GameSystem

@Composable
internal fun WorldEditorDialog(
    editor: WorldsViewState.WorldEditorState,
    onInteraction: (WorldsInteraction) -> Unit,
) {
    val isCreate = editor.worldId == null
    AlertDialog(
        onDismissRequest = { onInteraction(WorldsInteraction.EditorDismissed) },
        title = {
            Text(if (isCreate) "New world" else "Edit world")
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = editor.name,
                    onValueChange = { onInteraction(WorldsInteraction.EditorNameChanged(it)) },
                    label = { Text("Name") },
                    isError = editor.nameError != null,
                    supportingText = editor.nameError?.let { error ->
                        { Text(error) }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = editor.description,
                    onValueChange = { onInteraction(WorldsInteraction.EditorDescriptionChanged(it)) },
                    label = { Text("Description") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Game system")
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GameSystem.entries.forEach { system ->
                        FilterChip(
                            selected = editor.defaultGameSystem == system,
                            onClick = {
                                onInteraction(WorldsInteraction.EditorGameSystemSelected(system))
                            },
                            label = { Text(system.displayName) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onInteraction(WorldsInteraction.EditorSaved) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = { onInteraction(WorldsInteraction.EditorDismissed) }) {
                Text("Cancel")
            }
        }
    )
}
