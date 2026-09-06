package io.github.kmbisset89.worldweaver.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal val AdaptiveListDetailBreakpoint = 900.dp
internal val AdaptiveListDetailPaneWidth = 320.dp

@Composable
internal fun rememberAdaptiveListOpen(compact: Boolean): MutableState<Boolean> {
    val listOpen = remember { mutableStateOf(false) }
    LaunchedEffect(compact) {
        if (!compact) {
            listOpen.value = false
        }
    }
    return listOpen
}

/**
 * Wide windows keep [listPane] beside [detailPane]. Narrow windows give the detail the page
 * and house the list in a drawer, unless nothing is selected and the list should be the page.
 */
@Composable
internal fun AdaptiveListDetailComposeWidget(
    compact: Boolean,
    listOpen: Boolean,
    hasSelection: Boolean,
    listPane: @Composable (Modifier) -> Unit,
    detailPane: @Composable (Modifier) -> Unit,
    onListDismissed: () -> Unit,
    modifier: Modifier = Modifier,
    listWidth: Dp = AdaptiveListDetailPaneWidth,
    listAtEnd: Boolean = false,
    keepDetailWhenCompact: Boolean = false,
    dismissListLabel: String = "Dismiss list",
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            !compact -> {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (listAtEnd) {
                        detailPane(Modifier.weight(1f).fillMaxHeight())
                        listPane(Modifier.width(listWidth).fillMaxHeight())
                    } else {
                        listPane(Modifier.width(listWidth).fillMaxHeight())
                        detailPane(Modifier.weight(1f).fillMaxHeight())
                    }
                }
            }
            !hasSelection && !keepDetailWhenCompact -> {
                listPane(Modifier.fillMaxSize())
            }
            else -> {
                detailPane(Modifier.fillMaxSize())
                AdaptiveListOverlay(
                    visible = listOpen,
                    listAtEnd = listAtEnd,
                    listWidth = listWidth,
                    dismissLabel = dismissListLabel,
                    onDismiss = onListDismissed,
                    listPane = listPane,
                )
            }
        }
    }
}

@Composable
private fun BoxScope.AdaptiveListOverlay(
    visible: Boolean,
    listAtEnd: Boolean,
    listWidth: Dp,
    dismissLabel: String,
    onDismiss: () -> Unit,
    listPane: @Composable (Modifier) -> Unit,
) {
    val scrimInteractionSource = remember { MutableInteractionSource() }
    val alignment = if (listAtEnd) Alignment.CenterEnd else Alignment.CenterStart
    val slide: (Int) -> Int = if (listAtEnd) { width -> width } else { width -> -width }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f))
                .clickable(
                    interactionSource = scrimInteractionSource,
                    indication = null,
                    onClick = onDismiss,
                )
                .semantics { contentDescription = dismissLabel },
        )
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInHorizontally(initialOffsetX = slide),
        exit = fadeOut() + slideOutHorizontally(targetOffsetX = slide),
        modifier = Modifier.align(alignment),
    ) {
        Surface(
            modifier = Modifier.fillMaxHeight().width(listWidth),
            color = MaterialTheme.colorScheme.background,
            shadowElevation = 8.dp,
        ) {
            listPane(Modifier.fillMaxSize().padding(16.dp))
        }
    }
}
