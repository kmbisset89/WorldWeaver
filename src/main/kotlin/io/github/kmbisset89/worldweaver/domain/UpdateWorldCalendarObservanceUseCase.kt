package io.github.kmbisset89.worldweaver.domain

internal class UpdateWorldCalendarObservanceUseCase(
    private val observanceRepository: WorldCalendarObservanceRepository,
    private val worldCalendarRepository: WorldCalendarRepository,
    private val loreRepository: LoreRepository,
    private val instantProvider: InstantProvider,
    private val dateFormatter: WorldDateFormatter = WorldDateFormatter(),
) {
    sealed interface Result {
        data object Updated : Result
        data object InvalidName : Result
        data object DuplicateName : Result
        data object InvalidDate : Result
        data object NotFound : Result
        data object MissingCalendar : Result
    }

    suspend operator fun invoke(
        observanceId: String,
        draft: WorldCalendarObservanceDraft,
    ): Result {
        val existing = observanceRepository.getById(observanceId) ?: return Result.NotFound
        val calendar = worldCalendarRepository.getByWorld(existing.worldId) ?: return Result.MissingCalendar
        val name = draft.name.trim()
        if (name.isEmpty()) {
            return Result.InvalidName
        }
        val duplicate = observanceRepository.getByWorld(existing.worldId).any { observance ->
            observance.id != observanceId && observance.name.equals(name, ignoreCase = true)
        }
        if (duplicate) {
            return Result.DuplicateName
        }
        if (!dateFormatter.isValidObservance(calendar, draft.monthId, draft.day, draft.year)) {
            return Result.InvalidDate
        }
        val validLoreIds = loreRepository.getByWorld(existing.worldId).map { it.id }.toSet()
        observanceRepository.update(
            existing.copy(
                name = name,
                notes = draft.notes.trim(),
                kind = draft.kind,
                monthId = draft.monthId,
                day = draft.day,
                year = draft.year,
                loreIds = draft.loreIds.distinct().filter { it in validLoreIds },
                updatedAt = instantProvider.now(),
            )
        )
        return Result.Updated
    }
}
