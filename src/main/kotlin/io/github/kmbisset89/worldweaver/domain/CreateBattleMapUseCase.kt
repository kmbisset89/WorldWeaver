package io.github.kmbisset89.worldweaver.domain

internal class CreateBattleMapUseCase(
    private val battleMapRepository: BattleMapRepository,
    private val fileStore: BattleMapFileStore,
    private val pyramidFactory: BattleMapTilePyramidFactory,
    private val activeContextRepository: ActiveContextRepository,
    private val entityIdFactory: EntityIdFactory,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data class Created(val battleMap: BattleMap) : Result
        data object InvalidName : Result
        data object InvalidImage : Result
        data object InvalidGrid : Result
        data object NoActiveCampaign : Result
    }

    suspend operator fun invoke(draft: BattleMapDraft): Result {
        val campaignId = activeContextRepository.get().activeCampaignId
            ?: return Result.NoActiveCampaign
        val trimmedName = draft.name.trim()
        if (trimmedName.isEmpty()) {
            return Result.InvalidName
        }
        val pyramid = pyramidFactory.create(draft.imagePng) ?: return Result.InvalidImage
        val unitName = draft.unitName.trim()
        if (draft.columns < 1 ||
            draft.rows < 1 ||
            draft.columns > pyramid.originalWidth ||
            draft.rows > pyramid.originalHeight ||
            unitName.isEmpty() ||
            draft.unitsPerTile <= 0.0
        ) {
            return Result.InvalidGrid
        }
        val now = instantProvider.now()
        val battleMap = BattleMap(
            id = entityIdFactory.create(),
            campaignId = campaignId,
            name = trimmedName,
            originalWidth = pyramid.originalWidth,
            originalHeight = pyramid.originalHeight,
            tileSizePx = pyramid.tileSizePx,
            minZoom = pyramid.minZoom,
            maxZoom = pyramid.maxZoom,
            columns = draft.columns,
            rows = draft.rows,
            unitName = unitName,
            unitsPerTile = draft.unitsPerTile,
            createdAt = now,
            updatedAt = now,
        )
        fileStore.write(battleMap.id, pyramid)
        try {
            battleMapRepository.insert(battleMap)
        } catch (error: Exception) {
            fileStore.delete(battleMap.id)
            throw error
        }
        return Result.Created(battleMap)
    }
}
