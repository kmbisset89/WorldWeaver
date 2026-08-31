package net.tactware.worldweaver.domain

internal class UpdateLocationUseCase(
    private val locationRepository: LocationRepository,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data object Updated : Result
        data object InvalidName : Result
        data object InvalidParent : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(
        locationId: String,
        draft: LocationDraft,
    ): Result {
        val existing = locationRepository.getById(locationId) ?: return Result.NotFound
        val trimmedName = draft.name.trim()
        if (trimmedName.isEmpty()) {
            return Result.InvalidName
        }
        val parent = resolveParent(draft.parentLocationId)
        if (parent != null && (parent.worldId != existing.worldId || parent.id == existing.id)) {
            return Result.InvalidParent
        }
        if (!draft.type.acceptsParent(parent)) {
            return Result.InvalidParent
        }
        locationRepository.update(
            existing.copy(
                type = draft.type,
                parentLocationId = parent?.id,
                name = trimmedName,
                description = draft.description.trim(),
                climate = draft.climate.trim(),
                terrain = draft.terrain.trim(),
                government = draft.government.trim(),
                landmarks = draft.landmarks.map { it.trim() }.filter { it.isNotEmpty() },
                history = draft.history.trim(),
                notes = draft.notes.trim(),
                updatedAt = instantProvider.now(),
            )
        )
        return Result.Updated
    }

    private suspend fun resolveParent(parentLocationId: String?): Location? {
        val id = parentLocationId?.takeIf { it.isNotBlank() } ?: return null
        return locationRepository.getById(id)
    }
}
