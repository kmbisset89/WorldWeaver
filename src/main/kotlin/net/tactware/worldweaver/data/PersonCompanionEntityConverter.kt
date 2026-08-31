package net.tactware.worldweaver.data

import net.tactware.worldweaver.domain.CompanionKind
import net.tactware.worldweaver.domain.PersonCompanion
import net.tactware.worldweaver.domain.PersonRef

internal class PersonCompanionEntityConverter {
    fun toCompanion(entity: PersonCompanionEntity): PersonCompanion {
        return PersonCompanion(
            id = entity.id,
            owner = toRef(entity.ownerKind, entity.ownerId),
            companion = toRef(entity.companionKind, entity.companionId),
            kind = CompanionKind.fromStorage(entity.type),
        )
    }

    fun toEntity(companion: PersonCompanion): PersonCompanionEntity {
        return PersonCompanionEntity(
            id = companion.id,
            ownerKind = kindName(companion.owner),
            ownerId = companion.owner.id,
            companionKind = kindName(companion.companion),
            companionId = companion.companion.id,
            type = companion.kind.name,
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
