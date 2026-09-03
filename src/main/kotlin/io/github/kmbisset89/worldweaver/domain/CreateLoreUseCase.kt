package io.github.kmbisset89.worldweaver.domain

internal class CreateLoreUseCase(
    private val loreRepository: LoreRepository,
    private val locationRepository: LocationRepository,
    private val activeContextRepository: ActiveContextRepository,
    private val entityIdFactory: EntityIdFactory,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data class Created(val lore: Lore) : Result
        data object InvalidTitle : Result
        data object InvalidContent : Result
        data object NoActiveWorld : Result
        data object InvalidLocation : Result
    }

    suspend operator fun invoke(draft: LoreDraft): Result {
        val worldId = activeContextRepository.get().activeWorldId ?: return Result.NoActiveWorld
        val title = draft.title.trim()
        if (title.isEmpty()) {
            return Result.InvalidTitle
        }
        val content = draft.content.trim()
        if (content.isEmpty()) {
            return Result.InvalidContent
        }
        val locationId = when (val resolved = resolveLocation(draft.locationId, worldId)) {
            LocationResolution.None -> null
            LocationResolution.Invalid -> return Result.InvalidLocation
            is LocationResolution.Found -> resolved.id
        }
        val now = instantProvider.now()
        val lore = Lore(
            id = entityIdFactory.create(),
            worldId = worldId,
            title = title,
            content = content,
            category = draft.category,
            tags = trimValues(draft.tags),
            relatedEntryIds = resolveRelatedIds(draft.relatedEntryIds, worldId, excludeId = null),
            secrets = assignSecretIds(draft.secrets),
            locationId = locationId,
            characterId = draft.characterId?.trim()?.takeIf { it.isNotEmpty() },
            createdAt = now,
            updatedAt = now,
        )
        loreRepository.insert(lore)
        return Result.Created(lore)
    }

    private suspend fun resolveLocation(
        locationId: String?,
        worldId: String,
    ): LocationResolution {
        val id = locationId?.takeIf { it.isNotBlank() } ?: return LocationResolution.None
        val location = locationRepository.getById(id) ?: return LocationResolution.Invalid
        return if (location.worldId == worldId) {
            LocationResolution.Found(id)
        } else {
            LocationResolution.Invalid
        }
    }

    private suspend fun resolveRelatedIds(
        relatedEntryIds: List<String>,
        worldId: String,
        excludeId: String?,
    ): List<String> {
        val validIds = loreRepository.getByWorld(worldId).map { it.id }.toSet()
        return relatedEntryIds.distinct().filter { id ->
            id != excludeId && id in validIds
        }
    }

    private fun assignSecretIds(secrets: List<LoreSecret>): List<LoreSecret> {
        return secrets.map { secret ->
            secret.copy(
                id = secret.id.ifBlank { entityIdFactory.create() },
                title = secret.title.trim(),
                secret = secret.secret.trim(),
                hints = secret.hints.map { hint ->
                    hint.copy(
                        id = hint.id.ifBlank { entityIdFactory.create() },
                        text = hint.text.trim(),
                    )
                }.filter { it.text.isNotEmpty() },
            )
        }.filter { it.title.isNotEmpty() || it.secret.isNotEmpty() }
    }

    private fun trimValues(values: List<String>): List<String> {
        return values.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
    }

    private sealed interface LocationResolution {
        data object None : LocationResolution
        data object Invalid : LocationResolution
        data class Found(val id: String) : LocationResolution
    }
}
