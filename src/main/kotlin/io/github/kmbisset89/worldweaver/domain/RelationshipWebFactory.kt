package io.github.kmbisset89.worldweaver.domain

internal class RelationshipWebFactory {

    fun personNodeId(ref: PersonRef): String {
        return when (ref) {
            is PersonRef.World -> "person:world:${ref.id}"
            is PersonRef.Campaign -> "person:campaign:${ref.id}"
        }
    }

    fun factionNodeId(factionId: String): String {
        return "faction:$factionId"
    }

    fun create(
        people: PeopleSnapshot,
        factions: List<Faction>,
        relationships: List<PersonRelationship>,
        memberships: List<FactionMembership>,
    ): RelationshipWeb {
        val personNodes = personNodes(people)
        val factionNodes = factions
            .sortedBy { it.name.lowercase() }
            .map { faction ->
                RelationshipWeb.Node.Faction(
                    id = factionNodeId(faction.id),
                    name = faction.name,
                    factionId = faction.id,
                )
            }
        val nodesById = (personNodes + factionNodes).associateBy { it.id }
        val factionNames = factions.associate { it.id to it.name }
        val edges = buildList {
            relationships.forEach { relationship ->
                val fromId = personNodeId(relationship.from)
                val toId = personNodeId(relationship.to)
                if (fromId !in nodesById || toId !in nodesById) {
                    return@forEach
                }
                add(
                    RelationshipWeb.Edge.Relationship(
                        id = relationship.id,
                        fromId = fromId,
                        toId = toId,
                        type = relationship.type,
                        description = relationship.description,
                        factionLeanName = relationship.factionId?.let(factionNames::get),
                    )
                )
            }
            memberships.forEach { membership ->
                val fromId = personNodeId(membership.person)
                val toId = factionNodeId(membership.factionId)
                if (fromId !in nodesById || toId !in nodesById) {
                    return@forEach
                }
                add(
                    RelationshipWeb.Edge.Membership(
                        id = membership.id,
                        fromId = fromId,
                        toId = toId,
                        role = membership.role,
                    )
                )
            }
        }
        return RelationshipWeb(
            nodes = (personNodes + factionNodes).sortedBy { it.name.lowercase() },
            edges = edges,
        )
    }

    fun filter(
        web: RelationshipWeb,
        includeMemberships: Boolean,
        enabledRelationshipTypes: Set<RelationshipType>,
        includeIsolates: Boolean,
    ): RelationshipWeb {
        val edges = web.edges.filter { edge ->
            when (edge) {
                is RelationshipWeb.Edge.Relationship -> edge.type in enabledRelationshipTypes
                is RelationshipWeb.Edge.Membership -> includeMemberships
            }
        }
        val connectedIds = buildSet {
            edges.forEach { edge ->
                add(edge.fromId)
                add(edge.toId)
            }
        }
        val nodes = if (includeIsolates) {
            web.nodes
        } else {
            web.nodes.filter { it.id in connectedIds }
        }
        return RelationshipWeb(nodes = nodes, edges = edges)
    }

    private fun personNodes(people: PeopleSnapshot): List<RelationshipWeb.Node.Person> {
        val worldNodes = people.worldPeople.map { person ->
            RelationshipWeb.Node.Person(
                id = personNodeId(PersonRef.World(person.id)),
                name = person.name,
                ref = PersonRef.World(person.id),
                kind = person.kind,
            )
        }
        val campaignNodes = people.campaignPeople.map { person ->
            RelationshipWeb.Node.Person(
                id = personNodeId(PersonRef.Campaign(person.id)),
                name = person.name,
                ref = PersonRef.Campaign(person.id),
                kind = person.kind,
            )
        }
        return worldNodes + campaignNodes
    }
}
