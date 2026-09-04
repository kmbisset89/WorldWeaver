package io.github.kmbisset89.worldweaver.domain

internal class DeleteWorldMapUseCase(
    private val worldMapRepository: WorldMapRepository,
    private val fileStore: WorldMapFileStore,
) {
    sealed interface Result {
        data object Deleted : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(worldMapId: String): Result {
        worldMapRepository.getById(worldMapId) ?: return Result.NotFound
        worldMapRepository.delete(worldMapId)
        fileStore.delete(worldMapId)
        return Result.Deleted
    }
}
