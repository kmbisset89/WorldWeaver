package io.github.kmbisset89.worldweaver.ui.run

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.WikilinkDisplaySpan
import io.github.kmbisset89.worldweaver.domain.WikilinkTarget
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary
import io.github.kmbisset89.worldweaver.ui.wikilink.WikilinkBodyComposeWidget
import io.github.kmbisset89.worldweaver.ui.wikilink.WikilinkFieldComposeWidget

@Composable
internal fun RunNotesComposeWidget(
    sessionNotes: String,
    scratchNotes: String,
    recap: String,
    recapSpans: List<WikilinkDisplaySpan>,
    notesSuggestions: List<WikilinkTarget>,
    scratchSuggestions: List<WikilinkTarget>,
    onInteraction: (RunInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        RunCardComposeWidget(title = "Session notes") {
            WikilinkFieldComposeWidget(
                value = sessionNotes,
                onValueChange = { onInteraction(RunInteraction.SessionNotesChanged(it)) },
                suggestions = notesSuggestions,
                onSuggestionSelected = { target ->
                    onInteraction(RunInteraction.SessionNotesWikilinkSelected(target))
                },
                label = "Prep notes",
                minLines = 3,
            )
        }
        RunCardComposeWidget(title = "Scratch pad") {
            WikilinkFieldComposeWidget(
                value = scratchNotes,
                onValueChange = { onInteraction(RunInteraction.ScratchNotesChanged(it)) },
                suggestions = scratchSuggestions,
                onSuggestionSelected = { target ->
                    onInteraction(RunInteraction.ScratchNotesWikilinkSelected(target))
                },
                label = "Table notes",
                minLines = 4,
            )
        }
        if (recap.isNotBlank()) {
            RunCardComposeWidget(title = "What changed") {
                WikilinkBodyComposeWidget(
                    spans = recapSpans.ifEmpty {
                        listOf(
                            WikilinkDisplaySpan(text = recap, target = null),
                        )
                    },
                    onTargetSelected = { target ->
                        onInteraction(RunInteraction.WikilinkSelected(target))
                    },
                )
            }
        } else {
            Text(
                text = "Recap is written when you close the session. Type [[ in notes to link people, places, and lore.",
                fontSize = 13.sp,
                color = TextSecondary,
            )
        }
    }
}
