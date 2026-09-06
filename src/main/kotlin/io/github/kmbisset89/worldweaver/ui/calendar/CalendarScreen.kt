package io.github.kmbisset89.worldweaver.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PublicOff
import androidx.compose.material.icons.filled.Save
import androidx.compose.foundation.clickable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import io.github.kmbisset89.worldweaver.ui.components.ActionIconButtonComposeWidget
import io.github.kmbisset89.worldweaver.ui.components.ConfirmDestructiveDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.ui.components.FeatureEmptyState
import io.github.kmbisset89.worldweaver.ui.components.FeatureErrorState
import io.github.kmbisset89.worldweaver.ui.theme.ErrorRed
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun CalendarScreen(
    viewState: CalendarViewState,
    onInteraction: (CalendarInteraction) -> Unit,
) {
    LaunchedEffect(Unit) {
        onInteraction(CalendarInteraction.ScreenStarted)
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        when (viewState) {
            CalendarViewState.Loading -> {
                CalendarHeader(subtitle = "World calendar")
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is CalendarViewState.Error -> {
                CalendarHeader(subtitle = "World calendar")
                FeatureErrorState(
                    message = viewState.message,
                    canRetry = viewState.canRetry,
                    onRetry = { onInteraction(CalendarInteraction.RetrySelected) },
                )
            }
            CalendarViewState.NoActiveWorld -> {
                CalendarHeader(subtitle = "Select a world first")
                FeatureEmptyState(
                    icon = Icons.Default.PublicOff,
                    title = "No active world",
                    message = "Create or select a world first so the calendar has a setting.",
                    actionLabel = "Go to Worlds",
                    onAction = { onInteraction(CalendarInteraction.CreateWorldSelected) },
                )
            }
            is CalendarViewState.Content -> {
                CalendarHeader(subtitle = viewState.worldName)
                CalendarContent(state = viewState, onInteraction = onInteraction)
            }
        }
    }
}

@Composable
private fun CalendarHeader(subtitle: String) {
    Column {
        Text(
            text = "Calendar",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun CalendarContent(
    state: CalendarViewState.Content,
    onInteraction: (CalendarInteraction) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Era suffix", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                OutlinedTextField(
                    value = state.eraSuffix,
                    onValueChange = { onInteraction(CalendarInteraction.EraSuffixChanged(it)) },
                    label = { Text("Era") },
                    placeholder = { Text("DR") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Current world date", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = state.currentYear,
                        onValueChange = { onInteraction(CalendarInteraction.CurrentYearChanged(it)) },
                        label = { Text("Year") },
                        singleLine = true,
                        modifier = Modifier.width(120.dp)
                    )
                    OutlinedTextField(
                        value = state.currentDay,
                        onValueChange = { onInteraction(CalendarInteraction.CurrentDayChanged(it)) },
                        label = { Text("Day") },
                        singleLine = true,
                        modifier = Modifier.width(100.dp)
                    )
                    TextButton(onClick = { onInteraction(CalendarInteraction.CurrentDateCleared) }) {
                        Text("Clear")
                    }
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.months.forEach { month ->
                        FilterChip(
                            selected = state.currentMonthId == month.id,
                            onClick = {
                                onInteraction(CalendarInteraction.CurrentMonthSelected(month.id))
                            },
                            label = { Text(month.name.ifBlank { "Month" }) },
                            enabled = month.id.isNotEmpty() || month.name.isNotBlank(),
                        )
                    }
                }
                state.currentDateError?.let { error ->
                    Text(text = error, color = TextSecondary, fontSize = 13.sp)
                }
                Text(
                    text = state.preview?.let { "Preview: $it" } ?: "No current date set.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                )
                if (state.todayObservances.isNotEmpty()) {
                    Text("Today", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        state.todayObservances.forEach { observance ->
                            FilterChip(
                                selected = observance.id == state.selectedObservanceId,
                                onClick = {
                                    onInteraction(CalendarInteraction.ObservanceSelected(observance.id))
                                },
                                label = { Text("${observance.name} · ${observance.kindLabel}") },
                            )
                        }
                    }
                }
                if (state.todaySky.isNotEmpty()) {
                    Text("Today’s sky", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        state.todaySky.forEach { body ->
                            FilterChip(
                                selected = body.id == state.selectedCelestialBodyId,
                                onClick = {
                                    onInteraction(CalendarInteraction.CelestialBodySelected(body.id))
                                },
                                label = { Text("${body.name} · ${body.appearanceLabel}") },
                            )
                        }
                    }
                }
            }
        }
        ObservancesCard(state = state, onInteraction = onInteraction)
        CelestialBodiesCard(state = state, onInteraction = onInteraction)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Months", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    TextButton(onClick = { onInteraction(CalendarInteraction.MonthAdded) }) {
                        Text("Add month")
                    }
                }
                state.monthsError?.let { error ->
                    Text(text = error, color = TextSecondary, fontSize = 13.sp)
                }
                state.months.forEachIndexed { index, month ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = month.name,
                            onValueChange = {
                                onInteraction(CalendarInteraction.MonthNameChanged(index, it))
                            },
                            label = { Text("Name") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = month.daysText,
                            onValueChange = {
                                onInteraction(CalendarInteraction.MonthDaysChanged(index, it))
                            },
                            label = { Text("Days") },
                            singleLine = true,
                            modifier = Modifier.width(88.dp)
                        )
                        ActionIconButtonComposeWidget(
                            icon = Icons.Default.KeyboardArrowUp,
                            tooltip = "Up",
                            enabled = index > 0,
                            onClick = { onInteraction(CalendarInteraction.MonthMoved(index, -1)) },
                        )
                        ActionIconButtonComposeWidget(
                            icon = Icons.Default.KeyboardArrowDown,
                            tooltip = "Down",
                            enabled = index < state.months.lastIndex,
                            onClick = { onInteraction(CalendarInteraction.MonthMoved(index, 1)) },
                        )
                        ActionIconButtonComposeWidget(
                            icon = Icons.Default.Close,
                            tooltip = "Remove",
                            tint = ErrorRed,
                            enabled = state.months.size > 1 &&
                                (month.id.isEmpty() || month.id !in state.referencedMonthIds),
                            onClick = { onInteraction(CalendarInteraction.MonthRemoved(index)) },
                        )
                    }
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Weekdays", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    TextButton(onClick = { onInteraction(CalendarInteraction.WeekdayAdded) }) {
                        Text("Add weekday")
                    }
                }
                state.weekdaysError?.let { error ->
                    Text(text = error, color = TextSecondary, fontSize = 13.sp)
                }
                state.weekdays.forEachIndexed { index, weekday ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = weekday.name,
                            onValueChange = {
                                onInteraction(CalendarInteraction.WeekdayNameChanged(index, it))
                            },
                            label = { Text("Name") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        ActionIconButtonComposeWidget(
                            icon = Icons.Default.KeyboardArrowUp,
                            tooltip = "Up",
                            enabled = index > 0,
                            onClick = { onInteraction(CalendarInteraction.WeekdayMoved(index, -1)) },
                        )
                        ActionIconButtonComposeWidget(
                            icon = Icons.Default.KeyboardArrowDown,
                            tooltip = "Down",
                            enabled = index < state.weekdays.lastIndex,
                            onClick = { onInteraction(CalendarInteraction.WeekdayMoved(index, 1)) },
                        )
                        ActionIconButtonComposeWidget(
                            icon = Icons.Default.Close,
                            tooltip = "Remove",
                            tint = ErrorRed,
                            onClick = { onInteraction(CalendarInteraction.WeekdayRemoved(index)) },
                        )
                    }
                }
            }
        }
        state.saveError?.let { error ->
            Text(text = error, color = TextSecondary, fontSize = 13.sp)
        }
        ActionIconButtonComposeWidget(
            icon = Icons.Default.Save,
            tooltip = "Save calendar",
            filled = true,
            onClick = { onInteraction(CalendarInteraction.Saved) },
        )
        Spacer(modifier = Modifier.padding(bottom = 8.dp))
        state.editor?.let { editor ->
            ObservanceEditorDialog(
                editor = editor,
                months = state.months,
                onInteraction = onInteraction,
            )
        }
        state.bodyEditor?.let { editor ->
            CelestialBodyEditorDialog(
                editor = editor,
                onInteraction = onInteraction,
            )
        }
        state.pendingDelete?.let { pending ->
            ConfirmDestructiveDialog(
                title = "Delete this day?",
                message = "Delete “${pending.name}”? Lore links to this day will be removed.",
                confirmLabel = "Delete",
                onConfirm = { onInteraction(CalendarInteraction.DeleteConfirmed) },
                onDismiss = { onInteraction(CalendarInteraction.DeleteCancelled) },
            )
        }
        state.pendingBodyDelete?.let { pending ->
            ConfirmDestructiveDialog(
                title = "Delete this body?",
                message = "Delete “${pending.name}”? The sky on this calendar will no longer include it.",
                confirmLabel = "Delete",
                onConfirm = { onInteraction(CalendarInteraction.DeleteCelestialBodyConfirmed) },
                onDismiss = { onInteraction(CalendarInteraction.DeleteCelestialBodyCancelled) },
            )
        }
    }
}

