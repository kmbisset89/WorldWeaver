package io.github.kmbisset89.worldweaver.data

import io.github.kmbisset89.worldweaver.domain.FactionMembership
import io.github.kmbisset89.worldweaver.domain.PersonRef
import java.time.Instant

internal class FactionMembershipEntityConverter {
    fun toMembership(entity: FactionMembershipEntity): FactionMembership {
        return FactionMembership(
            id = entity.id,
            person = toRef(entity.personKind, entity.personId),
            factionId = entity.factionId,
            role = entity.role,
            notes = entity.notes,
            createdAt = Instant.ofEpochMilli(entity.createdAtEpochMillis),
        )
    }

    fun toEntity(membership: FactionMembership): FactionMembershipEntity {
        return FactionMembershipEntity(
            id = membership.id,
            personKind = kindName(membership.person),
            personId = membership.person.id,
            factionId = membership.factionId,
            role = membership.role,
            notes = membership.notes,
            createdAtEpochMillis = membership.createdAt.toEpochMilli(),
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
