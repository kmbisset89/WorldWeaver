package io.github.kmbisset89.worldweaver.domain

internal class DeleteBattleMapSituationUseCase(
    private val situationRepository: BattleMapSituationRepository,
    private val fileStore: BattleMapFileStore,
) {
    sealed interface Result {
        data object Deleted : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(situationId: String): Result {
        val existing = situationRepository.getById(situationId) ?: return Result.NotFound
        situationRepository.delete(existing.id)
        fileStore.deleteSituation(existing.battleMapId, existing.id)
        return Result.Deleted
    }
}
