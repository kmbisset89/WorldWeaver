package net.tactware.worldweaver.domain

internal class UpdateLocationOverlayUseCase(
    private val locationRepository: LocationRepository,
    private val locationOverlayRepository: LocationOverlayRepository,
    private val activeContextRepository: ActiveContextRepository,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data object Updated : Result
        data object NoActiveCampaign : Result
        data object LocationNotFound : Result
        data object LocationNotInActiveWorld : Result
    }

    suspend operator fun invoke(
        locationId: String,
        hasPartyPresence: Boolean,
        notes: String,
    ): Result {
        val context = activeContextRepository.get()
        val campaignId = context.activeCampaignId ?: return Result.NoActiveCampaign
        val location = locationRepository.getById(locationId) ?: return Result.LocationNotFound
        if (location.worldId != context.activeWorldId) {
            return Result.LocationNotInActiveWorld
        }
        locationOverlayRepository.upsert(
            LocationOverlay(
                campaignId = campaignId,
                locationId = locationId,
                hasPartyPresence = hasPartyPresence,
                notes = notes.trim(),
                updatedAt = instantProvider.now(),
            )
        )
        return Result.Updated
    }
}
