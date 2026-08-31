package net.tactware.worldweaver.domain

internal data class PersonRelationship(
    val id: String,
    val from: PersonRef,
    val to: PersonRef,
    val type: RelationshipType,
    val description: String,
    val factionLean: String,
)
