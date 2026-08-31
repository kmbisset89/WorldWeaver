package net.tactware.worldweaver.data

import net.tactware.worldweaver.domain.PersonRef
import net.tactware.worldweaver.domain.PersonRelationship
import net.tactware.worldweaver.domain.RelationshipType

internal class PersonRelationshipEntityConverter {
    fun toRelationship(entity: PersonRelationshipEntity): PersonRelationship {
        return PersonRelationship(
            id = entity.id,
            from = toRef(entity.fromKind, entity.fromId),
            to = toRef(entity.toKind, entity.toId),
            type = RelationshipType.fromStorage(entity.type),
            description = entity.description,
            factionLean = entity.factionLean,
        )
    }

    fun toEntity(relationship: PersonRelationship): PersonRelationshipEntity {
        return PersonRelationshipEntity(
            id = relationship.id,
            fromKind = kindName(relationship.from),
            fromId = relationship.from.id,
            toKind = kindName(relationship.to),
            toId = relationship.to.id,
            type = relationship.type.name,
            description = relationship.description,
            factionLean = relationship.factionLean,
        )
    }

    fun kindName(ref: PersonRef): String {
        return when (ref) {
            is PersonRef.World -> WORLD
            is PersonRef.Campaign -> CAMPAIGN
        }
    }

    private fun toRef(kind: String, id: String): PersonRef {
        return when (kind) {
            CAMPAIGN -> PersonRef.Campaign(id)
            else -> PersonRef.World(id)
        }
    }

    private companion object {
        const val WORLD = "World"
        const val CAMPAIGN = "Campaign"
    }
}
