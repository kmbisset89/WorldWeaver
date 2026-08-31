package net.tactware.worldweaver.ui.characters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.tactware.worldweaver.domain.AbilityScoreMethod
import net.tactware.worldweaver.domain.AbilityScores

@Composable
internal fun RandomNpcDialog(
    generator: CharactersViewState.GeneratorState,
    onInteraction: (CharactersInteraction) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onInteraction(CharactersInteraction.GeneratorDismissed) },
        title = { Text("Random NPC") },
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
                                onInteraction(CharactersInteraction.GeneratorMethodSelected(method))
                            },
                            label = { Text(method.displayName) },
                        )
                    }
                }
                val draft = generator.draft
                if (draft == null) {
                    Text("Roll to preview a name, race, and ability scores. Save writes the NPC to the world library.")
                } else {
                    Text("${draft.name} · ${draft.race}")
                    Text(scoreSummary(draft.abilityScores))
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = { onInteraction(CharactersInteraction.GeneratorRolled) }) {
                    Text(if (generator.draft == null) "Roll" else "Reroll")
                }
                if (generator.draft != null) {
                    TextButton(onClick = { onInteraction(CharactersInteraction.GeneratorSaved) }) {
                        Text("Save to library")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { onInteraction(CharactersInteraction.GeneratorDismissed) }) {
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
