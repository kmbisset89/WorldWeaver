package net.tactware.worldweaver.domain

import kotlinx.coroutines.flow.Flow

internal class ObserveFactionMembershipsUseCase(
    private val factionMembershipRepository: FactionMembershipRepository,
) {
    operator fun invoke(): Flow<List<FactionMembership>> {
        return factionMembershipRepository.observeAll()
    }
}
