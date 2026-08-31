package net.tactware.worldweaver.domain

internal class CreateLocationUseCase(
    private val locationRepository: LocationRepository,
    private val activeContextRepository: ActiveContextRepository,
    private val entityIdFactory: EntityIdFactory,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data class Created(val location: Location) : Result
        data object InvalidName : Result
        data object NoActiveWorld : Result
        data object InvalidParent : Result
    }

    suspend operator fun invoke(draft: LocationDraft): Result {
        val worldId = activeContextRepository.get().activeWorldId ?: return Result.NoActiveWorld
        val trimmedName = draft.name.trim()
        if (trimmedName.isEmpty()) {
            return Result.InvalidName
        }
        val parent = resolveParent(draft.parentLocationId)
        if (parent != null && parent.worldId != worldId) {
            return Result.InvalidParent
        }
        if (!draft.type.acceptsParent(parent)) {
            return Result.InvalidParent
        }
        val now = instantProvider.now()
        val location = Location(
            id = entityIdFactory.create(),
            worldId = worldId,
            type = draft.type,
            parentLocationId = parent?.id,
            name = trimmedName,
            description = draft.description.trim(),
            climate = draft.climate.trim(),
            terrain = draft.terrain.trim(),
            government = draft.government.trim(),
            landmarks = trimLandmarks(draft.landmarks),
            history = draft.history.trim(),
            notes = draft.notes.trim(),
            createdAt = now,
            updatedAt = now,
        )
        locationRepository.insert(location)
        return Result.Created(location)
    }

    private suspend fun resolveParent(parentLocationId: String?): Location? {
        val id = parentLocationId?.takeIf { it.isNotBlank() } ?: return null
        return locationRepository.getById(id)
    }

    private fun trimLandmarks(landmarks: List<String>): List<String> {
        return landmarks.map { it.trim() }.filter { it.isNotEmpty() }
    }
}
