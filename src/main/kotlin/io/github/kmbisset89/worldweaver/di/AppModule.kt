package io.github.kmbisset89.worldweaver.di

import io.github.kmbisset89.worldweaver.core.AppCoroutineScope
import io.github.kmbisset89.worldweaver.data.BattleMapEntityConverter
import io.github.kmbisset89.worldweaver.data.BattleMapRepositoryImpl
import io.github.kmbisset89.worldweaver.data.BattleMapSituationEntityConverter
import io.github.kmbisset89.worldweaver.data.BattleMapSituationRepositoryImpl
import io.github.kmbisset89.worldweaver.data.CampaignEntityConverter
import io.github.kmbisset89.worldweaver.data.CampaignPersonEntityConverter
import io.github.kmbisset89.worldweaver.data.CampaignPersonRepositoryImpl
import io.github.kmbisset89.worldweaver.data.CampaignRepositoryImpl
import io.github.kmbisset89.worldweaver.data.DatabaseProvider
import io.github.kmbisset89.worldweaver.data.EncounterEntityConverter
import io.github.kmbisset89.worldweaver.data.EncounterRepositoryImpl
import io.github.kmbisset89.worldweaver.data.FactionEntityConverter
import io.github.kmbisset89.worldweaver.data.FactionMembershipEntityConverter
import io.github.kmbisset89.worldweaver.data.FactionMembershipRepositoryImpl
import io.github.kmbisset89.worldweaver.data.FactionRepositoryImpl
import io.github.kmbisset89.worldweaver.data.FifthEditionSheetConverter
import io.github.kmbisset89.worldweaver.data.Pathfinder2ESheetConverter
import io.github.kmbisset89.worldweaver.data.PersonSheetEntityConverter
import io.github.kmbisset89.worldweaver.data.LocationEntityConverter
import io.github.kmbisset89.worldweaver.data.LocationOverlayEntityConverter
import io.github.kmbisset89.worldweaver.data.LocationOverlayRepositoryImpl
import io.github.kmbisset89.worldweaver.data.LocationRepositoryImpl
import io.github.kmbisset89.worldweaver.data.LoreEntityConverter
import io.github.kmbisset89.worldweaver.data.LoreRepositoryImpl
import io.github.kmbisset89.worldweaver.data.PersonCompanionEntityConverter
import io.github.kmbisset89.worldweaver.data.PersonCompanionRepositoryImpl
import io.github.kmbisset89.worldweaver.data.PersonRelationshipEntityConverter
import io.github.kmbisset89.worldweaver.data.PersonRelationshipRepositoryImpl
import io.github.kmbisset89.worldweaver.data.PlotThreadEntityConverter
import io.github.kmbisset89.worldweaver.data.PlotThreadRepositoryImpl
import io.github.kmbisset89.worldweaver.data.PreferenceActiveContextRepository
import io.github.kmbisset89.worldweaver.data.QuestEntityConverter
import io.github.kmbisset89.worldweaver.data.QuestRepositoryImpl
import io.github.kmbisset89.worldweaver.data.RoomTransactionRunner
import io.github.kmbisset89.worldweaver.data.ReferenceDocEntityConverter
import io.github.kmbisset89.worldweaver.data.ReferenceDocRepositoryImpl
import io.github.kmbisset89.worldweaver.data.SessionEntityConverter
import io.github.kmbisset89.worldweaver.data.SessionRepositoryImpl
import io.github.kmbisset89.worldweaver.data.WorldCalendarEntityConverter
import io.github.kmbisset89.worldweaver.data.WorldCalendarObservanceEntityConverter
import io.github.kmbisset89.worldweaver.data.WorldCalendarObservanceRepositoryImpl
import io.github.kmbisset89.worldweaver.data.WorldCalendarRepositoryImpl
import io.github.kmbisset89.worldweaver.data.WorldEntityConverter
import io.github.kmbisset89.worldweaver.data.WorldMapEntityConverter
import io.github.kmbisset89.worldweaver.data.WorldMapRepositoryImpl
import io.github.kmbisset89.worldweaver.data.WorldPersonEntityConverter
import io.github.kmbisset89.worldweaver.data.WorldPersonRepositoryImpl
import io.github.kmbisset89.worldweaver.data.WorldRepositoryImpl
import io.github.kmbisset89.worldweaver.data.WorldWeaverDatabase
import io.github.kmbisset89.worldweaver.domain.AbilityScoreRoller
import io.github.kmbisset89.worldweaver.domain.BattleMapFileStore
import io.github.kmbisset89.worldweaver.domain.BundledBattleMapCatalogLoader
import io.github.kmbisset89.worldweaver.domain.BundledSrdCatalogLoader
import io.github.kmbisset89.worldweaver.domain.ClearPersonAvatarUseCase
import io.github.kmbisset89.worldweaver.domain.ClearSrdCatalogUseCase
import io.github.kmbisset89.worldweaver.domain.CloseSessionUseCase
import io.github.kmbisset89.worldweaver.domain.ClearVoiceClipUseCase
import io.github.kmbisset89.worldweaver.domain.CreateWorldPersonFromSrdMonsterUseCase
import io.github.kmbisset89.worldweaver.domain.BattleMapImageScaler
import io.github.kmbisset89.worldweaver.domain.BattleMapRepository
import io.github.kmbisset89.worldweaver.domain.BattleMapSituationImageTransformer
import io.github.kmbisset89.worldweaver.domain.BattleMapSituationRepository
import io.github.kmbisset89.worldweaver.domain.MapTilePyramidFactory
import io.github.kmbisset89.worldweaver.domain.CalculateGridDistanceUseCase
import io.github.kmbisset89.worldweaver.domain.CalculateReachableCellsUseCase
import io.github.kmbisset89.worldweaver.domain.DiceRoller
import io.github.kmbisset89.worldweaver.domain.ActiveContextRepository
import io.github.kmbisset89.worldweaver.domain.AddWorldPersonToCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.AwardPartyExperienceUseCase
import io.github.kmbisset89.worldweaver.domain.AwardPartyLevelUseCase
import io.github.kmbisset89.worldweaver.domain.CampaignPersonRepository
import io.github.kmbisset89.worldweaver.domain.CampaignRepository
import io.github.kmbisset89.worldweaver.domain.ClearActiveCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.CreateCampaignPersonUseCase
import io.github.kmbisset89.worldweaver.domain.AdvanceEncounterTurnUseCase
import io.github.kmbisset89.worldweaver.domain.CreateBattleMapSituationUseCase
import io.github.kmbisset89.worldweaver.domain.CreateBattleMapUseCase
import io.github.kmbisset89.worldweaver.domain.CreateCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.CreateEncounterUseCase
import io.github.kmbisset89.worldweaver.domain.CreateFactionMembershipUseCase
import io.github.kmbisset89.worldweaver.domain.CreateFactionUseCase
import io.github.kmbisset89.worldweaver.domain.CreateWorldCalendarObservanceUseCase
import io.github.kmbisset89.worldweaver.domain.CreateWorldMapUseCase
import io.github.kmbisset89.worldweaver.domain.CreateLocationUseCase
import io.github.kmbisset89.worldweaver.domain.CreateLoreUseCase
import io.github.kmbisset89.worldweaver.domain.CreateOneShotUseCase
import io.github.kmbisset89.worldweaver.domain.CreatePersonCompanionUseCase
import io.github.kmbisset89.worldweaver.domain.CreatePersonRelationshipUseCase
import io.github.kmbisset89.worldweaver.domain.CreatePlotThreadUseCase
import io.github.kmbisset89.worldweaver.domain.CreateQuestUseCase
import io.github.kmbisset89.worldweaver.domain.CreateReferenceDocUseCase
import io.github.kmbisset89.worldweaver.domain.CreateSessionUseCase
import io.github.kmbisset89.worldweaver.domain.CreateWorldPersonUseCase
import io.github.kmbisset89.worldweaver.domain.CreateWorldUseCase
import io.github.kmbisset89.worldweaver.domain.DefaultWorldCalendarFactory
import io.github.kmbisset89.worldweaver.domain.FindSessionCalendarMonthIdsForWorldUseCase
import io.github.kmbisset89.worldweaver.domain.DatabaseSnapshotExporter
import io.github.kmbisset89.worldweaver.domain.DeleteCampaignPersonUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteBattleMapSituationUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteBattleMapUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteEncounterUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteFactionMembershipUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteFactionUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteLocationUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteLoreUseCase
import io.github.kmbisset89.worldweaver.domain.DeletePersonCompanionUseCase
import io.github.kmbisset89.worldweaver.domain.DeletePersonRelationshipUseCase
import io.github.kmbisset89.worldweaver.domain.DeletePlotThreadUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteQuestUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteReferenceDocUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteSessionUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteWorldPersonUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteWorldMapUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteWorldUseCase
import io.github.kmbisset89.worldweaver.domain.EncounterRepository
import io.github.kmbisset89.worldweaver.domain.EndEncounterUseCase
import io.github.kmbisset89.worldweaver.domain.EntityIdFactory
import io.github.kmbisset89.worldweaver.domain.AppBackupArchiveConverter
import io.github.kmbisset89.worldweaver.domain.ExportAppBackupUseCase
import io.github.kmbisset89.worldweaver.domain.FactionMembershipRepository
import io.github.kmbisset89.worldweaver.domain.FactionRepository
import io.github.kmbisset89.worldweaver.domain.ExportWorldBundleUseCase
import io.github.kmbisset89.worldweaver.domain.ExportUniversalVttUseCase
import io.github.kmbisset89.worldweaver.domain.UniversalVttDocumentFactory
import io.github.kmbisset89.worldweaver.domain.FifthEditionPickerCatalogResolver
import io.github.kmbisset89.worldweaver.domain.ImportBundledBattleMapUseCase
import io.github.kmbisset89.worldweaver.domain.ImportSrdCatalogUseCase
import io.github.kmbisset89.worldweaver.domain.ImportWorldBundleUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveFifthEditionPickerCatalogUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveSrdCatalogUseCase
import io.github.kmbisset89.worldweaver.domain.RestoreAppBackupUseCase
import io.github.kmbisset89.worldweaver.domain.TransactionRunner
import io.github.kmbisset89.worldweaver.domain.WorldBundleArchiveConverter
import io.github.kmbisset89.worldweaver.domain.WorldBundleIdRemapper
import io.github.kmbisset89.worldweaver.domain.WorldMapFileStore
import io.github.kmbisset89.worldweaver.domain.WorldMapRepository
import io.github.kmbisset89.worldweaver.domain.WorldBundleSnapshotFactory
import io.github.kmbisset89.worldweaver.domain.GenerateRandomNpcUseCase
import io.github.kmbisset89.worldweaver.domain.InstantProvider
import io.github.kmbisset89.worldweaver.domain.LocationOverlayRepository
import io.github.kmbisset89.worldweaver.domain.LocationRepository
import io.github.kmbisset89.worldweaver.domain.LoreRepository
import io.github.kmbisset89.worldweaver.domain.ObserveActiveContextDetailsUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveActiveContextUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveCampaignsForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveBattleMapSituationsForActiveCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveBattleMapsForActiveCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.ToggleBattleMapSituationUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveFactionMembershipsUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveFactionsForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveEncountersForActiveCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveLocationOverlaysForActiveCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveLocationsForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveLoreForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.ObservePeopleForActiveContextUseCase
import io.github.kmbisset89.worldweaver.domain.ObservePersonCompanionsUseCase
import io.github.kmbisset89.worldweaver.domain.ObservePersonRelationshipsUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveRelationshipWebUseCase
import io.github.kmbisset89.worldweaver.domain.RelationshipWebFactory
import io.github.kmbisset89.worldweaver.domain.ObservePlotThreadsForActiveCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveQuestsForActiveCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveReferenceDocsForActiveCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveSessionsForActiveCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveDashboardCountsUseCase
import io.github.kmbisset89.worldweaver.domain.SearchRecordsUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveWorldCalendarForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveWorldCalendarObservancesForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteWorldCalendarObservanceUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveWorldMapsForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveWorldsUseCase
import io.github.kmbisset89.worldweaver.domain.OneShotDraftFactory
import io.github.kmbisset89.worldweaver.domain.OneShotTemplateCatalog
import io.github.kmbisset89.worldweaver.domain.UpdateWorldCalendarObservanceUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateWorldCalendarUseCase
import io.github.kmbisset89.worldweaver.domain.WorldCalendarObservanceRepository
import io.github.kmbisset89.worldweaver.domain.PersonAvatarFileStore
import io.github.kmbisset89.worldweaver.domain.PersonSheetFactory
import io.github.kmbisset89.worldweaver.domain.PersonCompanionRepository
import io.github.kmbisset89.worldweaver.domain.PlaceBattleMapItemUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteBattleMapItemUseCase
import io.github.kmbisset89.worldweaver.domain.PlaceEncounterTokenUseCase
import io.github.kmbisset89.worldweaver.domain.SetPersonAvatarUseCase
import io.github.kmbisset89.worldweaver.domain.SetVoiceClipUseCase
import io.github.kmbisset89.worldweaver.domain.VoiceClipFileStore
import io.github.kmbisset89.worldweaver.domain.VoiceClipPlayer
import io.github.kmbisset89.worldweaver.domain.VoiceClipRecorder
import io.github.kmbisset89.worldweaver.domain.PersonRelationshipRepository
import io.github.kmbisset89.worldweaver.domain.PlotThreadRepository
import io.github.kmbisset89.worldweaver.domain.QuestRepository
import io.github.kmbisset89.worldweaver.domain.ReferenceDocRepository
import io.github.kmbisset89.worldweaver.domain.RollAllEncounterInitiativeUseCase
import io.github.kmbisset89.worldweaver.domain.RollEncounterInitiativeUseCase
import io.github.kmbisset89.worldweaver.domain.SaveSessionNpcDraftUseCase
import io.github.kmbisset89.worldweaver.domain.SessionRepository
import io.github.kmbisset89.worldweaver.domain.SetActiveCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.SetActiveSessionUseCase
import io.github.kmbisset89.worldweaver.domain.SetActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.SrdCatalogFileStore
import io.github.kmbisset89.worldweaver.domain.SrdCatalogJsonConverter
import io.github.kmbisset89.worldweaver.domain.SrdCatalogRepository
import io.github.kmbisset89.worldweaver.domain.StartEncounterUseCase
import io.github.kmbisset89.worldweaver.domain.SetCampaignStatusUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateCampaignPersonDeathSavesUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateCampaignPersonUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateEncounterParticipantCombatUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateBattleMapFogUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateBattleMapTerrainUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateBattleMapUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateEncounterUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateLocationMapAnchorUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateLocationOverlayUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateLocationUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateFactionUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateLoreUseCase
import io.github.kmbisset89.worldweaver.domain.UpdatePlotThreadUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateQuestUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateReferenceDocUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateSessionUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateWorldPersonUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateWorldUseCase
import io.github.kmbisset89.worldweaver.domain.WorldCalendarRepository
import io.github.kmbisset89.worldweaver.domain.WorldDateFormatter
import io.github.kmbisset89.worldweaver.domain.WorldPersonRepository
import io.github.kmbisset89.worldweaver.domain.WorldRepository
import io.github.kmbisset89.worldweaver.domain.WorldWeaverDataDirectory
import io.github.kmbisset89.worldweaver.ui.AppViewModel
import io.github.kmbisset89.worldweaver.ui.calendar.CalendarViewModel
import io.github.kmbisset89.worldweaver.ui.campaigns.CampaignsViewModel
import io.github.kmbisset89.worldweaver.ui.characters.CharactersViewModel
import io.github.kmbisset89.worldweaver.ui.sheet.CharacterSheetViewModel
import io.github.kmbisset89.worldweaver.ui.dice.DiceViewModel
import io.github.kmbisset89.worldweaver.ui.search.SearchViewModel
import io.github.kmbisset89.worldweaver.ui.factions.FactionsViewModel
import io.github.kmbisset89.worldweaver.ui.links.LinksViewModel
import io.github.kmbisset89.worldweaver.ui.links.RelationshipWebLayoutFactory
import io.github.kmbisset89.worldweaver.ui.encounters.EncountersViewModel
import io.github.kmbisset89.worldweaver.ui.home.HomeViewModel
import io.github.kmbisset89.worldweaver.ui.oneshot.OneShotWizardViewModel
import io.github.kmbisset89.worldweaver.ui.locations.LocationsViewModel
import io.github.kmbisset89.worldweaver.ui.lore.LoreViewModel
import io.github.kmbisset89.worldweaver.ui.maps.BattleMapBoardSession
import io.github.kmbisset89.worldweaver.ui.maps.BattleMapMapStateFactory
import io.github.kmbisset89.worldweaver.ui.maps.BattleMapMeasureOverlay
import io.github.kmbisset89.worldweaver.ui.maps.BattleMapMovementOverlay
import io.github.kmbisset89.worldweaver.ui.maps.BattleMapItemOverlay
import io.github.kmbisset89.worldweaver.ui.maps.BattleMapTokenOverlay
import io.github.kmbisset89.worldweaver.ui.maps.MapsViewModel
import io.github.kmbisset89.worldweaver.ui.worldmap.WorldMapMapStateFactory
import io.github.kmbisset89.worldweaver.ui.worldmap.WorldMapPinOverlay
import io.github.kmbisset89.worldweaver.ui.worldmap.WorldMapViewModel
import io.github.kmbisset89.worldweaver.ui.quests.QuestsViewModel
import io.github.kmbisset89.worldweaver.ui.run.RunViewModel
import io.github.kmbisset89.worldweaver.ui.sessions.SessionsViewModel
import io.github.kmbisset89.worldweaver.ui.settings.SettingsViewModel
import io.github.kmbisset89.worldweaver.ui.settings.ShellSettingsStore
import io.github.kmbisset89.worldweaver.ui.worlds.WorldsViewModel
import org.koin.dsl.module
import java.util.prefs.Preferences

