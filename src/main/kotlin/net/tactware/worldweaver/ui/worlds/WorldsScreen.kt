package net.tactware.worldweaver.ui.worlds

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PublicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.tactware.worldweaver.domain.World
import net.tactware.worldweaver.ui.components.ConfirmDestructiveDialog
import net.tactware.worldweaver.ui.components.FeatureEmptyState
import net.tactware.worldweaver.ui.components.FeatureErrorState
import net.tactware.worldweaver.ui.theme.NavyBlue
import net.tactware.worldweaver.ui.theme.SurfaceCard
import net.tactware.worldweaver.ui.theme.TextPrimary
import net.tactware.worldweaver.ui.theme.TextSecondary
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
internal fun WorldsScreen(
    viewState: WorldsViewState,
    onInteraction: (WorldsInteraction) -> Unit,
) {
    LaunchedEffect(Unit) {
        onInteraction(WorldsInteraction.ScreenStarted)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        WorldsHeader(
            isTransferring = isTransferring(viewState),
            onInteraction = onInteraction,
        )

        when (viewState) {
            WorldsViewState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is WorldsViewState.Error -> FeatureErrorState(
                message = viewState.message,
                canRetry = viewState.canRetry,
                onRetry = { onInteraction(WorldsInteraction.RetrySelected) },
            )
            is WorldsViewState.Empty -> {
                FeatureEmptyState(
                    icon = Icons.Default.PublicOff,
                    title = "No worlds yet",
                    message = "Create a world to start building places, people, and stories.",
                    actionLabel = "New world",
                    onAction = { onInteraction(WorldsInteraction.NewWorldSelected) },
                )
                Spacer(modifier = Modifier.padding(top = 4.dp))
                TextButton(
                    onClick = { onInteraction(WorldsInteraction.OneShotSelected) },
                    enabled = !viewState.isTransferring,
                ) {
                    Text("Create a one-shot")
                }
                Spacer(modifier = Modifier.padding(top = 4.dp))
                TextButton(
                    onClick = { chooseImportPath(onInteraction) },
                    enabled = !viewState.isTransferring,
                ) {
                    Text("Import world")
                }
                viewState.editor?.let { editor ->
                    WorldEditorDialog(editor = editor, onInteraction = onInteraction)
                }
            }
            is WorldsViewState.Content -> {
                WorldsContent(
                    state = viewState,
                    onInteraction = onInteraction,
                )
            }
        }
    }
}

@Composable
private fun WorldsHeader(
    isTransferring: Boolean,
    onInteraction: (WorldsInteraction) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Worlds",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Your world library",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { chooseImportPath(onInteraction) },
                enabled = !isTransferring,
            ) {
                Text("Import world")
            }
            OutlinedButton(
                onClick = { onInteraction(WorldsInteraction.OneShotSelected) },
                enabled = !isTransferring,
            ) {
                Text("One-shot")
            }
            Button(
                onClick = { onInteraction(WorldsInteraction.NewWorldSelected) },
                enabled = !isTransferring,
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text("New world")
            }
        }
    }
}

@Composable
private fun WorldsContent(
    state: WorldsViewState.Content,
    onInteraction: (WorldsInteraction) -> Unit,
) {
    if (state.blockDeleteReason != null) {
        Card(
            modifier = Modifier.fillMaxWidth().clickable {
                onInteraction(WorldsInteraction.BlockReasonDismissed)
            },
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Text(
                text = state.blockDeleteReason,
                modifier = Modifier.padding(16.dp),
                color = TextPrimary,
                fontSize = 13.sp
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(state.worlds, key = { it.id }) { world ->
            WorldRow(
                world = world,
                isActive = world.id == state.activeWorldId,
                isTransferring = state.isTransferring,
                onInteraction = onInteraction,
            )
        }
    }

    state.editor?.let { editor ->
        WorldEditorDialog(editor = editor, onInteraction = onInteraction)
    }
    state.pendingDelete?.let { pending ->
        ConfirmDestructiveDialog(
            title = "Delete world?",
            message = "Delete “${pending.worldName}”? This cannot be undone. Worlds with campaigns cannot be deleted.",
            confirmLabel = "Delete",
            onConfirm = { onInteraction(WorldsInteraction.DeleteConfirmed) },
            onDismiss = { onInteraction(WorldsInteraction.DeleteCancelled) },
        )
    }
}

@Composable
private fun WorldRow(
    world: World,
    isActive: Boolean,
    isTransferring: Boolean,
    onInteraction: (WorldsInteraction) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInteraction(WorldsInteraction.WorldSelected(world.id)) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isActive) {
                        Modifier.background(NavyBlue.copy(alpha = 0.08f))
                    } else {
                        Modifier
                    }
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = world.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    if (isActive) {
                        Text(
                            text = "Active",
                            fontSize = 11.sp,
                            color = NavyBlue,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
                if (world.description.isNotBlank()) {
                    Text(
                        text = world.description,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Text(
                    text = world.defaultGameSystem.displayName,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            TextButton(
                onClick = {
                    chooseExportPath(world.name)?.let { path ->
                        onInteraction(WorldsInteraction.ExportPathChosen(world.id, path))
                    }
                },
                enabled = !isTransferring,
            ) {
                Text("Export")
            }
            TextButton(
                onClick = { onInteraction(WorldsInteraction.EditWorldSelected(world.id)) },
                enabled = !isTransferring,
            ) {
                Text("Edit")
            }
            TextButton(
                onClick = { onInteraction(WorldsInteraction.DeleteWorldSelected(world.id)) },
                enabled = !isTransferring,
            ) {
                Text("Delete")
            }
        }
    }
}

private fun isTransferring(state: WorldsViewState): Boolean {
    return when (state) {
        is WorldsViewState.Empty -> state.isTransferring
        is WorldsViewState.Content -> state.isTransferring
        else -> false
    }
}

private fun chooseImportPath(onInteraction: (WorldsInteraction) -> Unit) {
    chooseBundlePath(title = "Import world", mode = FileDialog.LOAD)?.let { path ->
        onInteraction(WorldsInteraction.ImportPathChosen(path))
    }
}

private fun chooseExportPath(worldName: String): String? {
    return chooseBundlePath(
        title = "Export world",
        mode = FileDialog.SAVE,
        defaultFileName = sanitizedBundleFileName(worldName),
    )
}

private fun chooseBundlePath(
    title: String,
    mode: Int,
    defaultFileName: String? = null,
): String? {
    val dialog = FileDialog(null as Frame?, title, mode)
    dialog.setFilenameFilter { _, name -> name.endsWith(BUNDLE_EXTENSION, ignoreCase = true) }
    if (defaultFileName != null) {
        dialog.file = defaultFileName
    }
    dialog.isVisible = true
    val fileName = dialog.file ?: return null
    val directory = dialog.directory ?: return null
    val file = File(directory, fileName)
    return if (mode == FileDialog.SAVE && !file.name.endsWith(BUNDLE_EXTENSION, ignoreCase = true)) {
        File(directory, file.name + BUNDLE_EXTENSION).absolutePath
    } else {
        file.absolutePath
    }
}

private fun sanitizedBundleFileName(worldName: String): String {
    val cleaned = worldName.replace(Regex("[^A-Za-z0-9._-]+"), "_").trim('_')
    val base = cleaned.ifBlank { "world" }
    return base + BUNDLE_EXTENSION
}

private const val BUNDLE_EXTENSION = ".wwbundle"
