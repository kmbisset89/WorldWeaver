package net.tactware.worldweaver.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.tactware.worldweaver.ui.theme.SurfaceCard
import net.tactware.worldweaver.ui.theme.TextPrimary
import net.tactware.worldweaver.ui.theme.TextSecondary

@Composable
internal fun SettingsScreen(
    viewState: SettingsViewState,
    onInteraction: (SettingsInteraction) -> Unit,
) {
    LaunchedEffect(Unit) {
        onInteraction(SettingsInteraction.ScreenStarted)
    }
    when (viewState) {
        is SettingsViewState.Content -> SettingsContent(state = viewState)
    }
}

@Composable
private fun SettingsContent(
    state: SettingsViewState.Content,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Settings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Appearance and local profile",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }

        item {
            SettingsCard(
                title = "Appearance",
                rows = listOf("Theme" to state.themeLabel)
            )
        }

        item {
            SettingsCard(
                title = "Local profile",
                rows = listOf(
                    "Name" to state.displayName,
                    "Email" to state.email,
                )
            )
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    rows: List<Pair<String, String>>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            rows.forEachIndexed { index, (label, value) ->
                if (index > 0) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label, fontSize = 14.sp, color = TextSecondary)
                    Text(text = value, fontSize = 14.sp, color = TextPrimary)
                }
            }
        }
    }
}
