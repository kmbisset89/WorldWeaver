package io.github.kmbisset89.worldweaver.ui.assets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PublicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.ui.components.AdaptiveListDetailBreakpoint
import io.github.kmbisset89.worldweaver.ui.components.AdaptiveListDetailComposeWidget
import io.github.kmbisset89.worldweaver.ui.components.AdaptiveScreenHeaderComposeWidget
import io.github.kmbisset89.worldweaver.ui.components.ConfirmDestructiveDialog
import io.github.kmbisset89.worldweaver.ui.components.FeatureEmptyState
import io.github.kmbisset89.worldweaver.ui.components.FeatureErrorState
import io.github.kmbisset89.worldweaver.ui.components.rememberAdaptiveListOpen
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
internal fun AssetsScreen(
    viewState: AssetsViewState,
    onInteraction: (AssetsInteraction) -> Unit,
) {
    LaunchedEffect(Unit) {
        onInteraction(AssetsInteraction.ScreenStarted)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        when (viewState) {
            AssetsViewState.Loading -> {
                AssetsHeader(subtitle = "World files", showImport = false, onInteraction = onInteraction)
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is AssetsViewState.Error -> {
                AssetsHeader(subtitle = "World files", showImport = false, onInteraction = onInteraction)
                FeatureErrorState(
                    message = viewState.message,
                    canRetry = viewState.canRetry,
                    onRetry = { onInteraction(AssetsInteraction.RetrySelected) },
                )
            }
            AssetsViewState.NoActiveWorld -> {
                AssetsHeader(subtitle = "Select a world first", showImport = false, onInteraction = onInteraction)
                FeatureEmptyState(
                    icon = Icons.Default.PublicOff,
                    title = "No active world",
                    message = "Create or select a world first so assets have a setting.",
                    actionLabel = "Go to Worlds",
                    onAction = { onInteraction(AssetsInteraction.CreateWorldSelected) },
                )
            }
            is AssetsViewState.Empty -> {
                AssetsHeader(subtitle = viewState.worldName, showImport = true, onInteraction = onInteraction)
                FeatureEmptyState(
                    icon = Icons.Default.FolderOpen,
                    title = "No assets yet",
                    message = "Drop in maps, portraits, handouts, or other files you might use later. Nothing here is committed to a location or session.",
                    actionLabel = "Add files",
                    onAction = { chooseAssetFiles(onInteraction) },
                )
            }
            is AssetsViewState.Content -> {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val compact = maxWidth < AdaptiveListDetailBreakpoint
                    var listOpen by rememberAdaptiveListOpen(compact)
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        AssetsHeader(
                            subtitle = viewState.worldName,
                            showImport = true,
                            compact = compact,
                            selectedName = viewState.selectedAsset?.displayName,
                            onListToggle = { listOpen = !listOpen },
                            onInteraction = onInteraction,
                        )
                        AssetsContent(
                            state = viewState,
                            compact = compact,
                            listOpen = listOpen,
                            onListDismissed = { listOpen = false },
                            onInteraction = onInteraction,
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AssetsHeader(
    subtitle: String,
    showImport: Boolean,
    onInteraction: (AssetsInteraction) -> Unit,
    compact: Boolean = false,
    selectedName: String? = null,
    onListToggle: () -> Unit = {},
) {
    AdaptiveScreenHeaderComposeWidget(
        title = "Assets",
        subtitle = subtitle,
        compact = compact,
        switcherName = selectedName,
        listLabel = "assets list",
        onListToggle = onListToggle,
    ) {
        if (showImport) {
            Button(
                onClick = { chooseAssetFiles(onInteraction) },
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text("Add files")
            }
        }
    }
}

@Composable
private fun AssetsContent(
    state: AssetsViewState.Content,
    compact: Boolean,
    listOpen: Boolean,
    onListDismissed: () -> Unit,
    onInteraction: (AssetsInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listInteraction: (AssetsInteraction) -> Unit = { interaction ->
        if (interaction is AssetsInteraction.AssetSelected) {
            onListDismissed()
        }
        onInteraction(interaction)
    }
    AdaptiveListDetailComposeWidget(
        compact = compact,
        listOpen = listOpen,
        hasSelection = state.selectedAsset != null,
        listPane = { listModifier ->
            LazyColumn(
                modifier = listModifier,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.assets, key = { it.id }) { asset ->
                    AssetRow(
                        asset = asset,
                        isSelected = asset.id == state.selectedAsset?.id,
                        onInteraction = listInteraction,
                    )
                }
            }
        },
        detailPane = { detailModifier ->
            val selected = state.selectedAsset
            if (selected != null) {
                AssetsDetailPane(
                    asset = selected,
                    nameError = state.nameError,
                    onInteraction = onInteraction,
                    modifier = detailModifier,
                )
            } else {
                Text(
                    text = "Select a file to preview it.",
                    color = TextSecondary,
                    modifier = detailModifier.padding(16.dp)
                )
            }
        },
        onListDismissed = onListDismissed,
        dismissListLabel = "Dismiss assets list",
        modifier = modifier,
    )

    state.pendingDelete?.let { pending ->
        ConfirmDestructiveDialog(
            title = "Remove file?",
            message = "Remove “${pending.displayName}” from Assets? The copy stored in World Weaver is deleted.",
            confirmLabel = "Remove",
            onConfirm = { onInteraction(AssetsInteraction.DeleteConfirmed) },
            onDismiss = { onInteraction(AssetsInteraction.DeleteCancelled) },
        )
    }
}

@Composable
private fun AssetRow(
    asset: AssetsViewState.AssetItem,
    isSelected: Boolean,
    onInteraction: (AssetsInteraction) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInteraction(AssetsInteraction.AssetSelected(asset.id)) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isSelected) {
                        Modifier.background(NavyBlue.copy(alpha = 0.08f))
                    } else {
                        Modifier
                    }
                )
                .padding(14.dp)
        ) {
            Text(
                text = asset.displayName,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "${asset.originalFileName} · ${asset.sizeLabel}",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun chooseAssetFiles(onInteraction: (AssetsInteraction) -> Unit) {
    val dialog = FileDialog(null as Frame?, "Add assets", FileDialog.LOAD)
    dialog.isMultipleMode = true
    dialog.isVisible = true
    val files = dialog.files?.toList().orEmpty()
    if (files.isNotEmpty()) {
        onInteraction(AssetsInteraction.FilesChosen(files.map(File::getAbsolutePath)))
        return
    }
    val fileName = dialog.file ?: return
    val directory = dialog.directory ?: return
    onInteraction(AssetsInteraction.FilesChosen(listOf(File(directory, fileName).absolutePath)))
}
