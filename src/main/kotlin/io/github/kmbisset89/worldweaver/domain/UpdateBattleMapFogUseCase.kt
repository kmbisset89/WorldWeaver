package io.github.kmbisset89.worldweaver.domain

internal class UpdateBattleMapFogUseCase(
    private val battleMapRepository: BattleMapRepository,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data object Updated : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(battleMapId: String, edit: BattleMapFogEdit): Result {
        val existing = battleMapRepository.getById(battleMapId) ?: return Result.NotFound
        val next = applyEdit(existing, edit)
        battleMapRepository.update(
            next.copy(updatedAt = instantProvider.now()),
        )
        return Result.Updated
    }

    private fun applyEdit(map: BattleMap, edit: BattleMapFogEdit): BattleMap {
        val allCells = map.allCells()
        return when (edit) {
            BattleMapFogEdit.RevealAll -> {
                map.copy(fogEnabled = false, revealedCells = emptySet())
            }
            BattleMapFogEdit.HideAll -> {
                map.copy(fogEnabled = true, revealedCells = emptySet())
            }
            is BattleMapFogEdit.Hide -> {
                val currentRevealed = if (map.fogEnabled) {
                    map.revealedCells
                } else {
                    allCells
                }
                val revealed = currentRevealed - edit.cells
                if (revealed.isEmpty()) {
                    map.copy(fogEnabled = true, revealedCells = emptySet())
                } else if (revealed.size == allCells.size) {
                    map.copy(fogEnabled = false, revealedCells = emptySet())
                } else {
                    map.copy(fogEnabled = true, revealedCells = revealed)
                }
            }
            is BattleMapFogEdit.Reveal -> {
                if (!map.fogEnabled) {
                    map
                } else {
                    val revealed = map.revealedCells + edit.cells
                    if (revealed.size >= allCells.size) {
                        map.copy(fogEnabled = false, revealedCells = emptySet())
                    } else {
                        map.copy(fogEnabled = true, revealedCells = revealed)
                    }
                }
            }
        }
    }
}
