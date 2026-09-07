package io.github.kmbisset89.worldweaver.ui.links

import io.github.kmbisset89.worldweaver.domain.PersonKind
import io.github.kmbisset89.worldweaver.domain.RelationshipType
import io.github.kmbisset89.worldweaver.ui.characters.PersonMembership
import kotlin.math.hypot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class RelationshipWebForceLayoutFactoryTest {
    private val factory = RelationshipWebForceLayoutFactory()

    @Test
    fun emptyGraphReturnsNoPositions() {
        assertTrue(factory.create(nodes = emptyList(), edges = emptyList()).isEmpty())
    }

    @Test
    fun linkedPairIsPulledTogether() {
        val previous = mapOf(
            "a" to LinksViewState.LayoutPoint(-400f, 0f),
            "b" to LinksViewState.LayoutPoint(400f, 0f),
        )
        val linked = factory.create(
            nodes = listOf(person("a"), person("b")),
            edges = listOf(relationship("e", "a", "b")),
            previous = previous,
        )
        val distance = distance(linked.getValue("a"), linked.getValue("b"))
        assertTrue(distance < 250f)
        assertTrue(distance > 40f)
    }

    @Test
    fun membershipPullsPersonTowardFaction() {
        val previous = mapOf(
            "faction" to LinksViewState.LayoutPoint(0f, 0f),
            "person" to LinksViewState.LayoutPoint(400f, 0f),
        )
        val settled = factory.create(
            nodes = listOf(person("person"), faction("faction")),
            edges = listOf(membership("m", "person", "faction")),
            previous = previous,
        )
        assertTrue(
            distance(settled.getValue("person"), settled.getValue("faction")) < 400f,
        )
    }

    @Test
    fun previousPositionsAreReusedForSurvivingIds() {
        val previous = mapOf("keep" to LinksViewState.LayoutPoint(12f, 34f))
        val seeded = factory.create(
            nodes = listOf(person("keep"), person("new")),
            edges = emptyList(),
            previous = previous,
            iterations = 0,
        )
        assertEquals(12f, seeded.getValue("keep").x)
        assertEquals(34f, seeded.getValue("keep").y)
        assertTrue(seeded.containsKey("new"))
        assertTrue(
            seeded.getValue("new").x != 12f || seeded.getValue("new").y != 34f,
        )
    }

    private fun distance(
        left: LinksViewState.LayoutPoint,
        right: LinksViewState.LayoutPoint,
    ): Float {
        return hypot(left.x - right.x, left.y - right.y)
    }

    private fun person(id: String): LinksViewState.Node {
        return LinksViewState.Node(
            id = id,
            name = id,
            kind = LinksViewState.NodeKind.WorldPerson,
            personKind = PersonKind.Npc,
            personMembership = PersonMembership.WorldLibrary,
            personId = id,
            factionId = null,
        )
    }

    private fun faction(id: String): LinksViewState.Node {
        return LinksViewState.Node(
            id = id,
            name = id,
            kind = LinksViewState.NodeKind.Faction,
            personKind = null,
            personMembership = null,
            personId = null,
            factionId = id,
        )
    }

    private fun relationship(id: String, fromId: String, toId: String): LinksViewState.Edge {
        return LinksViewState.Edge(
            id = id,
            fromId = fromId,
            toId = toId,
            kind = LinksViewState.EdgeKind.Relationship,
            label = "Ally",
            relationshipType = RelationshipType.Ally,
        )
    }

    private fun membership(id: String, fromId: String, toId: String): LinksViewState.Edge {
        return LinksViewState.Edge(
            id = id,
            fromId = fromId,
            toId = toId,
            kind = LinksViewState.EdgeKind.Membership,
            label = "Member",
            relationshipType = null,
        )
    }
}
