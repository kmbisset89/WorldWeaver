package io.github.kmbisset89.worldweaver.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PublicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.ui.components.FeatureEmptyState
import io.github.kmbisset89.worldweaver.ui.components.FeatureErrorState
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        TextButton(
                            onClick = { onInteraction(CalendarInteraction.MonthMoved(index, -1)) },
                            enabled = index > 0,
                        ) {
                            Text("Up")
                        }
                        TextButton(
                            onClick = { onInteraction(CalendarInteraction.MonthMoved(index, 1)) },
                            enabled = index < state.months.lastIndex,
                        ) {
                            Text("Down")
                        }
                        TextButton(
                            onClick = { onInteraction(CalendarInteraction.MonthRemoved(index)) },
                            enabled = state.months.size > 1 &&
                                (month.id.isEmpty() || month.id !in state.referencedMonthIds),
                        ) {
                            Text("Remove")
                        }
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
                        TextButton(
                            onClick = { onInteraction(CalendarInteraction.WeekdayMoved(index, -1)) },
                            enabled = index > 0,
                        ) {
                            Text("Up")
                        }
                        TextButton(
                            onClick = { onInteraction(CalendarInteraction.WeekdayMoved(index, 1)) },
                            enabled = index < state.weekdays.lastIndex,
                        ) {
                            Text("Down")
                        }
                        TextButton(
                            onClick = { onInteraction(CalendarInteraction.WeekdayRemoved(index)) },
                        ) {
                            Text("Remove")
                        }
                    }
                }
            }
        }
        state.saveError?.let { error ->
            Text(text = error, color = TextSecondary, fontSize = 13.sp)
        }
        Button(
            onClick = { onInteraction(CalendarInteraction.Saved) },
            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
        ) {
            Text("Save calendar")
        }
        Spacer(modifier = Modifier.padding(bottom = 8.dp))
    }
}
