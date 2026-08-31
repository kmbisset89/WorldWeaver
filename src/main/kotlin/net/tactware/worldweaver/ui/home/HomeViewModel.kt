package net.tactware.worldweaver.ui.home

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import net.tactware.worldweaver.core.AppCoroutineScope
import net.tactware.worldweaver.domain.ObserveActiveContextDetailsUseCase
import net.tactware.worldweaver.domain.ObserveDashboardCountsUseCase
import net.tactware.worldweaver.domain.ObserveWorldsUseCase
import net.tactware.worldweaver.domain.SetActiveWorldUseCase
import net.tactware.worldweaver.ui.settings.ShellSettingsStore

internal class HomeViewModel(
    private val shellSettingsStore: ShellSettingsStore,
    private val appScope: AppCoroutineScope,
    private val observeWorlds: ObserveWorldsUseCase,
    private val observeActiveContextDetails: ObserveActiveContextDetailsUseCase,
    private val observeDashboardCounts: ObserveDashboardCountsUseCase,
    private val setActiveWorld: SetActiveWorldUseCase,
) {
    private val _state = MutableStateFlow<HomeViewState>(HomeViewState.Loading)
    val state: StateFlow<HomeViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<HomeViewEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<HomeViewEffect> = _effects.asSharedFlow()

    private var observeJob: Job? = null

    init {
        observe()
    }

    fun onInteraction(interaction: HomeInteraction) {
        when (interaction) {
            HomeInteraction.ScreenStarted -> Unit
            HomeInteraction.RetrySelected -> observe()
            HomeInteraction.NewWorldSelected -> emitEffect(HomeViewEffect.OpenWorldCreator)
            is HomeInteraction.WorldSelected -> selectWorld(interaction.worldId)
            HomeInteraction.ContinueCampaignSelected -> emitEffect(HomeViewEffect.OpenCampaigns)
            HomeInteraction.WorldsCountSelected -> emitEffect(HomeViewEffect.OpenWorlds)
            HomeInteraction.CampaignsCountSelected -> emitEffect(HomeViewEffect.OpenCampaigns)
            HomeInteraction.PeopleCountSelected -> emitEffect(HomeViewEffect.OpenCharacters)
        }
    }

    private fun observe() {
        observeJob?.cancel()
        _state.value = HomeViewState.Loading
        observeJob = appScope.scope.launch {
            combine(
                observeWorlds(),
                observeActiveContextDetails(),
                observeDashboardCounts(),
                shellSettingsStore.settings,
            ) { worlds, details, counts, settings ->
                if (worlds.isEmpty()) {
                    HomeViewState.Empty(displayName = settings.displayName)
                } else {
                    val campaign = details.campaign
                    val world = details.world
                    HomeViewState.Content(
                        displayName = settings.displayName,
                        recentWorlds = worlds.take(RECENT_WORLD_LIMIT),
                        continueCampaign = if (campaign != null && world != null) {
                            HomeViewState.ContinueCampaign(
                                campaignName = campaign.name,
                                worldName = world.name,
                            )
                        } else {
                            null
                        },
                        worldCount = counts.worlds,
                        campaignCount = counts.campaigns,
                        peopleCount = counts.people,
                    )
                }
            }
                .catch { error ->
                    _state.value = HomeViewState.Error(
                        message = error.message ?: "Could not load home",
                        canRetry = true,
                    )
                }
                .collect { nextState ->
                    _state.value = nextState
                }
        }
    }

    private fun selectWorld(worldId: String) {
        appScope.scope.launch {
            setActiveWorld(worldId)
            emitEffect(HomeViewEffect.OpenWorlds)
        }
    }

    private fun emitEffect(effect: HomeViewEffect) {
        _effects.tryEmit(effect)
    }

    private companion object {
        const val RECENT_WORLD_LIMIT = 5
    }
}
