package io.github.kmbisset89.worldweaver.ui.links

import io.github.kmbisset89.worldweaver.domain.PersonKind
import io.github.kmbisset89.worldweaver.domain.RelationshipType
import io.github.kmbisset89.worldweaver.ui.characters.PersonMembership

internal sealed class LinksViewState {
    data object Loading : LinksViewState()

    data class Error(
        val message: String,
        val canRetry: Boolean,
    ) : LinksViewState()

    data object NoActiveWorld : LinksViewState()

    data class Empty(
        val worldName: String,
        val campaignName: String?,
        val searchQuery: String,
        val showIsolates: Boolean,
        val showMemberships: Boolean,
        val enabledRelationshipTypes: Set<RelationshipType>,
        val hiddenByFilters: Boolean,
    ) : LinksViewState()

    data class Content(
        val worldName: String,
        val campaignName: String?,
        val nodes: List<Node>,
        val edges: List<Edge>,
        val positions: Map<String, LayoutPoint>,
        val selectedNodeId: String?,
        val inspector: Inspector?,
        val searchQuery: String,
        val showIsolates: Boolean,
        val showMemberships: Boolean,
        val enabledRelationshipTypes: Set<RelationshipType>,
    ) : LinksViewState()

    data class Node(
        val id: String,
        val name: String,
        val kind: NodeKind,
        val personKind: PersonKind?,
        val personMembership: PersonMembership?,
        val personId: String?,
        val factionId: String?,
    )

    enum class NodeKind {
        WorldPerson,
        CampaignPerson,
        Faction,
    }

    data class Edge(
        val id: String,
        val fromId: String,
        val toId: String,
        val kind: EdgeKind,
        val label: String,
        val relationshipType: RelationshipType?,
    )

    enum class EdgeKind {
        Relationship,
        Membership,
    }

    data class Inspector(
        val nodeId: String,
        val name: String,
        val subtitle: String,
        val edges: List<InspectorEdge>,
    )

    data class InspectorEdge(
        val id: String,
        val label: String,
    )

    data class LayoutPoint(
        val x: Float,
        val y: Float,
    )
}
