package io.github.kmbisset89.worldweaver.ui.links

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.RelationshipType
import io.github.kmbisset89.worldweaver.ui.theme.ErrorRed
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.SuccessGreen
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary
import kotlin.math.hypot

@Composable
internal fun RelationshipWebCanvasComposeWidget(
    nodes: List<LinksViewState.Node>,
    edges: List<LinksViewState.Edge>,
    positions: Map<String, LinksViewState.LayoutPoint>,
    selectedNodeId: String?,
    searchQuery: String,
    onNodeSelected: (String) -> Unit,
    onSelectionCleared: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val surface = SurfaceCard
    val textPrimary = TextPrimary
    val textSecondary = TextSecondary
    val navy = NavyBlue
    val factionFill = MaterialTheme.colorScheme.secondaryContainer
    val factionStroke = MaterialTheme.colorScheme.secondary
    val campaignFill = MaterialTheme.colorScheme.tertiaryContainer
    val campaignStroke = MaterialTheme.colorScheme.tertiary
    var pan by remember { mutableStateOf(Offset.Zero) }
    var scale by remember { mutableStateOf(1f) }
    var hoverPosition by remember { mutableStateOf<Offset?>(null) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(surface)
            .pointerInput(Unit) {
                detectTransformGestures { _, panChange, zoomChange, _ ->
                    scale = (scale * zoomChange).coerceIn(MinScale, MaxScale)
                    pan += panChange
                }
            }
            .pointerInput(nodes, positions, pan, scale) {
                detectTapGestures { tap ->
                    val world = screenToWorld(tap, pan, scale, size.width.toFloat(), size.height.toFloat())
                    val hit = hitNode(world, nodes, positions)
                    if (hit == null) {
                        onSelectionCleared()
                    } else {
                        onNodeSelected(hit)
                    }
                }
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        when (event.type) {
                            PointerEventType.Move -> {
                                hoverPosition = event.changes.first().position
                            }
                            PointerEventType.Exit -> {
                                hoverPosition = null
                            }
                            PointerEventType.Scroll -> {
                                val delta = event.changes.first().scrollDelta.y
                                if (delta != 0f) {
                                    val factor = if (delta > 0f) 0.9f else 1.1f
                                    scale = (scale * factor).coerceIn(MinScale, MaxScale)
                                }
                            }
                            else -> Unit
                        }
                    }
                }
            }
    ) {
        val origin = Offset(size.width / 2f + pan.x, size.height / 2f + pan.y)
        fun toScreen(point: LinksViewState.LayoutPoint): Offset {
            return Offset(origin.x + point.x * scale, origin.y + point.y * scale)
        }

        val query = searchQuery.trim()
        val matchingIds = if (query.isEmpty()) {
            nodes.map { it.id }.toSet()
        } else {
            nodes.filter { it.name.contains(query, ignoreCase = true) }.map { it.id }.toSet()
        }
        val hoveredId = hoverPosition?.let { hover ->
            val world = screenToWorld(hover, pan, scale, size.width, size.height)
            hitNode(world, nodes, positions)
        }
        val hoveredEdgeId = if (hoveredId == null) {
            hoverPosition?.let { hover ->
                val world = screenToWorld(hover, pan, scale, size.width, size.height)
                hitEdge(world, edges, positions, scale)
            }
        } else {
            null
        }

        edges.forEach { edge ->
            val from = positions[edge.fromId] ?: return@forEach
            val to = positions[edge.toId] ?: return@forEach
            val dimmed = query.isNotEmpty() &&
                edge.fromId !in matchingIds &&
                edge.toId !in matchingIds
            val selected = selectedNodeId == edge.fromId ||
                selectedNodeId == edge.toId ||
                hoveredEdgeId == edge.id
            val color = edgeColor(edge).copy(alpha = if (dimmed) 0.18f else if (selected) 0.95f else 0.55f)
            drawLine(
                color = color,
                start = toScreen(from),
                end = toScreen(to),
                strokeWidth = if (selected) 3.2f else 1.8f,
                pathEffect = if (edge.kind == LinksViewState.EdgeKind.Membership) {
                    PathEffect.dashPathEffect(floatArrayOf(10f, 7f))
                } else {
                    null
                },
            )
        }

        nodes.forEach { node ->
            val point = positions[node.id] ?: return@forEach
            val center = toScreen(point)
            val radius = nodeRadius(node.kind) * scale
            val dimmed = query.isNotEmpty() && node.id !in matchingIds
            val selected = node.id == selectedNodeId || node.id == hoveredId
            val fill = nodeFill(node.kind, navy, factionFill, campaignFill)
                .copy(alpha = if (dimmed) 0.22f else 1f)
            val stroke = nodeStroke(node.kind, navy, factionStroke, campaignStroke)
                .copy(alpha = if (dimmed) 0.3f else 1f)
            drawCircle(color = fill, radius = radius, center = center)
            drawCircle(
                color = stroke,
                radius = radius,
                center = center,
                style = Stroke(width = if (selected) 4f else 2f),
            )
            val labelStyle = TextStyle(
                color = textPrimary.copy(alpha = if (dimmed) 0.35f else 1f),
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            )
            val measured = textMeasurer.measure(text = node.name, style = labelStyle)
            drawText(
                textMeasurer = textMeasurer,
                text = node.name,
                style = labelStyle,
                topLeft = Offset(
                    center.x - measured.size.width / 2f,
                    center.y + radius + 6f,
                ),
            )
        }

        val labelEdge = edges.firstOrNull { edge ->
            edge.id == hoveredEdgeId ||
                (selectedNodeId != null && (edge.fromId == selectedNodeId || edge.toId == selectedNodeId) &&
                    hoveredEdgeId == null && hoveredId == null)
        }
        if (labelEdge != null) {
            val from = positions[labelEdge.fromId]
            val to = positions[labelEdge.toId]
            if (from != null && to != null) {
                val mid = Offset(
                    (toScreen(from).x + toScreen(to).x) / 2f,
                    (toScreen(from).y + toScreen(to).y) / 2f,
                )
                val style = TextStyle(
                    color = textSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                )
                val measured = textMeasurer.measure(text = labelEdge.label, style = style)
                drawText(
                    textMeasurer = textMeasurer,
                    text = labelEdge.label,
                    style = style,
                    topLeft = Offset(
                        mid.x - measured.size.width / 2f,
                        mid.y - measured.size.height - 4f,
                    ),
                )
            }
        }
    }
}

