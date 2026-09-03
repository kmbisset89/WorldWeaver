package io.github.kmbisset89.worldweaver.domain

internal class UpdateLoreUseCase(
    private val loreRepository: LoreRepository,
    private val locationRepository: LocationRepository,
    private val entityIdFactory: EntityIdFactory,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data object Updated : Result
        data object InvalidTitle : Result
        data object InvalidContent : Result
        data object InvalidLocation : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(
        loreId: String,
        draft: LoreDraft,
    ): Result {
        val existing = loreRepository.getById(loreId) ?: return Result.NotFound
        val title = draft.title.trim()
        if (title.isEmpty()) {
            return Result.InvalidTitle
        }
        val content = draft.content.trim()
        if (content.isEmpty()) {
            return Result.InvalidContent
        }
        val locationId = when (val resolved = resolveLocation(draft.locationId, existing.worldId)) {
            LocationResolution.None -> null
            LocationResolution.Invalid -> return Result.InvalidLocation
            is LocationResolution.Found -> resolved.id
        }
        loreRepository.update(
            existing.copy(
                title = title,
                content = content,
                category = draft.category,
                tags = draft.tags.map { it.trim() }.filter { it.isNotEmpty() }.distinct(),
                relatedEntryIds = resolveRelatedIds(
                    draft.relatedEntryIds,
                    existing.worldId,
                    excludeId = existing.id,
                ),
                secrets = assignSecretIds(draft.secrets),
                locationId = locationId,
                characterId = draft.characterId?.trim()?.takeIf { it.isNotEmpty() },
                updatedAt = instantProvider.now(),
            )
        )
        return Result.Updated
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
        excludeId: String,
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

    private sealed interface LocationResolution {
        data object None : LocationResolution
        data object Invalid : LocationResolution
        data class Found(val id: String) : LocationResolution
    }
}
