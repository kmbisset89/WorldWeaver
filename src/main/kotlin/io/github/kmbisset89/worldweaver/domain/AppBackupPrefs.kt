package io.github.kmbisset89.worldweaver.domain

import kotlinx.serialization.Serializable

@Serializable
internal data class AppBackupPrefs(
    val activeWorldId: String? = null,
    val activeCampaignId: String? = null,
    val activeSessionId: String? = null,
    val displayName: String,
    val email: String,
    val themeMode: String,
    val themeSkin: String,
    val navExpanded: Boolean,
    val diceColorStyle: String,
    val homeAssistantBaseUrl: String = "",
    val homeAssistantToken: String = "",
    val hueBridgeHost: String = "",
    val hueApplicationKey: String = "",
    val goveeDevices: List<GoveeDevice> = emptyList(),
    val hueLights: List<HueLight> = emptyList(),
    val atmosphereAlwaysOnTop: Boolean = false,
    val atmosphereScenes: List<AtmosphereScene> = emptyList(),
    val atmosphereMoods: List<AtmosphereMood> = emptyList(),
    val atmosphereSelectedHueLightIds: List<String> = emptyList(),
    val atmosphereSelectedGoveeDeviceIds: List<String> = emptyList(),
)
