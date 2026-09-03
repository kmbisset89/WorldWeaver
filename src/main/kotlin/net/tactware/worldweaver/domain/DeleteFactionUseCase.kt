package net.tactware.worldweaver.domain

internal class DeleteFactionUseCase(
    private val factionRepository: FactionRepository,
    private val factionMembershipRepository: FactionMembershipRepository,
    private val personRelationshipRepository: PersonRelationshipRepository,
) {
    sealed interface Result {
        data object Deleted : Result
        data class Blocked(
            val membershipCount: Int,
            val relationshipCount: Int,
        ) : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(factionId: String): Result {
        factionRepository.getById(factionId) ?: return Result.NotFound
        val membershipCount = factionMembershipRepository.countByFaction(factionId)
        val relationshipCount = personRelationshipRepository.countByFaction(factionId)
        if (membershipCount > 0 || relationshipCount > 0) {
            return Result.Blocked(
                membershipCount = membershipCount,
                relationshipCount = relationshipCount,
            )
        }
        factionRepository.delete(factionId)
        return Result.Deleted
    }
}
