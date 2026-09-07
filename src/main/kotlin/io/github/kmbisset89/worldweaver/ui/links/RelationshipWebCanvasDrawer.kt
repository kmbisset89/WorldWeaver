package io.github.kmbisset89.worldweaver.ui.links

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.RelationshipType
import io.github.kmbisset89.worldweaver.ui.theme.ErrorRed
import io.github.kmbisset89.worldweaver.ui.theme.SuccessGreen
import kotlin.math.hypot
import kotlin.math.sqrt

/**
 * Paints the relationship web onto a [DrawScope] with glow, focus dimming, and zoom-aware labels.
 */
internal class RelationshipWebCanvasDrawer {

    data class Palette(
        val textPrimary: Color,
        val textSecondary: Color,
        val navy: Color,
        val factionFill: Color,
        val factionStroke: Color,
        val campaignFill: Color,
        val campaignStroke: Color,
    )

    data class Request(
        val nodes: List<LinksViewState.Node>,
        val edges: List<LinksViewState.Edge>,
        val positions: Map<String, LinksViewState.LayoutPoint>,
        val degrees: Map<String, Int>,
        val origin: Offset,
        val scale: Float,
        val selectedNodeId: String?,
        val hoveredNodeId: String?,
        val hoveredEdgeId: String?,
        val matchingIds: Set<String>,
        val searchActive: Boolean,
        val palette: Palette,
    )

    fun radius(kind: LinksViewState.NodeKind, degree: Int): Float {
        val base = when (kind) {
            LinksViewState.NodeKind.Faction -> 16f
            LinksViewState.NodeKind.WorldPerson,
            LinksViewState.NodeKind.CampaignPerson,
            -> 9f
        }
        return base + sqrt(degree.coerceAtLeast(0).toFloat()) * 2.2f
    }

    fun draw(
        scope: DrawScope,
        textMeasurer: TextMeasurer,
        request: Request,
    ) {
        val focusIds = focusedNodeIds(request)
        val showAllLabels = request.scale >= LabelZoomThreshold
        request.edges.forEach { edge ->
            drawEdge(scope, request, edge, focusIds)
        }
        request.nodes.forEach { node ->
            drawNode(
                scope = scope,
                textMeasurer = textMeasurer,
                request = request,
                node = node,
                focusIds = focusIds,
                showAllLabels = showAllLabels,
            )
        }
        drawEdgeLabel(scope, textMeasurer, request)
    }

    private fun drawEdge(
        scope: DrawScope,
        request: Request,
        edge: LinksViewState.Edge,
        focusIds: Set<String>,
    ) {
        val from = request.positions[edge.fromId] ?: return
        val to = request.positions[edge.toId] ?: return
        val start = toScreen(from, request.origin, request.scale)
        val end = toScreen(to, request.origin, request.scale)
        val searchDimmed = request.searchActive &&
            edge.fromId !in request.matchingIds &&
            edge.toId !in request.matchingIds
        val focusDimmed = focusIds.isNotEmpty() &&
            edge.fromId !in focusIds &&
            edge.toId !in focusIds
        val dimmed = searchDimmed || focusDimmed
        val highlighted = edge.id == request.hoveredEdgeId ||
            request.selectedNodeId == edge.fromId ||
            request.selectedNodeId == edge.toId
        val alpha = when {
            dimmed -> 0.12f
            highlighted -> 0.92f
            else -> 0.42f
        }
        val path = Path().apply {
            moveTo(start.x, start.y)
            val control = curveControl(start, end, edge.id)
            quadraticTo(control.x, control.y, end.x, end.y)
        }
        scope.drawPath(
            path = path,
            color = edgeColor(edge).copy(alpha = alpha),
            style = Stroke(
                width = if (highlighted) 2.6f else 1.35f,
                pathEffect = if (edge.kind == LinksViewState.EdgeKind.Membership) {
                    PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                } else {
                    null
                },
            ),
        )
    }

