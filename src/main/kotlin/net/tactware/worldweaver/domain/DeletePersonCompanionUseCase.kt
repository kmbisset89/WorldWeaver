package net.tactware.worldweaver.domain

internal class DeletePersonCompanionUseCase(
    private val personCompanionRepository: PersonCompanionRepository,
) {
    sealed interface Result {
        data object Deleted : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(companionId: String): Result {
        personCompanionRepository.getById(companionId) ?: return Result.NotFound
        personCompanionRepository.delete(companionId)
        return Result.Deleted
    }
}
