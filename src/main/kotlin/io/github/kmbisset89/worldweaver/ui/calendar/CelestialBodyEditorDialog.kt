package io.github.kmbisset89.worldweaver.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.CelestialBodyKind
import io.github.kmbisset89.worldweaver.ui.components.ActionIconButtonComposeWidget
import io.github.kmbisset89.worldweaver.ui.theme.ErrorRed
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun CelestialBodyEditorDialog(
    editor: CalendarViewState.CelestialBodyEditorState,
    onInteraction: (CalendarInteraction) -> Unit,
) {
    val isCreate = editor.bodyId == null
    AlertDialog(
        onDismissRequest = { onInteraction(CalendarInteraction.BodyEditorDismissed) },
        title = {
            Text(if (isCreate) "New celestial body" else "Edit celestial body")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = editor.name,
                    onValueChange = { onInteraction(CalendarInteraction.BodyEditorNameChanged(it)) },
                    label = { Text("Name") },
                    isError = editor.nameError != null,
                    supportingText = editor.nameError?.let { error ->
                        { Text(error) }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Kind", color = TextSecondary, fontSize = 13.sp)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CelestialBodyKind.entries.forEach { kind ->
                            FilterChip(
                                selected = editor.kind == kind,
                                onClick = {
                                    onInteraction(CalendarInteraction.BodyEditorKindSelected(kind))
                                },
                                label = { Text(kind.displayName) },
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editor.periodDaysText,
                        onValueChange = {
                            onInteraction(CalendarInteraction.BodyEditorPeriodChanged(it))
                        },
                        label = { Text("Period (days)") },
                        isError = editor.periodError != null,
                        supportingText = editor.periodError?.let { error ->
                            { Text(error) }
                        },
                        singleLine = true,
                        modifier = Modifier.width(160.dp)
                    )
                    OutlinedTextField(
                        value = editor.epochOffsetDaysText,
                        onValueChange = {
                            onInteraction(CalendarInteraction.BodyEditorOffsetChanged(it))
                        },
                        label = { Text("Offset") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = "Offset is how far into the cycle this body is on year 1, day 1 of the first month.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                )
                OutlinedTextField(
                    value = editor.notes,
                    onValueChange = { onInteraction(CalendarInteraction.BodyEditorNotesChanged(it)) },
                    label = { Text("Notes") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                editor.saveError?.let { error ->
                    Text(text = error, color = ErrorRed, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            ActionIconButtonComposeWidget(
                icon = Icons.Default.Save,
                tooltip = "Save",
                onClick = { onInteraction(CalendarInteraction.BodyEditorSaved) },
            )
        },
        dismissButton = {
            TextButton(onClick = { onInteraction(CalendarInteraction.BodyEditorDismissed) }) {
                Text("Cancel")
            }
        },
    )
}
