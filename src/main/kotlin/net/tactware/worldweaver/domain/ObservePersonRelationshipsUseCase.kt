package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal class ObservePersonRelationshipsUseCase(
    private val personRelationshipRepository: PersonRelationshipRepository,
) {
    operator fun invoke(): Flow<List<PersonRelationship>> {
        return personRelationshipRepository.observeAll()
    }
}
