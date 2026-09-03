package io.github.kmbisset89.worldweaver.domain

internal class ImportBundledBattleMapUseCase(
    private val catalogLoader: BundledBattleMapCatalogLoader,
    private val createBattleMap: CreateBattleMapUseCase,
    private val createSituation: CreateBattleMapSituationUseCase,
    private val battleMapRepository: BattleMapRepository,
    private val activeContextRepository: ActiveContextRepository,
) {
    sealed interface Result {
        data class Imported(val battleMap: BattleMap) : Result
        data object AlreadyPresent : Result
        data object MissingAsset : Result
        data object InvalidImage : Result
        data object UnknownEntry : Result
        data object NoActiveCampaign : Result
    }

    suspend operator fun invoke(entryId: String): Result {
        val campaignId = activeContextRepository.get().activeCampaignId
            ?: return Result.NoActiveCampaign
        val entry = BundledBattleMapCatalog.entryById(entryId) ?: return Result.UnknownEntry
        val alreadyPresent = battleMapRepository.getByCampaign(campaignId).any { map ->
            map.name.equals(entry.name, ignoreCase = true)
        }
        if (alreadyPresent) {
            return Result.AlreadyPresent
        }
        val imagePng = catalogLoader.loadPng(entry.fileName) ?: return Result.MissingAsset
        val created = when (
            val result = createBattleMap(
                BattleMapDraft(
                    name = entry.name,
                    imagePng = imagePng,
                    columns = entry.columns,
                    rows = entry.rows,
                    unitName = UNIT_NAME,
                    unitsPerTile = UNITS_PER_TILE,
                )
            )
        ) {
            is CreateBattleMapUseCase.Result.Created -> result.battleMap
            CreateBattleMapUseCase.Result.InvalidImage -> return Result.InvalidImage
            CreateBattleMapUseCase.Result.NoActiveCampaign -> return Result.NoActiveCampaign
            CreateBattleMapUseCase.Result.InvalidName,
            CreateBattleMapUseCase.Result.InvalidGrid,
            -> return Result.InvalidImage
        }
        entry.situations.forEach { situation ->
            val situationPng = catalogLoader.loadPng(situation.fileName) ?: return@forEach
            createSituation(
                BattleMapSituationDraft(
                    battleMapId = created.id,
                    name = situation.name,
                    imagePng = situationPng,
                    visible = false,
                )
            )
        }
        return Result.Imported(created)
    }

    private companion object {
        const val UNIT_NAME = "ft"
        const val UNITS_PER_TILE = 5.0
    }
}
