package io.github.kmbisset89.worldweaver.ui.advancement

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun AdvancementPromptComposeWidget(
    prompt: AdvancementPrompt,
    onDismiss: () -> Unit,
    onAwardLevel: () -> Unit,
    onAmountChanged: (String) -> Unit,
    onAwardExperience: () -> Unit,
) {
    when (prompt) {
        AdvancementPrompt.AwardLevel -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Award a party level?") },
            text = {
                Text("Each player character gains one level. Spell slots and features are not updated automatically.")
            },
            confirmButton = {
                TextButton(onClick = onAwardLevel) {
                    Text("Award level")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Not now")
                }
            },
        )
        is AdvancementPrompt.AwardExperience -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Award party XP") },
            text = {
                OutlinedTextField(
                    value = prompt.amountText,
                    onValueChange = onAmountChanged,
                    label = { Text("XP for each PC") },
                    isError = prompt.amountError != null,
                    supportingText = prompt.amountError?.let { error ->
                        { Text(error) }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = onAwardExperience) {
                    Text("Award XP")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Not now")
                }
            },
        )
    }
}