private fun nodeRadius(kind: LinksViewState.NodeKind): Float {
    return when (kind) {
        LinksViewState.NodeKind.Faction -> 28f
        LinksViewState.NodeKind.WorldPerson,
        LinksViewState.NodeKind.CampaignPerson,
        -> 16f
    }
}

private fun nodeFill(
    kind: LinksViewState.NodeKind,
    navy: Color,
    factionFill: Color,
    campaignFill: Color,
): Color {
    return when (kind) {
        LinksViewState.NodeKind.Faction -> factionFill
        LinksViewState.NodeKind.CampaignPerson -> campaignFill
        LinksViewState.NodeKind.WorldPerson -> navy.copy(alpha = 0.22f)
    }
}

private fun nodeStroke(
    kind: LinksViewState.NodeKind,
    navy: Color,
    factionStroke: Color,
    campaignStroke: Color,
): Color {
    return when (kind) {
        LinksViewState.NodeKind.Faction -> factionStroke
        LinksViewState.NodeKind.CampaignPerson -> campaignStroke
        LinksViewState.NodeKind.WorldPerson -> navy
    }
}

private fun edgeColor(edge: LinksViewState.Edge): Color {
    return when (edge.kind) {
        LinksViewState.EdgeKind.Membership -> Color(0xFF64748B)
        LinksViewState.EdgeKind.Relationship -> relationshipColor(edge.relationshipType)
    }
}

private fun relationshipColor(type: RelationshipType?): Color {
    return when (type) {
        RelationshipType.Parent,
        RelationshipType.Child,
        RelationshipType.Sibling,
        RelationshipType.Spouse,
        RelationshipType.Ancestor,
        RelationshipType.Descendant,
        -> Color(0xFF8B5CF6)
        RelationshipType.Mentor,
        RelationshipType.Student,
        -> Color(0xFF0EA5E9)
        RelationshipType.Ally -> SuccessGreen
        RelationshipType.Rival,
        RelationshipType.Enemy,
        -> ErrorRed
        RelationshipType.Other, null -> Color(0xFF94A3B8)
    }
}

private fun screenToWorld(
    screen: Offset,
    pan: Offset,
    scale: Float,
    width: Float,
    height: Float,
): Offset {
    val originX = width / 2f + pan.x
    val originY = height / 2f + pan.y
    return Offset(
        x = (screen.x - originX) / scale,
        y = (screen.y - originY) / scale,
    )
}

private fun hitNode(
    world: Offset,
    nodes: List<LinksViewState.Node>,
    positions: Map<String, LinksViewState.LayoutPoint>,
): String? {
    return nodes
        .mapNotNull { node ->
            val point = positions[node.id] ?: return@mapNotNull null
            val radius = nodeRadius(node.kind) + 8f
            val distance = hypot(world.x - point.x, world.y - point.y)
            if (distance <= radius) node.id to distance else null
        }
        .minByOrNull { it.second }
        ?.first
}

private fun hitEdge(
    world: Offset,
    edges: List<LinksViewState.Edge>,
    positions: Map<String, LinksViewState.LayoutPoint>,
    scale: Float,
): String? {
    val threshold = 10f / scale
    return edges
        .mapNotNull { edge ->
            val from = positions[edge.fromId] ?: return@mapNotNull null
            val to = positions[edge.toId] ?: return@mapNotNull null
            val distance = distanceToSegment(world, Offset(from.x, from.y), Offset(to.x, to.y))
            if (distance <= threshold) edge.id to distance else null
        }
        .minByOrNull { it.second }
        ?.first
}

private fun distanceToSegment(point: Offset, start: Offset, end: Offset): Float {
    val dx = end.x - start.x
    val dy = end.y - start.y
    val lengthSquared = dx * dx + dy * dy
    if (lengthSquared == 0f) {
        return hypot(point.x - start.x, point.y - start.y)
    }
    val t = ((point.x - start.x) * dx + (point.y - start.y) * dy) / lengthSquared
    val clamped = t.coerceIn(0f, 1f)
    val nearest = Offset(start.x + clamped * dx, start.y + clamped * dy)
    return hypot(point.x - nearest.x, point.y - nearest.y)
}

private const val MinScale = 0.3f
private const val MaxScale = 4f
