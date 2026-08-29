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
import net.tactware.worldweaver.domain.ObserveWorldsUseCase
import net.tactware.worldweaver.domain.SetActiveWorldUseCase
import net.tactware.worldweaver.ui.session.LocalUser

internal class HomeViewModel(
    private val localUser: LocalUser,
    private val appScope: AppCoroutineScope,
    private val observeWorlds: ObserveWorldsUseCase,
    private val observeActiveContextDetails: ObserveActiveContextDetailsUseCase,
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
        }
    }

    private fun observe() {
        observeJob?.cancel()
        _state.value = HomeViewState.Loading
        observeJob = appScope.scope.launch {
            combine(
                observeWorlds(),
                observeActiveContextDetails(),
            ) { worlds, details ->
                if (worlds.isEmpty()) {
                    HomeViewState.Empty(displayName = localUser.displayName)
                } else {
                    val campaign = details.campaign
                    val world = details.world
                    HomeViewState.Content(
                        displayName = localUser.displayName,
                        recentWorlds = worlds.take(RECENT_WORLD_LIMIT),
                        continueCampaign = if (campaign != null && world != null) {
                            HomeViewState.ContinueCampaign(
                                campaignName = campaign.name,
                                worldName = world.name,
                            )
                        } else {
                            null
                        },
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
