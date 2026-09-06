package io.github.kmbisset89.worldweaver.ui.atmosphere

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.ui.components.ActionIconButtonComposeWidget
import io.github.kmbisset89.worldweaver.ui.theme.ErrorRed
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun AtmosphereScreen(
    viewState: AtmosphereViewState,
    onInteraction: (AtmosphereInteraction) -> Unit,
) {
    LaunchedEffect(Unit) {
        onInteraction(AtmosphereInteraction.ScreenStarted)
    }
    when (viewState) {
        is AtmosphereViewState.Content -> AtmosphereContent(
            state = viewState,
            onInteraction = onInteraction,
        )
    }
}

@Composable
private fun AtmosphereContent(
    state: AtmosphereViewState.Content,
    onInteraction: (AtmosphereInteraction) -> Unit,
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
                    text = "Atmosphere",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Text(
                    text = "Trigger Home Assistant, Philips Hue, and Govee lighting from one tray",
                    fontSize = 14.sp,
                    color = TextSecondary,
                )
            }
            if (state.isFloatingOpen) {
                OutlinedButton(
                    onClick = { onInteraction(AtmosphereInteraction.FloatingClosed) },
                ) {
                    Text("Close window")
                }
            } else {
                Button(
                    onClick = { onInteraction(AtmosphereInteraction.FloatingOpened) },
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                AtmosphereCard(title = "Home Assistant") {
                    Text(
                        text = "Use a local URL and a long-lived access token. Scenes you create in Home Assistant can set lights and speakers together.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.draftBaseUrl,
                        onValueChange = { onInteraction(AtmosphereInteraction.BaseUrlChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Base URL") },
                        placeholder = { Text("http://homeassistant.local:8123") },
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.draftToken,
                        onValueChange = { onInteraction(AtmosphereInteraction.TokenChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Long-lived access token") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionIconButtonComposeWidget(
                            icon = Icons.Default.Save,
                            tooltip = "Save connection",
                            filled = true,
                            enabled = state.isConnectionDirty,
                            onClick = { onInteraction(AtmosphereInteraction.ConnectionSaveSelected) },
                        )
                        OutlinedButton(
                            onClick = { onInteraction(AtmosphereInteraction.ConnectionTestSelected) },
                            enabled = !state.isTestingConnection,
                        ) {
                            Text(if (state.isTestingConnection) "Testing…" else "Test connection")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    when (val check = state.connectionCheck) {
                        AtmosphereViewState.ConnectionCheck.Idle -> Text(
                            text = if (state.isHomeAssistantConfigured) {
                                "Connection saved on this computer."
                            } else {
                                "Not connected yet."
                            },
                            fontSize = 13.sp,
                            color = TextSecondary,
                        )
                        AtmosphereViewState.ConnectionCheck.Connected -> Text(
                            text = "Connected to Home Assistant.",
                            fontSize = 13.sp,
                            color = TextPrimary,
                        )
                        is AtmosphereViewState.ConnectionCheck.Failed -> Text(
                            text = check.message,
                            fontSize = 13.sp,
                            color = TextPrimary,
                        )
                    }
                }
            }
            item {
                AtmosphereCard(title = "Philips Hue") {
                    Text(
                        text = "Pair a local Hue bridge. Load Hue lights and pick lamps, then use Look — or load a Hue scene instead. A Hue scene still applies only to the lamps you pick.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.draftHueHost,
                        onValueChange = { onInteraction(AtmosphereInteraction.HueHostChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Bridge IP") },
                        placeholder = { Text("192.168.1.40") },
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.draftHueKey,
                        onValueChange = { onInteraction(AtmosphereInteraction.HueKeyChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Application key") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onInteraction(AtmosphereInteraction.HueDiscoverSelected) },
                            enabled = !state.isDiscoveringHue,
                        ) {
                            Text(if (state.isDiscoveringHue) "Finding…" else "Find bridge")
                        }
                        Button(
                            onClick = { onInteraction(AtmosphereInteraction.HuePairSelected) },
                            enabled = !state.isPairingHue,
                            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                        ) {
                            Text(if (state.isPairingHue) "Pairing…" else "Pair")
                        }
                        ActionIconButtonComposeWidget(
                            icon = Icons.Default.Save,
                            tooltip = "Save",
                            enabled = state.isHueDirty,
                            onClick = { onInteraction(AtmosphereInteraction.HueSaveSelected) },
                        )
                        OutlinedButton(
                            onClick = { onInteraction(AtmosphereInteraction.HueTestSelected) },
                            enabled = !state.isTestingHue,
                        ) {
                            Text(if (state.isTestingHue) "Testing…" else "Test")
                        }
                        OutlinedButton(
                            onClick = { onInteraction(AtmosphereInteraction.HueCatalogRefreshSelected) },
                            enabled = !state.isLoadingHueCatalog,
                        ) {
                            Text(if (state.isLoadingHueCatalog) "Loading…" else "Load Hue scenes")
                        }
                        OutlinedButton(
                            onClick = { onInteraction(AtmosphereInteraction.HueLightsRefreshSelected) },
                            enabled = !state.isLoadingHueLights,
                        ) {
                            Text(if (state.isLoadingHueLights) "Loading…" else "Load Hue lights")
                        }
                    }
                    if (state.hueBridges.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.hueBridges.forEach { bridge ->
                                FilterChip(
                                    selected = state.draftHueHost == bridge.ipAddress,
                                    onClick = {
                                        onInteraction(AtmosphereInteraction.HueBridgeSelected(bridge.ipAddress))
                                    },
                                    label = { Text(bridge.ipAddress) },
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    when (val check = state.hueCheck) {
                        AtmosphereViewState.ConnectionCheck.Idle -> Text(
                            text = if (state.isHueConfigured) {
                                "Hue bridge saved on this computer."
                            } else {
                                "Not paired yet."
                            },
                            fontSize = 13.sp,
                            color = TextSecondary,
                        )
                        AtmosphereViewState.ConnectionCheck.Connected -> Text(
                            text = "Connected to the Hue bridge.",
                            fontSize = 13.sp,
                            color = TextPrimary,
                        )
                        is AtmosphereViewState.ConnectionCheck.Failed -> Text(
                            text = check.message,
                            fontSize = 13.sp,
                            color = TextPrimary,
                        )
                    }
                    if (state.hueLights.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Lights for Look and the next mapping. Selected lamps stay selected for the pop-out tray. With Look and no Hue scene, Atmosphere sets color on these lamps. Leave them unselected only when you want a Hue scene to change every lamp in that scene.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.hueLights.forEach { light ->
                                FilterChip(
                                    selected = light.id in state.selectedHueLightIds,
                                    onClick = {
                                        onInteraction(AtmosphereInteraction.HueLightToggled(light.id))
                                    },
                                    label = { Text(light.name) },
                                )
                            }
                        }
                    }
                }
            }
            item {
                AtmosphereCard(title = "Govee lighting") {
                    Text(
                        text = "Turn on LAN Control in the Govee app, then scan this network. Selected lights follow Look here and in the pop-out tray. Govee has no local scene catalog.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { onInteraction(AtmosphereInteraction.GoveeScanSelected) },
                        enabled = !state.isScanningGovee,
                    ) {
                        Text(if (state.isScanningGovee) "Scanning…" else "Scan LAN")
                    }
                    state.goveeMessage?.let { message ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = message, fontSize = 13.sp, color = TextPrimary)
                    }
                    if (state.goveeDevices.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Lights for the next mapping", fontSize = 13.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.goveeDevices.forEach { device ->
                                FilterChip(
                                    selected = device.deviceId in state.selectedGoveeDeviceIds,
                                    onClick = {
                                        onInteraction(AtmosphereInteraction.GoveeDeviceToggled(device.deviceId))
                                    },
                                    label = { Text(device.displayName) },
                                )
                            }
                        }
                    }
                }
            }
            if (state.hueLights.isNotEmpty() || state.goveeDevices.isNotEmpty()) {
                item {
                    AtmosphereCard(title = "Look") {
                        AtmosphereLookComposeWidget(
                            colorHex = state.draftLookColorHex,
                            brightness = state.draftLookBrightness,
                            powerOn = state.draftLookPowerOn,
                            moods = state.moods,
                            draftMoodName = state.draftMoodName,
                            lookError = state.lookError,
                            moodError = state.moodError,
                            hasSelectedLights = state.hasSelectedLookLights,
                            onInteraction = onInteraction,
                        )
                    }
                }
            }
            item {
                AtmosphereCard(title = "Scene mappings") {
                    Text(
                        text = "Give the table a short name. Attach any combination of a Home Assistant scene, Hue lights with a Look (or a Hue scene), and Govee lights. This does not change Foundry or battle-map lighting.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.draftSceneName,
                        onValueChange = { onInteraction(AtmosphereInteraction.DraftSceneNameChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Name") },
                        placeholder = { Text("Tavern") },
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.draftEntityId,
                        onValueChange = { onInteraction(AtmosphereInteraction.DraftEntityIdChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Home Assistant scene id") },
                        placeholder = { Text("scene.tavern") },
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onInteraction(AtmosphereInteraction.SceneCreateSelected) },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                        ) {
                            Text("Add scene")
                        }
                        OutlinedButton(
                            onClick = { onInteraction(AtmosphereInteraction.CatalogRefreshSelected) },
                            enabled = !state.isLoadingCatalog,
                        ) {
                            Text(if (state.isLoadingCatalog) "Loading…" else "Load HA scenes")
                        }
                        OutlinedButton(
                            onClick = { onInteraction(AtmosphereInteraction.HueCatalogRefreshSelected) },
                            enabled = !state.isLoadingHueCatalog,
                        ) {
                            Text(if (state.isLoadingHueCatalog) "Loading…" else "Load Hue scenes")
                        }
                    }
                    state.sceneError?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = error, fontSize = 13.sp, color = TextPrimary)
                    }
                    if (state.catalog.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Home Assistant scenes",
                            fontSize = 13.sp,
                            color = TextSecondary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.catalog.forEach { scene ->
                                FilterChip(
                                    selected = state.selectedCatalogEntityId == scene.entityId,
                                    onClick = {
                                        onInteraction(AtmosphereInteraction.CatalogSceneSelected(scene.entityId))
                                    },
                                    label = { Text(scene.name) },
                                )
                            }
                        }
                    }
                    if (state.hueCatalog.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Hue scenes (optional if Hue lights use Look)",
                            fontSize = 13.sp,
                            color = TextSecondary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.hueCatalog.forEach { scene ->
                                FilterChip(
                                    selected = state.selectedHueSceneId == scene.id,
                                    onClick = {
                                        onInteraction(AtmosphereInteraction.HueCatalogSceneSelected(scene.id))
                                    },
                                    label = { Text(scene.name) },
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if (state.scenes.isEmpty()) {
                        Text(
                            text = "No scenes mapped yet.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                        )
                    } else {
                        state.scenes.forEach { scene ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                    Text(
                                        text = scene.name,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary,
                                    )
                                    Text(
                                        text = scene.targetSummary().ifBlank { "No targets" },
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            onInteraction(AtmosphereInteraction.SceneActivateSelected(scene.id))
                                        },
                                        enabled = !state.isActivating,
                                    ) {
                                        Text("Activate")
                                    }
                                    ActionIconButtonComposeWidget(
                                        icon = Icons.Default.Delete,
                                        tooltip = "Delete",
                                        tint = ErrorRed,
                                        onClick = {
                                            onInteraction(AtmosphereInteraction.SceneDeleteSelected(scene.id))
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                AtmosphereCard(title = "Table tray") {
                    AtmosphereTrayComposeWidget(
                        state = state,
                        onInteraction = onInteraction,
                        contentPadding = 0.dp,
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun AtmosphereCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            content()
        }
    }
}
