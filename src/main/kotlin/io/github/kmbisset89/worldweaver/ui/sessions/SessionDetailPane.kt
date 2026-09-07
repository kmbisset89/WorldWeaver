package io.github.kmbisset89.worldweaver.ui.sessions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.PlotThread
import io.github.kmbisset89.worldweaver.domain.ReferenceDoc
import io.github.kmbisset89.worldweaver.domain.Session
import io.github.kmbisset89.worldweaver.domain.WikilinkBacklink
import io.github.kmbisset89.worldweaver.domain.WikilinkDisplaySpan
import io.github.kmbisset89.worldweaver.domain.WikilinkTarget
import io.github.kmbisset89.worldweaver.ui.components.ActionIconButtonComposeWidget
import io.github.kmbisset89.worldweaver.ui.theme.ErrorRed
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary
import io.github.kmbisset89.worldweaver.ui.wikilink.WikilinkBacklinksComposeWidget
import io.github.kmbisset89.worldweaver.ui.wikilink.WikilinkBodyComposeWidget
import io.github.kmbisset89.worldweaver.ui.wikilink.WikilinkFieldComposeWidget

@Composable
internal fun SessionDetailPane(
    session: Session,
    dateLabel: String?,
    checklist: SessionsViewState.ChecklistState,
    linkedQuests: List<SessionsViewState.LinkedQuest>,
    threads: List<PlotThread>,
    docs: List<ReferenceDoc>,
    personOptions: List<SessionsViewState.PersonOption>,
    notesSpans: List<WikilinkDisplaySpan>,
    recapSpans: List<WikilinkDisplaySpan>,
    scratchSpans: List<WikilinkDisplaySpan>,
    wikilinkBacklinks: List<WikilinkBacklink>,
    sceneWikilinkSuggestions: List<WikilinkTarget>,
    sceneWikilinkIndex: Int?,
    onInteraction: (SessionsInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = session.name,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        if (dateLabel != null) {
            Text(text = dateLabel, fontSize = 14.sp, color = TextSecondary)
        }
        if (session.notes.isNotBlank()) {
            WikilinkBodyComposeWidget(
                spans = notesSpans,
                onTargetSelected = { target ->
                    onInteraction(SessionsInteraction.WikilinkSelected(target))
                },
            )
        }
        if (session.recap.isNotBlank()) {
            Text(
                text = "What changed",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
            )
            WikilinkBodyComposeWidget(
                spans = recapSpans,
                onTargetSelected = { target ->
                    onInteraction(SessionsInteraction.WikilinkSelected(target))
                },
            )
        }
        if (session.scratchNotes.isNotBlank()) {
            Text(
                text = "Scratch pad",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
            )
            WikilinkBodyComposeWidget(
                spans = scratchSpans,
                onTargetSelected = { target ->
                    onInteraction(SessionsInteraction.WikilinkSelected(target))
                },
            )
        }
        ChecklistSection(checklist = checklist)
        LinkedQuestsSection(linkedQuests = linkedQuests, onInteraction = onInteraction)
        ScenesSection(
            session = session,
            sceneWikilinkSuggestions = sceneWikilinkSuggestions,
            sceneWikilinkIndex = sceneWikilinkIndex,
            onInteraction = onInteraction,
        )
        ThreadsSection(sessionId = session.id, threads = threads, onInteraction = onInteraction)
        DocsSection(sessionId = session.id, docs = docs, onInteraction = onInteraction)
        MarchOrderSection(
            session = session,
            personOptions = personOptions,
            onInteraction = onInteraction,
        )
        WikilinkBacklinksComposeWidget(
            backlinks = wikilinkBacklinks,
            onBacklinkSelected = { backlink ->
                onInteraction(SessionsInteraction.BacklinkSelected(backlink))
            },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(onClick = { onInteraction(SessionsInteraction.GeneratorOpened) }) {
                Text("Save NPC draft")
            }
            ActionIconButtonComposeWidget(
                icon = Icons.Default.Edit,
                tooltip = "Edit",
                onClick = { onInteraction(SessionsInteraction.EditSessionSelected(session.id)) },
            )
            ActionIconButtonComposeWidget(
                icon = Icons.Default.Delete,
                tooltip = "Delete",
                tint = ErrorRed,
                onClick = { onInteraction(SessionsInteraction.DeleteSessionSelected(session.id)) },
            )
        }
    }
}

