package io.github.kmbisset89.worldweaver.ui.atmosphere

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import io.github.kmbisset89.worldweaver.core.AppCoroutineScope
import io.github.kmbisset89.worldweaver.domain.ActivateAtmosphereSceneUseCase
import io.github.kmbisset89.worldweaver.domain.ApplyAtmosphereLookUseCase
import io.github.kmbisset89.worldweaver.domain.AtmosphereLightingEffect
import io.github.kmbisset89.worldweaver.domain.AtmosphereLightingLoop
import io.github.kmbisset89.worldweaver.domain.AtmosphereSettings
import io.github.kmbisset89.worldweaver.domain.AtmosphereSettingsStore
import io.github.kmbisset89.worldweaver.domain.CreateAtmosphereMoodUseCase
import io.github.kmbisset89.worldweaver.domain.CreateAtmosphereSceneUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteAtmosphereMoodUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteAtmosphereSceneUseCase
import io.github.kmbisset89.worldweaver.domain.DiscoverHueBridgesUseCase
import io.github.kmbisset89.worldweaver.domain.GoveeColorHexParser
import io.github.kmbisset89.worldweaver.domain.GoveeLightingPreset
import io.github.kmbisset89.worldweaver.domain.LightingLook
import io.github.kmbisset89.worldweaver.domain.LightingTransitionCalculator
import io.github.kmbisset89.worldweaver.domain.ListHomeAssistantScenesUseCase
import io.github.kmbisset89.worldweaver.domain.ListHueLightsUseCase
import io.github.kmbisset89.worldweaver.domain.ListHueScenesUseCase
import io.github.kmbisset89.worldweaver.domain.PairHueBridgeUseCase
import io.github.kmbisset89.worldweaver.domain.PlayAtmosphereLightingEffectUseCase
import io.github.kmbisset89.worldweaver.domain.PlayAtmosphereLightingLoopUseCase
import io.github.kmbisset89.worldweaver.domain.SaveHomeAssistantConnectionUseCase
import io.github.kmbisset89.worldweaver.domain.SaveHueConnectionUseCase
import io.github.kmbisset89.worldweaver.domain.ScanGoveeDevicesUseCase
import io.github.kmbisset89.worldweaver.domain.TestHomeAssistantConnectionUseCase
import io.github.kmbisset89.worldweaver.domain.TestHueConnectionUseCase

