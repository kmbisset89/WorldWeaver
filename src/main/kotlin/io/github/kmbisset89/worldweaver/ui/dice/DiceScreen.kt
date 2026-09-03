package io.github.kmbisset89.worldweaver.ui.dice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun DiceScreen(
    viewState: DiceViewState,
    onInteraction: (DiceInteraction) -> Unit,
) {
    LaunchedEffect(Unit) {
        onInteraction(DiceInteraction.ScreenStarted)
    }
    when (viewState) {
        is DiceViewState.Content -> DiceContent(
            state = viewState,
            onInteraction = onInteraction,
        )
    }
}

@Composable
private fun DiceContent(
    state: DiceViewState.Content,
    onInteraction: (DiceInteraction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                Text(
                    text = "Dice",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Text(
                    text = "Roll digital dice or log the faces on your table",
                    fontSize = 14.sp,
                    color = TextSecondary,
                )
            }
            if (state.isFloatingOpen) {
                OutlinedButton(
                    onClick = { onInteraction(DiceInteraction.FloatingClosed) },
                ) {
                    Text("Close window")
                }
            } else {
                Button(
                    onClick = { onInteraction(DiceInteraction.FloatingOpened) },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                    Text(
                        text = "Pop out",
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
        DiceTrayComposeWidget(
            state = state,
            onInteraction = onInteraction,
            contentPadding = 24.dp,
        )
    }
}
