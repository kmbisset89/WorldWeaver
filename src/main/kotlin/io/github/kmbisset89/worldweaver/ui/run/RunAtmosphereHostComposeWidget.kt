package io.github.kmbisset89.worldweaver.ui.run

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kmbisset89.worldweaver.ui.atmosphere.AtmosphereInteraction
import io.github.kmbisset89.worldweaver.ui.atmosphere.AtmosphereTrayComposeWidget
import io.github.kmbisset89.worldweaver.ui.atmosphere.AtmosphereViewState

@Composable
internal fun RunAtmosphereHostComposeWidget(
    atmosphereState: AtmosphereViewState.Content?,
    onAtmosphereInteraction: (AtmosphereInteraction) -> Unit,
    onPopOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RunCardComposeWidget(title = "Atmosphere", modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(onClick = onPopOut) {
                Text("Pop out")
            }
        }
        if (atmosphereState == null) {
            Text("Atmosphere is loading.")
        } else {
            AtmosphereTrayComposeWidget(
                state = atmosphereState,
                onInteraction = onAtmosphereInteraction,
                contentPadding = 0.dp,
                showLook = false,
            )
        }
    }
}
