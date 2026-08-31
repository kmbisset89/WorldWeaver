package net.tactware.worldweaver.ui.locations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.tactware.worldweaver.domain.LocationType

@Composable
internal fun LocationEditorDialog(
    editor: LocationsViewState.LocationEditorState,
    onInteraction: (LocationsInteraction) -> Unit,
) {
    val isCreate = editor.locationId == null
    AlertDialog(
        onDismissRequest = { onInteraction(LocationsInteraction.EditorDismissed) },
        title = {
            Text(if (isCreate) "New location" else "Edit location")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Type")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LocationType.entries.forEach { type ->
                        FilterChip(
                            selected = editor.type == type,
                            onClick = { onInteraction(LocationsInteraction.EditorTypeSelected(type)) },
                            label = { Text(type.displayName) },
                        )
                    }
                }
                val requiredParent = editor.type.requiredParentType()
                if (requiredParent != null) {
                    Text("Parent ${requiredParent.displayName.lowercase()}")
                    if (editor.parentOptions.isEmpty()) {
                        Text("Create a ${requiredParent.displayName.lowercase()} first.")
                    } else {
                        editor.parentOptions.forEach { parent ->
                            FilterChip(
                                selected = editor.parentLocationId == parent.id,
                                onClick = {
                                    onInteraction(LocationsInteraction.EditorParentSelected(parent.id))
                                },
                                label = { Text(parent.name) },
                            )
                        }
                    }
                    editor.parentError?.let { error ->
                        Text(error)
                    }
                }
                OutlinedTextField(
                    value = editor.name,
                    onValueChange = { onInteraction(LocationsInteraction.EditorNameChanged(it)) },
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
                        onInteraction(LocationsInteraction.EditorDescriptionChanged(it))
                    },
                    label = { Text("Description") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editor.climate,
                    onValueChange = { onInteraction(LocationsInteraction.EditorClimateChanged(it)) },
                    label = { Text("Climate") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editor.terrain,
                    onValueChange = { onInteraction(LocationsInteraction.EditorTerrainChanged(it)) },
                    label = { Text("Terrain") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editor.government,
                    onValueChange = {
                        onInteraction(LocationsInteraction.EditorGovernmentChanged(it))
                    },
                    label = { Text("Government") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editor.landmarksText,
                    onValueChange = {
                        onInteraction(LocationsInteraction.EditorLandmarksChanged(it))
                    },
                    label = { Text("Landmarks (one per line)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editor.history,
                    onValueChange = { onInteraction(LocationsInteraction.EditorHistoryChanged(it)) },
                    label = { Text("History") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editor.notes,
                    onValueChange = { onInteraction(LocationsInteraction.EditorNotesChanged(it)) },
                    label = { Text("World notes") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onInteraction(LocationsInteraction.EditorSaved) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = { onInteraction(LocationsInteraction.EditorDismissed) }) {
                Text("Cancel")
            }
        }
    )
}
