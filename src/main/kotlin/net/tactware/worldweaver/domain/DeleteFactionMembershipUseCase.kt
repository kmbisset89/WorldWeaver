package net.tactware.worldweaver.domain

internal class DeleteFactionMembershipUseCase(
    private val factionMembershipRepository: FactionMembershipRepository,
) {
    sealed interface Result {
        data object Deleted : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(membershipId: String): Result {
        factionMembershipRepository.getById(membershipId) ?: return Result.NotFound
        factionMembershipRepository.delete(membershipId)
        return Result.Deleted
    }
}
