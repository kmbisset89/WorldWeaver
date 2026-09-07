package io.github.kmbisset89.worldweaver.ui.atmosphere

import io.github.kmbisset89.worldweaver.domain.AtmosphereLightingEffect
import io.github.kmbisset89.worldweaver.domain.AtmosphereLightingLoop
import io.github.kmbisset89.worldweaver.domain.AtmosphereMood
import io.github.kmbisset89.worldweaver.domain.AtmosphereMusicTrack
import io.github.kmbisset89.worldweaver.domain.AtmosphereScene
import io.github.kmbisset89.worldweaver.domain.GoveeDevice
import io.github.kmbisset89.worldweaver.domain.HueBridge
import io.github.kmbisset89.worldweaver.domain.HueLight
import io.github.kmbisset89.worldweaver.domain.HueScene
import io.github.kmbisset89.worldweaver.domain.HomeAssistantScene

internal sealed class AtmosphereViewState {
    data class Content(
        val draftBaseUrl: String,
        val draftToken: String,
        val savedBaseUrl: String,
        val savedToken: String,
        val draftHueHost: String,
        val draftHueKey: String,
        val savedHueHost: String,
        val savedHueKey: String,
        val hueBridges: List<HueBridge>,
        val hueCatalog: List<HueScene>,
        val hueLights: List<HueLight>,
        val goveeDevices: List<GoveeDevice>,
        val scenes: List<AtmosphereScene>,
        val moods: List<AtmosphereMood>,
        val musicTracks: List<AtmosphereMusicTrack>,
        val catalog: List<HomeAssistantScene>,
        val draftSceneName: String,
        val draftEntityId: String,
        val draftMusicTrackId: String?,
        val draftMoodName: String,
        val selectedCatalogEntityId: String?,
        val selectedHueSceneId: String?,
        val selectedHueLightIds: List<String>,
        val selectedGoveeDeviceIds: List<String>,
        val draftLookPowerOn: Boolean,
        val draftLookBrightness: String,
        val draftLookColorHex: String,
        val draftLookTransitionMs: Int,
        val playingEffect: AtmosphereLightingEffect?,
        val playingLoop: AtmosphereLightingLoop?,
        val connectionCheck: ConnectionCheck,
        val hueCheck: ConnectionCheck,
        val goveeMessage: String?,
        val lookError: String?,
        val moodError: String?,
        val sceneError: String?,
        val activationError: String?,
        val musicError: String?,
        val playingTrackId: String?,
        val musicVolume: Int,
        val musicLoopEnabled: Boolean,
        val lastActivatedSceneId: String?,
        val isTestingConnection: Boolean,
        val isLoadingCatalog: Boolean,
        val isPairingHue: Boolean,
        val isDiscoveringHue: Boolean,
        val isTestingHue: Boolean,
        val isLoadingHueCatalog: Boolean,
        val isLoadingHueLights: Boolean,
        val isScanningGovee: Boolean,
        val isActivating: Boolean,
        val activatingSceneId: String?,
        val isFloatingOpen: Boolean,
        val isAlwaysOnTop: Boolean,
    ) : AtmosphereViewState() {
        val isConnectionDirty: Boolean
            get() = draftBaseUrl != savedBaseUrl || draftToken != savedToken

        val isHueDirty: Boolean
            get() = draftHueHost != savedHueHost || draftHueKey != savedHueKey

        val isHomeAssistantConfigured: Boolean
            get() = savedBaseUrl.isNotBlank() && savedToken.isNotBlank()

        val isHueConfigured: Boolean
            get() = savedHueHost.isNotBlank() && savedHueKey.isNotBlank()

        val hasGoveeDevices: Boolean
            get() = goveeDevices.isNotEmpty()

        val hasLookLights: Boolean
            get() = hueLights.isNotEmpty() || goveeDevices.isNotEmpty()

        val hasSelectedLookLights: Boolean
            get() = selectedHueLightIds.isNotEmpty() || selectedGoveeDeviceIds.isNotEmpty()

        val isConfigured: Boolean
            get() = isHomeAssistantConfigured || isHueConfigured || hasGoveeDevices
    }

    sealed interface ConnectionCheck {
        data object Idle : ConnectionCheck
        data object Connected : ConnectionCheck
        data class Failed(val message: String) : ConnectionCheck
    }
}
