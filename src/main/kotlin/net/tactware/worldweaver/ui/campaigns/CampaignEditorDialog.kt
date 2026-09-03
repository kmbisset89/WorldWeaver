package net.tactware.worldweaver.ui.campaigns

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
import net.tactware.worldweaver.domain.GameSystem

@Composable
internal fun CampaignEditorDialog(
    editor: CampaignsViewState.CampaignEditorState,
    worldName: String,
    onInteraction: (CampaignsInteraction) -> Unit,
) {
    val isCreate = editor.campaignId == null
    AlertDialog(
        onDismissRequest = { onInteraction(CampaignsInteraction.EditorDismissed) },
        title = {
            Text(if (isCreate) "New campaign" else "Edit campaign")
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("World: $worldName")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = editor.name,
                    onValueChange = { onInteraction(CampaignsInteraction.EditorNameChanged(it)) },
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
                    onValueChange = {
                        onInteraction(CampaignsInteraction.EditorDescriptionChanged(it))
                    },
                    label = { Text("Description") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = editor.notes,
                    onValueChange = { onInteraction(CampaignsInteraction.EditorNotesChanged(it)) },
                    label = { Text("Notes") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Mechanics")
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GameSystem.entries.forEach { system ->
                        FilterChip(
                            selected = editor.gameSystem == system,
                            onClick = {
                                onInteraction(CampaignsInteraction.EditorGameSystemSelected(system))
                            },
                            label = { Text(system.displayName) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onInteraction(CampaignsInteraction.EditorSaved) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = { onInteraction(CampaignsInteraction.EditorDismissed) }) {
                Text("Cancel")
            }
        }
    )
}
