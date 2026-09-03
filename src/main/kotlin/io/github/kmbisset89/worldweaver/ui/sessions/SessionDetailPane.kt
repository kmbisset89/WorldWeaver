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
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun SessionDetailPane(
    session: Session,
    dateLabel: String?,
    checklist: SessionsViewState.ChecklistState,
    linkedQuests: List<SessionsViewState.LinkedQuest>,
    threads: List<PlotThread>,
    docs: List<ReferenceDoc>,
    personOptions: List<SessionsViewState.PersonOption>,
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
            Text(text = session.notes, fontSize = 14.sp, color = TextPrimary)
        }
        if (session.recap.isNotBlank()) {
            Text(
                text = "What changed",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
            )
            Text(text = session.recap, fontSize = 14.sp, color = TextPrimary)
        }
        ChecklistSection(checklist = checklist)
        LinkedQuestsSection(linkedQuests = linkedQuests, onInteraction = onInteraction)
        ScenesSection(session = session, onInteraction = onInteraction)
        ThreadsSection(sessionId = session.id, threads = threads, onInteraction = onInteraction)
        DocsSection(sessionId = session.id, docs = docs, onInteraction = onInteraction)
        MarchOrderSection(
            session = session,
            personOptions = personOptions,
            onInteraction = onInteraction,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(onClick = { onInteraction(SessionsInteraction.GeneratorOpened) }) {
                Text("Save NPC draft")
            }
            TextButton(onClick = { onInteraction(SessionsInteraction.EditSessionSelected(session.id)) }) {
                Text("Edit")
            }
            TextButton(onClick = { onInteraction(SessionsInteraction.DeleteSessionSelected(session.id)) }) {
                Text("Delete")
            }
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
            OutlinedTextField(
                value = scene.notes,
                onValueChange = {
                    onInteraction(SessionsInteraction.SceneNotesChanged(index, it))
                },
                label = { Text("Notes") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    onClick = { onInteraction(SessionsInteraction.SceneMoved(index, -1)) },
                    enabled = index > 0,
                ) {
                    Text("Up")
                }
                TextButton(
                    onClick = { onInteraction(SessionsInteraction.SceneMoved(index, 1)) },
                    enabled = index < session.scenes.lastIndex,
                ) {
                    Text("Down")
                }
                TextButton(onClick = { onInteraction(SessionsInteraction.SceneRemoved(index)) }) {
                    Text("Remove")
                }
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
                    TextButton(
                        onClick = { onInteraction(SessionsInteraction.ThreadEditSelected(thread.id)) }
                    ) {
                        Text("Edit")
                    }
                    TextButton(
                        onClick = { onInteraction(SessionsInteraction.ThreadDeleteSelected(thread.id)) }
                    ) {
                        Text("Delete")
                    }
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
                    TextButton(
                        onClick = { onInteraction(SessionsInteraction.DocEditSelected(doc.id)) }
                    ) {
                        Text("Edit")
                    }
                    TextButton(
                        onClick = { onInteraction(SessionsInteraction.DocDeleteSelected(doc.id)) }
                    ) {
                        Text("Delete")
                    }
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
                    TextButton(
                        onClick = { onInteraction(SessionsInteraction.MarchEntryMoved(index, -1)) },
                        enabled = index > 0,
                    ) {
                        Text("Up")
                    }
                    TextButton(
                        onClick = { onInteraction(SessionsInteraction.MarchEntryMoved(index, 1)) },
                        enabled = index < session.marchOrder.lastIndex,
                    ) {
                        Text("Down")
                    }
                    TextButton(
                        onClick = { onInteraction(SessionsInteraction.MarchEntryRemoved(index)) }
                    ) {
                        Text("Remove")
                    }
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
