package io.github.kmbisset89.worldweaver.domain

internal class UpdateBattleMapTerrainUseCase(
    private val battleMapRepository: BattleMapRepository,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data object Updated : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(battleMapId: String, edit: BattleMapTerrainEdit): Result {
        val existing = battleMapRepository.getById(battleMapId) ?: return Result.NotFound
        val next = applyEdit(existing, edit)
        battleMapRepository.update(next.copy(updatedAt = instantProvider.now()))
        return Result.Updated
    }

    private fun applyEdit(map: BattleMap, edit: BattleMapTerrainEdit): BattleMap {
        return when (edit) {
            is BattleMapTerrainEdit.SetBlocked -> {
                map.copy(
                    blockedCells = map.blockedCells + edit.cells,
                    difficultCells = map.difficultCells - edit.cells,
                )
            }
            is BattleMapTerrainEdit.SetDifficult -> {
                map.copy(
                    difficultCells = map.difficultCells + edit.cells,
                    blockedCells = map.blockedCells - edit.cells,
                )
            }
            is BattleMapTerrainEdit.Clear -> {
                map.copy(
                    blockedCells = map.blockedCells - edit.cells,
                    difficultCells = map.difficultCells - edit.cells,
                )
            }
        }
    }
}
