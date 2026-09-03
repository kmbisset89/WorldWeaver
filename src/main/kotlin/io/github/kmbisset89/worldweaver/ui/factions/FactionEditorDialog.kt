package io.github.kmbisset89.worldweaver.ui.factions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun FactionEditorDialog(
    editor: FactionsViewState.FactionEditorState,
    onInteraction: (FactionsInteraction) -> Unit,
) {
    val isCreate = editor.factionId == null
    AlertDialog(
        onDismissRequest = { onInteraction(FactionsInteraction.EditorDismissed) },
        title = {
            Text(if (isCreate) "New faction" else "Edit faction")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = editor.name,
                    onValueChange = { onInteraction(FactionsInteraction.EditorNameChanged(it)) },
                    label = { Text("Name") },
                    isError = editor.nameError != null,
                    supportingText = editor.nameError?.let { error ->
                        { Text(error) }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editor.description,
                    onValueChange = {
                        onInteraction(FactionsInteraction.EditorDescriptionChanged(it))
                    },
                    label = { Text("Description") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editor.goals,
                    onValueChange = { onInteraction(FactionsInteraction.EditorGoalsChanged(it)) },
                    label = { Text("Goals") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editor.notes,
                    onValueChange = { onInteraction(FactionsInteraction.EditorNotesChanged(it)) },
                    label = { Text("Notes") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onInteraction(FactionsInteraction.EditorSaved) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = { onInteraction(FactionsInteraction.EditorDismissed) }) {
                Text("Cancel")
            }
        },
    )
}
