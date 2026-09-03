package io.github.kmbisset89.worldweaver.domain

internal class UpdateSessionUseCase(
    private val sessionRepository: SessionRepository,
    private val campaignRepository: CampaignRepository,
    private val worldCalendarRepository: WorldCalendarRepository,
    private val entityIdFactory: EntityIdFactory,
    private val instantProvider: InstantProvider,
    private val dateFormatter: WorldDateFormatter = WorldDateFormatter(),
) {
    sealed interface Result {
        data object Updated : Result
        data object InvalidName : Result
        data object InvalidDate : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(
        sessionId: String,
        draft: SessionDraft,
    ): Result {
        val existing = sessionRepository.getById(sessionId) ?: return Result.NotFound
        val name = draft.name.trim()
        if (name.isEmpty()) {
            return Result.InvalidName
        }
        val inWorldDate = when (val resolved = resolveDate(draft.inWorldDate, existing.campaignId)) {
            DateResolution.None -> null
            DateResolution.Invalid -> return Result.InvalidDate
            is DateResolution.Found -> resolved.date
        }
        sessionRepository.update(
            existing.copy(
                name = name,
                notes = draft.notes.trim(),
                inWorldDate = inWorldDate,
                scenes = assignScenes(draft.scenes),
                marchOrder = assignMarchOrder(draft.marchOrder),
                updatedAt = instantProvider.now(),
            )
        )
        return Result.Updated
    }

    private suspend fun resolveDate(
        date: WorldDate?,
        campaignId: String,
    ): DateResolution {
        if (date == null) {
            return DateResolution.None
        }
        val campaign = campaignRepository.getById(campaignId) ?: return DateResolution.Invalid
        val calendar = worldCalendarRepository.getByWorld(campaign.worldId)
            ?: return DateResolution.Invalid
        return if (dateFormatter.isValid(calendar, date)) {
            DateResolution.Found(date)
        } else {
            DateResolution.Invalid
        }
    }

    private sealed interface DateResolution {
        data object None : DateResolution
        data object Invalid : DateResolution
        data class Found(val date: WorldDate) : DateResolution
    }

    private fun assignScenes(scenes: List<SessionScene>): List<SessionScene> {
        return scenes.map { scene ->
            scene.copy(
                id = scene.id.ifBlank { entityIdFactory.create() },
                title = scene.title.trim(),
                notes = scene.notes.trim(),
            )
        }.filter { it.title.isNotEmpty() || it.notes.isNotEmpty() }
    }

    private fun assignMarchOrder(entries: List<MarchOrderEntry>): List<MarchOrderEntry> {
        val seen = mutableSetOf<Pair<String, String>>()
        return entries.mapNotNull { entry ->
            val key = personKey(entry.person)
            if (entry.displayName.isBlank() || !seen.add(key)) {
                null
            } else {
                entry.copy(
                    id = entry.id.ifBlank { entityIdFactory.create() },
                    displayName = entry.displayName.trim(),
                )
            }
        }
    }

    private fun personKey(person: PersonRef): Pair<String, String> {
        return when (person) {
            is PersonRef.World -> "WORLD" to person.id
            is PersonRef.Campaign -> "CAMPAIGN" to person.id
        }
    }
}
