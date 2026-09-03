package io.github.kmbisset89.worldweaver.domain

internal class DeletePlotThreadUseCase(
    private val plotThreadRepository: PlotThreadRepository,
) {
    sealed interface Result {
        data object Deleted : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(threadId: String): Result {
        plotThreadRepository.getById(threadId) ?: return Result.NotFound
        plotThreadRepository.delete(threadId)
        return Result.Deleted
    }
}
