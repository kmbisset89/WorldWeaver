package io.github.kmbisset89.worldweaver.domain

internal class ToggleBattleMapSituationUseCase(
    private val situationRepository: BattleMapSituationRepository,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data class Toggled(val situation: BattleMapSituation) : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(situationId: String): Result {
        val existing = situationRepository.getById(situationId) ?: return Result.NotFound
        val updated = existing.copy(
            visible = !existing.visible,
            updatedAt = instantProvider.now(),
        )
        situationRepository.update(updated)
        return Result.Toggled(updated)
    }
}
