package io.github.kmbisset89.worldweaver.ui.run

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.SearchHit
import io.github.kmbisset89.worldweaver.domain.SessionReferencePeek
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun RunLookupComposeWidget(
    query: String,
    results: List<SearchHit>,
    peek: SessionReferencePeek?,
    onInteraction: (RunInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    RunCardComposeWidget(title = "Lookup", modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = { onInteraction(RunInteraction.LookupQueryChanged(it)) },
            label = { Text("People, locations, lore") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        if (query.trim().length >= 2 && peek == null) {
            if (results.isEmpty()) {
                Text("No matching records.", fontSize = 13.sp, color = TextSecondary)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(results, key = { "${it.kind}-${it.id}" }) { hit ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onInteraction(RunInteraction.LookupResultSelected(hit))
                                },
                        ) {
                            Text(
                                text = hit.title,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                fontSize = 13.sp,
                            )
                            Text(
                                text = hit.kind.displayName,
                                fontSize = 12.sp,
                                color = TextSecondary,
                            )
                            if (hit.snippet.isNotBlank()) {
                                Text(text = hit.snippet, fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
        peek?.let { current ->
            PeekBody(peek = current)
            OutlinedButton(onClick = { onInteraction(RunInteraction.LookupOpened) }) {
                Text("Open")
            }
            TextButton(onClick = { onInteraction(RunInteraction.LookupPeekDismissed) }) {
                Text("Close peek")
            }
        }
    }
}

@Composable
private fun PeekBody(peek: SessionReferencePeek) {
    Text(
        text = peek.title,
        fontWeight = FontWeight.SemiBold,
        color = TextPrimary,
        fontSize = 15.sp,
    )
    when (peek) {
        is SessionReferencePeek.Location -> {
            Text("${peek.typeLabel}", fontSize = 12.sp, color = NavyBlue)
            if (peek.description.isNotBlank()) {
                Text(peek.description, fontSize = 13.sp, color = TextPrimary)
            }
            if (peek.campaignNotes.isNotBlank()) {
                Text("Campaign notes", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Text(peek.campaignNotes, fontSize = 13.sp, color = TextPrimary)
            }
        }
        is SessionReferencePeek.Lore -> {
            Text(peek.categoryLabel, fontSize = 12.sp, color = NavyBlue)
            if (peek.content.isNotBlank()) {
                Text(peek.content, fontSize = 13.sp, color = TextPrimary)
            }
            peek.secrets.forEach { secret ->
                Text(secret.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Text(secret.secret, fontSize = 13.sp, color = TextPrimary)
            }
        }
        is SessionReferencePeek.Person -> {
            Text(peek.kindLabel, fontSize = 12.sp, color = NavyBlue)
            if (peek.description.isNotBlank()) {
                Text(peek.description, fontSize = 13.sp, color = TextPrimary)
            }
            if (peek.campaignNotes.isNotBlank()) {
                Text("Campaign notes", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Text(peek.campaignNotes, fontSize = 13.sp, color = TextPrimary)
            }
        }
    }
}
