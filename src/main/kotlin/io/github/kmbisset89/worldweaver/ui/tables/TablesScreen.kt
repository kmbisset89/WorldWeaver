package io.github.kmbisset89.worldweaver.ui.tables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.PublicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.RandomTable
import io.github.kmbisset89.worldweaver.ui.components.AdaptiveListDetailBreakpoint
import io.github.kmbisset89.worldweaver.ui.components.AdaptiveListDetailComposeWidget
import io.github.kmbisset89.worldweaver.ui.components.AdaptiveScreenHeaderComposeWidget
import io.github.kmbisset89.worldweaver.ui.components.ConfirmDestructiveDialog
import io.github.kmbisset89.worldweaver.ui.components.FeatureEmptyState
import io.github.kmbisset89.worldweaver.ui.components.FeatureErrorState
import io.github.kmbisset89.worldweaver.ui.components.rememberAdaptiveListOpen
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun TablesScreen(
    viewState: TablesViewState,
    onInteraction: (TablesInteraction) -> Unit,
) {
    LaunchedEffect(Unit) {
        onInteraction(TablesInteraction.ScreenStarted)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        when (viewState) {
            TablesViewState.Loading -> {
                TablesHeader(subtitle = "World tables", showCreate = false, onInteraction = onInteraction)
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is TablesViewState.Error -> {
                TablesHeader(subtitle = "World tables", showCreate = false, onInteraction = onInteraction)
                FeatureErrorState(
                    message = viewState.message,
                    canRetry = viewState.canRetry,
                    onRetry = { onInteraction(TablesInteraction.RetrySelected) },
                )
            }
            TablesViewState.NoActiveWorld -> {
                TablesHeader(subtitle = "Select a world first", showCreate = false, onInteraction = onInteraction)
                FeatureEmptyState(
                    icon = Icons.Default.PublicOff,
                    title = "No active world",
                    message = "Create or select a world first so tables have a setting.",
                    actionLabel = "Go to Worlds",
                    onAction = { onInteraction(TablesInteraction.CreateWorldSelected) },
                )
            }
            is TablesViewState.Empty -> {
                TablesHeader(subtitle = viewState.worldName, showCreate = true, onInteraction = onInteraction)
                FeatureEmptyState(
                    icon = Icons.Default.Casino,
                    title = "No tables yet",
                    message = "Create a weighted table for encounters, loot, rumors, or weather.",
                    actionLabel = "New table",
                    onAction = { onInteraction(TablesInteraction.NewTableSelected) },
                )
                viewState.editor?.let { editor ->
                    TableEditorDialog(editor = editor, onInteraction = onInteraction)
                }
            }
            is TablesViewState.Content -> {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val compact = maxWidth < AdaptiveListDetailBreakpoint
                    var listOpen by rememberAdaptiveListOpen(compact)
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        TablesHeader(
                            subtitle = viewState.worldName,
                            showCreate = true,
                            compact = compact,
                            selectedName = viewState.selectedTable?.name,
                            onListToggle = { listOpen = !listOpen },
                            onInteraction = onInteraction,
                        )
                        TablesContent(
                            state = viewState,
                            compact = compact,
                            listOpen = listOpen,
                            onListDismissed = { listOpen = false },
                            onInteraction = onInteraction,
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TablesHeader(
    subtitle: String,
    showCreate: Boolean,
    onInteraction: (TablesInteraction) -> Unit,
    compact: Boolean = false,
    selectedName: String? = null,
    onListToggle: () -> Unit = {},
) {
    AdaptiveScreenHeaderComposeWidget(
        title = "Tables",
        subtitle = subtitle,
        compact = compact,
        switcherName = selectedName,
        listLabel = "tables list",
        onListToggle = onListToggle,
    ) {
        if (showCreate) {
            Button(
                onClick = { onInteraction(TablesInteraction.NewTableSelected) },
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text("New table")
            }
        }
    }
}

@Composable
private fun TablesContent(
    state: TablesViewState.Content,
    compact: Boolean,
    listOpen: Boolean,
    onListDismissed: () -> Unit,
    onInteraction: (TablesInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listInteraction: (TablesInteraction) -> Unit = { interaction ->
        if (interaction is TablesInteraction.TableSelected) {
            onListDismissed()
        }
        onInteraction(interaction)
    }
    AdaptiveListDetailComposeWidget(
        compact = compact,
        listOpen = listOpen,
        hasSelection = state.selectedTable != null,
        listPane = { listModifier ->
            LazyColumn(
                modifier = listModifier,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.tables, key = { it.id }) { table ->
                    TableRow(
                        table = table,
                        isSelected = table.id == state.selectedTable?.id,
                        onInteraction = listInteraction,
                    )
                }
            }
        },
        detailPane = { detailModifier ->
            if (state.selectedTable != null) {
                TablesDetailPane(
                    table = state.selectedTable,
                    lastRoll = state.lastRoll,
                    onInteraction = onInteraction,
                    modifier = detailModifier,
                )
            } else {
                Text(
                    text = "Select a table to roll it.",
                    color = TextSecondary,
                    modifier = detailModifier.padding(16.dp)
                )
            }
        },
        onListDismissed = onListDismissed,
        dismissListLabel = "Dismiss tables list",
        modifier = modifier,
    )

    state.editor?.let { editor ->
        TableEditorDialog(editor = editor, onInteraction = onInteraction)
    }
    state.pendingDelete?.let { pending ->
        ConfirmDestructiveDialog(
            title = "Delete table?",
            message = "Delete “${pending.tableName}”? This cannot be undone.",
            confirmLabel = "Delete",
            onConfirm = { onInteraction(TablesInteraction.DeleteConfirmed) },
            onDismiss = { onInteraction(TablesInteraction.DeleteCancelled) },
        )
    }
}

@Composable
private fun TableRow(
    table: RandomTable,
    isSelected: Boolean,
    onInteraction: (TablesInteraction) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInteraction(TablesInteraction.TableSelected(table.id)) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isSelected) {
                        Modifier.background(NavyBlue.copy(alpha = 0.08f))
                    } else {
                        Modifier
                    }
                )
                .padding(14.dp)
        ) {
            Text(
                text = table.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "${table.rows.size} rows",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
