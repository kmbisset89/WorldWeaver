package net.tactware.worldweaver.ui.lore

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
import net.tactware.worldweaver.domain.LoreCategory

@Composable
internal fun LoreEditorDialog(
    editor: LoreViewState.LoreEditorState,
    onInteraction: (LoreInteraction) -> Unit,
) {
    val isCreate = editor.loreId == null
    AlertDialog(
        onDismissRequest = { onInteraction(LoreInteraction.EditorDismissed) },
        title = {
            Text(if (isCreate) "New lore" else "Edit lore")
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
                    onValueChange = { onInteraction(LoreInteraction.EditorTitleChanged(it)) },
                    label = { Text("Title") },
                    isError = editor.titleError != null,
                    supportingText = editor.titleError?.let { error ->
                        { Text(error) }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editor.content,
                    onValueChange = { onInteraction(LoreInteraction.EditorContentChanged(it)) },
                    label = { Text("Content") },
                    isError = editor.contentError != null,
                    supportingText = editor.contentError?.let { error ->
                        { Text(error) }
                    },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Category")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LoreCategory.entries.forEach { category ->
                        FilterChip(
                            selected = editor.category == category,
                            onClick = {
                                onInteraction(LoreInteraction.EditorCategorySelected(category))
                            },
                            label = { Text(category.displayName) },
                        )
                    }
                }
                OutlinedTextField(
                    value = editor.tagsText,
                    onValueChange = { onInteraction(LoreInteraction.EditorTagsChanged(it)) },
                    label = { Text("Tags (one per line)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                if (editor.relatedOptions.isNotEmpty()) {
                    Text("Related lore")
                    editor.relatedOptions.forEach { option ->
                        FilterChip(
                            selected = option.id in editor.relatedEntryIds,
                            onClick = {
                                onInteraction(LoreInteraction.EditorRelatedToggled(option.id))
                            },
                            label = { Text(option.title) },
                        )
                    }
                }
                Text("Attach to location")
                FilterChip(
                    selected = editor.locationId == null,
                    onClick = { onInteraction(LoreInteraction.EditorLocationSelected(null)) },
                    label = { Text("None") },
                )
                editor.locationOptions.forEach { location ->
                    FilterChip(
                        selected = editor.locationId == location.id,
                        onClick = {
                            onInteraction(LoreInteraction.EditorLocationSelected(location.id))
                        },
                        label = { Text(location.name) },
                    )
                }
                Text("Attach to character")
                FilterChip(
                    selected = editor.characterId == null,
                    onClick = { onInteraction(LoreInteraction.EditorCharacterSelected(null)) },
                    label = { Text("None") },
                )
                editor.characterOptions.forEach { option ->
                    FilterChip(
                        selected = editor.characterId == option.id,
                        onClick = {
                            onInteraction(LoreInteraction.EditorCharacterSelected(option.id))
                        },
                        label = { Text(option.name) },
                    )
                }
                Text("DM-only secrets")
                editor.secrets.forEachIndexed { index, secret ->
                    SecretEditor(
                        index = index,
                        secret = secret,
                        onInteraction = onInteraction,
                    )
                }
                TextButton(onClick = { onInteraction(LoreInteraction.EditorSecretAdded) }) {
                    Text("Add secret")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onInteraction(LoreInteraction.EditorSaved) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = { onInteraction(LoreInteraction.EditorDismissed) }) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SecretEditor(
    index: Int,
    secret: LoreViewState.SecretEditorState,
    onInteraction: (LoreInteraction) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = secret.title,
            onValueChange = {
                onInteraction(LoreInteraction.EditorSecretTitleChanged(index, it))
            },
            label = { Text("Secret title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = secret.secret,
            onValueChange = {
                onInteraction(LoreInteraction.EditorSecretBodyChanged(index, it))
            },
            label = { Text("Secret") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
        secret.hints.forEachIndexed { hintIndex, hint ->
            OutlinedTextField(
                value = hint.text,
                onValueChange = {
                    onInteraction(
                        LoreInteraction.EditorHintTextChanged(index, hintIndex, it)
                    )
                },
                label = { Text("Hint") },
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(
                onClick = {
                    onInteraction(LoreInteraction.EditorHintRemoved(index, hintIndex))
                }
            ) {
                Text("Remove hint")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { onInteraction(LoreInteraction.EditorHintAdded(index)) }) {
                Text("Add hint")
            }
            TextButton(onClick = { onInteraction(LoreInteraction.EditorSecretRemoved(index)) }) {
                Text("Remove secret")
            }
        }
    }
}
