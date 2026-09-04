package io.github.kmbisset89.worldweaver.domain

internal class CreateWorldMapUseCase(
    private val worldMapRepository: WorldMapRepository,
    private val locationRepository: LocationRepository,
    private val fileStore: WorldMapFileStore,
    private val pyramidFactory: MapTilePyramidFactory,
    private val activeContextRepository: ActiveContextRepository,
    private val entityIdFactory: EntityIdFactory,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data class Created(val worldMap: WorldMap) : Result
        data object InvalidImage : Result
        data object NoActiveWorld : Result
        data object LocationNotFound : Result
    }

    suspend operator fun invoke(draft: WorldMapDraft): Result {
        val worldId = activeContextRepository.get().activeWorldId ?: return Result.NoActiveWorld
        val locationId = draft.locationId
        if (locationId != null) {
            val location = locationRepository.getById(locationId) ?: return Result.LocationNotFound
            if (location.worldId != worldId) {
                return Result.LocationNotFound
            }
        }
        val pyramid = pyramidFactory.create(draft.imagePng) ?: return Result.InvalidImage
        val existing = worldMapRepository.getByLocation(worldId, locationId)
        val now = instantProvider.now()
        val worldMap = if (existing == null) {
            WorldMap(
                id = entityIdFactory.create(),
                worldId = worldId,
                locationId = locationId,
                originalWidth = pyramid.originalWidth,
                originalHeight = pyramid.originalHeight,
                tileSizePx = pyramid.tileSizePx,
                minZoom = pyramid.minZoom,
                maxZoom = pyramid.maxZoom,
                createdAt = now,
                updatedAt = now,
            )
        } else {
            existing.copy(
                originalWidth = pyramid.originalWidth,
                originalHeight = pyramid.originalHeight,
                tileSizePx = pyramid.tileSizePx,
                minZoom = pyramid.minZoom,
                maxZoom = pyramid.maxZoom,
                updatedAt = now,
            )
        }
        fileStore.write(worldMap.id, pyramid)
        try {
            if (existing == null) {
                worldMapRepository.insert(worldMap)
            } else {
                worldMapRepository.update(worldMap)
            }
        } catch (error: Exception) {
            if (existing == null) {
                fileStore.delete(worldMap.id)
            }
            throw error
        }
        return Result.Created(worldMap)
    }
}
