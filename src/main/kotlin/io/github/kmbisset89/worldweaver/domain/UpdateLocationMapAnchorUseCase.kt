package io.github.kmbisset89.worldweaver.domain

internal class UpdateLocationMapAnchorUseCase(
    private val locationRepository: LocationRepository,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data object Updated : Result
        data object InvalidAnchor : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(
        locationId: String,
        mapAnchorX: Double?,
        mapAnchorY: Double?,
    ): Result {
        val existing = locationRepository.getById(locationId) ?: return Result.NotFound
        if (!isValidAnchor(mapAnchorX, mapAnchorY)) {
            return Result.InvalidAnchor
        }
        locationRepository.update(
            existing.copy(
                mapAnchorX = mapAnchorX,
                mapAnchorY = mapAnchorY,
                updatedAt = instantProvider.now(),
            )
        )
        return Result.Updated
    }

    private fun isValidAnchor(mapAnchorX: Double?, mapAnchorY: Double?): Boolean {
        if (mapAnchorX == null && mapAnchorY == null) {
            return true
        }
        if (mapAnchorX == null || mapAnchorY == null) {
            return false
        }
        return mapAnchorX in ANCHOR_RANGE && mapAnchorY in ANCHOR_RANGE
    }

    private companion object {
        val ANCHOR_RANGE = 0.0..1.0
    }
}
