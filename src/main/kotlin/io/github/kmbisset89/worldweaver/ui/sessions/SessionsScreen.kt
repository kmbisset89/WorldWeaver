package io.github.kmbisset89.worldweaver.ui.sessions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PublicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.AbilityScoreMethod
import io.github.kmbisset89.worldweaver.domain.AbilityScores
import io.github.kmbisset89.worldweaver.domain.PlotThreadPriority
import io.github.kmbisset89.worldweaver.domain.PlotThreadStatus
import io.github.kmbisset89.worldweaver.domain.Session
import io.github.kmbisset89.worldweaver.domain.SessionNpcDraftDestination
import io.github.kmbisset89.worldweaver.ui.components.ConfirmDestructiveDialog
import io.github.kmbisset89.worldweaver.ui.components.FeatureEmptyState
import io.github.kmbisset89.worldweaver.ui.components.FeatureErrorState
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun SessionsScreen(
    viewState: SessionsViewState,
    onInteraction: (SessionsInteraction) -> Unit,
) {
    LaunchedEffect(Unit) {
        onInteraction(SessionsInteraction.ScreenStarted)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        when (viewState) {
            SessionsViewState.Loading -> {
                SessionsHeader(subtitle = "Campaign sessions", showCreate = false, onInteraction = onInteraction)
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is SessionsViewState.Error -> {
                SessionsHeader(subtitle = "Campaign sessions", showCreate = false, onInteraction = onInteraction)
                FeatureErrorState(
                    message = viewState.message,
                    canRetry = viewState.canRetry,
                    onRetry = { onInteraction(SessionsInteraction.RetrySelected) },
                )
            }
            SessionsViewState.NoActiveWorld -> {
                SessionsHeader(subtitle = "Select a world first", showCreate = false, onInteraction = onInteraction)
                FeatureEmptyState(
                    icon = Icons.Default.PublicOff,
                    title = "No active world",
                    message = "Create or select a world first so sessions have a setting.",
                    actionLabel = "Go to Worlds",
                    onAction = { onInteraction(SessionsInteraction.CreateWorldSelected) },
                )
            }
            SessionsViewState.NoActiveCampaign -> {
                SessionsHeader(subtitle = "Select a campaign first", showCreate = false, onInteraction = onInteraction)
                FeatureEmptyState(
                    icon = Icons.Default.Flag,
                    title = "No active campaign",
                    message = "Create or select a campaign to record sessions.",
                    actionLabel = "Go to Campaigns",
                    onAction = { onInteraction(SessionsInteraction.CreateCampaignSelected) },
                )
            }
            is SessionsViewState.Empty -> {
                SessionsHeader(
                    subtitle = "${viewState.campaignName} · ${viewState.worldName}",
                    showCreate = true,
                    onInteraction = onInteraction,
                )
                FeatureEmptyState(
                    icon = Icons.Default.Event,
                    title = "No sessions yet",
                    message = "Create the first session for this campaign.",
                    actionLabel = "New session",
                    onAction = { onInteraction(SessionsInteraction.NewSessionSelected) },
                )
                viewState.editor?.let { editor ->
                    SessionEditorDialog(editor = editor, onInteraction = onInteraction)
                }
            }
            is SessionsViewState.Content -> {
                SessionsHeader(
                    subtitle = "${viewState.campaignName} · ${viewState.worldName}",
                    showCreate = true,
                    onInteraction = onInteraction,
                )
                SessionsContent(state = viewState, onInteraction = onInteraction)
            }
        }
    }
}

@Composable
private fun SessionsHeader(
    subtitle: String,
    showCreate: Boolean,
    onInteraction: (SessionsInteraction) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Sessions",
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
        if (showCreate) {
            Button(
                onClick = { onInteraction(SessionsInteraction.NewSessionSelected) },
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text("New session")
            }
        }
    }
}

