package net.tactware.worldweaver.di

import net.tactware.worldweaver.core.AppCoroutineScope
import net.tactware.worldweaver.data.CampaignEntityConverter
import net.tactware.worldweaver.data.CampaignRepositoryImpl
import net.tactware.worldweaver.data.DatabaseProvider
import net.tactware.worldweaver.data.PreferenceActiveContextRepository
import net.tactware.worldweaver.data.WorldEntityConverter
import net.tactware.worldweaver.data.WorldRepositoryImpl
import net.tactware.worldweaver.data.WorldWeaverDatabase
import net.tactware.worldweaver.domain.ActiveContextRepository
import net.tactware.worldweaver.domain.CampaignRepository
import net.tactware.worldweaver.domain.ClearActiveCampaignUseCase
import net.tactware.worldweaver.domain.CreateCampaignUseCase
import net.tactware.worldweaver.domain.CreateWorldUseCase
import net.tactware.worldweaver.domain.DeleteCampaignUseCase
import net.tactware.worldweaver.domain.DeleteWorldUseCase
import net.tactware.worldweaver.domain.EntityIdFactory
import net.tactware.worldweaver.domain.InstantProvider
import net.tactware.worldweaver.domain.ObserveActiveContextDetailsUseCase
import net.tactware.worldweaver.domain.ObserveActiveContextUseCase
import net.tactware.worldweaver.domain.ObserveCampaignsForActiveWorldUseCase
import net.tactware.worldweaver.domain.ObserveWorldsUseCase
import net.tactware.worldweaver.domain.SetActiveCampaignUseCase
import net.tactware.worldweaver.domain.SetActiveWorldUseCase
import net.tactware.worldweaver.domain.SetCampaignStatusUseCase
import net.tactware.worldweaver.domain.UpdateCampaignUseCase
import net.tactware.worldweaver.domain.UpdateWorldUseCase
import net.tactware.worldweaver.domain.WorldRepository
import net.tactware.worldweaver.ui.AppViewModel
import net.tactware.worldweaver.ui.campaigns.CampaignsViewModel
import net.tactware.worldweaver.ui.home.HomeViewModel
import net.tactware.worldweaver.ui.session.LocalUser
import net.tactware.worldweaver.ui.settings.SettingsViewModel
import net.tactware.worldweaver.ui.theme.ThemeMode
import net.tactware.worldweaver.ui.worlds.WorldsViewModel
import org.koin.dsl.module

internal fun appModule() = module {
    single { AppCoroutineScope() }
    single { InstantProvider() }
    single { EntityIdFactory() }
    single { WorldEntityConverter() }
    single { CampaignEntityConverter() }
    single { DatabaseProvider() }
    single { get<DatabaseProvider>().database }
    single { get<WorldWeaverDatabase>().worldDao() }
    single { get<WorldWeaverDatabase>().campaignDao() }
    single<WorldRepository> { WorldRepositoryImpl(get(), get()) }
    single<CampaignRepository> { CampaignRepositoryImpl(get(), get()) }
    single<ActiveContextRepository> { PreferenceActiveContextRepository() }

    factory { SetActiveWorldUseCase(get(), get(), get(), get()) }
    factory { SetActiveCampaignUseCase(get(), get()) }
    factory { ClearActiveCampaignUseCase(get()) }
    factory { CreateWorldUseCase(get(), get(), get(), get()) }
    factory { UpdateWorldUseCase(get(), get()) }
    factory { DeleteWorldUseCase(get(), get(), get()) }
    factory { ObserveWorldsUseCase(get()) }
    factory { CreateCampaignUseCase(get(), get(), get(), get(), get()) }
    factory { UpdateCampaignUseCase(get(), get()) }
    factory { SetCampaignStatusUseCase(get(), get()) }
    factory { DeleteCampaignUseCase(get(), get()) }
    factory { ObserveCampaignsForActiveWorldUseCase(get(), get()) }
    factory { ObserveActiveContextUseCase(get()) }
    factory { ObserveActiveContextDetailsUseCase(get(), get(), get()) }

    single { LocalUser() }
    single {
        HomeViewModel(
            localUser = get(),
            appScope = get(),
            observeWorlds = get(),
            observeActiveContextDetails = get(),
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
        )
    }
    single {
        CampaignsViewModel(
            appScope = get(),
            observeActiveContextDetails = get(),
            observeCampaigns = get(),
            createCampaign = get(),
            updateCampaign = get(),
            setCampaignStatus = get(),
            deleteCampaign = get(),
            setActiveCampaign = get(),
        )
    }
    single {
        SettingsViewModel(
            localUser = get(),
            themeMode = ThemeMode.load(),
        )
    }
    single {
        AppViewModel(
            homeViewModel = get(),
            worldsViewModel = get(),
            campaignsViewModel = get(),
            settingsViewModel = get(),
            localUser = get(),
            appScope = get(),
            observeActiveContextDetails = get(),
        )
    }
}
