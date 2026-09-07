package io.github.kmbisset89.worldweaver.domain

internal class CreateRandomTableUseCase(
    private val randomTableRepository: RandomTableRepository,
    private val activeContextRepository: ActiveContextRepository,
    private val entityIdFactory: EntityIdFactory,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data class Created(val table: RandomTable) : Result
        data object InvalidName : Result
        data object InvalidRows : Result
        data object DuplicateName : Result
        data object NoActiveWorld : Result
    }

    suspend operator fun invoke(draft: RandomTableDraft): Result {
        val worldId = activeContextRepository.get().activeWorldId ?: return Result.NoActiveWorld
        val name = draft.name.trim()
        if (name.isEmpty()) {
            return Result.InvalidName
        }
        if (nameTaken(worldId, name, excludeId = null)) {
            return Result.DuplicateName
        }
        val rows = normalizedRows(draft.rows) ?: return Result.InvalidRows
        val now = instantProvider.now()
        val table = RandomTable(
            id = entityIdFactory.create(),
            worldId = worldId,
            name = name,
            notes = draft.notes.trim(),
            rows = rows,
            createdAt = now,
            updatedAt = now,
        )
        randomTableRepository.insert(table)
        return Result.Created(table)
    }

    private fun normalizedRows(drafts: List<RandomTableRowDraft>): List<RandomTableRow>? {
        val rows = drafts.mapNotNull { draft ->
            val label = draft.label.trim()
            if (label.isEmpty()) {
                return@mapNotNull null
            }
            if (draft.weight < 1) {
                return null
            }
            RandomTableRow(
                id = draft.id?.takeIf { it.isNotBlank() } ?: entityIdFactory.create(),
                label = label,
                weight = draft.weight,
                nestedTableId = draft.nestedTableId?.takeIf { it.isNotBlank() },
            )
        }
        return rows.takeIf { it.isNotEmpty() }
    }

    private suspend fun nameTaken(
        worldId: String,
        name: String,
        excludeId: String?,
    ): Boolean {
        return randomTableRepository.getByWorld(worldId).any { table ->
            table.id != excludeId && table.name.equals(name, ignoreCase = true)
        }
    }
}
