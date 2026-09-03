package io.github.kmbisset89.worldweaver.ui.quests

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
import io.github.kmbisset89.worldweaver.domain.QuestObjectiveStatus
import io.github.kmbisset89.worldweaver.domain.QuestStatus

@Composable
internal fun QuestEditorDialog(
    editor: QuestsViewState.QuestEditorState,
    onInteraction: (QuestsInteraction) -> Unit,
) {
    val isCreate = editor.questId == null
    AlertDialog(
        onDismissRequest = { onInteraction(QuestsInteraction.EditorDismissed) },
        title = {
            Text(if (isCreate) "New quest" else "Edit quest")
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
                    value = editor.title,
                    onValueChange = { onInteraction(QuestsInteraction.EditorTitleChanged(it)) },
                    label = { Text("Title") },
                    isError = editor.titleError != null,
                    supportingText = editor.titleError?.let { error ->
                        { Text(error) }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editor.summary,
                    onValueChange = { onInteraction(QuestsInteraction.EditorSummaryChanged(it)) },
                    label = { Text("Summary") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Status")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuestStatus.entries.forEach { status ->
                        FilterChip(
                            selected = editor.status == status,
                            onClick = { onInteraction(QuestsInteraction.EditorStatusSelected(status)) },
                            label = { Text(status.displayName) },
                        )
                    }
                }
                Text("Linked location")
                FilterChip(
                    selected = editor.locationId == null,
                    onClick = { onInteraction(QuestsInteraction.EditorLocationSelected(null)) },
                    label = { Text("None") },
                )
                editor.locationOptions.forEach { location ->
                    FilterChip(
                        selected = editor.locationId == location.id,
                        onClick = {
                            onInteraction(QuestsInteraction.EditorLocationSelected(location.id))
                        },
                        label = { Text(location.name) },
                    )
                }
                Text("Objectives")
                editor.objectives.forEachIndexed { index, objective ->
                    OutlinedTextField(
                        value = objective.title,
                        onValueChange = {
                            onInteraction(QuestsInteraction.EditorObjectiveTitleChanged(index, it))
                        },
                        label = { Text("Objective ${index + 1}") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QuestObjectiveStatus.entries.forEach { status ->
                            FilterChip(
                                selected = objective.status == status,
                                onClick = {
                                    onInteraction(
                                        QuestsInteraction.EditorObjectiveStatusSelected(index, status)
                                    )
                                },
                                label = { Text(status.displayName) },
                            )
                        }
                        TextButton(
                            onClick = { onInteraction(QuestsInteraction.EditorObjectiveRemoved(index)) }
                        ) {
                            Text("Remove")
                        }
                    }
                }
                TextButton(onClick = { onInteraction(QuestsInteraction.EditorObjectiveAdded) }) {
                    Text("Add objective")
                }
                if (editor.loreOptions.isNotEmpty()) {
                    Text("Linked lore")
                    editor.loreOptions.forEach { option ->
                        FilterChip(
                            selected = option.id in editor.loreIds,
                            onClick = { onInteraction(QuestsInteraction.EditorLoreToggled(option.id)) },
                            label = { Text(option.title) },
                        )
                    }
                }
                if (editor.personOptions.isNotEmpty()) {
                    Text("Linked people")
                    editor.personOptions.forEach { option ->
                        val selected = if (option.worldOwned) {
                            option.id in editor.worldPersonIds
                        } else {
                            option.id in editor.campaignPersonIds
                        }
                        FilterChip(
                            selected = selected,
                            onClick = {
                                if (option.worldOwned) {
                                    onInteraction(QuestsInteraction.EditorWorldPersonToggled(option.id))
                                } else {
                                    onInteraction(
                                        QuestsInteraction.EditorCampaignPersonToggled(option.id)
                                    )
                                }
                            },
                            label = { Text(option.name) },
                        )
                    }
                }
                if (editor.sessionOptions.isNotEmpty()) {
                    Text("Linked sessions")
                    editor.sessionOptions.forEach { option ->
                        FilterChip(
                            selected = option.id in editor.sessionIds,
                            onClick = {
                                onInteraction(QuestsInteraction.EditorSessionToggled(option.id))
                            },
                            label = { Text(option.name) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onInteraction(QuestsInteraction.EditorSaved) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = { onInteraction(QuestsInteraction.EditorDismissed) }) {
                Text("Cancel")
            }
        }
    )
}
