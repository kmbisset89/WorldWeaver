package net.tactware.worldweaver.di

import net.tactware.worldweaver.core.AppCoroutineScope
import net.tactware.worldweaver.data.BattleMapEntityConverter
import net.tactware.worldweaver.data.BattleMapRepositoryImpl
import net.tactware.worldweaver.data.BattleMapSituationEntityConverter
import net.tactware.worldweaver.data.BattleMapSituationRepositoryImpl
import net.tactware.worldweaver.data.CampaignEntityConverter
import net.tactware.worldweaver.data.CampaignPersonEntityConverter
import net.tactware.worldweaver.data.CampaignPersonRepositoryImpl
import net.tactware.worldweaver.data.CampaignRepositoryImpl
import net.tactware.worldweaver.data.DatabaseProvider
import net.tactware.worldweaver.data.EncounterEntityConverter
import net.tactware.worldweaver.data.EncounterRepositoryImpl
import net.tactware.worldweaver.data.FifthEditionSheetConverter
import net.tactware.worldweaver.data.LocationEntityConverter
import net.tactware.worldweaver.data.LocationOverlayEntityConverter
import net.tactware.worldweaver.data.LocationOverlayRepositoryImpl
import net.tactware.worldweaver.data.LocationRepositoryImpl
import net.tactware.worldweaver.data.LoreEntityConverter
import net.tactware.worldweaver.data.LoreRepositoryImpl
import net.tactware.worldweaver.data.PersonCompanionEntityConverter
import net.tactware.worldweaver.data.PersonCompanionRepositoryImpl
import net.tactware.worldweaver.data.PersonRelationshipEntityConverter
import net.tactware.worldweaver.data.PersonRelationshipRepositoryImpl
import net.tactware.worldweaver.data.PlotThreadEntityConverter
import net.tactware.worldweaver.data.PlotThreadRepositoryImpl
import net.tactware.worldweaver.data.PreferenceActiveContextRepository
import net.tactware.worldweaver.data.QuestEntityConverter
import net.tactware.worldweaver.data.QuestRepositoryImpl
import net.tactware.worldweaver.data.RoomTransactionRunner
import net.tactware.worldweaver.data.ReferenceDocEntityConverter
import net.tactware.worldweaver.data.ReferenceDocRepositoryImpl
import net.tactware.worldweaver.data.SessionEntityConverter
import net.tactware.worldweaver.data.SessionRepositoryImpl
import net.tactware.worldweaver.data.WorldCalendarEntityConverter
import net.tactware.worldweaver.data.WorldCalendarRepositoryImpl
import net.tactware.worldweaver.data.WorldEntityConverter
import net.tactware.worldweaver.data.WorldPersonEntityConverter
import net.tactware.worldweaver.data.WorldPersonRepositoryImpl
import net.tactware.worldweaver.data.WorldRepositoryImpl
import net.tactware.worldweaver.data.WorldWeaverDatabase
import net.tactware.worldweaver.domain.AbilityScoreRoller
import net.tactware.worldweaver.domain.BattleMapFileStore
import net.tactware.worldweaver.domain.ClearPersonAvatarUseCase
import net.tactware.worldweaver.domain.ClearVoiceClipUseCase
import net.tactware.worldweaver.domain.BattleMapImageScaler
import net.tactware.worldweaver.domain.BattleMapRepository
import net.tactware.worldweaver.domain.BattleMapSituationImageTransformer
import net.tactware.worldweaver.domain.BattleMapSituationRepository
import net.tactware.worldweaver.domain.BattleMapTilePyramidFactory
import net.tactware.worldweaver.domain.CalculateGridDistanceUseCase
import net.tactware.worldweaver.domain.CalculateReachableCellsUseCase
import net.tactware.worldweaver.domain.DiceRoller
import net.tactware.worldweaver.domain.ActiveContextRepository
import net.tactware.worldweaver.domain.AddWorldPersonToCampaignUseCase
import net.tactware.worldweaver.domain.CampaignPersonRepository
import net.tactware.worldweaver.domain.CampaignRepository
import net.tactware.worldweaver.domain.ClearActiveCampaignUseCase
import net.tactware.worldweaver.domain.CreateCampaignPersonUseCase
import net.tactware.worldweaver.domain.AdvanceEncounterTurnUseCase
import net.tactware.worldweaver.domain.CreateBattleMapSituationUseCase
import net.tactware.worldweaver.domain.CreateBattleMapUseCase
import net.tactware.worldweaver.domain.CreateCampaignUseCase
import net.tactware.worldweaver.domain.CreateEncounterUseCase
import net.tactware.worldweaver.domain.CreateLocationUseCase
import net.tactware.worldweaver.domain.CreateLoreUseCase
import net.tactware.worldweaver.domain.CreatePersonCompanionUseCase
import net.tactware.worldweaver.domain.CreatePersonRelationshipUseCase
import net.tactware.worldweaver.domain.CreatePlotThreadUseCase
import net.tactware.worldweaver.domain.CreateQuestUseCase
import net.tactware.worldweaver.domain.CreateReferenceDocUseCase
import net.tactware.worldweaver.domain.CreateSessionUseCase
import net.tactware.worldweaver.domain.CreateWorldPersonUseCase
import net.tactware.worldweaver.domain.CreateWorldUseCase
import net.tactware.worldweaver.domain.DefaultWorldCalendarFactory
import net.tactware.worldweaver.domain.FindSessionCalendarMonthIdsForWorldUseCase
import net.tactware.worldweaver.domain.DatabaseSnapshotExporter
import net.tactware.worldweaver.domain.DeleteCampaignPersonUseCase
import net.tactware.worldweaver.domain.DeleteBattleMapSituationUseCase
import net.tactware.worldweaver.domain.DeleteBattleMapUseCase
import net.tactware.worldweaver.domain.DeleteCampaignUseCase
import net.tactware.worldweaver.domain.DeleteEncounterUseCase
import net.tactware.worldweaver.domain.DeleteLocationUseCase
import net.tactware.worldweaver.domain.DeleteLoreUseCase
import net.tactware.worldweaver.domain.DeletePersonCompanionUseCase
import net.tactware.worldweaver.domain.DeletePersonRelationshipUseCase
import net.tactware.worldweaver.domain.DeletePlotThreadUseCase
import net.tactware.worldweaver.domain.DeleteQuestUseCase
import net.tactware.worldweaver.domain.DeleteReferenceDocUseCase
import net.tactware.worldweaver.domain.DeleteSessionUseCase
import net.tactware.worldweaver.domain.DeleteWorldPersonUseCase
import net.tactware.worldweaver.domain.DeleteWorldUseCase
import net.tactware.worldweaver.domain.EncounterRepository
import net.tactware.worldweaver.domain.EndEncounterUseCase
import net.tactware.worldweaver.domain.EntityIdFactory
import net.tactware.worldweaver.domain.AppBackupArchiveConverter
import net.tactware.worldweaver.domain.ExportAppBackupUseCase
import net.tactware.worldweaver.domain.ExportWorldBundleUseCase
import net.tactware.worldweaver.domain.ImportWorldBundleUseCase
import net.tactware.worldweaver.domain.RestoreAppBackupUseCase
import net.tactware.worldweaver.domain.TransactionRunner
import net.tactware.worldweaver.domain.WorldBundleArchiveConverter
import net.tactware.worldweaver.domain.WorldBundleIdRemapper
import net.tactware.worldweaver.domain.WorldBundleSnapshotFactory
import net.tactware.worldweaver.domain.GenerateRandomNpcUseCase
import net.tactware.worldweaver.domain.InstantProvider
import net.tactware.worldweaver.domain.LocationOverlayRepository
import net.tactware.worldweaver.domain.LocationRepository
import net.tactware.worldweaver.domain.LoreRepository
import net.tactware.worldweaver.domain.ObserveActiveContextDetailsUseCase
import net.tactware.worldweaver.domain.ObserveActiveContextUseCase
import net.tactware.worldweaver.domain.ObserveCampaignsForActiveWorldUseCase
import net.tactware.worldweaver.domain.ObserveBattleMapSituationsForActiveCampaignUseCase
import net.tactware.worldweaver.domain.ObserveBattleMapsForActiveCampaignUseCase
import net.tactware.worldweaver.domain.ToggleBattleMapSituationUseCase
import net.tactware.worldweaver.domain.ObserveEncountersForActiveCampaignUseCase
import net.tactware.worldweaver.domain.ObserveLocationOverlaysForActiveCampaignUseCase
import net.tactware.worldweaver.domain.ObserveLocationsForActiveWorldUseCase
import net.tactware.worldweaver.domain.ObserveLoreForActiveWorldUseCase
import net.tactware.worldweaver.domain.ObservePeopleForActiveContextUseCase
import net.tactware.worldweaver.domain.ObservePersonCompanionsUseCase
import net.tactware.worldweaver.domain.ObservePersonRelationshipsUseCase
import net.tactware.worldweaver.domain.ObservePlotThreadsForActiveCampaignUseCase
import net.tactware.worldweaver.domain.ObserveQuestsForActiveCampaignUseCase
import net.tactware.worldweaver.domain.ObserveReferenceDocsForActiveCampaignUseCase
import net.tactware.worldweaver.domain.ObserveSessionsForActiveCampaignUseCase
import net.tactware.worldweaver.domain.ObserveDashboardCountsUseCase
import net.tactware.worldweaver.domain.SearchRecordsUseCase
import net.tactware.worldweaver.domain.ObserveWorldCalendarForActiveWorldUseCase
import net.tactware.worldweaver.domain.ObserveWorldsUseCase
import net.tactware.worldweaver.domain.UpdateWorldCalendarUseCase
import net.tactware.worldweaver.domain.PersonAvatarFileStore
import net.tactware.worldweaver.domain.PersonCompanionRepository
import net.tactware.worldweaver.domain.PlaceEncounterTokenUseCase
import net.tactware.worldweaver.domain.SetPersonAvatarUseCase
import net.tactware.worldweaver.domain.SetVoiceClipUseCase
import net.tactware.worldweaver.domain.VoiceClipFileStore
import net.tactware.worldweaver.domain.VoiceClipPlayer
import net.tactware.worldweaver.domain.VoiceClipRecorder
import net.tactware.worldweaver.domain.PersonRelationshipRepository
import net.tactware.worldweaver.domain.PlotThreadRepository
import net.tactware.worldweaver.domain.QuestRepository
import net.tactware.worldweaver.domain.ReferenceDocRepository
import net.tactware.worldweaver.domain.RollAllEncounterInitiativeUseCase
import net.tactware.worldweaver.domain.RollEncounterInitiativeUseCase
import net.tactware.worldweaver.domain.SaveSessionNpcDraftUseCase
import net.tactware.worldweaver.domain.SessionRepository
import net.tactware.worldweaver.domain.SetActiveCampaignUseCase
import net.tactware.worldweaver.domain.StartEncounterUseCase
import net.tactware.worldweaver.domain.SetActiveWorldUseCase
import net.tactware.worldweaver.domain.SetCampaignStatusUseCase
import net.tactware.worldweaver.domain.UpdateCampaignPersonDeathSavesUseCase
import net.tactware.worldweaver.domain.UpdateCampaignPersonUseCase
import net.tactware.worldweaver.domain.UpdateEncounterParticipantCombatUseCase
import net.tactware.worldweaver.domain.UpdateBattleMapFogUseCase
import net.tactware.worldweaver.domain.UpdateBattleMapUseCase
import net.tactware.worldweaver.domain.UpdateEncounterUseCase
import net.tactware.worldweaver.domain.UpdateCampaignUseCase
import net.tactware.worldweaver.domain.UpdateLocationOverlayUseCase
import net.tactware.worldweaver.domain.UpdateLocationUseCase
import net.tactware.worldweaver.domain.UpdateLoreUseCase
import net.tactware.worldweaver.domain.UpdatePlotThreadUseCase
import net.tactware.worldweaver.domain.UpdateQuestUseCase
import net.tactware.worldweaver.domain.UpdateReferenceDocUseCase
import net.tactware.worldweaver.domain.UpdateSessionUseCase
import net.tactware.worldweaver.domain.UpdateWorldPersonUseCase
import net.tactware.worldweaver.domain.UpdateWorldUseCase
import net.tactware.worldweaver.domain.WorldCalendarRepository
import net.tactware.worldweaver.domain.WorldDateFormatter
import net.tactware.worldweaver.domain.WorldPersonRepository
import net.tactware.worldweaver.domain.WorldRepository
import net.tactware.worldweaver.domain.WorldWeaverDataDirectory
import net.tactware.worldweaver.ui.AppViewModel
import net.tactware.worldweaver.ui.calendar.CalendarViewModel
import net.tactware.worldweaver.ui.campaigns.CampaignsViewModel
import net.tactware.worldweaver.ui.characters.CharactersViewModel
import net.tactware.worldweaver.ui.dice.DiceViewModel
import net.tactware.worldweaver.ui.search.SearchViewModel
import net.tactware.worldweaver.ui.encounters.EncountersViewModel
import net.tactware.worldweaver.ui.home.HomeViewModel
import net.tactware.worldweaver.ui.locations.LocationsViewModel
import net.tactware.worldweaver.ui.lore.LoreViewModel
import net.tactware.worldweaver.ui.maps.BattleMapBoardSession
import net.tactware.worldweaver.ui.maps.BattleMapMapStateFactory
import net.tactware.worldweaver.ui.maps.BattleMapMeasureOverlay
import net.tactware.worldweaver.ui.maps.BattleMapMovementOverlay
import net.tactware.worldweaver.ui.maps.BattleMapTokenOverlay
import net.tactware.worldweaver.ui.maps.MapsViewModel
import net.tactware.worldweaver.ui.quests.QuestsViewModel
import net.tactware.worldweaver.ui.sessions.SessionsViewModel
import net.tactware.worldweaver.ui.settings.SettingsViewModel
import net.tactware.worldweaver.ui.settings.ShellSettingsStore
import net.tactware.worldweaver.ui.worlds.WorldsViewModel
import org.koin.dsl.module
import java.util.prefs.Preferences

