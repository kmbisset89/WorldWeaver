package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

internal class ObserveRelationshipWebUseCase(
    private val observeActiveContextDetails: ObserveActiveContextDetailsUseCase,
    private val observePeople: ObservePeopleForActiveContextUseCase,
    private val observeFactions: ObserveFactionsForActiveWorldUseCase,
    private val observeRelationships: ObservePersonRelationshipsUseCase,
    private val observeMemberships: ObserveFactionMembershipsUseCase,
    private val relationshipWebFactory: RelationshipWebFactory,
) {
    operator fun invoke(): Flow<RelationshipWebSnapshot> {
        return combine(
            observeActiveContextDetails(),
            observePeople(),
            observeFactions(),
            observeRelationships(),
            observeMemberships(),
        ) { details, people, factions, relationships, memberships ->
            RelationshipWebSnapshot(
                details = details,
                web = relationshipWebFactory.create(
                    people = people,
                    factions = factions,
                    relationships = relationships,
                    memberships = memberships,
                ),
            )
        }
    }
}
