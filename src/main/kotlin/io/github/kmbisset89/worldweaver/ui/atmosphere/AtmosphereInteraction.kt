package io.github.kmbisset89.worldweaver.ui.atmosphere

import io.github.kmbisset89.worldweaver.domain.AtmosphereLightingEffect
import io.github.kmbisset89.worldweaver.domain.AtmosphereLightingLoop

internal sealed interface AtmosphereInteraction {
    data object ScreenStarted : AtmosphereInteraction
    data class BaseUrlChanged(val value: String) : AtmosphereInteraction
    data class TokenChanged(val value: String) : AtmosphereInteraction
    data object ConnectionSaveSelected : AtmosphereInteraction
    data object ConnectionTestSelected : AtmosphereInteraction
    data object CatalogRefreshSelected : AtmosphereInteraction
    data class HueHostChanged(val value: String) : AtmosphereInteraction
    data class HueKeyChanged(val value: String) : AtmosphereInteraction
    data object HueSaveSelected : AtmosphereInteraction
    data object HueDiscoverSelected : AtmosphereInteraction
    data class HueBridgeSelected(val ipAddress: String) : AtmosphereInteraction
    data object HuePairSelected : AtmosphereInteraction
    data object HueTestSelected : AtmosphereInteraction
    data object HueCatalogRefreshSelected : AtmosphereInteraction
    data class HueCatalogSceneSelected(val sceneId: String) : AtmosphereInteraction
    data object HueLightsRefreshSelected : AtmosphereInteraction
    data class HueLightToggled(val lightId: String) : AtmosphereInteraction
    data object GoveeScanSelected : AtmosphereInteraction
    data class GoveeDeviceToggled(val deviceId: String) : AtmosphereInteraction
    data class LookPowerChanged(val powerOn: Boolean) : AtmosphereInteraction
    data class LookBrightnessChanged(val value: String) : AtmosphereInteraction
    data class LookColorHexChanged(val value: String) : AtmosphereInteraction
    data class LookPresetSelected(val colorHex: String, val brightness: Int, val powerOn: Boolean) : AtmosphereInteraction
    data class LookTransitionChanged(val durationMs: Int) : AtmosphereInteraction
    data class LightingEffectSelected(val effect: AtmosphereLightingEffect) : AtmosphereInteraction
    data class LightingLoopSelected(val loop: AtmosphereLightingLoop) : AtmosphereInteraction
    data class LookMoodNameChanged(val value: String) : AtmosphereInteraction
    data object LookMoodSaveSelected : AtmosphereInteraction
    data class LookMoodDeleteSelected(val moodId: String) : AtmosphereInteraction
    data class DraftSceneNameChanged(val value: String) : AtmosphereInteraction
    data class DraftEntityIdChanged(val value: String) : AtmosphereInteraction
    data class CatalogSceneSelected(val entityId: String) : AtmosphereInteraction
    data object SceneCreateSelected : AtmosphereInteraction
    data class SceneDeleteSelected(val sceneId: String) : AtmosphereInteraction
    data class SceneActivateSelected(val sceneId: String) : AtmosphereInteraction
    data class DraftSceneMusicTrackSelected(val trackId: String) : AtmosphereInteraction
    data class SceneMusicTrackSelected(val sceneId: String, val trackId: String) : AtmosphereInteraction
    data class MusicFilesChosen(val paths: List<String>) : AtmosphereInteraction
    data class MusicTrackPlaySelected(val trackId: String) : AtmosphereInteraction
    data object MusicStopSelected : AtmosphereInteraction
    data object MusicLoopToggled : AtmosphereInteraction
    data class MusicVolumeChanged(val volume: Int) : AtmosphereInteraction
    data class MusicTrackDeleteSelected(val trackId: String) : AtmosphereInteraction
    data object FloatingOpened : AtmosphereInteraction
    data object FloatingClosed : AtmosphereInteraction
    data object AlwaysOnTopToggled : AtmosphereInteraction
}
