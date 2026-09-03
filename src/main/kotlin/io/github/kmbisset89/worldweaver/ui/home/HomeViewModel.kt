package io.github.kmbisset89.worldweaver.ui.home

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
import io.github.kmbisset89.worldweaver.core.AppCoroutineScope
import io.github.kmbisset89.worldweaver.domain.ActiveContextDetails
import io.github.kmbisset89.worldweaver.domain.DashboardCounts
import io.github.kmbisset89.worldweaver.domain.ObserveActiveContextDetailsUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveActiveContextUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveDashboardCountsUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveSessionsForActiveCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveWorldsUseCase
import io.github.kmbisset89.worldweaver.domain.SetActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.World
import io.github.kmbisset89.worldweaver.ui.settings.ShellSettingsStore

internal class HomeViewModel(
    private val shellSettingsStore: ShellSettingsStore,
    private val appScope: AppCoroutineScope,
    private val observeWorlds: ObserveWorldsUseCase,
    private val observeActiveContextDetails: ObserveActiveContextDetailsUseCase,
    private val observeActiveContext: ObserveActiveContextUseCase,
    private val observeSessions: ObserveSessionsForActiveCampaignUseCase,
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
            HomeInteraction.OneShotSelected -> emitEffect(HomeViewEffect.OpenOneShotWizard)
            is HomeInteraction.WorldSelected -> selectWorld(interaction.worldId)
            HomeInteraction.ContinueCampaignSelected -> emitEffect(HomeViewEffect.OpenRun)
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
                combine(
                    observeWorlds(),
                    observeActiveContextDetails(),
                    observeDashboardCounts(),
                    shellSettingsStore.settings,
                ) { worlds, details, counts, settings ->
                    HomeBundle(worlds, details, counts, settings.displayName)
                },
                observeActiveContext(),
                observeSessions(),
            ) { bundle, context, sessions ->
                if (bundle.worlds.isEmpty()) {
                    HomeViewState.Empty(displayName = bundle.displayName)
                } else {
                    val campaign = bundle.details.campaign
                    val world = bundle.details.world
                    val session = sessions.firstOrNull { it.id == context.activeSessionId }
                    HomeViewState.Content(
                        displayName = bundle.displayName,
                        recentWorlds = bundle.worlds.take(RECENT_WORLD_LIMIT),
                        continueCampaign = if (campaign != null && world != null) {
                            HomeViewState.ContinueCampaign(
                                campaignName = campaign.name,
                                worldName = world.name,
                                sessionName = session?.name,
                            )
                        } else {
                            null
                        },
                        worldCount = bundle.counts.worlds,
                        campaignCount = bundle.counts.campaigns,
                        peopleCount = bundle.counts.people,
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

    private data class HomeBundle(
        val worlds: List<World>,
        val details: ActiveContextDetails,
        val counts: DashboardCounts,
        val displayName: String,
    )
}
