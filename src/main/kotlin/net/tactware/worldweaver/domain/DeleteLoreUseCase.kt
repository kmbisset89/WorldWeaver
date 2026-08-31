package net.tactware.worldweaver.domain

internal class DeleteLoreUseCase(
    private val loreRepository: LoreRepository,
    private val questRepository: QuestRepository,
) {
    sealed interface Result {
        data object Deleted : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(loreId: String): Result {
        val existing = loreRepository.getById(loreId) ?: return Result.NotFound
        loreRepository.getByWorld(existing.worldId)
            .filter { it.id != loreId && loreId in it.relatedEntryIds }
            .forEach { lore ->
                loreRepository.update(
                    lore.copy(relatedEntryIds = lore.relatedEntryIds.filterNot { it == loreId })
                )
            }
        questRepository.deleteLinksByTarget(QuestLinkKind.LORE, loreId)
        loreRepository.delete(loreId)
        return Result.Deleted
    }
}
