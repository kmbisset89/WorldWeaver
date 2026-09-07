package io.github.kmbisset89.worldweaver.domain

internal class DeleteRandomTableUseCase(
    private val randomTableRepository: RandomTableRepository,
) {
    sealed interface Result {
        data object Deleted : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(tableId: String): Result {
        randomTableRepository.getById(tableId) ?: return Result.NotFound
        randomTableRepository.delete(tableId)
        return Result.Deleted
    }
}