internal fun appModule() = module {
    single { AppCoroutineScope() }
    single { InstantProvider() }
    single { EntityIdFactory() }
    single { WorldEntityConverter() }
    single { WorldCalendarEntityConverter() }
    single { WorldCalendarObservanceEntityConverter() }
    single { WorldDateFormatter() }
    single { DefaultWorldCalendarFactory(get()) }
    single { CampaignEntityConverter() }
    single { LocationEntityConverter() }
    single { LocationOverlayEntityConverter() }
    single { LoreEntityConverter() }
    single { FactionEntityConverter() }
    single { FactionMembershipEntityConverter() }
    single { FifthEditionSheetConverter() }
    single { Pathfinder2ESheetConverter() }
    single { PersonSheetEntityConverter(get(), get()) }
    single { PersonSheetFactory() }
    single { WorldPersonEntityConverter(get()) }
    single { CampaignPersonEntityConverter(get()) }
    single { PersonRelationshipEntityConverter() }
    single { PersonCompanionEntityConverter() }
    single { QuestEntityConverter() }
    single { SessionEntityConverter() }
    single { PlotThreadEntityConverter() }
    single { ReferenceDocEntityConverter() }
    single { EncounterEntityConverter() }
    single { BattleMapEntityConverter() }
    single { BattleMapSituationEntityConverter() }
    single { WorldMapEntityConverter() }
    single { MapTilePyramidFactory() }
    single { BattleMapImageScaler() }
    single { BattleMapSituationImageTransformer() }
    single { BundledBattleMapCatalogLoader() }
    single { WorldWeaverDataDirectory() }
    single { BattleMapFileStore(get<WorldWeaverDataDirectory>().mapsDir) }
    single { WorldMapFileStore(get<WorldWeaverDataDirectory>().worldMapsDir) }
    single { PersonAvatarFileStore(get<WorldWeaverDataDirectory>().avatarsDir) }
    single { VoiceClipFileStore(get<WorldWeaverDataDirectory>().voicesDir) }
    single { SrdCatalogJsonConverter() }
    single { BundledSrdCatalogLoader(get()) }
    single { FifthEditionPickerCatalogResolver() }
    single<SrdCatalogRepository> {
        SrdCatalogFileStore(get<WorldWeaverDataDirectory>().srdDir, get())
    }
    single { VoiceClipRecorder() }
    single { VoiceClipPlayer() }
    single { DiceRoller() }
    single { AbilityScoreRoller(get()) }
    single { DatabaseProvider(get()) }
    single<DatabaseSnapshotExporter> { get<DatabaseProvider>() }
    single { get<DatabaseProvider>().database }
    single { get<WorldWeaverDatabase>().worldDao() }
    single { get<WorldWeaverDatabase>().worldCalendarDao() }
    single { get<WorldWeaverDatabase>().worldCalendarMonthDao() }
    single { get<WorldWeaverDatabase>().worldCalendarWeekdayDao() }
    single { get<WorldWeaverDatabase>().worldCalendarObservanceDao() }
    single { get<WorldWeaverDatabase>().worldCalendarObservanceLoreLinkDao() }
    single { get<WorldWeaverDatabase>().campaignDao() }
    single { get<WorldWeaverDatabase>().locationDao() }
    single { get<WorldWeaverDatabase>().locationOverlayDao() }
    single { get<WorldWeaverDatabase>().loreDao() }
    single { get<WorldWeaverDatabase>().loreSecretDao() }
    single { get<WorldWeaverDatabase>().loreHintDao() }
    single { get<WorldWeaverDatabase>().worldPersonDao() }
    single { get<WorldWeaverDatabase>().campaignPersonDao() }
    single { get<WorldWeaverDatabase>().factionDao() }
    single { get<WorldWeaverDatabase>().factionMembershipDao() }
    single { get<WorldWeaverDatabase>().personRelationshipDao() }
    single { get<WorldWeaverDatabase>().personCompanionDao() }
    single { get<WorldWeaverDatabase>().questDao() }
    single { get<WorldWeaverDatabase>().questObjectiveDao() }
    single { get<WorldWeaverDatabase>().questLinkDao() }
    single { get<WorldWeaverDatabase>().sessionDao() }
    single { get<WorldWeaverDatabase>().sessionSceneDao() }
    single { get<WorldWeaverDatabase>().sessionMarchEntryDao() }
    single { get<WorldWeaverDatabase>().plotThreadDao() }
    single { get<WorldWeaverDatabase>().referenceDocDao() }
    single { get<WorldWeaverDatabase>().encounterDao() }
    single { get<WorldWeaverDatabase>().encounterParticipantDao() }
    single { get<WorldWeaverDatabase>().battleMapDao() }
    single { get<WorldWeaverDatabase>().battleMapSituationDao() }
    single { get<WorldWeaverDatabase>().worldMapDao() }
    single<WorldRepository> { WorldRepositoryImpl(get(), get()) }
    single<WorldCalendarRepository> { WorldCalendarRepositoryImpl(get(), get(), get(), get()) }
    single<WorldCalendarObservanceRepository> {
        WorldCalendarObservanceRepositoryImpl(get(), get(), get())
    }
    single<CampaignRepository> { CampaignRepositoryImpl(get(), get()) }
    single<LocationRepository> { LocationRepositoryImpl(get(), get()) }
    single<LocationOverlayRepository> { LocationOverlayRepositoryImpl(get(), get()) }
    single<LoreRepository> { LoreRepositoryImpl(get(), get(), get(), get()) }
    single<FactionRepository> { FactionRepositoryImpl(get(), get()) }
    single<FactionMembershipRepository> { FactionMembershipRepositoryImpl(get(), get()) }
    single<WorldPersonRepository> { WorldPersonRepositoryImpl(get(), get()) }
    single<CampaignPersonRepository> { CampaignPersonRepositoryImpl(get(), get()) }
    single<PersonRelationshipRepository> { PersonRelationshipRepositoryImpl(get(), get()) }
    single<PersonCompanionRepository> { PersonCompanionRepositoryImpl(get(), get()) }
    single<QuestRepository> { QuestRepositoryImpl(get(), get(), get(), get()) }
    single<SessionRepository> { SessionRepositoryImpl(get(), get(), get(), get()) }
    single<PlotThreadRepository> { PlotThreadRepositoryImpl(get(), get()) }
    single<ReferenceDocRepository> { ReferenceDocRepositoryImpl(get(), get()) }
    single<EncounterRepository> { EncounterRepositoryImpl(get(), get(), get()) }
    single<BattleMapRepository> { BattleMapRepositoryImpl(get(), get()) }
    single<BattleMapSituationRepository> { BattleMapSituationRepositoryImpl(get(), get()) }
    single<WorldMapRepository> { WorldMapRepositoryImpl(get(), get()) }
    single<ActiveContextRepository> { PreferenceActiveContextRepository() }
    single<TransactionRunner> { RoomTransactionRunner(get()) }
    single { WorldBundleArchiveConverter() }
    single { AppBackupArchiveConverter() }
    single { WorldBundleIdRemapper(get()) }
    single {
        WorldBundleSnapshotFactory(
            worldRepository = get(),
            worldCalendarRepository = get(),
            observanceRepository = get(),
            campaignRepository = get(),
            locationRepository = get(),
            loreRepository = get(),
            factionRepository = get(),
            factionMembershipRepository = get(),
            worldPersonRepository = get(),
            campaignPersonRepository = get(),
            locationOverlayRepository = get(),
            questRepository = get(),
            sessionRepository = get(),
            plotThreadRepository = get(),
            referenceDocRepository = get(),
            battleMapRepository = get(),
            battleMapSituationRepository = get(),
            encounterRepository = get(),
            personRelationshipRepository = get(),
            personCompanionRepository = get(),
            avatarFileStore = get(),
            battleMapFileStore = get(),
            worldMapRepository = get(),
            worldMapFileStore = get(),
            voiceClipFileStore = get(),
            instantProvider = get(),
        )
    }

    factory { SetActiveWorldUseCase(get(), get(), get(), get()) }
    factory { SetActiveCampaignUseCase(get(), get()) }
    factory { SetActiveSessionUseCase(get(), get(), get()) }
    factory { ClearActiveCampaignUseCase(get()) }
    factory { CreateWorldUseCase(get(), get(), get(), get(), get(), get()) }
    factory { OneShotTemplateCatalog() }
    factory { OneShotDraftFactory(get()) }
    factory { CreateOneShotUseCase(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { UpdateWorldUseCase(get(), get()) }
    factory { DeleteWorldUseCase(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { ExportWorldBundleUseCase(get(), get()) }
    factory { UniversalVttDocumentFactory() }
    factory { ExportUniversalVttUseCase(get(), get(), get()) }
    factory {
        ExportAppBackupUseCase(
            dataDirectory = get(),
            snapshotExporter = get(),
            archiveConverter = get(),
            activeContextRepository = get(),
            shellSettingsStore = get(),
            instantProvider = get(),
        )
    }
    factory {
        RestoreAppBackupUseCase(
            dataDirectory = get(),
            snapshotExporter = get(),
            archiveConverter = get(),
            activeContextRepository = get(),
            shellSettingsStore = get(),
        )
    }
    factory {
        ImportWorldBundleUseCase(
            archiveConverter = get(),
            idRemapper = get(),
            transactionRunner = get(),
            worldRepository = get(),
            worldCalendarRepository = get(),
            defaultCalendarFactory = get(),
            observanceRepository = get(),
            campaignRepository = get(),
            locationRepository = get(),
            loreRepository = get(),
            factionRepository = get(),
            factionMembershipRepository = get(),
            worldPersonRepository = get(),
            campaignPersonRepository = get(),
            locationOverlayRepository = get(),
            questRepository = get(),
            sessionRepository = get(),
            plotThreadRepository = get(),
            referenceDocRepository = get(),
            battleMapRepository = get(),
            battleMapSituationRepository = get(),
            encounterRepository = get(),
            personRelationshipRepository = get(),
            personCompanionRepository = get(),
            avatarFileStore = get(),
            battleMapFileStore = get(),
            worldMapRepository = get(),
            worldMapFileStore = get(),
            voiceClipFileStore = get(),
            setActiveWorld = get(),
        )
    }
    factory { ObserveWorldsUseCase(get()) }
    factory { ObserveDashboardCountsUseCase(get(), get(), get(), get()) }
    factory { SearchRecordsUseCase(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { CreateCampaignUseCase(get(), get(), get(), get(), get()) }
    factory { UpdateCampaignUseCase(get(), get()) }
    factory { SetCampaignStatusUseCase(get(), get()) }
    factory { DeleteCampaignUseCase(get(), get()) }
    factory { ObserveCampaignsForActiveWorldUseCase(get(), get()) }
    factory { CreateLocationUseCase(get(), get(), get(), get()) }
    factory { UpdateLocationUseCase(get(), get()) }
    factory { UpdateLocationMapAnchorUseCase(get(), get()) }
    factory { DeleteLocationUseCase(get(), get(), get(), get(), get()) }
    factory { ObserveLocationsForActiveWorldUseCase(get(), get()) }
    factory { ObserveLocationOverlaysForActiveCampaignUseCase(get(), get()) }
    factory { UpdateLocationOverlayUseCase(get(), get(), get(), get()) }
    factory { CreateLoreUseCase(get(), get(), get(), get(), get()) }
    factory { UpdateLoreUseCase(get(), get(), get(), get()) }
    factory { DeleteLoreUseCase(get(), get()) }
    factory { ObserveLoreForActiveWorldUseCase(get(), get()) }
    factory { CreateFactionUseCase(get(), get(), get(), get()) }
    factory { UpdateFactionUseCase(get(), get()) }
    factory { DeleteFactionUseCase(get(), get(), get()) }
    factory { ObserveFactionsForActiveWorldUseCase(get(), get()) }
    factory { CreateFactionMembershipUseCase(get(), get(), get(), get(), get(), get(), get()) }
    factory { DeleteFactionMembershipUseCase(get()) }
    factory { ObserveFactionMembershipsUseCase(get()) }
    factory { ObserveWorldCalendarForActiveWorldUseCase(get(), get()) }
    factory { ObserveWorldCalendarObservancesForActiveWorldUseCase(get(), get()) }
    factory { FindSessionCalendarMonthIdsForWorldUseCase(get(), get()) }
    factory { UpdateWorldCalendarUseCase(get(), get(), get(), get(), get(), get()) }
    factory { CreateWorldCalendarObservanceUseCase(get(), get(), get(), get(), get(), get()) }
    factory { UpdateWorldCalendarObservanceUseCase(get(), get(), get(), get()) }
    factory { DeleteWorldCalendarObservanceUseCase(get()) }
    factory { CreateWorldPersonUseCase(get(), get(), get(), get()) }
    factory { UpdateWorldPersonUseCase(get(), get()) }
    factory { DeleteWorldPersonUseCase(get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { CreateCampaignPersonUseCase(get(), get(), get(), get()) }
    factory { UpdateCampaignPersonUseCase(get(), get()) }
    factory { DeleteCampaignPersonUseCase(get(), get(), get(), get(), get(), get(), get()) }
    factory { AddWorldPersonToCampaignUseCase(get(), get(), get(), get(), get(), get(), get()) }
    factory { SetPersonAvatarUseCase(get(), get(), get(), get()) }
    factory { ClearPersonAvatarUseCase(get(), get(), get(), get()) }
    factory { SetVoiceClipUseCase(get(), get(), get(), get(), get()) }
    factory { ClearVoiceClipUseCase(get(), get(), get(), get(), get()) }
    factory { PlaceEncounterTokenUseCase(get(), get()) }
    factory { PlaceBattleMapItemUseCase(get(), get(), get()) }
    factory { DeleteBattleMapItemUseCase(get(), get()) }
    factory { ObservePeopleForActiveContextUseCase(get(), get(), get()) }
    factory { GenerateRandomNpcUseCase(get()) }
    factory { ObserveSrdCatalogUseCase(get()) }
    factory { ObserveFifthEditionPickerCatalogUseCase(get(), get()) }
    factory { ImportSrdCatalogUseCase(get(), get(), get(), get()) }
    factory { ClearSrdCatalogUseCase(get()) }
    factory { CreateWorldPersonFromSrdMonsterUseCase(get(), get(), get()) }
    factory { CreatePersonRelationshipUseCase(get(), get(), get(), get(), get(), get()) }
    factory { DeletePersonRelationshipUseCase(get()) }
    factory { ObservePersonRelationshipsUseCase(get()) }
    factory { RelationshipWebFactory() }
    factory { ObserveRelationshipWebUseCase(get(), get(), get(), get(), get(), get()) }
    factory { CreatePersonCompanionUseCase(get(), get(), get(), get()) }
    factory { DeletePersonCompanionUseCase(get()) }
    factory { ObservePersonCompanionsUseCase(get()) }
    factory { CreateQuestUseCase(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { UpdateQuestUseCase(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { DeleteQuestUseCase(get()) }
    factory { ObserveQuestsForActiveCampaignUseCase(get(), get()) }
    factory { CreateSessionUseCase(get(), get(), get(), get(), get(), get(), get()) }
    factory { UpdateSessionUseCase(get(), get(), get(), get(), get(), get()) }
    factory { DeleteSessionUseCase(get(), get(), get()) }
    factory { ObserveSessionsForActiveCampaignUseCase(get(), get()) }
    factory { CreatePlotThreadUseCase(get(), get(), get(), get(), get()) }
    factory { UpdatePlotThreadUseCase(get(), get(), get()) }
    factory { DeletePlotThreadUseCase(get()) }
    factory { ObservePlotThreadsForActiveCampaignUseCase(get(), get()) }
    factory { CreateReferenceDocUseCase(get(), get(), get(), get(), get()) }
    factory { UpdateReferenceDocUseCase(get(), get(), get()) }
    factory { DeleteReferenceDocUseCase(get()) }
    factory { ObserveReferenceDocsForActiveCampaignUseCase(get(), get()) }
    factory { SaveSessionNpcDraftUseCase(get(), get()) }
    factory { CreateEncounterUseCase(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { UpdateEncounterUseCase(get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { CreateBattleMapUseCase(get(), get(), get(), get(), get(), get()) }
    factory { ImportBundledBattleMapUseCase(get(), get(), get(), get(), get()) }
    factory { UpdateBattleMapUseCase(get(), get()) }
    factory { UpdateBattleMapFogUseCase(get(), get()) }
    factory { UpdateBattleMapTerrainUseCase(get(), get()) }
    factory { CloseSessionUseCase(get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { AwardPartyLevelUseCase(get(), get(), get()) }
    factory { AwardPartyExperienceUseCase(get(), get(), get()) }
    factory { DeleteBattleMapUseCase(get(), get(), get(), get()) }
    factory { ObserveBattleMapsForActiveCampaignUseCase(get(), get()) }
    factory { CreateWorldMapUseCase(get(), get(), get(), get(), get(), get(), get()) }
    factory { DeleteWorldMapUseCase(get(), get()) }
    factory { ObserveWorldMapsForActiveWorldUseCase(get(), get()) }
    factory { CreateBattleMapSituationUseCase(get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { ToggleBattleMapSituationUseCase(get(), get()) }
    factory { DeleteBattleMapSituationUseCase(get(), get()) }
    factory { ObserveBattleMapSituationsForActiveCampaignUseCase(get(), get()) }
    factory { CalculateReachableCellsUseCase() }
    factory { CalculateGridDistanceUseCase() }
    single { BattleMapMapStateFactory(get()) }
    factory { BattleMapMovementOverlay() }
    factory { BattleMapMeasureOverlay() }
    factory { BattleMapTokenOverlay() }
    factory { BattleMapItemOverlay() }
    factory { DeleteEncounterUseCase(get()) }
    factory { ObserveEncountersForActiveCampaignUseCase(get(), get()) }
    factory { StartEncounterUseCase(get(), get()) }
    factory { EndEncounterUseCase(get(), get(), get(), get()) }
    factory { AdvanceEncounterTurnUseCase(get(), get()) }
    factory { UpdateEncounterParticipantCombatUseCase(get(), get(), get(), get()) }
    factory { RollEncounterInitiativeUseCase(get()) }
    factory { RollAllEncounterInitiativeUseCase(get(), get(), get()) }
    factory { UpdateCampaignPersonDeathSavesUseCase(get(), get()) }
    factory {
        BattleMapBoardSession(
            appScope = get(),
            mapStateFactory = get(),
            movementOverlay = get(),
            measureOverlay = get(),
            tokenOverlay = get(),
            itemOverlay = get(),
            calculateReachableCells = get(),
            calculateGridDistance = get(),
            placeEncounterToken = get(),
            updateBattleMapFog = get(),
            updateBattleMapTerrain = get(),
            placeBattleMapItem = get(),
            deleteBattleMapItem = get(),
            avatarFileStore = get(),
        )
    }
    factory { ObserveActiveContextUseCase(get()) }
    factory { ObserveActiveContextDetailsUseCase(get(), get(), get()) }

    single {
        ShellSettingsStore(
            preferences = Preferences.userRoot().node(ShellSettingsStore.PREF_NODE),
        )
    }
    single {
        HomeViewModel(
            shellSettingsStore = get(),
            appScope = get(),
            observeWorlds = get(),
            observeActiveContextDetails = get(),
            observeActiveContext = get(),
            observeSessions = get(),
            observeDashboardCounts = get(),
            setActiveWorld = get(),
        )
    }
    single {
        WorldsViewModel(
            appScope = get(),
            observeWorlds = get(),
            observeActiveContext = get(),
            createWorld = get(),
            updateWorld = get(),
            deleteWorld = get(),
            setActiveWorld = get(),
            exportWorldBundle = get(),
            importWorldBundle = get(),
        )
    }
    single {
        OneShotWizardViewModel(
            appScope = get(),
            createOneShot = get(),
            catalog = get(),
            draftFactory = get(),
        )
    }
    single {
        CampaignsViewModel(
            appScope = get(),
            observeActiveContextDetails = get(),
            observeCampaigns = get(),
            observePeople = get(),
            observeQuests = get(),
            observeSessions = get(),
            observeLocations = get(),
            observeOverlays = get(),
            observeCalendar = get(),
            createCampaign = get(),
            updateCampaign = get(),
            setCampaignStatus = get(),
            deleteCampaign = get(),
            setActiveCampaign = get(),
        )
    }
    single {
        LocationsViewModel(
            appScope = get(),
            observeActiveContextDetails = get(),
            observeLocations = get(),
            observeOverlays = get(),
            observeLore = get(),
            observeQuests = get(),
            observeWorldMaps = get(),
            createLocation = get(),
            updateLocation = get(),
            deleteLocation = get(),
            updateLocationOverlay = get(),
            setVoiceClip = get(),
            clearVoiceClip = get(),
            voiceClipFileStore = get(),
            voiceClipRecorder = get(),
            voiceClipPlayer = get(),
        )
    }
    single {
        CalendarViewModel(
            appScope = get(),
            observeActiveContextDetails = get(),
            observeCalendar = get(),
            observeObservances = get(),
            observeLore = get(),
            findSessionMonthIds = get(),
            updateCalendar = get(),
            createObservance = get(),
            updateObservance = get(),
            deleteObservance = get(),
            dateFormatter = get(),
        )
    }
    single {
        LoreViewModel(
            appScope = get(),
            observeActiveContextDetails = get(),
            observeLore = get(),
            observeObservances = get(),
            observeCalendar = get(),
            observeLocations = get(),
            observePeople = get(),
            createLore = get(),
            updateLore = get(),
            deleteLore = get(),
        )
    }
    single {
        FactionsViewModel(
            appScope = get(),
            observeActiveContextDetails = get(),
            observeFactions = get(),
            observeMemberships = get(),
            observePeople = get(),
            createFaction = get(),
            updateFaction = get(),
            deleteFaction = get(),
            deleteMembership = get(),
        )
    }
    single { RelationshipWebLayoutFactory() }
    single {
        LinksViewModel(
            appScope = get(),
            observeRelationshipWeb = get(),
            relationshipWebFactory = get(),
            layoutFactory = get(),
        )
    }
    single {
        CharactersViewModel(
            appScope = get(),
            observeActiveContextDetails = get(),
            observePeople = get(),
            observeRelationships = get(),
            observeCompanions = get(),
            observeLore = get(),
            observeQuests = get(),
            observeFactions = get(),
            observeMemberships = get(),
            createWorldPerson = get(),
            updateWorldPerson = get(),
            deleteWorldPerson = get(),
            createCampaignPerson = get(),
            updateCampaignPerson = get(),
            deleteCampaignPerson = get(),
            addWorldPersonToCampaign = get(),
            setPersonAvatar = get(),
            clearPersonAvatar = get(),
            avatarFileStore = get(),
            setVoiceClip = get(),
            clearVoiceClip = get(),
            voiceClipFileStore = get(),
            voiceClipRecorder = get(),
            voiceClipPlayer = get(),
            generateRandomNpc = get(),
            observePickerCatalog = get(),
            createFromSrdMonster = get(),
            createPersonRelationship = get(),
            deletePersonRelationship = get(),
            createFactionMembership = get(),
            deleteFactionMembership = get(),
            createPersonCompanion = get(),
            deletePersonCompanion = get(),
        )
    }
    single {
        CharacterSheetViewModel(
            appScope = get(),
            observePeople = get(),
            updateWorldPerson = get(),
            updateCampaignPerson = get(),
            updateDeathSaves = get(),
            avatarFileStore = get(),
            observeActiveContextDetails = get(),
        )
    }
    single {
        QuestsViewModel(
            appScope = get(),
            observeActiveContextDetails = get(),
            observeQuests = get(),
            observeLocations = get(),
            observeLore = get(),
            observePeople = get(),
            observeSessions = get(),
            createQuest = get(),
            updateQuest = get(),
            deleteQuest = get(),
            awardPartyLevel = get(),
            awardPartyExperience = get(),
        )
    }
    single {
        SessionsViewModel(
            appScope = get(),
            observeActiveContextDetails = get(),
            observeSessions = get(),
            observeQuests = get(),
            observeLocations = get(),
            observeOverlays = get(),
            observePeople = get(),
            observeThreads = get(),
            observeDocs = get(),
            observeCalendar = get(),
            createSession = get(),
            updateSession = get(),
            deleteSession = get(),
            createPlotThread = get(),
            updatePlotThread = get(),
            deletePlotThread = get(),
            createReferenceDoc = get(),
            updateReferenceDoc = get(),
            deleteReferenceDoc = get(),
            generateRandomNpc = get(),
            saveSessionNpcDraft = get(),
            setActiveSession = get(),
        )
    }
    single {
        EncountersViewModel(
            appScope = get(),
            observeActiveContextDetails = get(),
            observeEncounters = get(),
            observeLocations = get(),
            observePeople = get(),
            observeCompanions = get(),
            observeBattleMaps = get(),
            observeSituations = get(),
            createEncounter = get(),
            updateEncounter = get(),
            deleteEncounter = get(),
            startEncounter = get(),
            endEncounter = get(),
            advanceTurn = get(),
            updateCombat = get(),
            rollEncounterInitiative = get(),
            rollAllInitiative = get(),
            updateDeathSaves = get(),
            boardSession = get(),
        )
    }
    single {
        MapsViewModel(
            appScope = get(),
            observeActiveContextDetails = get(),
            observeBattleMaps = get(),
            observeSituations = get(),
            createBattleMap = get(),
            deleteBattleMap = get(),
            createSituation = get(),
            toggleSituation = get(),
            deleteSituation = get(),
            mapStateFactory = get(),
            movementOverlay = get(),
            measureOverlay = get(),
            tokenOverlay = get(),
            itemOverlay = get(),
            calculateReachableCells = get(),
            calculateGridDistance = get(),
            placeEncounterToken = get(),
            observeEncounters = get(),
            observePeople = get(),
            avatarFileStore = get(),
            imageScaler = get(),
            updateBattleMapFog = get(),
            updateBattleMapTerrain = get(),
            placeBattleMapItem = get(),
            deleteBattleMapItem = get(),
            importBundledBattleMap = get(),
            exportUniversalVtt = get(),
            bundledCatalogLoader = get(),
        )
    }
    single { WorldMapMapStateFactory(get()) }
    factory { WorldMapPinOverlay() }
    single {
        WorldMapViewModel(
            appScope = get(),
            observeActiveContextDetails = get(),
            observeLocations = get(),
            observeWorldMaps = get(),
            createWorldMap = get(),
            deleteWorldMap = get(),
            updateLocationMapAnchor = get(),
            mapStateFactory = get(),
            pinOverlay = get(),
        )
    }
    single {
        RunViewModel(
            appScope = get(),
            observeActiveContext = get(),
            observeActiveContextDetails = get(),
            observeSessions = get(),
            observeQuests = get(),
            observePeople = get(),
            observeEncounters = get(),
            observeCalendar = get(),
            observeObservances = get(),
            observeOverlays = get(),
            observeLocations = get(),
            closeSession = get(),
            awardPartyLevel = get(),
            awardPartyExperience = get(),
        )
    }
    single {
        DiceViewModel(
            diceRoller = get(),
            activeContextRepository = get(),
            appScope = get(),
        )
    }
    single {
        SearchViewModel(
            appScope = get(),
            searchRecords = get(),
        )
    }
    single {
        SettingsViewModel(
            shellSettingsStore = get(),
            exportAppBackup = get(),
            restoreAppBackup = get(),
            observeSrdCatalog = get(),
            importSrdCatalog = get(),
            clearSrdCatalog = get(),
            appScope = get(),
        )
    }
    single {
        AppViewModel(
            homeViewModel = get(),
            worldsViewModel = get(),
            oneShotWizardViewModel = get(),
            campaignsViewModel = get(),
            locationsViewModel = get(),
            loreViewModel = get(),
            calendarViewModel = get(),
            factionsViewModel = get(),
            linksViewModel = get(),
            charactersViewModel = get(),
            characterSheetViewModel = get(),
            questsViewModel = get(),
            sessionsViewModel = get(),
            encountersViewModel = get(),
            mapsViewModel = get(),
            worldMapViewModel = get(),
            runViewModel = get(),
            diceViewModel = get(),
            searchViewModel = get(),
            settingsViewModel = get(),
            shellSettingsStore = get(),
            appScope = get(),
            observeActiveContextDetails = get(),
            setActiveWorld = get(),
            setActiveCampaign = get(),
        )
    }
}
