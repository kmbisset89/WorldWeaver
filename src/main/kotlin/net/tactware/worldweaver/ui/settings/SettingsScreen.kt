package net.tactware.worldweaver.ui.settings

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.tactware.worldweaver.ui.components.ConfirmDestructiveDialog
import net.tactware.worldweaver.ui.theme.SurfaceCard
import net.tactware.worldweaver.ui.theme.TextPrimary
import net.tactware.worldweaver.ui.theme.TextSecondary
import net.tactware.worldweaver.ui.theme.ThemeMode
import net.tactware.worldweaver.ui.theme.ThemeSkin
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.time.LocalDate

@Composable
internal fun SettingsScreen(
    viewState: SettingsViewState,
    onInteraction: (SettingsInteraction) -> Unit,
) {
    LaunchedEffect(Unit) {
        onInteraction(SettingsInteraction.ScreenStarted)
    }
    when (viewState) {
        is SettingsViewState.Content -> SettingsContent(
            state = viewState,
            onInteraction = onInteraction,
        )
    }
}

@Composable
private fun SettingsContent(
    state: SettingsViewState.Content,
    onInteraction: (SettingsInteraction) -> Unit,
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
                    text = "Appearance, local profile, and backups",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }

        item {
            SettingsCard(title = "Appearance") {
                Text(
                    text = "Mode",
                    fontSize = 14.sp,
                    color = TextSecondary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = state.themeMode == mode,
                            onClick = {
                                onInteraction(SettingsInteraction.ThemeModeSelected(mode))
                            },
                            label = { Text(mode.label()) },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Skin",
                    fontSize = 14.sp,
                    color = TextSecondary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeSkin.entries.forEach { skin ->
                        FilterChip(
                            selected = state.themeSkin == skin,
                            onClick = {
                                onInteraction(SettingsInteraction.ThemeSkinSelected(skin))
                            },
                            label = { Text(skin.label()) },
                        )
                    }
                }
            }
        }

        item {
            SettingsCard(title = "Navigation") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                        Text(
                            text = "Expanded sidebar",
                            fontSize = 14.sp,
                            color = TextPrimary,
                        )
                        Text(
                            text = "Collapse the rail to icons only. The choice is remembered.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                        )
                    }
                    Switch(
                        checked = state.navExpanded,
                        onCheckedChange = { expanded ->
                            onInteraction(SettingsInteraction.NavExpandedChanged(expanded))
                        },
                    )
                }
            }
        }

        item {
            SettingsCard(title = "Local profile") {
                OutlinedTextField(
                    value = state.draftDisplayName,
                    onValueChange = { name ->
                        onInteraction(SettingsInteraction.DisplayNameChanged(name))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Name") },
                    singleLine = true,
                    isError = state.profileError != null,
                    supportingText = state.profileError?.let { error ->
                        { Text(error) }
                    },
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.draftEmail,
                    onValueChange = { email ->
                        onInteraction(SettingsInteraction.EmailChanged(email))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Email") },
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onInteraction(SettingsInteraction.ProfileSaved) },
                    enabled = state.isProfileDirty,
                ) {
                    Text("Save profile")
                }
            }
        }

        item {
            SettingsCard(title = "Backup and restore") {
                Text(
                    text = "Export a single backup of this machine’s worlds, campaigns, maps, avatars, and voice clips. Restore replaces everything on this computer. WorldWeaver will quit after restore.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { chooseExportPath(onInteraction) },
                        enabled = !state.isTransferring,
                    ) {
                        Text("Export backup")
                    }
                    OutlinedButton(
                        onClick = { chooseRestorePath(onInteraction) },
                        enabled = !state.isTransferring,
                    ) {
                        Text("Restore backup")
                    }
                }
            }
        }
    }

    state.pendingRestorePath?.let {
        ConfirmDestructiveDialog(
            title = "Replace all WorldWeaver data?",
            message = "This replaces all local worlds, campaigns, maps, avatars, and voice clips. Profile and appearance come from the backup. WorldWeaver will quit so you can reopen with the restored files. This cannot be undone unless you exported first.",
            confirmLabel = "Restore and quit",
            onConfirm = { onInteraction(SettingsInteraction.RestoreConfirmed) },
            onDismiss = { onInteraction(SettingsInteraction.RestoreCancelled) },
        )
    }
}

private fun chooseExportPath(onInteraction: (SettingsInteraction) -> Unit) {
    chooseBackupPath(
        title = "Export backup",
        mode = FileDialog.SAVE,
        defaultFileName = defaultBackupFileName(),
    )?.let { path ->
        onInteraction(SettingsInteraction.ExportPathChosen(path))
    }
}

private fun chooseRestorePath(onInteraction: (SettingsInteraction) -> Unit) {
    chooseBackupPath(
        title = "Restore backup",
        mode = FileDialog.LOAD,
    )?.let { path ->
        onInteraction(SettingsInteraction.RestorePathChosen(path))
    }
}

private fun chooseBackupPath(
    title: String,
    mode: Int,
    defaultFileName: String? = null,
): String? {
    val dialog = FileDialog(null as Frame?, title, mode)
    dialog.setFilenameFilter { _, name -> name.endsWith(BACKUP_EXTENSION, ignoreCase = true) }
    if (defaultFileName != null) {
        dialog.file = defaultFileName
    }
    dialog.isVisible = true
    val fileName = dialog.file ?: return null
    val directory = dialog.directory ?: return null
    val file = File(directory, fileName)
    return if (mode == FileDialog.SAVE && !file.name.endsWith(BACKUP_EXTENSION, ignoreCase = true)) {
        File(directory, file.name + BACKUP_EXTENSION).absolutePath
    } else {
        file.absolutePath
    }
}

private fun defaultBackupFileName(): String {
    val date = LocalDate.now().toString().replace("-", "")
    return "worldweaver-$date$BACKUP_EXTENSION"
}

private const val BACKUP_EXTENSION = ".wwbackup"

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit,
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
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            content()
        }
    }
}