@Composable
private fun SessionsContent(
    state: SessionsViewState.Content,
    onInteraction: (SessionsInteraction) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.width(320.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.sessions, key = { it.id }) { session ->
                SessionRow(
                    session = session,
                    dateLabel = state.sessionDateLabels[session.id],
                    isSelected = session.id == state.selectedSession?.id,
                    onInteraction = onInteraction,
                )
            }
        }

        if (state.selectedSession != null) {
            SessionDetailPane(
                session = state.selectedSession,
                dateLabel = state.selectedDateLabel,
                checklist = state.checklist,
                linkedQuests = state.linkedQuests,
                threads = state.threads,
                docs = state.docs,
                personOptions = state.personOptions,
                onInteraction = onInteraction,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        } else {
            Text(
                text = "Select a session to prepare it.",
                color = TextSecondary,
                modifier = Modifier.weight(1f).padding(16.dp)
            )
        }
    }

    state.editor?.let { editor ->
        SessionEditorDialog(editor = editor, onInteraction = onInteraction)
    }
    state.threadEditor?.let { editor ->
        ThreadEditorDialog(editor = editor, onInteraction = onInteraction)
    }
    state.docEditor?.let { editor ->
        DocEditorDialog(editor = editor, onInteraction = onInteraction)
    }
    state.generator?.let { generator ->
        SessionNpcDialog(generator = generator, onInteraction = onInteraction)
    }
    state.pendingDelete?.let { pending ->
        ConfirmDestructiveDialog(
            title = "Delete session?",
            message = "Delete “${pending.sessionName}”? Scene plans and march order for this session will be removed.",
            confirmLabel = "Delete",
            onConfirm = { onInteraction(SessionsInteraction.DeleteConfirmed) },
            onDismiss = { onInteraction(SessionsInteraction.DeleteCancelled) },
        )
    }
    state.pendingThreadDelete?.let { pending ->
        ConfirmDestructiveDialog(
            title = "Delete plot thread?",
            message = "Delete “${pending.title}”?",
            confirmLabel = "Delete",
            onConfirm = { onInteraction(SessionsInteraction.ThreadDeleteConfirmed) },
            onDismiss = { onInteraction(SessionsInteraction.ThreadDeleteCancelled) },
        )
    }
    state.pendingDocDelete?.let { pending ->
        ConfirmDestructiveDialog(
            title = "Delete reference doc?",
            message = "Delete “${pending.title}”?",
            confirmLabel = "Delete",
            onConfirm = { onInteraction(SessionsInteraction.DocDeleteConfirmed) },
            onDismiss = { onInteraction(SessionsInteraction.DocDeleteCancelled) },
        )
    }
}

