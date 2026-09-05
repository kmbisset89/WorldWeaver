package io.github.kmbisset89.worldweaver.domain

internal class CreateWorldCalendarObservanceUseCase(
    private val observanceRepository: WorldCalendarObservanceRepository,
    private val worldCalendarRepository: WorldCalendarRepository,
    private val loreRepository: LoreRepository,
    private val activeContextRepository: ActiveContextRepository,
    private val entityIdFactory: EntityIdFactory,
    private val instantProvider: InstantProvider,
    private val dateFormatter: WorldDateFormatter = WorldDateFormatter(),
) {
    sealed interface Result {
        data class Created(val observance: WorldCalendarObservance) : Result
        data object InvalidName : Result
        data object DuplicateName : Result
        data object InvalidDate : Result
        data object NoActiveWorld : Result
        data object MissingCalendar : Result
    }

    suspend operator fun invoke(draft: WorldCalendarObservanceDraft): Result {
        val worldId = activeContextRepository.get().activeWorldId ?: return Result.NoActiveWorld
        val calendar = worldCalendarRepository.getByWorld(worldId) ?: return Result.MissingCalendar
        val name = draft.name.trim()
        if (name.isEmpty()) {
            return Result.InvalidName
        }
        if (nameTaken(worldId, name, excludeId = null)) {
            return Result.DuplicateName
        }
        if (!dateFormatter.isValidObservance(calendar, draft.monthId, draft.day, draft.year)) {
            return Result.InvalidDate
        }
        val now = instantProvider.now()
        val observance = WorldCalendarObservance(
            id = entityIdFactory.create(),
            worldId = worldId,
            name = name,
            notes = draft.notes.trim(),
            kind = draft.kind,
            monthId = draft.monthId,
            day = draft.day,
            year = draft.year,
            loreIds = resolveLoreIds(draft.loreIds, worldId),
            createdAt = now,
            updatedAt = now,
        )
        observanceRepository.insert(observance)
        return Result.Created(observance)
    }

    private suspend fun nameTaken(
        worldId: String,
        name: String,
        excludeId: String?,
    ): Boolean {
        return observanceRepository.getByWorld(worldId).any { observance ->
            observance.id != excludeId && observance.name.equals(name, ignoreCase = true)
        }
    }

    private suspend fun resolveLoreIds(
        loreIds: List<String>,
        worldId: String,
    ): List<String> {
        val validIds = loreRepository.getByWorld(worldId).map { it.id }.toSet()
        return loreIds.distinct().filter { it in validIds }
    }
}