@Composable
private fun ChecklistSection(
    checklist: SessionsViewState.ChecklistState,
) {
    DetailCard(title = "Start-of-session checklist") {
        ChecklistLine(
            label = "Active quests",
            value = if (checklist.activeQuestTitles.isEmpty()) {
                ""
            } else {
                checklist.activeQuestTitles.joinToString(", ")
            },
            empty = "No active quests.",
        )
        ChecklistLine(
            label = "Last session recap",
            value = checklist.lastSessionRecap.orEmpty(),
            empty = "No previous session notes.",
        )
        ChecklistLine(
            label = "Party location",
            value = if (checklist.partyLocationNames.isEmpty()) {
                ""
            } else {
                checklist.partyLocationNames.joinToString(", ")
            },
            empty = "Party location is not marked.",
        )
    }
}

@Composable
private fun ChecklistLine(
    label: String,
    value: String,
    empty: String,
) {
    Text(
        text = label,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary,
        modifier = Modifier.padding(top = 8.dp)
    )
    Text(
        text = value.ifBlank { empty },
        fontSize = 13.sp,
        color = if (value.isBlank()) TextSecondary else TextPrimary,
    )
}

@Composable
private fun LinkedQuestsSection(
    linkedQuests: List<SessionsViewState.LinkedQuest>,
    onInteraction: (SessionsInteraction) -> Unit,
) {
    DetailCard(title = "Linked quests") {
        if (linkedQuests.isEmpty()) {
            Text(
                text = "No quests are linked to this session.",
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 6.dp)
            )
        } else {
            linkedQuests.forEach { quest ->
                Text(
                    text = quest.title,
                    fontSize = 13.sp,
                    color = NavyBlue,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .clickable {
                            onInteraction(SessionsInteraction.LinkedQuestSelected(quest.questId))
                        }
                )
            }
        }
    }
}