@Composable
private fun SessionRow(
    session: Session,
    dateLabel: String?,
    isSelected: Boolean,
    onInteraction: (SessionsInteraction) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInteraction(SessionsInteraction.SessionSelected(session.id)) },
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
                text = session.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            if (dateLabel != null) {
                Text(
                    text = dateLabel,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (session.notes.isNotBlank()) {
                Text(
                    text = session.notes.lineSequence().first(),
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ThreadEditorDialog(
    editor: SessionsViewState.ThreadEditorState,
    onInteraction: (SessionsInteraction) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onInteraction(SessionsInteraction.ThreadEditorDismissed) },
        title = { Text(if (editor.threadId == null) "New plot thread" else "Edit plot thread") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = editor.title,
                    onValueChange = { onInteraction(SessionsInteraction.ThreadTitleChanged(it)) },
                    label = { Text("Title") },
                    isError = editor.titleError != null,
                    supportingText = editor.titleError?.let { error -> { Text(error) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editor.details,
                    onValueChange = { onInteraction(SessionsInteraction.ThreadDetailsChanged(it)) },
                    label = { Text("Details") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Status")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PlotThreadStatus.entries.forEach { status ->
                        FilterChip(
                            selected = editor.status == status,
                            onClick = { onInteraction(SessionsInteraction.ThreadStatusSelected(status)) },
                            label = { Text(status.displayName) },
                        )
                    }
                }
                Text("Priority")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PlotThreadPriority.entries.forEach { priority ->
                        FilterChip(
                            selected = editor.priority == priority,
                            onClick = {
                                onInteraction(SessionsInteraction.ThreadPrioritySelected(priority))
                            },
                            label = { Text(priority.displayName) },
                        )
                    }
                }
                FilterChip(
                    selected = editor.attachToSession,
                    onClick = {
                        onInteraction(SessionsInteraction.ThreadAttachToggled(!editor.attachToSession))
                    },
                    label = { Text("Attach to this session") },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onInteraction(SessionsInteraction.ThreadSaved) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = { onInteraction(SessionsInteraction.ThreadEditorDismissed) }) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DocEditorDialog(
    editor: SessionsViewState.DocEditorState,
    onInteraction: (SessionsInteraction) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onInteraction(SessionsInteraction.DocEditorDismissed) },
        title = { Text(if (editor.docId == null) "New reference" else "Edit reference") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = editor.title,
                    onValueChange = { onInteraction(SessionsInteraction.DocTitleChanged(it)) },
                    label = { Text("Title") },
                    isError = editor.titleError != null,
                    supportingText = editor.titleError?.let { error -> { Text(error) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editor.pathOrUrl,
                    onValueChange = { onInteraction(SessionsInteraction.DocPathChanged(it)) },
                    label = { Text("Path or URL") },
                    isError = editor.pathError != null,
                    supportingText = editor.pathError?.let { error -> { Text(error) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                FilterChip(
                    selected = editor.attachToSession,
                    onClick = {
                        onInteraction(SessionsInteraction.DocAttachToggled(!editor.attachToSession))
                    },
                    label = { Text("Attach to this session") },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onInteraction(SessionsInteraction.DocSaved) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = { onInteraction(SessionsInteraction.DocEditorDismissed) }) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SessionNpcDialog(
    generator: SessionsViewState.GeneratorState,
    onInteraction: (SessionsInteraction) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onInteraction(SessionsInteraction.GeneratorDismissed) },
        title = { Text("Save NPC draft") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Roll method")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AbilityScoreMethod.entries.forEach { method ->
                        FilterChip(
                            selected = generator.method == method,
                            onClick = {
                                onInteraction(SessionsInteraction.GeneratorMethodSelected(method))
                            },
                            label = { Text(method.displayName) },
                        )
                    }
                }
                Text("Save to")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SessionNpcDraftDestination.entries.forEach { destination ->
                        FilterChip(
                            selected = generator.destination == destination,
                            onClick = {
                                onInteraction(SessionsInteraction.GeneratorDestinationSelected(destination))
                            },
                            label = {
                                Text(
                                    if (destination == SessionNpcDraftDestination.WorldLibrary) {
                                        "World library"
                                    } else {
                                        "Campaign only"
                                    }
                                )
                            },
                        )
                    }
                }
                val draft = generator.draft
                if (draft == null) {
                    Text("Roll a draft, then save it into the world library or as a campaign-only NPC.")
                } else {
                    Text("${draft.name} · ${draft.race}")
                    Text(scoreSummary(draft.abilityScores))
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = { onInteraction(SessionsInteraction.GeneratorRolled) }) {
                    Text(if (generator.draft == null) "Roll" else "Reroll")
                }
                if (generator.draft != null) {
                    TextButton(onClick = { onInteraction(SessionsInteraction.GeneratorSaved) }) {
                        Text("Save")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { onInteraction(SessionsInteraction.GeneratorDismissed) }) {
                Text("Cancel")
            }
        }
    )
}

private fun scoreSummary(scores: AbilityScores): String {
    return listOf(
        "STR ${scores.strength}",
        "DEX ${scores.dexterity}",
        "CON ${scores.constitution}",
        "INT ${scores.intelligence}",
        "WIS ${scores.wisdom}",
        "CHA ${scores.charisma}",
    ).joinToString("   ")
}
