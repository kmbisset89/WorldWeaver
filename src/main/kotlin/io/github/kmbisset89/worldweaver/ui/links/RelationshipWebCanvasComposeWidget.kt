package io.github.kmbisset89.worldweaver.ui.links

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntSize
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary
import kotlin.math.hypot

@Composable
internal fun RelationshipWebCanvasComposeWidget(
    nodes: List<LinksViewState.Node>,
    edges: List<LinksViewState.Edge>,
    selectedNodeId: String?,
    searchQuery: String,
    onNodeSelected: (String) -> Unit,
    onSelectionCleared: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val drawer = remember { RelationshipWebCanvasDrawer() }
    val layoutFactory = remember { RelationshipWebForceLayoutFactory() }
    val gestureState = remember { CanvasGestureState() }
    val background = MaterialTheme.colorScheme.background
    val palette = RelationshipWebCanvasDrawer.Palette(
        textPrimary = TextPrimary,
        textSecondary = TextSecondary,
        navy = NavyBlue,
        factionFill = MaterialTheme.colorScheme.secondaryContainer,
        factionStroke = MaterialTheme.colorScheme.secondary,
        campaignFill = MaterialTheme.colorScheme.tertiaryContainer,
        campaignStroke = MaterialTheme.colorScheme.tertiary,
    )
    var pan by remember { mutableStateOf(Offset.Zero) }
    var scale by remember { mutableStateOf(1f) }
    var hoverPosition by remember { mutableStateOf<Offset?>(null) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var positions by remember { mutableStateOf<Map<String, LinksViewState.LayoutPoint>>(emptyMap()) }
    var pinnedNodeId by remember { mutableStateOf<String?>(null) }
    var simTick by remember { mutableIntStateOf(0) }
    val nodeKey = remember(nodes) { nodes.joinToString(separator = ",") { it.id } }
    val edgeKey = remember(edges) { edges.joinToString(separator = ",") { it.id } }
    val degrees = remember(nodes, edges) {
        nodes.associate { node ->
            node.id to edges.count { edge -> edge.fromId == node.id || edge.toId == node.id }
        }
    }
    gestureState.pan = pan
    gestureState.scale = scale
    gestureState.positions = positions
    gestureState.degrees = degrees
    gestureState.canvasSize = canvasSize

    LaunchedEffect(nodeKey, edgeKey) {
        positions = layoutFactory.seed(nodes, edges, positions)
        simTick += 1
    }

    LaunchedEffect(simTick) {
        if (simTick == 0) {
            return@LaunchedEffect
        }
        var frames = 0
        while (true) {
            val settled = withFrameNanos {
                val pinned = setOfNotNull(pinnedNodeId)
                val current = positions
                val next = layoutFactory.step(current, nodes, edges, pinned)
                positions = next
                val count = nodes.size.coerceAtLeast(1)
                pinned.isEmpty() && layoutFactory.energy(current, next, pinned) / count < SettleEnergy
            }
            frames += 1
            if (settled || frames >= MaxSimFrames) {
                break
            }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(background)
            .onSizeChanged { canvasSize = it }
            .pointerInput(nodes) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val world = screenToWorld(
                        screen = down.position,
                        pan = gestureState.pan,
                        scale = gestureState.scale,
                        width = size.width.toFloat(),
                        height = size.height.toFloat(),
                    )
                    val hit = hitNode(
                        world = world,
                        nodes = nodes,
                        positions = gestureState.positions,
                        degrees = gestureState.degrees,
                        drawer = drawer,
                    )
                    val start = down.position
                    var last = down.position
                    var dragged = false
                    if (hit != null) {
                        pinnedNodeId = hit
                        simTick += 1
                        drag(down.id) { change ->
                            val nextWorld = screenToWorld(
                                screen = change.position,
                                pan = gestureState.pan,
                                scale = gestureState.scale,
                                width = size.width.toFloat(),
                                height = size.height.toFloat(),
                            )
                            val nextPositions = gestureState.positions + (
                                hit to LinksViewState.LayoutPoint(nextWorld.x, nextWorld.y)
                                )
                            gestureState.positions = nextPositions
                            positions = nextPositions
                            if (!dragged && hypot(
                                    change.position.x - start.x,
                                    change.position.y - start.y,
                                ) > DragSlop
                            ) {
                                dragged = true
                            }
                            change.consume()
                        }
                        pinnedNodeId = null
                        simTick += 1
                    } else {
                        drag(down.id) { change ->
                            val nextPan = gestureState.pan + (change.position - last)
                            last = change.position
                            gestureState.pan = nextPan
                            pan = nextPan
                            if (!dragged && hypot(
                                    change.position.x - start.x,
                                    change.position.y - start.y,
                                ) > DragSlop
                            ) {
                                dragged = true
                            }
                            change.consume()
                        }
                    }
                    if (!dragged) {
                        if (hit == null) {
                            onSelectionCleared()
                        } else {
                            onNodeSelected(hit)
                        }
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
                                val change = event.changes.first()
                                val delta = change.scrollDelta.y
                                val width = gestureState.canvasSize.width.toFloat()
                                val height = gestureState.canvasSize.height.toFloat()
                                if (delta != 0f && width > 0f && height > 0f) {
                                    val factor = if (delta > 0f) 0.9f else 1.1f
                                    val zoomed = zoomToward(
                                        pointer = change.position,
                                        pan = gestureState.pan,
                                        scale = gestureState.scale,
                                        factor = factor,
                                        width = width,
                                        height = height,
                                    )
                                    gestureState.scale = zoomed.scale
                                    gestureState.pan = zoomed.pan
                                    scale = zoomed.scale
                                    pan = zoomed.pan
                                }
                            }
                            else -> Unit
                        }
                    }
                }
            }
    ) {
        val origin = Offset(size.width / 2f + pan.x, size.height / 2f + pan.y)
        val query = searchQuery.trim()
        val matchingIds = if (query.isEmpty()) {
            nodes.map { it.id }.toSet()
        } else {
            nodes.filter { it.name.contains(query, ignoreCase = true) }.map { it.id }.toSet()
        }
        val hoveredId = hoverPosition?.let { hover ->
            val world = screenToWorld(hover, pan, scale, size.width, size.height)
            hitNode(world, nodes, positions, degrees, drawer)
        }
        val hoveredEdgeId = if (hoveredId == null) {
            hoverPosition?.let { hover ->
                val world = screenToWorld(hover, pan, scale, size.width, size.height)
                hitEdge(world, edges, positions, scale)
            }
        } else {
            null
        }
        drawer.draw(
            scope = this,
            textMeasurer = textMeasurer,
            request = RelationshipWebCanvasDrawer.Request(
                nodes = nodes,
                edges = edges,
                positions = positions,
                degrees = degrees,
                origin = origin,
                scale = scale,
                selectedNodeId = selectedNodeId,
                hoveredNodeId = hoveredId,
                hoveredEdgeId = hoveredEdgeId,
                matchingIds = matchingIds,
                searchActive = query.isNotEmpty(),
                palette = palette,
            ),
        )
    }
}