@Composable
private fun ScenesSection(
    session: Session,
    sceneWikilinkSuggestions: List<WikilinkTarget>,
    sceneWikilinkIndex: Int?,
    onInteraction: (SessionsInteraction) -> Unit,
) {
    DetailCard(title = "Scene plan") {
        session.scenes.forEachIndexed { index, scene ->
            OutlinedTextField(
                value = scene.title,
                onValueChange = {
                    onInteraction(SessionsInteraction.SceneTitleChanged(index, it))
                },
                label = { Text("Scene ${index + 1}") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            WikilinkFieldComposeWidget(
                value = scene.notes,
                onValueChange = {
                    onInteraction(SessionsInteraction.SceneNotesChanged(index, it))
                },
                suggestions = if (sceneWikilinkIndex == index) sceneWikilinkSuggestions else emptyList(),
                onSuggestionSelected = { target ->
                    onInteraction(SessionsInteraction.SceneNotesWikilinkSelected(index, target))
                },
                label = "Notes",
                minLines = 2,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ActionIconButtonComposeWidget(
                    icon = Icons.Default.KeyboardArrowUp,
                    tooltip = "Up",
                    enabled = index > 0,
                    onClick = { onInteraction(SessionsInteraction.SceneMoved(index, -1)) },
                )
                ActionIconButtonComposeWidget(
                    icon = Icons.Default.KeyboardArrowDown,
                    tooltip = "Down",
                    enabled = index < session.scenes.lastIndex,
                    onClick = { onInteraction(SessionsInteraction.SceneMoved(index, 1)) },
                )
                ActionIconButtonComposeWidget(
                    icon = Icons.Default.Close,
                    tooltip = "Remove",
                    tint = ErrorRed,
                    onClick = { onInteraction(SessionsInteraction.SceneRemoved(index)) },
                )
            }
        }
        TextButton(onClick = { onInteraction(SessionsInteraction.SceneAdded) }) {
            Text("Add scene")
        }
    }
}

@Composable
private fun ThreadsSection(
    sessionId: String,
    threads: List<PlotThread>,
    onInteraction: (SessionsInteraction) -> Unit,
) {
    DetailCard(title = "Plot threads") {
        if (threads.isEmpty()) {
            Text(
                text = "No plot threads yet.",
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 6.dp)
            )
        } else {
            threads.forEach { thread ->
                val attachment = if (thread.sessionId == sessionId) {
                    "This session"
                } else if (thread.sessionId == null) {
                    "Campaign"
                } else {
                    "Another session"
                }
                Text(
                    text = thread.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "${thread.status.displayName} · ${thread.priority.displayName} · $attachment",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                if (thread.details.isNotBlank()) {
                    Text(text = thread.details, fontSize = 13.sp, color = TextPrimary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ActionIconButtonComposeWidget(
                        icon = Icons.Default.Edit,
                        tooltip = "Edit",
                        onClick = { onInteraction(SessionsInteraction.ThreadEditSelected(thread.id)) },
                    )
                    ActionIconButtonComposeWidget(
                        icon = Icons.Default.Delete,
                        tooltip = "Delete",
                        tint = ErrorRed,
                        onClick = { onInteraction(SessionsInteraction.ThreadDeleteSelected(thread.id)) },
                    )
                }
            }
        }
        TextButton(onClick = { onInteraction(SessionsInteraction.ThreadEditorOpened) }) {
            Text("Add plot thread")
        }
    }
}

@Composable
private fun DocsSection(
    sessionId: String,
    docs: List<ReferenceDoc>,
    onInteraction: (SessionsInteraction) -> Unit,
) {
    DetailCard(title = "Reference docs") {
        if (docs.isEmpty()) {
            Text(
                text = "No reference docs yet.",
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 6.dp)
            )
        } else {
            docs.forEach { doc ->
                val scope = if (doc.sessionId == sessionId) {
                    "This session"
                } else if (doc.sessionId == null) {
                    "Campaign"
                } else {
                    "Another session"
                }
                Text(
                    text = doc.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "${doc.pathOrUrl} · $scope",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ActionIconButtonComposeWidget(
                        icon = Icons.Default.Edit,
                        tooltip = "Edit",
                        onClick = { onInteraction(SessionsInteraction.DocEditSelected(doc.id)) },
                    )
                    ActionIconButtonComposeWidget(
                        icon = Icons.Default.Delete,
                        tooltip = "Delete",
                        tint = ErrorRed,
                        onClick = { onInteraction(SessionsInteraction.DocDeleteSelected(doc.id)) },
                    )
                }
            }
        }
        TextButton(onClick = { onInteraction(SessionsInteraction.DocEditorOpened) }) {
            Text("Add reference")
        }
    }
}

@Composable
private fun MarchOrderSection(
    session: Session,
    personOptions: List<SessionsViewState.PersonOption>,
    onInteraction: (SessionsInteraction) -> Unit,
) {
    DetailCard(title = "March order") {
        if (session.marchOrder.isEmpty()) {
            Text(
                text = "No march order snapshot yet.",
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 6.dp)
            )
        } else {
            session.marchOrder.forEachIndexed { index, entry ->
                Text(
                    text = "${index + 1}. ${entry.displayName}",
                    fontSize = 13.sp,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ActionIconButtonComposeWidget(
                        icon = Icons.Default.KeyboardArrowUp,
                        tooltip = "Up",
                        enabled = index > 0,
                        onClick = { onInteraction(SessionsInteraction.MarchEntryMoved(index, -1)) },
                    )
                    ActionIconButtonComposeWidget(
                        icon = Icons.Default.KeyboardArrowDown,
                        tooltip = "Down",
                        enabled = index < session.marchOrder.lastIndex,
                        onClick = { onInteraction(SessionsInteraction.MarchEntryMoved(index, 1)) },
                    )
                    ActionIconButtonComposeWidget(
                        icon = Icons.Default.Close,
                        tooltip = "Remove",
                        tint = ErrorRed,
                        onClick = { onInteraction(SessionsInteraction.MarchEntryRemoved(index)) },
                    )
                }
            }
        }
        val available = personOptions.filter { option ->
            session.marchOrder.none { it.person == option.person }
        }
        if (available.isNotEmpty()) {
            Text(
                text = "Add to snapshot",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )
            available.forEach { option ->
                FilterChip(
                    selected = false,
                    onClick = { onInteraction(SessionsInteraction.MarchPersonAdded(option.person)) },
                    label = { Text(option.name) },
                )
            }
        }
    }
}

@Composable
private fun DetailCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            content()
        }
    }
}
