package net.tactware.worldweaver.ui.links

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

internal class RelationshipWebLayoutFactory {

    fun create(
        nodes: List<LinksViewState.Node>,
        edges: List<LinksViewState.Edge>,
    ): Map<String, LinksViewState.LayoutPoint> {
        if (nodes.isEmpty()) {
            return emptyMap()
        }
        val factions = nodes.filter { it.kind == LinksViewState.NodeKind.Faction }
            .sortedBy { it.name.lowercase() }
        val people = nodes.filter { it.kind != LinksViewState.NodeKind.Faction }
            .sortedBy { it.name.lowercase() }
        val firstFactionByPerson = edges
            .filter { it.kind == LinksViewState.EdgeKind.Membership }
            .groupBy { it.fromId }
            .mapValues { entry -> entry.value.minBy { it.id }.toId }
        val positions = linkedMapOf<String, LinksViewState.LayoutPoint>()
        placeOnRing(
            ids = factions.map { it.id },
            radius = factionRingRadius(factions.size),
            positions = positions,
        )
        val membersByFaction = people.groupBy { firstFactionByPerson[it.id] }
        membersByFaction.forEach { (factionId, members) ->
            if (factionId == null) {
                return@forEach
            }
            val center = positions[factionId] ?: return@forEach
            placeAround(
                ids = members.map { it.id },
                center = center,
                radius = memberRingRadius(members.size),
                positions = positions,
            )
        }
        val leftover = people.filter { it.id !in positions }.map { it.id }
        if (leftover.isNotEmpty()) {
            val leftoverRadius = if (factions.isEmpty()) {
                leftoverRingRadius(leftover.size)
            } else {
                leftoverRingRadius(leftover.size) + FactionRingRadius
            }
            placeOnRing(
                ids = leftover,
                radius = leftoverRadius,
                positions = positions,
            )
        }
        return positions
    }

    private fun placeOnRing(
        ids: List<String>,
        radius: Float,
        positions: MutableMap<String, LinksViewState.LayoutPoint>,
    ) {
        if (ids.isEmpty()) {
            return
        }
        if (ids.size == 1) {
            positions[ids.first()] = LinksViewState.LayoutPoint(0f, 0f)
            return
        }
        ids.forEachIndexed { index, id ->
            val angle = (TwoPi * index / ids.size) - HalfPi
            positions[id] = LinksViewState.LayoutPoint(
                x = cos(angle).toFloat() * radius,
                y = sin(angle).toFloat() * radius,
            )
        }
    }

    private fun placeAround(
        ids: List<String>,
        center: LinksViewState.LayoutPoint,
        radius: Float,
        positions: MutableMap<String, LinksViewState.LayoutPoint>,
    ) {
        if (ids.isEmpty()) {
            return
        }
        if (ids.size == 1) {
            positions[ids.first()] = LinksViewState.LayoutPoint(
                x = center.x + radius,
                y = center.y,
            )
            return
        }
        ids.forEachIndexed { index, id ->
            val angle = (TwoPi * index / ids.size) - HalfPi
            positions[id] = LinksViewState.LayoutPoint(
                x = center.x + cos(angle).toFloat() * radius,
                y = center.y + sin(angle).toFloat() * radius,
            )
        }
    }

    private fun factionRingRadius(count: Int): Float {
        if (count <= 1) {
            return 0f
        }
        return FactionRingRadius + max(0, count - 3) * 24f
    }

    private fun memberRingRadius(count: Int): Float {
        return MemberRingRadius + max(0, count - 6) * 12f
    }

    private fun leftoverRingRadius(count: Int): Float {
        return LeftoverRingRadius + max(0, count - 8) * 16f
    }

    private companion object {
        const val TwoPi = PI * 2.0
        const val HalfPi = PI / 2.0
        const val FactionRingRadius = 280f
        const val MemberRingRadius = 140f
        const val LeftoverRingRadius = 220f
    }
}