private class CanvasGestureState {
    var pan: Offset = Offset.Zero
    var scale: Float = 1f
    var positions: Map<String, LinksViewState.LayoutPoint> = emptyMap()
    var degrees: Map<String, Int> = emptyMap()
    var canvasSize: IntSize = IntSize.Zero
}

private fun zoomToward(
    pointer: Offset,
    pan: Offset,
    scale: Float,
    factor: Float,
    width: Float,
    height: Float,
): ZoomedView {
    val world = screenToWorld(pointer, pan, scale, width, height)
    val nextScale = (scale * factor).coerceIn(MinScale, MaxScale)
    return ZoomedView(
        scale = nextScale,
        pan = Offset(
            x = pointer.x - width / 2f - world.x * nextScale,
            y = pointer.y - height / 2f - world.y * nextScale,
        ),
    )
}

private data class ZoomedView(
    val scale: Float,
    val pan: Offset,
)

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
    degrees: Map<String, Int>,
    drawer: RelationshipWebCanvasDrawer,
): String? {
    return nodes
        .mapNotNull { node ->
            val point = positions[node.id] ?: return@mapNotNull null
            val radius = drawer.radius(node.kind, degrees[node.id] ?: 0) + 8f
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
private const val DragSlop = 6f
private const val SettleEnergy = 0.08f
private const val MaxSimFrames = 360
