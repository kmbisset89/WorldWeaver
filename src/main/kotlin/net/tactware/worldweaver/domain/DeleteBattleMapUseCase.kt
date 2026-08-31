package net.tactware.worldweaver.domain

internal class DeleteBattleMapUseCase(
    private val battleMapRepository: BattleMapRepository,
    private val encounterRepository: EncounterRepository,
    private val fileStore: BattleMapFileStore,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data object Deleted : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(battleMapId: String): Result {
        val battleMap = battleMapRepository.getById(battleMapId) ?: return Result.NotFound
        val now = instantProvider.now()
        encounterRepository.getByCampaign(battleMap.campaignId)
            .filter { encounter -> encounter.battleMapId == battleMapId }
            .forEach { encounter ->
                encounterRepository.update(
                    encounter.copy(
                        battleMapId = null,
                        updatedAt = now,
                    )
                )
            }
        battleMapRepository.delete(battleMapId)
        fileStore.delete(battleMapId)
        return Result.Deleted
    }
}
