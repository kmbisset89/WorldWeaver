package io.github.kmbisset89.worldweaver.ui.run

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun RunNotesComposeWidget(
    sessionNotes: String,
    scratchNotes: String,
    recap: String,
    onInteraction: (RunInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        RunCardComposeWidget(title = "Session notes") {
            OutlinedTextField(
                value = sessionNotes,
                onValueChange = { onInteraction(RunInteraction.SessionNotesChanged(it)) },
                label = { Text("Prep notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )
        }
        RunCardComposeWidget(title = "Scratch pad") {
            OutlinedTextField(
                value = scratchNotes,
                onValueChange = { onInteraction(RunInteraction.ScratchNotesChanged(it)) },
                label = { Text("Table notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
            )
        }
        if (recap.isNotBlank()) {
            RunCardComposeWidget(title = "What changed") {
                Text(text = recap, fontSize = 13.sp, color = TextPrimary)
            }
        } else {
            Text(
                text = "Recap is written when you close the session.",
                fontSize = 13.sp,
                color = TextSecondary,
            )
        }
    }
}
