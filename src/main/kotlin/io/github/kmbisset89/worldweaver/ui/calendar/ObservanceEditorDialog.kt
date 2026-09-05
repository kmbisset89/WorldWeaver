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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.WorldCalendarObservanceKind
import io.github.kmbisset89.worldweaver.ui.theme.ErrorRed
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun ObservanceEditorDialog(
    editor: CalendarViewState.ObservanceEditorState,
    months: List<CalendarViewState.MonthEditor>,
    onInteraction: (CalendarInteraction) -> Unit,
) {
    val isCreate = editor.observanceId == null
    AlertDialog(
        onDismissRequest = { onInteraction(CalendarInteraction.EditorDismissed) },
        title = {
            Text(if (isCreate) "New holiday or important day" else "Edit holiday or important day")
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
                    onValueChange = { onInteraction(CalendarInteraction.EditorNameChanged(it)) },
                    label = { Text("Name") },
                    isError = editor.nameError != null,
                    supportingText = editor.nameError?.let { error ->
                        { Text(error) }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                EditorSection("Kind") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        WorldCalendarObservanceKind.entries.forEach { kind ->
                            FilterChip(
                                selected = editor.kind == kind,
                                onClick = { onInteraction(CalendarInteraction.EditorKindSelected(kind)) },
                                label = { Text(kind.displayName) },
                            )
                        }
                    }
                }
                EditorSection("Month") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        months.filter { it.id.isNotEmpty() || it.name.isNotBlank() }.forEach { month ->
                            FilterChip(
                                selected = editor.monthId == month.id,
                                onClick = {
                                    onInteraction(CalendarInteraction.EditorMonthSelected(month.id))
                                },
                                label = { Text(month.name.ifBlank { "Month" }) },
                                enabled = month.id.isNotEmpty(),
                            )
                        }
                    }
                }
                EditorSection("Date") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editor.dayText,
                            onValueChange = { onInteraction(CalendarInteraction.EditorDayChanged(it)) },
                            label = { Text("Day") },
                            singleLine = true,
                            modifier = Modifier.width(100.dp)
                        )
                        OutlinedTextField(
                            value = editor.yearText,
                            onValueChange = { onInteraction(CalendarInteraction.EditorYearChanged(it)) },
                            label = { Text("Year") },
                            placeholder = { Text("Every year") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Text(
                        text = "Leave year empty for a day that repeats every year.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                    )
                    editor.dateError?.let { error ->
                        Text(text = error, color = ErrorRed, fontSize = 13.sp)
                    }
                }
                OutlinedTextField(
                    value = editor.notes,
                    onValueChange = { onInteraction(CalendarInteraction.EditorNotesChanged(it)) },
                    label = { Text("Notes") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                if (editor.loreOptions.isNotEmpty()) {
                    EditorSection("Linked lore") {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            editor.loreOptions.forEach { option ->
                                FilterChip(
                                    selected = option.id in editor.loreIds,
                                    onClick = {
                                        onInteraction(CalendarInteraction.EditorLoreToggled(option.id))
                                    },
                                    label = { Text(option.title) },
                                )
                            }
                        }
                    }
                }
                editor.saveError?.let { error ->
                    Text(text = error, color = ErrorRed, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onInteraction(CalendarInteraction.EditorSaved) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = { onInteraction(CalendarInteraction.EditorDismissed) }) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun EditorSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = title, color = TextSecondary, fontSize = 13.sp)
        content()
    }
}