internal fun appModule() = module {
    single { AppCoroutineScope() }
    single { InstantProvider() }
    single { EntityIdFactory() }
    single { WorldEntityConverter() }
    single { WorldCalendarEntityConverter() }
    single { WorldDateFormatter() }
    single { DefaultWorldCalendarFactory(get()) }
    single { CampaignEntityConverter() }
    single { LocationEntityConverter() }
    single { LocationOverlayEntityConverter() }
    single { LoreEntityConverter() }
    single { FifthEditionSheetConverter() }
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
    single { BattleMapTilePyramidFactory() }
    single { BattleMapImageScaler() }
    single { BattleMapSituationImageTransformer() }
    single { WorldWeaverDataDirectory() }
    single { BattleMapFileStore(get<WorldWeaverDataDirectory>().mapsDir) }
    single { PersonAvatarFileStore(get<WorldWeaverDataDirectory>().avatarsDir) }
    single { VoiceClipFileStore(get<WorldWeaverDataDirectory>().voicesDir) }
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
    single { get<WorldWeaverDatabase>().campaignDao() }
    single { get<WorldWeaverDatabase>().locationDao() }
    single { get<WorldWeaverDatabase>().locationOverlayDao() }
    single { get<WorldWeaverDatabase>().loreDao() }
    single { get<WorldWeaverDatabase>().loreSecretDao() }
    single { get<WorldWeaverDatabase>().loreHintDao() }
    single { get<WorldWeaverDatabase>().worldPersonDao() }
    single { get<WorldWeaverDatabase>().campaignPersonDao() }
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
    single<WorldRepository> { WorldRepositoryImpl(get(), get()) }
    single<WorldCalendarRepository> { WorldCalendarRepositoryImpl(get(), get(), get(), get()) }
    single<CampaignRepository> { CampaignRepositoryImpl(get(), get()) }
    single<LocationRepository> { LocationRepositoryImpl(get(), get()) }
    single<LocationOverlayRepository> { LocationOverlayRepositoryImpl(get(), get()) }
    single<LoreRepository> { LoreRepositoryImpl(get(), get(), get(), get()) }
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
    single<ActiveContextRepository> { PreferenceActiveContextRepository() }
    single<TransactionRunner> { RoomTransactionRunner(get()) }
    single { WorldBundleArchiveConverter() }
    single { AppBackupArchiveConverter() }
    single { WorldBundleIdRemapper(get()) }
    single {
        WorldBundleSnapshotFactory(
            worldRepository = get(),
            worldCalendarRepository = get(),
            campaignRepository = get(),
            locationRepository = get(),
            loreRepository = get(),
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
            voiceClipFileStore = get(),
            instantProvider = get(),
        )
    }

    factory { SetActiveWorldUseCase(get(), get(), get(), get()) }
    factory { SetActiveCampaignUseCase(get(), get()) }
    factory { ClearActiveCampaignUseCase(get()) }
    factory { CreateWorldUseCase(get(), get(), get(), get(), get(), get()) }
    factory { UpdateWorldUseCase(get(), get()) }
    factory { DeleteWorldUseCase(get(), get(), get()) }
    factory { ExportWorldBundleUseCase(get(), get()) }
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
            campaignRepository = get(),
            locationRepository = get(),
            loreRepository = get(),
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
            voiceClipFileStore = get(),
            setActiveWorld = get(),
        )
    }
    factory { ObserveWorldsUseCase(get()) }
    factory { ObserveDashboardCountsUseCase(get(), get(), get(), get()) }
    factory { SearchRecordsUseCase(get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { CreateCampaignUseCase(get(), get(), get(), get(), get()) }
    factory { UpdateCampaignUseCase(get(), get()) }
    factory { SetCampaignStatusUseCase(get(), get()) }
    factory { DeleteCampaignUseCase(get(), get()) }
    factory { ObserveCampaignsForActiveWorldUseCase(get(), get()) }
    factory { CreateLocationUseCase(get(), get(), get(), get()) }
    factory { UpdateLocationUseCase(get(), get()) }
    factory { DeleteLocationUseCase(get(), get(), get()) }
    factory { ObserveLocationsForActiveWorldUseCase(get(), get()) }
    factory { ObserveLocationOverlaysForActiveCampaignUseCase(get(), get()) }
    factory { UpdateLocationOverlayUseCase(get(), get(), get(), get()) }
    factory { CreateLoreUseCase(get(), get(), get(), get(), get()) }
    factory { UpdateLoreUseCase(get(), get(), get(), get()) }
    factory { DeleteLoreUseCase(get(), get()) }
    factory { ObserveLoreForActiveWorldUseCase(get(), get()) }
    factory { ObserveWorldCalendarForActiveWorldUseCase(get(), get()) }
    factory { FindSessionCalendarMonthIdsForWorldUseCase(get(), get()) }
    factory { UpdateWorldCalendarUseCase(get(), get(), get(), get(), get()) }
    factory { CreateWorldPersonUseCase(get(), get(), get(), get()) }
    factory { UpdateWorldPersonUseCase(get(), get()) }
    factory { DeleteWorldPersonUseCase(get(), get(), get(), get(), get(), get(), get()) }
    factory { CreateCampaignPersonUseCase(get(), get(), get(), get()) }
    factory { UpdateCampaignPersonUseCase(get(), get()) }
    factory { DeleteCampaignPersonUseCase(get(), get(), get(), get(), get(), get()) }
    factory { AddWorldPersonToCampaignUseCase(get(), get(), get(), get(), get(), get(), get()) }
    factory { SetPersonAvatarUseCase(get(), get(), get(), get()) }
    factory { ClearPersonAvatarUseCase(get(), get(), get(), get()) }
    factory { SetVoiceClipUseCase(get(), get(), get(), get(), get()) }
    factory { ClearVoiceClipUseCase(get(), get(), get(), get(), get()) }
    factory { PlaceEncounterTokenUseCase(get(), get()) }
    factory { ObservePeopleForActiveContextUseCase(get(), get(), get()) }
    factory { GenerateRandomNpcUseCase(get()) }
    factory { CreatePersonRelationshipUseCase(get(), get(), get(), get()) }
    factory { DeletePersonRelationshipUseCase(get()) }
    factory { ObservePersonRelationshipsUseCase(get()) }
    factory { CreatePersonCompanionUseCase(get(), get(), get(), get()) }
    factory { DeletePersonCompanionUseCase(get()) }
    factory { ObservePersonCompanionsUseCase(get()) }
    factory { CreateQuestUseCase(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { UpdateQuestUseCase(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { DeleteQuestUseCase(get()) }
    factory { ObserveQuestsForActiveCampaignUseCase(get(), get()) }
    factory { CreateSessionUseCase(get(), get(), get(), get(), get(), get(), get()) }
    factory { UpdateSessionUseCase(get(), get(), get(), get(), get(), get()) }
    factory { DeleteSessionUseCase(get(), get()) }
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
    factory { UpdateBattleMapUseCase(get(), get()) }
    factory { UpdateBattleMapFogUseCase(get(), get()) }
    factory { DeleteBattleMapUseCase(get(), get(), get(), get()) }
    factory { ObserveBattleMapsForActiveCampaignUseCase(get(), get()) }
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
    factory { DeleteEncounterUseCase(get()) }
    factory { ObserveEncountersForActiveCampaignUseCase(get(), get()) }
    factory { StartEncounterUseCase(get(), get()) }
    factory { EndEncounterUseCase(get(), get(), get()) }
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
            calculateReachableCells = get(),
            calculateGridDistance = get(),
            placeEncounterToken = get(),
            updateBattleMapFog = get(),
            avatarFileStore = get(),
        )
    }
    factory { ObserveActiveContextUseCase(get()) }
    factory { ObserveActiveContextDetailsUseCase(get(), get(), get()) }

    single {
        ShellSettingsStore(
            preferences = Preferences.userRoot().node(ShellSettingsStore.PREF_NODE),
            legacyPreferences = Preferences.userRoot(),
        )
    }
    single {
        HomeViewModel(
            shellSettingsStore = get(),
            appScope = get(),
            observeWorlds = get(),
            observeActiveContextDetails = get(),
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
            findSessionMonthIds = get(),
            updateCalendar = get(),
            dateFormatter = get(),
        )
    }
    single {
        LoreViewModel(
            appScope = get(),
            observeActiveContextDetails = get(),
            observeLore = get(),
            observeLocations = get(),
            observePeople = get(),
            createLore = get(),
            updateLore = get(),
            deleteLore = get(),
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
            createPersonRelationship = get(),
            deletePersonRelationship = get(),
            createPersonCompanion = get(),
            deletePersonCompanion = get(),
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
            calculateReachableCells = get(),
            calculateGridDistance = get(),
            placeEncounterToken = get(),
            observeEncounters = get(),
            observePeople = get(),
            avatarFileStore = get(),
            imageScaler = get(),
            updateBattleMapFog = get(),
        )
    }
    single {
        DiceViewModel(
            diceRoller = get(),
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
            appScope = get(),
        )
    }
    single {
        AppViewModel(
            homeViewModel = get(),
            worldsViewModel = get(),
            campaignsViewModel = get(),
            locationsViewModel = get(),
            loreViewModel = get(),
            calendarViewModel = get(),
            charactersViewModel = get(),
            questsViewModel = get(),
            sessionsViewModel = get(),
            encountersViewModel = get(),
            mapsViewModel = get(),
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