@Composable
private fun ObservancesCard(
    state: CalendarViewState.Content,
    onInteraction: (CalendarInteraction) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Holidays and important days", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                TextButton(onClick = { onInteraction(CalendarInteraction.NewObservanceSelected) }) {
                    Text("Add day")
                }
            }
            if (state.observances.isEmpty()) {
                Text(
                    text = "Add festivals, holy days, and dated events for this world’s calendar.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                )
            }
            state.observances.forEach { observance ->
                ObservanceRow(
                    observance = observance,
                    selected = observance.id == state.selectedObservanceId,
                    onInteraction = onInteraction,
                )
            }
        }
    }
}

@Composable
private fun ObservanceRow(
    observance: CalendarViewState.ObservanceLine,
    selected: Boolean,
    onInteraction: (CalendarInteraction) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInteraction(CalendarInteraction.ObservanceSelected(observance.id)) },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) SurfaceCard.copy(alpha = 0.65f) else SurfaceCard,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 2.dp else 0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = observance.name,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                    )
                    Text(
                        text = "${observance.dateLabel} · ${observance.kindLabel}",
                        color = TextSecondary,
                        fontSize = 13.sp,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ActionIconButtonComposeWidget(
                        icon = Icons.Default.Edit,
                        tooltip = "Edit",
                        onClick = { onInteraction(CalendarInteraction.EditObservanceSelected(observance.id)) },
                    )
                    ActionIconButtonComposeWidget(
                        icon = Icons.Default.Delete,
                        tooltip = "Delete",
                        tint = ErrorRed,
                        onClick = {
                            onInteraction(CalendarInteraction.DeleteObservanceSelected(observance.id))
                        },
                    )
                }
            }
            if (observance.notes.isNotBlank()) {
                Text(text = observance.notes, color = TextSecondary, fontSize = 13.sp)
            }
            if (observance.loreLinks.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    observance.loreLinks.forEach { link ->
                        FilterChip(
                            selected = false,
                            onClick = {
                                onInteraction(CalendarInteraction.LinkedLoreSelected(link.loreId))
                            },
                            label = { Text(link.title) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CelestialBodiesCard(
    state: CalendarViewState.Content,
    onInteraction: (CalendarInteraction) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Celestial bodies", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                TextButton(onClick = { onInteraction(CalendarInteraction.NewCelestialBodySelected) }) {
                    Text("Add body")
                }
            }
            if (state.celestialBodies.isEmpty()) {
                Text(
                    text = "Add a sun, moon, or planet with a cycle length so the calendar can show tonight’s sky.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                )
            }
            state.celestialBodies.forEachIndexed { index, body ->
                CelestialBodyRow(
                    body = body,
                    selected = body.id == state.selectedCelestialBodyId,
                    canMoveUp = index > 0,
                    canMoveDown = index < state.celestialBodies.lastIndex,
                    onInteraction = onInteraction,
                )
            }
        }
    }
}

@Composable
private fun CelestialBodyRow(
    body: CalendarViewState.CelestialBodyLine,
    selected: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onInteraction: (CalendarInteraction) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInteraction(CalendarInteraction.CelestialBodySelected(body.id)) },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) SurfaceCard.copy(alpha = 0.65f) else SurfaceCard,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 2.dp else 0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = body.name,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                    )
                    Text(
                        text = "${body.kindLabel} · ${body.cycleLabel}",
                        color = TextSecondary,
                        fontSize = 13.sp,
                    )
                    body.appearanceLabel?.let { appearance ->
                        Text(
                            text = appearance,
                            color = TextSecondary,
                            fontSize = 13.sp,
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ActionIconButtonComposeWidget(
                        icon = Icons.Default.KeyboardArrowUp,
                        tooltip = "Up",
                        enabled = canMoveUp,
                        onClick = {
                            onInteraction(CalendarInteraction.CelestialBodyMoved(body.id, -1))
                        },
                    )
                    ActionIconButtonComposeWidget(
                        icon = Icons.Default.KeyboardArrowDown,
                        tooltip = "Down",
                        enabled = canMoveDown,
                        onClick = {
                            onInteraction(CalendarInteraction.CelestialBodyMoved(body.id, 1))
                        },
                    )
                    ActionIconButtonComposeWidget(
                        icon = Icons.Default.Edit,
                        tooltip = "Edit",
                        onClick = {
                            onInteraction(CalendarInteraction.EditCelestialBodySelected(body.id))
                        },
                    )
                    ActionIconButtonComposeWidget(
                        icon = Icons.Default.Delete,
                        tooltip = "Delete",
                        tint = ErrorRed,
                        onClick = {
                            onInteraction(CalendarInteraction.DeleteCelestialBodySelected(body.id))
                        },
                    )
                }
            }
            if (body.notes.isNotBlank()) {
                Text(text = body.notes, color = TextSecondary, fontSize = 13.sp)
            }
        }
    }
}
