package io.github.kmbisset89.worldweaver.ui.links

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.RelationshipType
import io.github.kmbisset89.worldweaver.ui.components.AdaptiveListDetailPaneWidth
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun LinksDrawerComposeWidget(
    visible: Boolean,
    filters: LinksDrawerFilterState,
    inspector: LinksViewState.Inspector?,
    onDismissed: () -> Unit,
    onInteraction: (LinksInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        val scrimInteractionSource = remember { MutableInteractionSource() }
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
                        onClick = onDismissed,
                    )
                    .semantics { contentDescription = "Dismiss link details" },
            )
        }
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd),
        ) {
            Surface(
                modifier = Modifier.fillMaxHeight().width(AdaptiveListDetailPaneWidth),
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 8.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Filters",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                    )
                    LinksFilterChips(
                        state = filters,
                        onInteraction = onInteraction,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinksInspectorSection(
                        inspector = inspector,
                        onInteraction = onInteraction,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LinksFilterChips(
    state: LinksDrawerFilterState,
    onInteraction: (LinksInteraction) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        FilterChip(
            selected = state.showIsolates,
            onClick = { onInteraction(LinksInteraction.IsolateVisibilityToggled) },
            label = { Text("Show unlinked") },
        )
        FilterChip(
            selected = state.showMemberships,
            onClick = { onInteraction(LinksInteraction.MembershipEdgesToggled) },
            label = { Text("Memberships") },
        )
        RelationshipType.entries.forEach { type ->
            FilterChip(
                selected = type in state.enabledRelationshipTypes,
                onClick = { onInteraction(LinksInteraction.RelationshipTypeFilterToggled(type)) },
                label = { Text(type.displayName) },
            )
        }
    }
}

@Composable
private fun LinksInspectorSection(
    inspector: LinksViewState.Inspector?,
    onInteraction: (LinksInteraction) -> Unit,
) {
    Text(
        text = "Details",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextPrimary,
    )
    if (inspector == null) {
        Text(
            text = "Select a person or faction to see their links.",
            color = TextSecondary,
            fontSize = 13.sp,
        )
        return
    }
    Text(
        text = inspector.name,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextPrimary,
    )
    Text(
        text = inspector.subtitle,
        fontSize = 13.sp,
        color = TextSecondary,
    )
    Button(
        onClick = { onInteraction(LinksInteraction.NodeOpened(inspector.nodeId)) },
        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Open")
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "Linkages",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextPrimary,
    )
    if (inspector.edges.isEmpty()) {
        Text(
            text = "No visible links.",
            fontSize = 13.sp,
            color = TextSecondary,
        )
    } else {
        inspector.edges.forEach { edge ->
            Text(
                text = edge.label,
                fontSize = 13.sp,
                color = TextSecondary,
            )
        }
    }
}
