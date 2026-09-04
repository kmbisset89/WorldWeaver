package io.github.kmbisset89.worldweaver.domain

internal class DeleteLocationUseCase(
    private val locationRepository: LocationRepository,
    private val locationOverlayRepository: LocationOverlayRepository,
    private val worldMapRepository: WorldMapRepository,
    private val worldMapFileStore: WorldMapFileStore,
    private val voiceClipFileStore: VoiceClipFileStore,
) {
    sealed interface Result {
        data object Deleted : Result
        data class Blocked(val childCount: Int) : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(locationId: String): Result {
        val location = locationRepository.getById(locationId) ?: return Result.NotFound
        val childCount = locationRepository.countByParent(locationId)
        if (childCount > 0) {
            return Result.Blocked(childCount)
        }
        locationOverlayRepository.deleteByLocation(locationId)
        voiceClipFileStore.delete(VoiceClipRef.Location(locationId))
        val worldMap = worldMapRepository.getByLocation(location.worldId, locationId)
        if (worldMap != null) {
            worldMapRepository.delete(worldMap.id)
            worldMapFileStore.delete(worldMap.id)
        }
        locationRepository.delete(locationId)
        return Result.Deleted
    }
}
