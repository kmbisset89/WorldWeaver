package io.github.kmbisset89.worldweaver.domain

internal class DeleteReferenceDocUseCase(
    private val referenceDocRepository: ReferenceDocRepository,
) {
    sealed interface Result {
        data object Deleted : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(docId: String): Result {
        referenceDocRepository.getById(docId) ?: return Result.NotFound
        referenceDocRepository.delete(docId)
        return Result.Deleted
    }
}
