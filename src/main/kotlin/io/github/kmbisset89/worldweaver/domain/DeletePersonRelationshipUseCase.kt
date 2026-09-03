package io.github.kmbisset89.worldweaver.domain

internal class DeletePersonRelationshipUseCase(
    private val personRelationshipRepository: PersonRelationshipRepository,
) {
    sealed interface Result {
        data object Deleted : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(relationshipId: String): Result {
        personRelationshipRepository.getById(relationshipId) ?: return Result.NotFound
        personRelationshipRepository.delete(relationshipId)
        return Result.Deleted
    }
}