    private fun drawNode(
        scope: DrawScope,
        textMeasurer: TextMeasurer,
        request: Request,
        node: LinksViewState.Node,
        focusIds: Set<String>,
        showAllLabels: Boolean,
    ) {
        val point = request.positions[node.id] ?: return
        val center = toScreen(point, request.origin, request.scale)
        val degree = request.degrees[node.id] ?: 0
        val radius = radius(node.kind, degree) * request.scale
        val searchDimmed = request.searchActive && node.id !in request.matchingIds
        val focusDimmed = focusIds.isNotEmpty() && node.id !in focusIds
        val dimmed = searchDimmed || focusDimmed
        val focused = node.id == request.selectedNodeId || node.id == request.hoveredNodeId
        val alpha = if (dimmed) 0.18f else 1f
        val fill = nodeFill(node.kind, request.palette).copy(alpha = alpha)
        val stroke = nodeStroke(node.kind, request.palette).copy(alpha = if (dimmed) 0.28f else 1f)
        val glowRadius = radius * 2.6f
        if (!dimmed) {
            scope.drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        stroke.copy(alpha = if (focused) 0.55f else 0.32f),
                        stroke.copy(alpha = 0f),
                    ),
                    center = center,
                    radius = glowRadius.coerceAtLeast(1f),
                ),
                radius = glowRadius,
                center = center,
            )
        }
        scope.drawCircle(color = fill, radius = radius, center = center)
        scope.drawCircle(
            color = stroke,
            radius = radius,
            center = center,
            style = Stroke(width = if (focused) 2.6f else 1.4f),
        )
        val showLabel = showAllLabels || focused || node.id in focusIds
        if (!showLabel) {
            return
        }
        val labelStyle = TextStyle(
            color = request.palette.textPrimary.copy(alpha = if (dimmed) 0.35f else 1f),
            fontSize = 11.sp,
            fontWeight = if (focused) FontWeight.SemiBold else FontWeight.Medium,
        )
        val measured = textMeasurer.measure(text = node.name, style = labelStyle)
        scope.drawText(
            textMeasurer = textMeasurer,
            text = node.name,
            style = labelStyle,
            topLeft = Offset(
                center.x - measured.size.width / 2f,
                center.y + radius + 6f,
            ),
        )
    }

    private fun drawEdgeLabel(
        scope: DrawScope,
        textMeasurer: TextMeasurer,
        request: Request,
    ) {
        val labelEdge = request.edges.firstOrNull { edge ->
            edge.id == request.hoveredEdgeId ||
                (
                    request.hoveredEdgeId == null &&
                        request.hoveredNodeId == null &&
                        request.selectedNodeId != null &&
                        (edge.fromId == request.selectedNodeId || edge.toId == request.selectedNodeId)
                    )
        } ?: return
        val from = request.positions[labelEdge.fromId] ?: return
        val to = request.positions[labelEdge.toId] ?: return
        val start = toScreen(from, request.origin, request.scale)
        val end = toScreen(to, request.origin, request.scale)
        val control = curveControl(start, end, labelEdge.id)
        val style = TextStyle(
            color = request.palette.textSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
        )
        val measured = textMeasurer.measure(text = labelEdge.label, style = style)
        scope.drawText(
            textMeasurer = textMeasurer,
            text = labelEdge.label,
            style = style,
            topLeft = Offset(
                control.x - measured.size.width / 2f,
                control.y - measured.size.height - 4f,
            ),
        )
    }

    private fun focusedNodeIds(request: Request): Set<String> {
        val focusId = request.hoveredNodeId ?: request.selectedNodeId ?: return emptySet()
        val neighbors = request.edges.flatMap { edge ->
            when (focusId) {
                edge.fromId -> listOf(edge.toId)
                edge.toId -> listOf(edge.fromId)
                else -> emptyList()
            }
        }
        return buildSet {
            add(focusId)
            addAll(neighbors)
        }
    }

    private fun toScreen(
        point: LinksViewState.LayoutPoint,
        origin: Offset,
        scale: Float,
    ): Offset {
        return Offset(origin.x + point.x * scale, origin.y + point.y * scale)
    }

    private fun curveControl(start: Offset, end: Offset, edgeId: String): Offset {
        val dx = end.x - start.x
        val dy = end.y - start.y
        val length = hypot(dx, dy).coerceAtLeast(1f)
        val nx = -dy / length
        val ny = dx / length
        val sign = if (edgeId.hashCode() >= 0) 1f else -1f
        val bulge = (18f).coerceAtMost(length * 0.18f) * sign
        return Offset(
            (start.x + end.x) / 2f + nx * bulge,
            (start.y + end.y) / 2f + ny * bulge,
        )
    }

    private fun nodeFill(kind: LinksViewState.NodeKind, palette: Palette): Color {
        return when (kind) {
            LinksViewState.NodeKind.Faction -> palette.factionFill
            LinksViewState.NodeKind.CampaignPerson -> palette.campaignFill
            LinksViewState.NodeKind.WorldPerson -> palette.navy.copy(alpha = 0.55f)
        }
    }

    private fun nodeStroke(kind: LinksViewState.NodeKind, palette: Palette): Color {
        return when (kind) {
            LinksViewState.NodeKind.Faction -> palette.factionStroke
            LinksViewState.NodeKind.CampaignPerson -> palette.campaignStroke
            LinksViewState.NodeKind.WorldPerson -> palette.navy
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

    private companion object {
        const val LabelZoomThreshold = 1.35f
    }
}
