package io.github.kmbisset89.worldweaver.domain

internal class UpdateRandomTableUseCase(
    private val randomTableRepository: RandomTableRepository,
    private val entityIdFactory: EntityIdFactory,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data object Updated : Result
        data object InvalidName : Result
        data object InvalidRows : Result
        data object DuplicateName : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(
        tableId: String,
        draft: RandomTableDraft,
    ): Result {
        val existing = randomTableRepository.getById(tableId) ?: return Result.NotFound
        val name = draft.name.trim()
        if (name.isEmpty()) {
            return Result.InvalidName
        }
        val duplicate = randomTableRepository.getByWorld(existing.worldId).any { table ->
            table.id != tableId && table.name.equals(name, ignoreCase = true)
        }
        if (duplicate) {
            return Result.DuplicateName
        }
        val rows = normalizedRows(draft.rows, tableId) ?: return Result.InvalidRows
        randomTableRepository.update(
            existing.copy(
                name = name,
                notes = draft.notes.trim(),
                rows = rows,
                updatedAt = instantProvider.now(),
            )
        )
        return Result.Updated
    }

    private fun normalizedRows(
        drafts: List<RandomTableRowDraft>,
        tableId: String,
    ): List<RandomTableRow>? {
        val rows = drafts.mapNotNull { draft ->
            val label = draft.label.trim()
            if (label.isEmpty()) {
                return@mapNotNull null
            }
            if (draft.weight < 1) {
                return null
            }
            val nestedId = draft.nestedTableId?.takeIf { it.isNotBlank() && it != tableId }
            RandomTableRow(
                id = draft.id?.takeIf { it.isNotBlank() } ?: entityIdFactory.create(),
                label = label,
                weight = draft.weight,
                nestedTableId = nestedId,
            )
        }
        return rows.takeIf { it.isNotEmpty() }
    }
}
