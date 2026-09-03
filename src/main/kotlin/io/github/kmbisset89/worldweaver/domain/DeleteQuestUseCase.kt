package io.github.kmbisset89.worldweaver.domain

internal class DeleteQuestUseCase(
    private val questRepository: QuestRepository,
) {
    sealed interface Result {
        data object Deleted : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(questId: String): Result {
        questRepository.getById(questId) ?: return Result.NotFound
        questRepository.delete(questId)
        return Result.Deleted
    }
}
