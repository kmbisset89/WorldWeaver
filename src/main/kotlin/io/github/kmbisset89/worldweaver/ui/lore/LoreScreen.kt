package io.github.kmbisset89.worldweaver.ui.lore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.PublicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.Lore
import io.github.kmbisset89.worldweaver.domain.LoreCategory
import io.github.kmbisset89.worldweaver.ui.components.ConfirmDestructiveDialog
import io.github.kmbisset89.worldweaver.ui.components.FeatureEmptyState
import io.github.kmbisset89.worldweaver.ui.components.FeatureErrorState
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun LoreScreen(
    viewState: LoreViewState,
    onInteraction: (LoreInteraction) -> Unit,
) {
    LaunchedEffect(Unit) {
        onInteraction(LoreInteraction.ScreenStarted)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        when (viewState) {
            LoreViewState.Loading -> {
                LoreHeader(subtitle = "World lore", showCreate = false, onInteraction = onInteraction)
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is LoreViewState.Error -> {
                LoreHeader(subtitle = "World lore", showCreate = false, onInteraction = onInteraction)
                FeatureErrorState(
                    message = viewState.message,
                    canRetry = viewState.canRetry,
                    onRetry = { onInteraction(LoreInteraction.RetrySelected) },
                )
            }
            LoreViewState.NoActiveWorld -> {
                LoreHeader(subtitle = "Select a world first", showCreate = false, onInteraction = onInteraction)
                FeatureEmptyState(
                    icon = Icons.Default.PublicOff,
                    title = "No active world",
                    message = "Create or select a world first so lore has a setting.",
                    actionLabel = "Go to Worlds",
                    onAction = { onInteraction(LoreInteraction.CreateWorldSelected) },
                )
            }
            is LoreViewState.Empty -> {
                LoreHeader(subtitle = viewState.worldName, showCreate = true, onInteraction = onInteraction)
                FeatureEmptyState(
                    icon = Icons.Default.AutoStories,
                    title = "No lore yet",
                    message = "Write the first entry for this world.",
                    actionLabel = "New lore",
                    onAction = { onInteraction(LoreInteraction.NewLoreSelected) },
                )
                viewState.editor?.let { editor ->
                    LoreEditorDialog(editor = editor, onInteraction = onInteraction)
                }
            }
            is LoreViewState.Content -> {
                LoreHeader(subtitle = viewState.worldName, showCreate = true, onInteraction = onInteraction)
                LoreContent(state = viewState, onInteraction = onInteraction)
            }
        }
    }
}

@Composable
private fun LoreHeader(
    subtitle: String,
    showCreate: Boolean,
    onInteraction: (LoreInteraction) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Lore",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
        if (showCreate) {
            Button(
                onClick = { onInteraction(LoreInteraction.NewLoreSelected) },
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text("New lore")
            }
        }
    }
}

@Composable
private fun LoreContent(
    state: LoreViewState.Content,
    onInteraction: (LoreInteraction) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.categoryFilter == null,
                onClick = { onInteraction(LoreInteraction.CategoryFilterSelected(null)) },
                label = { Text("All") },
            )
            LoreCategory.entries.forEach { category ->
                FilterChip(
                    selected = state.categoryFilter == category,
                    onClick = { onInteraction(LoreInteraction.CategoryFilterSelected(category)) },
                    label = { Text(category.displayName) },
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.width(320.dp).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.groups.isEmpty()) {
                    item {
                        Text(
                            text = "No lore in this category.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                } else {
                    state.groups.forEach { group ->
                        item(key = "header-${group.category.name}") {
                            Text(
                                text = group.category.displayName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                            )
                        }
                        items(group.entries, key = { it.id }) { entry ->
                            LoreRow(
                                lore = entry,
                                isSelected = entry.id == state.selectedLore?.id,
                                onInteraction = onInteraction,
                            )
                        }
                    }
                }
            }

            if (state.selectedLore != null) {
                LoreDetailPane(
                    lore = state.selectedLore,
                    relatedLinks = state.relatedLinks,
                    attachedLocationName = state.attachedLocationName,
                    attachedCharacterName = state.attachedCharacterName,
                    onInteraction = onInteraction,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            } else {
                Text(
                    text = "Select a lore entry to read it.",
                    color = TextSecondary,
                    modifier = Modifier.weight(1f).padding(16.dp)
                )
            }
        }
    }

    state.editor?.let { editor ->
        LoreEditorDialog(editor = editor, onInteraction = onInteraction)
    }
    state.pendingDelete?.let { pending ->
        ConfirmDestructiveDialog(
            title = "Delete lore?",
            message = "Delete “${pending.loreTitle}”? Related links to this entry will be removed.",
            confirmLabel = "Delete",
            onConfirm = { onInteraction(LoreInteraction.DeleteConfirmed) },
            onDismiss = { onInteraction(LoreInteraction.DeleteCancelled) },
        )
    }
}

@Composable
private fun LoreRow(
    lore: Lore,
    isSelected: Boolean,
    onInteraction: (LoreInteraction) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInteraction(LoreInteraction.LoreSelected(lore.id)) },
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
                text = lore.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = lore.category.displayName,
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
