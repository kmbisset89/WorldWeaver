package io.github.kmbisset89.worldweaver.domain

internal class CreateBattleMapSituationUseCase(
    private val situationRepository: BattleMapSituationRepository,
    private val battleMapRepository: BattleMapRepository,
    private val fileStore: BattleMapFileStore,
    private val pyramidFactory: MapTilePyramidFactory,
    private val imageTransformer: BattleMapSituationImageTransformer,
    private val activeContextRepository: ActiveContextRepository,
    private val entityIdFactory: EntityIdFactory,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data class Created(val situation: BattleMapSituation) : Result
        data object InvalidName : Result
        data object InvalidImage : Result
        data object MapNotFound : Result
        data object NoActiveCampaign : Result
    }

    suspend operator fun invoke(draft: BattleMapSituationDraft): Result {
        val campaignId = activeContextRepository.get().activeCampaignId
            ?: return Result.NoActiveCampaign
        val battleMap = battleMapRepository.getById(draft.battleMapId) ?: return Result.MapNotFound
        if (battleMap.campaignId != campaignId) {
            return Result.MapNotFound
        }
        val trimmedName = draft.name.trim()
        if (trimmedName.isEmpty()) {
            return Result.InvalidName
        }
        val fitted = imageTransformer.transform(
            imagePng = draft.imagePng,
            targetWidth = battleMap.originalWidth,
            targetHeight = battleMap.originalHeight,
        ) ?: return Result.InvalidImage
        val pyramid = pyramidFactory.create(fitted) ?: return Result.InvalidImage
        val now = instantProvider.now()
        val sortIndex = situationRepository.getByBattleMap(battleMap.id).maxOfOrNull { it.sortIndex }?.plus(1) ?: 0
        val situation = BattleMapSituation(
            id = entityIdFactory.create(),
            battleMapId = battleMap.id,
            name = trimmedName,
            visible = draft.visible,
            sortIndex = sortIndex,
            createdAt = now,
            updatedAt = now,
        )
        fileStore.writeSituation(battleMap.id, situation.id, pyramid)
        try {
            situationRepository.insert(situation)
        } catch (error: Exception) {
            fileStore.deleteSituation(battleMap.id, situation.id)
            throw error
        }
        return Result.Created(situation)
    }
}
