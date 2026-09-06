package io.github.kmbisset89.worldweaver.domain

/**
 * Machine-local atmosphere connections, named scene mappings, and custom moods.
 */
internal data class AtmosphereSettings(
    val connection: HomeAssistantConnection,
    val hue: HueConnection,
    val goveeDevices: List<GoveeDevice>,
    val hueLights: List<HueLight> = emptyList(),
    val scenes: List<AtmosphereScene>,
    val moods: List<AtmosphereMood> = emptyList(),
    val selectedHueLightIds: List<String> = emptyList(),
    val selectedGoveeDeviceIds: List<String> = emptyList(),
    val isAlwaysOnTop: Boolean,
    val lookTransitionMs: Int = LightingTransitionCalculator.DEFAULT_DURATION_MS,
)
