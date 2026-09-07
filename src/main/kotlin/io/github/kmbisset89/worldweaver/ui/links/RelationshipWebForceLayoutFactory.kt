package io.github.kmbisset89.worldweaver.ui.links

import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Settles relationship-web positions with repulsion, edge springs, and a weak pull toward the origin.
 *
 * Membership springs are stronger than relationship springs so faction groups stay clustered.
 * Surviving node ids keep [previous] coordinates; new nodes start from the ring seed layout.
 */
internal class RelationshipWebForceLayoutFactory(
    private val seedFactory: RelationshipWebLayoutFactory = RelationshipWebLayoutFactory(),
) {

    fun create(
        nodes: List<LinksViewState.Node>,
        edges: List<LinksViewState.Edge>,
        previous: Map<String, LinksViewState.LayoutPoint> = emptyMap(),
        iterations: Int = DefaultIterations,
    ): Map<String, LinksViewState.LayoutPoint> {
        var positions = seed(nodes, edges, previous)
        repeat(iterations.coerceAtLeast(0)) {
            positions = step(positions, nodes, edges, pinnedIds = emptySet())
        }
        return positions
    }

    fun seed(
        nodes: List<LinksViewState.Node>,
        edges: List<LinksViewState.Edge>,
        previous: Map<String, LinksViewState.LayoutPoint>,
    ): Map<String, LinksViewState.LayoutPoint> {
        if (nodes.isEmpty()) {
            return emptyMap()
        }
        val seeded = seedFactory.create(nodes, edges)
        return nodes.associate { node ->
            node.id to (previous[node.id] ?: seeded.getValue(node.id))
        }
    }

    fun step(
        positions: Map<String, LinksViewState.LayoutPoint>,
        nodes: List<LinksViewState.Node>,
        edges: List<LinksViewState.Edge>,
        pinnedIds: Set<String>,
    ): Map<String, LinksViewState.LayoutPoint> {
        if (nodes.isEmpty()) {
            return emptyMap()
        }
        val forces = LinkedHashMap<String, Force>(nodes.size)
        nodes.forEach { node ->
            forces[node.id] = Force()
        }
        applyRepulsion(nodes, positions, forces)
        applySprings(edges, positions, forces)
        applyCentering(nodes, positions, forces)
        return nodes.associate { node ->
            val point = positions[node.id] ?: LinksViewState.LayoutPoint(0f, 0f)
            if (node.id in pinnedIds) {
                node.id to point
            } else {
                val force = forces.getValue(node.id)
                node.id to LinksViewState.LayoutPoint(
                    x = point.x + (force.x * Damping).coerceIn(-MaxDisplacement, MaxDisplacement),
                    y = point.y + (force.y * Damping).coerceIn(-MaxDisplacement, MaxDisplacement),
                )
            }
        }
    }

    fun energy(
        before: Map<String, LinksViewState.LayoutPoint>,
        after: Map<String, LinksViewState.LayoutPoint>,
        pinnedIds: Set<String>,
    ): Float {
        var total = 0f
        after.forEach { (id, point) ->
            if (id in pinnedIds) {
                return@forEach
            }
            val previous = before[id] ?: return@forEach
            total += hypot(point.x - previous.x, point.y - previous.y)
        }
        return total
    }

    private fun applyRepulsion(
        nodes: List<LinksViewState.Node>,
        positions: Map<String, LinksViewState.LayoutPoint>,
        forces: MutableMap<String, Force>,
    ) {
        for (i in nodes.indices) {
            val left = nodes[i]
            val leftPoint = positions[left.id] ?: continue
            for (j in i + 1 until nodes.size) {
                val right = nodes[j]
                val rightPoint = positions[right.id] ?: continue
                val delta = separation(left.id, leftPoint, rightPoint)
                val magnitude = Repulsion / (delta.distance * delta.distance)
                forces.getValue(left.id).add(-delta.nx * magnitude, -delta.ny * magnitude)
                forces.getValue(right.id).add(delta.nx * magnitude, delta.ny * magnitude)
            }
        }
    }

    private fun applySprings(
        edges: List<LinksViewState.Edge>,
        positions: Map<String, LinksViewState.LayoutPoint>,
        forces: MutableMap<String, Force>,
    ) {
        edges.forEach { edge ->
            val from = positions[edge.fromId] ?: return@forEach
            val to = positions[edge.toId] ?: return@forEach
            if (edge.fromId !in forces || edge.toId !in forces) {
                return@forEach
            }
            val delta = separation(edge.id, from, to)
            val rest = when (edge.kind) {
                LinksViewState.EdgeKind.Membership -> MembershipRestLength
                LinksViewState.EdgeKind.Relationship -> RelationshipRestLength
            }
            val stiffness = when (edge.kind) {
                LinksViewState.EdgeKind.Membership -> MembershipStiffness
                LinksViewState.EdgeKind.Relationship -> RelationshipStiffness
            }
            val magnitude = stiffness * (delta.distance - rest)
            forces.getValue(edge.fromId).add(delta.nx * magnitude, delta.ny * magnitude)
            forces.getValue(edge.toId).add(-delta.nx * magnitude, -delta.ny * magnitude)
        }
    }

    private fun applyCentering(
        nodes: List<LinksViewState.Node>,
        positions: Map<String, LinksViewState.LayoutPoint>,
        forces: MutableMap<String, Force>,
    ) {
        nodes.forEach { node ->
            val point = positions[node.id] ?: return@forEach
            forces.getValue(node.id).add(-point.x * Gravity, -point.y * Gravity)
        }
    }

    private fun separation(
        stableId: String,
        from: LinksViewState.LayoutPoint,
        to: LinksViewState.LayoutPoint,
    ): Separation {
        var dx = to.x - from.x
        var dy = to.y - from.y
        var distance = hypot(dx, dy)
        if (distance < MinDistance) {
            val angle = (stableId.hashCode() and 0xFFFF) / 65535f * TwoPi
            dx = cos(angle)
            dy = sin(angle)
            distance = MinDistance
        }
        return Separation(
            distance = distance,
            nx = dx / distance,
            ny = dy / distance,
        )
    }

    private class Force(
        var x: Float = 0f,
        var y: Float = 0f,
    ) {
        fun add(dx: Float, dy: Float) {
            x += dx
            y += dy
        }
    }

    private class Separation(
        val distance: Float,
        val nx: Float,
        val ny: Float,
    )

    private companion object {
        const val DefaultIterations = 220
        const val Repulsion = 4200f
        const val RelationshipStiffness = 0.045f
        const val MembershipStiffness = 0.09f
        const val RelationshipRestLength = 140f
        const val MembershipRestLength = 90f
        const val Gravity = 0.012f
        const val Damping = 0.72f
        const val MaxDisplacement = 24f
        const val MinDistance = 0.01f
        const val TwoPi = (Math.PI * 2.0).toFloat()
    }
}