internal class AtmosphereViewModel(
    private val store: AtmosphereSettingsStore,
    private val saveConnection: SaveHomeAssistantConnectionUseCase,
    private val testConnection: TestHomeAssistantConnectionUseCase,
    private val listScenes: ListHomeAssistantScenesUseCase,
    private val saveHueConnection: SaveHueConnectionUseCase,
    private val pairHueBridge: PairHueBridgeUseCase,
    private val testHueConnection: TestHueConnectionUseCase,
    private val discoverHueBridges: DiscoverHueBridgesUseCase,
    private val listHueScenes: ListHueScenesUseCase,
    private val listHueLights: ListHueLightsUseCase,
    private val scanGoveeDevices: ScanGoveeDevicesUseCase,
    private val createScene: CreateAtmosphereSceneUseCase,
    private val deleteScene: DeleteAtmosphereSceneUseCase,
    private val createMood: CreateAtmosphereMoodUseCase,
    private val deleteMood: DeleteAtmosphereMoodUseCase,
    private val activateScene: ActivateAtmosphereSceneUseCase,
    private val applyLook: ApplyAtmosphereLookUseCase,
    private val playEffect: PlayAtmosphereLightingEffectUseCase,
    private val playLoop: PlayAtmosphereLightingLoopUseCase,
    private val appScope: AppCoroutineScope,
) {
    private val _state = MutableStateFlow<AtmosphereViewState>(contentFrom(store.settings.value))
    val state: StateFlow<AtmosphereViewState> = _state.asStateFlow()
    private val colorParser = GoveeColorHexParser()
    private var previewLookJob: Job? = null
    private var effectJob: Job? = null
    private var lastAppliedLook: LightingLook? = null

    init {
        appScope.scope.launch {
            store.settings.collect { settings ->
                syncFromStore(settings)
            }
        }
    }

    fun onInteraction(interaction: AtmosphereInteraction) {
        when (interaction) {
            AtmosphereInteraction.ScreenStarted -> Unit
            is AtmosphereInteraction.BaseUrlChanged -> updateContent { current ->
                current.copy(draftBaseUrl = interaction.value, connectionCheck = idleIfFailed(current.connectionCheck))
            }
            is AtmosphereInteraction.TokenChanged -> updateContent { current ->
                current.copy(draftToken = interaction.value, connectionCheck = idleIfFailed(current.connectionCheck))
            }
            AtmosphereInteraction.ConnectionSaveSelected -> saveHubConnection()
            AtmosphereInteraction.ConnectionTestSelected -> testHubConnection()
            AtmosphereInteraction.CatalogRefreshSelected -> refreshCatalog()
            is AtmosphereInteraction.HueHostChanged -> updateContent { current ->
                current.copy(draftHueHost = interaction.value, hueCheck = idleIfFailed(current.hueCheck))
            }
            is AtmosphereInteraction.HueKeyChanged -> updateContent { current ->
                current.copy(draftHueKey = interaction.value, hueCheck = idleIfFailed(current.hueCheck))
            }
            AtmosphereInteraction.HueSaveSelected -> saveHue()
            AtmosphereInteraction.HueDiscoverSelected -> discoverHue()
            is AtmosphereInteraction.HueBridgeSelected -> updateContent { current ->
                current.copy(draftHueHost = interaction.ipAddress)
            }
            AtmosphereInteraction.HuePairSelected -> pairHue()
            AtmosphereInteraction.HueTestSelected -> testHue()
            AtmosphereInteraction.HueCatalogRefreshSelected -> refreshHueCatalog()
            is AtmosphereInteraction.HueCatalogSceneSelected -> selectHueScene(interaction.sceneId)
            AtmosphereInteraction.HueLightsRefreshSelected -> refreshHueLights()
            is AtmosphereInteraction.HueLightToggled -> toggleHueLight(interaction.lightId)
            AtmosphereInteraction.GoveeScanSelected -> scanGovee()
            is AtmosphereInteraction.GoveeDeviceToggled -> toggleGoveeDevice(interaction.deviceId)
            is AtmosphereInteraction.LookPowerChanged -> updateLook(
                powerOn = interaction.powerOn,
            )
            is AtmosphereInteraction.LookBrightnessChanged -> updateLook(
                brightness = interaction.value,
            )
            is AtmosphereInteraction.LookColorHexChanged -> updateLook(
                colorHex = interaction.value,
            )
            is AtmosphereInteraction.LookPresetSelected -> selectLook(interaction)
            is AtmosphereInteraction.LookTransitionChanged -> updateTransition(interaction.durationMs)
            is AtmosphereInteraction.LightingEffectSelected -> playLightingEffect(interaction.effect)
            is AtmosphereInteraction.LightingLoopSelected -> playLightingLoop(interaction.loop)
            is AtmosphereInteraction.LookMoodNameChanged -> updateContent { current ->
                current.copy(draftMoodName = interaction.value, moodError = null)
            }
            AtmosphereInteraction.LookMoodSaveSelected -> saveMood()
            is AtmosphereInteraction.LookMoodDeleteSelected -> deleteSavedMood(interaction.moodId)
            is AtmosphereInteraction.DraftSceneNameChanged -> updateContent { current ->
                current.copy(draftSceneName = interaction.value, sceneError = null)
            }
            is AtmosphereInteraction.DraftEntityIdChanged -> updateContent { current ->
                current.copy(
                    draftEntityId = interaction.value,
                    selectedCatalogEntityId = null,
                    sceneError = null,
                )
            }
            is AtmosphereInteraction.CatalogSceneSelected -> selectCatalogScene(interaction.entityId)
            AtmosphereInteraction.SceneCreateSelected -> createMappedScene()
            is AtmosphereInteraction.SceneDeleteSelected -> deleteMappedScene(interaction.sceneId)
            is AtmosphereInteraction.SceneActivateSelected -> activateMappedScene(interaction.sceneId)
            AtmosphereInteraction.FloatingOpened -> updateContent { current ->
                current.copy(isFloatingOpen = true)
            }
            AtmosphereInteraction.FloatingClosed -> updateContent { current ->
                current.copy(isFloatingOpen = false)
            }
            AtmosphereInteraction.AlwaysOnTopToggled -> toggleAlwaysOnTop()
        }
    }

    private fun saveHubConnection() {
        val content = currentContent() ?: return
        when (val result = saveConnection(content.draftBaseUrl, content.draftToken)) {
            SaveHomeAssistantConnectionUseCase.Result.Saved -> {
                val saved = store.settings.value.connection
                updateContent { current ->
                    current.copy(
                        savedBaseUrl = saved.baseUrl,
                        savedToken = saved.token,
                        draftBaseUrl = saved.baseUrl,
                        draftToken = saved.token,
                    )
                }
            }
            SaveHomeAssistantConnectionUseCase.Result.BlankUrl -> failConnection("Enter a Home Assistant URL")
            SaveHomeAssistantConnectionUseCase.Result.BlankToken -> failConnection("Enter a long-lived access token")
            SaveHomeAssistantConnectionUseCase.Result.InvalidUrl -> failConnection(INVALID_URL_MESSAGE)
        }
    }

    private fun testHubConnection() {
        val content = currentContent() ?: return
        if (content.isTestingConnection) {
            return
        }
        updateContent { current ->
            current.copy(isTestingConnection = true, connectionCheck = AtmosphereViewState.ConnectionCheck.Idle)
        }
        val baseUrl = content.draftBaseUrl
        val token = content.draftToken
        appScope.scope.launch {
            when (val result = testConnection(baseUrl, token)) {
                TestHomeAssistantConnectionUseCase.Result.Connected -> {
                    updateContent { current ->
                        current.copy(
                            isTestingConnection = false,
                            connectionCheck = AtmosphereViewState.ConnectionCheck.Connected,
                        )
                    }
                    loadCatalog(baseUrl, token)
                }
                TestHomeAssistantConnectionUseCase.Result.BlankUrl -> failConnection("Enter a Home Assistant URL")
                TestHomeAssistantConnectionUseCase.Result.BlankToken -> failConnection("Enter a long-lived access token")
                TestHomeAssistantConnectionUseCase.Result.InvalidUrl -> failConnection(INVALID_URL_MESSAGE)
                TestHomeAssistantConnectionUseCase.Result.Unauthorized -> failConnection(UNAUTHORIZED_MESSAGE)
                is TestHomeAssistantConnectionUseCase.Result.Unreachable -> failConnection(result.message)
            }
        }
    }

    private fun refreshCatalog() {
        val content = currentContent() ?: return
        loadCatalog(content.draftBaseUrl, content.draftToken)
    }

    private fun loadCatalog(baseUrl: String, token: String) {
        val content = currentContent() ?: return
        if (content.isLoadingCatalog) {
            return
        }
        updateContent { current ->
            current.copy(isLoadingCatalog = true)
        }
        appScope.scope.launch {
            when (val result = listScenes(baseUrl, token)) {
                is ListHomeAssistantScenesUseCase.Result.Listed -> updateContent { current ->
                    current.copy(
                        catalog = result.scenes,
                        isLoadingCatalog = false,
                    )
                }
                ListHomeAssistantScenesUseCase.Result.BlankUrl -> {
                    updateContent { current -> current.copy(isLoadingCatalog = false) }
                    failConnection("Enter a Home Assistant URL")
                }
                ListHomeAssistantScenesUseCase.Result.BlankToken -> {
                    updateContent { current -> current.copy(isLoadingCatalog = false) }
                    failConnection("Enter a long-lived access token")
                }
                ListHomeAssistantScenesUseCase.Result.InvalidUrl -> {
                    updateContent { current -> current.copy(isLoadingCatalog = false) }
                    failConnection(INVALID_URL_MESSAGE)
                }
                ListHomeAssistantScenesUseCase.Result.Unauthorized -> {
                    updateContent { current -> current.copy(isLoadingCatalog = false) }
                    failConnection(UNAUTHORIZED_MESSAGE)
                }
                is ListHomeAssistantScenesUseCase.Result.Failed -> {
                    updateContent { current -> current.copy(isLoadingCatalog = false) }
                    failConnection(result.message)
                }
            }
        }
    }

    private fun saveHue() {
        val content = currentContent() ?: return
        when (val result = saveHueConnection(content.draftHueHost, content.draftHueKey)) {
            SaveHueConnectionUseCase.Result.Saved -> {
                val saved = store.settings.value.hue
                updateContent { current ->
                    current.copy(
                        savedHueHost = saved.bridgeHost,
                        savedHueKey = saved.applicationKey,
                        draftHueHost = saved.bridgeHost,
                        draftHueKey = saved.applicationKey,
                    )
                }
            }
            SaveHueConnectionUseCase.Result.BlankHost -> failHue("Enter a Hue bridge IP")
            SaveHueConnectionUseCase.Result.InvalidHost -> failHue("Enter a Hue bridge IP")
            SaveHueConnectionUseCase.Result.BlankKey -> failHue("Pair the Hue bridge or paste an application key")
        }
    }

    private fun discoverHue() {
        val content = currentContent() ?: return
        if (content.isDiscoveringHue) {
            return
        }
        updateContent { current -> current.copy(isDiscoveringHue = true) }
        appScope.scope.launch {
            when (val result = discoverHueBridges()) {
                is DiscoverHueBridgesUseCase.Result.Found -> updateContent { current ->
                    current.copy(
                        isDiscoveringHue = false,
                        hueBridges = result.bridges,
                        draftHueHost = current.draftHueHost.ifBlank { result.bridges.first().ipAddress },
                        hueCheck = AtmosphereViewState.ConnectionCheck.Idle,
                    )
                }
                is DiscoverHueBridgesUseCase.Result.Failed -> {
                    updateContent { current -> current.copy(isDiscoveringHue = false) }
                    failHue(result.message)
                }
            }
        }
    }

    private fun pairHue() {
        val content = currentContent() ?: return
        if (content.isPairingHue) {
            return
        }
        updateContent { current ->
            current.copy(isPairingHue = true, hueCheck = AtmosphereViewState.ConnectionCheck.Idle)
        }
        val host = content.draftHueHost
        appScope.scope.launch {
            when (val result = pairHueBridge(host, HUE_DEVICE_NAME)) {
                PairHueBridgeUseCase.Result.Paired -> {
                    val saved = store.settings.value.hue
                    updateContent { current ->
                        current.copy(
                            isPairingHue = false,
                            savedHueHost = saved.bridgeHost,
                            savedHueKey = saved.applicationKey,
                            draftHueHost = saved.bridgeHost,
                            draftHueKey = saved.applicationKey,
                            hueCheck = AtmosphereViewState.ConnectionCheck.Connected,
                        )
                    }
                    refreshHueCatalog()
                    refreshHueLights()
                }
                PairHueBridgeUseCase.Result.BlankHost,
                PairHueBridgeUseCase.Result.InvalidHost,
                -> {
                    updateContent { current -> current.copy(isPairingHue = false) }
                    failHue("Enter a Hue bridge IP")
                }
                PairHueBridgeUseCase.Result.LinkButtonNotPressed -> {
                    updateContent { current -> current.copy(isPairingHue = false) }
                    failHue("Press the Hue bridge link button, then pair again")
                }
                is PairHueBridgeUseCase.Result.Failed -> {
                    updateContent { current -> current.copy(isPairingHue = false) }
                    failHue(result.message)
                }
            }
        }
    }

    private fun testHue() {
        val content = currentContent() ?: return
        if (content.isTestingHue) {
            return
        }
        updateContent { current ->
            current.copy(isTestingHue = true, hueCheck = AtmosphereViewState.ConnectionCheck.Idle)
        }
        val host = content.draftHueHost
        val key = content.draftHueKey
        appScope.scope.launch {
            when (val result = testHueConnection(host, key)) {
                TestHueConnectionUseCase.Result.Connected -> {
                    updateContent { current ->
                        current.copy(
                            isTestingHue = false,
                            hueCheck = AtmosphereViewState.ConnectionCheck.Connected,
                        )
                    }
                    refreshHueCatalog()
                    refreshHueLights()
                }
                TestHueConnectionUseCase.Result.BlankHost,
                TestHueConnectionUseCase.Result.InvalidHost,
                -> failHue("Enter a Hue bridge IP")
                TestHueConnectionUseCase.Result.BlankKey -> failHue("Pair the Hue bridge or paste an application key")
                TestHueConnectionUseCase.Result.Unauthorized -> failHue("Hue rejected the application key")
                is TestHueConnectionUseCase.Result.Unreachable -> failHue(result.message)
            }
        }
    }

    private fun refreshHueCatalog() {
        val content = currentContent() ?: return
        if (content.isLoadingHueCatalog) {
            return
        }
        updateContent { current -> current.copy(isLoadingHueCatalog = true) }
        appScope.scope.launch {
            when (val result = listHueScenes()) {
                is ListHueScenesUseCase.Result.Listed -> updateContent { current ->
                    current.copy(hueCatalog = result.scenes, isLoadingHueCatalog = false)
                }
                ListHueScenesUseCase.Result.NotConfigured -> {
                    updateContent { current -> current.copy(isLoadingHueCatalog = false) }
                    failHue("Pair the Hue bridge first")
                }
                ListHueScenesUseCase.Result.Unauthorized -> {
                    updateContent { current -> current.copy(isLoadingHueCatalog = false) }
                    failHue("Hue rejected the application key")
                }
                is ListHueScenesUseCase.Result.Failed -> {
                    updateContent { current -> current.copy(isLoadingHueCatalog = false) }
                    failHue(result.message)
                }
            }
        }
    }

    private fun refreshHueLights() {
        val content = currentContent() ?: return
        if (content.isLoadingHueLights) {
            return
        }
        updateContent { current -> current.copy(isLoadingHueLights = true) }
        appScope.scope.launch {
            when (val result = listHueLights()) {
                is ListHueLightsUseCase.Result.Listed -> updateContent { current ->
                    val validIds = result.lights.map { it.id }.toSet()
                    val nextSelected = current.selectedHueLightIds.filter { it in validIds }
                    if (nextSelected != current.selectedHueLightIds) {
                        store.setSelectedHueLightIds(nextSelected)
                    }
                    current.copy(
                        hueLights = result.lights,
                        selectedHueLightIds = nextSelected,
                        isLoadingHueLights = false,
                    )
                }
                ListHueLightsUseCase.Result.NotConfigured -> {
                    updateContent { current -> current.copy(isLoadingHueLights = false) }
                    failHue("Pair the Hue bridge first")
                }
                ListHueLightsUseCase.Result.Unauthorized -> {
                    updateContent { current -> current.copy(isLoadingHueLights = false) }
                    failHue("Hue rejected the application key")
                }
                is ListHueLightsUseCase.Result.Failed -> {
                    updateContent { current -> current.copy(isLoadingHueLights = false) }
                    failHue(result.message)
                }
            }
        }
    }

    private fun toggleHueLight(lightId: String) {
        val content = currentContent() ?: return
        val next = if (lightId in content.selectedHueLightIds) {
            content.selectedHueLightIds.filterNot { it == lightId }
        } else {
            content.selectedHueLightIds + lightId
        }
        store.setSelectedHueLightIds(next)
        updateContent { current ->
            current.copy(selectedHueLightIds = next, sceneError = null)
        }
    }

    private fun updateLook(
        powerOn: Boolean? = null,
        brightness: String? = null,
        colorHex: String? = null,
    ) {
        updateContent { current ->
            current.copy(
                draftLookPowerOn = powerOn ?: current.draftLookPowerOn,
                draftLookBrightness = brightness ?: current.draftLookBrightness,
                draftLookColorHex = colorHex ?: current.draftLookColorHex,
                selectedHueSceneId = null,
                lookError = null,
            )
        }
        schedulePreviewLook()
    }

    private fun selectLook(interaction: AtmosphereInteraction.LookPresetSelected) {
        previewLookJob?.cancel()
        effectJob?.cancel()
        updateContent { current ->
            current.copy(
                draftLookPowerOn = interaction.powerOn,
                draftLookBrightness = interaction.brightness.toString(),
                draftLookColorHex = interaction.colorHex,
                selectedHueSceneId = null,
                lookError = null,
                playingEffect = null,
                playingLoop = null,
            )
        }
        previewLook()
    }

    private fun updateTransition(durationMs: Int) {
        val duration = durationMs.coerceIn(0, LightingTransitionCalculator.MAX_DURATION_MS)
        store.setLookTransitionMs(duration)
        updateContent { current ->
            current.copy(draftLookTransitionMs = duration)
        }
    }

    private fun playLightingEffect(effect: AtmosphereLightingEffect) {
        val content = currentContent() ?: return
        if (!content.hasSelectedLookLights) {
            updateContent { current ->
                current.copy(lookError = "Select Hue or Govee lights above to apply this look.")
            }
            return
        }
        val look = lastAppliedLook ?: lookFromDraft(content) ?: return
        previewLookJob?.cancel()
        effectJob?.cancel()
        updateContent { current ->
            current.copy(playingEffect = effect, playingLoop = null, lookError = null)
        }
        val hueLightIds = content.selectedHueLightIds
        val goveeDeviceIds = content.selectedGoveeDeviceIds
        effectJob = appScope.scope.launch {
            try {
                when (
                    val result = playEffect(
                        effect = effect,
                        currentLook = look,
                        hueLightIds = hueLightIds,
                        goveeDeviceIds = goveeDeviceIds,
                    )
                ) {
                    PlayAtmosphereLightingEffectUseCase.Result.Played,
                    PlayAtmosphereLightingEffectUseCase.Result.NoTargets,
                    -> updateContent { current ->
                        current.copy(lookError = null)
                    }
                    is PlayAtmosphereLightingEffectUseCase.Result.Partial -> updateContent { current ->
                        current.copy(lookError = result.message)
                    }
                    is PlayAtmosphereLightingEffectUseCase.Result.Failed -> updateContent { current ->
                        current.copy(lookError = result.message)
                    }
                }
            } finally {
                updateContent { current ->
                    if (current.playingEffect == effect) {
                        current.copy(playingEffect = null)
                    } else {
                        current
                    }
                }
            }
        }
    }

    private fun playLightingLoop(loop: AtmosphereLightingLoop) {
        val content = currentContent() ?: return
        if (content.playingLoop == loop) {
            stopLightingLoop()
            return
        }
        if (!content.hasSelectedLookLights) {
            updateContent { current ->
                current.copy(lookError = "Select Hue or Govee lights above to apply this look.")
            }
            return
        }
        previewLookJob?.cancel()
        effectJob?.cancel()
        updateContent { current ->
            current.copy(playingLoop = loop, playingEffect = null, lookError = null)
        }
        val hueLightIds = content.selectedHueLightIds
        val goveeDeviceIds = content.selectedGoveeDeviceIds
        val from = lastAppliedLook ?: lookFromDraft(content)
        effectJob = appScope.scope.launch {
            try {
                when (
                    val result = playLoop(
                        loop = loop,
                        hueLightIds = hueLightIds,
                        goveeDeviceIds = goveeDeviceIds,
                        from = from,
                    )
                ) {
                    PlayAtmosphereLightingLoopUseCase.Result.NoTargets -> updateContent { current ->
                        current.copy(lookError = "Select Hue or Govee lights above to apply this look.")
                    }
                    is PlayAtmosphereLightingLoopUseCase.Result.Failed -> updateContent { current ->
                        current.copy(lookError = result.message)
                    }
                }
            } finally {
                updateContent { current ->
                    if (current.playingLoop == loop) {
                        current.copy(playingLoop = null)
                    } else {
                        current
                    }
                }
            }
        }
    }

    private fun stopLightingLoop() {
        effectJob?.cancel()
        updateContent { current ->
            current.copy(playingLoop = null)
        }
        restoreTableLook()
    }

    private fun restoreTableLook() {
        val content = currentContent() ?: return
        if (!content.hasSelectedLookLights) {
            return
        }
        val look = lastAppliedLook ?: lookFromDraft(content) ?: return
        appScope.scope.launch {
            applyLook(
                hueLightIds = content.selectedHueLightIds,
                goveeDeviceIds = content.selectedGoveeDeviceIds,
                powerOn = look.powerOn,
                brightness = look.brightness,
                colorHex = look.toColorHex(),
                transitionDurationMs = content.draftLookTransitionMs,
                from = null,
            )
        }
    }

    private fun saveMood() {
        val content = currentContent() ?: return
        val brightness = content.draftLookBrightness.toIntOrNull() ?: 80
        when (
            val result = createMood(
                name = content.draftMoodName,
                powerOn = content.draftLookPowerOn,
                brightness = brightness,
                colorHex = content.draftLookColorHex,
            )
        ) {
            is CreateAtmosphereMoodUseCase.Result.Created -> updateContent { current ->
                current.copy(
                    moods = store.settings.value.moods,
                    draftMoodName = "",
                    moodError = null,
                )
            }
            CreateAtmosphereMoodUseCase.Result.InvalidName -> updateContent { current ->
                current.copy(moodError = "Enter a mood name")
            }
            CreateAtmosphereMoodUseCase.Result.InvalidColor -> updateContent { current ->
                current.copy(moodError = "Enter a color such as #E39B5A")
            }
            CreateAtmosphereMoodUseCase.Result.DuplicateName -> updateContent { current ->
                current.copy(moodError = "A mood with that name already exists")
            }
        }
    }

    private fun deleteSavedMood(moodId: String) {
        deleteMood(moodId)
        updateContent { current ->
            current.copy(moods = store.settings.value.moods)
        }
    }

    private fun schedulePreviewLook() {
        previewLookJob?.cancel()
        previewLookJob = appScope.scope.launch {
            delay(LOOK_PREVIEW_DEBOUNCE_MS)
            val content = currentContent() ?: return@launch
            previewLook(content.draftLookTransitionMs.coerceAtMost(LightingTransitionCalculator.PREVIEW_CAP_MS))
        }
    }

    private fun previewLook(transitionMs: Int? = null) {
        val content = currentContent() ?: return
        if (!content.hasSelectedLookLights) {
            return
        }
        val to = lookFromDraft(content) ?: return
        val duration = (transitionMs ?: content.draftLookTransitionMs)
            .coerceIn(0, LightingTransitionCalculator.MAX_DURATION_MS)
        effectJob?.cancel()
        appScope.scope.launch {
            when (
                val result = applyLook(
                    hueLightIds = content.selectedHueLightIds,
                    goveeDeviceIds = content.selectedGoveeDeviceIds,
                    powerOn = to.powerOn,
                    brightness = to.brightness,
                    colorHex = to.toColorHex(),
                    transitionDurationMs = duration,
                    from = lastAppliedLook,
                )
            ) {
                ApplyAtmosphereLookUseCase.Result.Applied,
                ApplyAtmosphereLookUseCase.Result.NoTargets,
                -> updateContent { current ->
                    current.copy(lookError = null)
                }
                is ApplyAtmosphereLookUseCase.Result.Partial -> updateContent { current ->
                    current.copy(lookError = result.message)
                }
                is ApplyAtmosphereLookUseCase.Result.Failed -> updateContent { current ->
                    current.copy(lookError = result.message)
                }
            }
            lastAppliedLook = to
        }
    }

    private fun lookFromDraft(content: AtmosphereViewState.Content): LightingLook? {
        val rgb = if (content.draftLookPowerOn) {
            colorParser.parse(content.draftLookColorHex) ?: return null
        } else {
            GoveeColorHexParser.Rgb(0, 0, 0)
        }
        val brightness = content.draftLookBrightness.toIntOrNull()?.coerceIn(1, 100) ?: 80
        return LightingLook(
            powerOn = content.draftLookPowerOn,
            brightness = brightness,
            red = rgb.red,
            green = rgb.green,
            blue = rgb.blue,
        )
    }

    private fun selectHueScene(sceneId: String) {
        val content = currentContent() ?: return
        val scene = content.hueCatalog.firstOrNull { it.id == sceneId } ?: return
        if (content.selectedHueSceneId == sceneId) {
            updateContent { current ->
                current.copy(selectedHueSceneId = null, sceneError = null)
            }
            return
        }
        updateContent { current ->
            current.copy(
                selectedHueSceneId = scene.id,
                draftSceneName = current.draftSceneName.ifBlank { scene.name },
                sceneError = null,
            )
        }
    }

    private fun scanGovee() {
        val content = currentContent() ?: return
        if (content.isScanningGovee) {
            return
        }
        updateContent { current -> current.copy(isScanningGovee = true, goveeMessage = null) }
        appScope.scope.launch {
            when (val result = scanGoveeDevices()) {
                is ScanGoveeDevicesUseCase.Result.Found -> updateContent { current ->
                    val validIds = result.devices.map { it.deviceId }.toSet()
                    val nextSelected = current.selectedGoveeDeviceIds.filter { it in validIds }
                    if (nextSelected != current.selectedGoveeDeviceIds) {
                        store.setSelectedGoveeDeviceIds(nextSelected)
                    }
                    current.copy(
                        isScanningGovee = false,
                        goveeDevices = result.devices,
                        selectedGoveeDeviceIds = nextSelected,
                        goveeMessage = "Found ${result.devices.size} Govee light(s).",
                    )
                }
                is ScanGoveeDevicesUseCase.Result.Failed -> updateContent { current ->
                    current.copy(isScanningGovee = false, goveeMessage = result.message)
                }
            }
        }
    }

    private fun toggleGoveeDevice(deviceId: String) {
        val content = currentContent() ?: return
        val next = if (deviceId in content.selectedGoveeDeviceIds) {
            content.selectedGoveeDeviceIds.filterNot { it == deviceId }
        } else {
            content.selectedGoveeDeviceIds + deviceId
        }
        store.setSelectedGoveeDeviceIds(next)
        updateContent { current ->
            current.copy(selectedGoveeDeviceIds = next, sceneError = null)
        }
    }

    private fun selectCatalogScene(entityId: String) {
        val content = currentContent() ?: return
        val catalogScene = content.catalog.firstOrNull { it.entityId == entityId } ?: return
        updateContent { current ->
            current.copy(
                selectedCatalogEntityId = entityId,
                draftEntityId = catalogScene.entityId,
                draftSceneName = current.draftSceneName.ifBlank { catalogScene.name },
                sceneError = null,
            )
        }
    }

    private fun createMappedScene() {
        val content = currentContent() ?: return
        val hueScene = content.hueCatalog.firstOrNull { it.id == content.selectedHueSceneId }
        val brightness = content.draftLookBrightness.toIntOrNull() ?: 80
        when (
            val result = createScene(
                name = content.draftSceneName,
                entityId = content.draftEntityId,
                hueSceneId = hueScene?.id.orEmpty(),
                hueGroupId = hueScene?.groupId.orEmpty(),
                hueLightIds = content.selectedHueLightIds,
                goveeDeviceIds = content.selectedGoveeDeviceIds,
                goveePowerOn = content.draftLookPowerOn,
                goveeBrightness = brightness,
                goveeColorHex = content.draftLookColorHex,
            )
        ) {
            is CreateAtmosphereSceneUseCase.Result.Created -> updateContent { current ->
                current.copy(
                    scenes = store.settings.value.scenes,
                    draftSceneName = "",
                    draftEntityId = "",
                    selectedCatalogEntityId = null,
                    selectedHueSceneId = null,
                    sceneError = null,
                )
            }
            CreateAtmosphereSceneUseCase.Result.InvalidName -> updateContent { current ->
                current.copy(sceneError = "Enter a scene name")
            }
            CreateAtmosphereSceneUseCase.Result.InvalidEntityId -> updateContent { current ->
                current.copy(sceneError = "Enter a Home Assistant scene id such as scene.tavern")
            }
            CreateAtmosphereSceneUseCase.Result.InvalidGoveeColor -> updateContent { current ->
                current.copy(sceneError = "Enter a color such as #E39B5A")
            }
            CreateAtmosphereSceneUseCase.Result.NoTargets -> updateContent { current ->
                current.copy(sceneError = "Pick a Home Assistant scene, Hue lights or a Hue scene, or Govee lights")
            }
            CreateAtmosphereSceneUseCase.Result.DuplicateName -> updateContent { current ->
                current.copy(sceneError = "A scene with that name already exists")
            }
            CreateAtmosphereSceneUseCase.Result.DuplicateEntityId -> updateContent { current ->
                current.copy(sceneError = "That Home Assistant scene is already mapped")
            }
        }
    }

    private fun deleteMappedScene(sceneId: String) {
        deleteScene(sceneId)
        updateContent { current ->
            current.copy(
                scenes = store.settings.value.scenes,
                lastActivatedSceneId = current.lastActivatedSceneId.takeUnless { it == sceneId },
            )
        }
    }

    private fun activateMappedScene(sceneId: String) {
        val content = currentContent() ?: return
        if (content.isActivating) {
            return
        }
        previewLookJob?.cancel()
        effectJob?.cancel()
        updateContent { current ->
            current.copy(
                isActivating = true,
                activatingSceneId = sceneId,
                activationError = null,
                playingEffect = null,
                playingLoop = null,
            )
        }
        appScope.scope.launch {
            when (
                val result = activateScene(
                    sceneId = sceneId,
                    transitionDurationMs = content.draftLookTransitionMs,
                    fromLook = lastAppliedLook,
                )
            ) {
                ActivateAtmosphereSceneUseCase.Result.Activated -> {
                    rememberSceneLook(sceneId)
                    updateContent { current ->
                        current.copy(
                            isActivating = false,
                            activatingSceneId = null,
                            lastActivatedSceneId = sceneId,
                            activationError = null,
                        )
                    }
                }
                is ActivateAtmosphereSceneUseCase.Result.Partial -> {
                    rememberSceneLook(sceneId)
                    updateContent { current ->
                        current.copy(
                            isActivating = false,
                            activatingSceneId = null,
                            lastActivatedSceneId = sceneId,
                            activationError = result.message,
                        )
                    }
                }
                ActivateAtmosphereSceneUseCase.Result.NotFound -> finishActivation("That scene is no longer mapped")
                ActivateAtmosphereSceneUseCase.Result.NoTargets -> finishActivation(
                    "That scene has no lights or Home Assistant target",
                )
                is ActivateAtmosphereSceneUseCase.Result.Failed -> finishActivation(result.message)
            }
        }
    }

    private fun rememberSceneLook(sceneId: String) {
        val scene = store.settings.value.scenes.firstOrNull { it.id == sceneId } ?: return
        val appliesLook = scene.hasGovee || (scene.hueLightIds.isNotEmpty() && scene.hueSceneId.isBlank())
        if (!appliesLook) {
            return
        }
        val rgb = if (scene.goveePowerOn) {
            colorParser.parse(scene.goveeColorHex) ?: return
        } else {
            GoveeColorHexParser.Rgb(0, 0, 0)
        }
        lastAppliedLook = LightingLook(
            powerOn = scene.goveePowerOn,
            brightness = scene.goveeBrightness,
            red = rgb.red,
            green = rgb.green,
            blue = rgb.blue,
        )
    }

    private fun finishActivation(message: String) {
        updateContent { current ->
            current.copy(
                isActivating = false,
                activatingSceneId = null,
                activationError = message,
            )
        }
    }

    private fun toggleAlwaysOnTop() {
        val current = currentContent() ?: return
        val next = !current.isAlwaysOnTop
        store.setAlwaysOnTop(next)
        updateContent { state -> state.copy(isAlwaysOnTop = next) }
    }

    private fun failConnection(message: String) {
        updateContent { current ->
            current.copy(
                isTestingConnection = false,
                isLoadingCatalog = false,
                connectionCheck = AtmosphereViewState.ConnectionCheck.Failed(message),
            )
        }
    }

    private fun failHue(message: String) {
        updateContent { current ->
            current.copy(
                isTestingHue = false,
                isPairingHue = false,
                isDiscoveringHue = false,
                isLoadingHueCatalog = false,
                isLoadingHueLights = false,
                hueCheck = AtmosphereViewState.ConnectionCheck.Failed(message),
            )
        }
    }

    private fun idleIfFailed(
        check: AtmosphereViewState.ConnectionCheck,
    ): AtmosphereViewState.ConnectionCheck {
        return if (check is AtmosphereViewState.ConnectionCheck.Failed) {
            AtmosphereViewState.ConnectionCheck.Idle
        } else {
            check
        }
    }

    private fun syncFromStore(settings: AtmosphereSettings) {
        updateContent { current ->
            val overwriteHa = !current.isConnectionDirty
            val overwriteHue = !current.isHueDirty
            current.copy(
                savedBaseUrl = settings.connection.baseUrl,
                savedToken = settings.connection.token,
                draftBaseUrl = if (overwriteHa) settings.connection.baseUrl else current.draftBaseUrl,
                draftToken = if (overwriteHa) settings.connection.token else current.draftToken,
                savedHueHost = settings.hue.bridgeHost,
                savedHueKey = settings.hue.applicationKey,
                draftHueHost = if (overwriteHue) settings.hue.bridgeHost else current.draftHueHost,
                draftHueKey = if (overwriteHue) settings.hue.applicationKey else current.draftHueKey,
                goveeDevices = settings.goveeDevices,
                hueLights = settings.hueLights,
                scenes = settings.scenes,
                moods = settings.moods,
                selectedHueLightIds = settings.selectedHueLightIds.filter { id ->
                    settings.hueLights.any { it.id == id }
                },
                selectedGoveeDeviceIds = settings.selectedGoveeDeviceIds.filter { id ->
                    settings.goveeDevices.any { it.deviceId == id }
                },
                isAlwaysOnTop = settings.isAlwaysOnTop,
                draftLookTransitionMs = settings.lookTransitionMs,
            )
        }
    }

    private fun currentContent(): AtmosphereViewState.Content? {
        return _state.value as? AtmosphereViewState.Content
    }

    private fun updateContent(
        transform: (AtmosphereViewState.Content) -> AtmosphereViewState.Content,
    ) {
        _state.update { current ->
            when (current) {
                is AtmosphereViewState.Content -> transform(current)
            }
        }
    }

    private companion object {
        const val INVALID_URL_MESSAGE = "Enter a URL such as http://homeassistant.local:8123"
        const val UNAUTHORIZED_MESSAGE = "Home Assistant rejected the token"
        const val HUE_DEVICE_NAME = "WorldWeaver#desktop"
        const val LOOK_PREVIEW_DEBOUNCE_MS = 200L

        fun contentFrom(settings: AtmosphereSettings): AtmosphereViewState.Content {
            val warm = GoveeLightingPreset.WARM
            return AtmosphereViewState.Content(
                draftBaseUrl = settings.connection.baseUrl,
                draftToken = settings.connection.token,
                savedBaseUrl = settings.connection.baseUrl,
                savedToken = settings.connection.token,
                draftHueHost = settings.hue.bridgeHost,
                draftHueKey = settings.hue.applicationKey,
                savedHueHost = settings.hue.bridgeHost,
                savedHueKey = settings.hue.applicationKey,
                hueBridges = emptyList(),
                hueCatalog = emptyList(),
                hueLights = settings.hueLights,
                goveeDevices = settings.goveeDevices,
                scenes = settings.scenes,
                moods = settings.moods,
                catalog = emptyList(),
                draftSceneName = "",
                draftEntityId = "",
                draftMoodName = "",
                selectedCatalogEntityId = null,
                selectedHueSceneId = null,
                selectedHueLightIds = settings.selectedHueLightIds.filter { id ->
                    settings.hueLights.any { it.id == id }
                },
                selectedGoveeDeviceIds = settings.selectedGoveeDeviceIds.filter { id ->
                    settings.goveeDevices.any { it.deviceId == id }
                },
                draftLookPowerOn = warm.powerOn,
                draftLookBrightness = warm.brightness.toString(),
                draftLookColorHex = warm.colorHex,
                draftLookTransitionMs = settings.lookTransitionMs,
                playingEffect = null,
                playingLoop = null,
                connectionCheck = AtmosphereViewState.ConnectionCheck.Idle,
                hueCheck = AtmosphereViewState.ConnectionCheck.Idle,
                goveeMessage = null,
                lookError = null,
                moodError = null,
                sceneError = null,
                activationError = null,
                lastActivatedSceneId = null,
                isTestingConnection = false,
                isLoadingCatalog = false,
                isPairingHue = false,
                isDiscoveringHue = false,
                isTestingHue = false,
                isLoadingHueCatalog = false,
                isLoadingHueLights = false,
                isScanningGovee = false,
                isActivating = false,
                activatingSceneId = null,
                isFloatingOpen = false,
                isAlwaysOnTop = settings.isAlwaysOnTop,
            )
        }
    }
}
