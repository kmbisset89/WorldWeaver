package net.tactware.worldweaver.domain

internal data class RelationshipWeb(
    val nodes: List<Node>,
    val edges: List<Edge>,
) {
    sealed class Node {
        abstract val id: String
        abstract val name: String

        data class Person(
            override val id: String,
            override val name: String,
            val ref: PersonRef,
            val kind: PersonKind,
        ) : Node()

        data class Faction(
            override val id: String,
            override val name: String,
            val factionId: String,
        ) : Node()
    }

    sealed class Edge {
        abstract val id: String
        abstract val fromId: String
        abstract val toId: String
        abstract val label: String

        data class Relationship(
            override val id: String,
            override val fromId: String,
            override val toId: String,
            val type: RelationshipType,
            val description: String,
            val factionLeanName: String?,
        ) : Edge() {
            override val label: String
                get() = type.displayName
        }

        data class Membership(
            override val id: String,
            override val fromId: String,
            override val toId: String,
            val role: String,
        ) : Edge() {
            override val label: String
                get() = if (role.isBlank()) "Member" else role
        }
    }
}
