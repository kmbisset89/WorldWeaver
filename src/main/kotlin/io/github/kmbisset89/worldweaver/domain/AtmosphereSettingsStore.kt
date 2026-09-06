package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.util.prefs.Preferences

/**
 * Persists atmosphere provider credentials, scene mappings, and custom moods on this computer.
 */
internal class AtmosphereSettingsStore(
    private val preferences: Preferences,
) {
    private val _settings = MutableStateFlow(read())
    val settings: StateFlow<AtmosphereSettings> = _settings.asStateFlow()

    fun setConnection(connection: HomeAssistantConnection) {
        preferences.put(KEY_BASE_URL, connection.baseUrl)
        preferences.put(KEY_TOKEN, connection.token)
        _settings.value = _settings.value.copy(connection = connection)
    }

    fun setHueConnection(connection: HueConnection) {
        preferences.put(KEY_HUE_HOST, connection.bridgeHost)
        preferences.put(KEY_HUE_KEY, connection.applicationKey)
        _settings.value = _settings.value.copy(hue = connection)
    }

    fun setGoveeDevices(devices: List<GoveeDevice>) {
        preferences.put(KEY_GOVEE_DEVICES, json.encodeToString(ListSerializer(GoveeDevice.serializer()), devices))
        _settings.value = _settings.value.copy(goveeDevices = devices)
    }

    fun setHueLights(lights: List<HueLight>) {
        preferences.put(KEY_HUE_LIGHTS, json.encodeToString(ListSerializer(HueLight.serializer()), lights))
        _settings.value = _settings.value.copy(hueLights = lights)
    }

    fun setScenes(scenes: List<AtmosphereScene>) {
        preferences.put(KEY_SCENES, json.encodeToString(ListSerializer(AtmosphereScene.serializer()), scenes))
        _settings.value = _settings.value.copy(scenes = scenes)
    }

    fun setMoods(moods: List<AtmosphereMood>) {
        preferences.put(KEY_MOODS, json.encodeToString(ListSerializer(AtmosphereMood.serializer()), moods))
        _settings.value = _settings.value.copy(moods = moods)
    }

    fun setSelectedHueLightIds(ids: List<String>) {
        preferences.put(KEY_SELECTED_HUE_LIGHTS, json.encodeToString(ListSerializer(String.serializer()), ids))
        _settings.value = _settings.value.copy(selectedHueLightIds = ids)
    }

    fun setSelectedGoveeDeviceIds(ids: List<String>) {
        preferences.put(KEY_SELECTED_GOVEE_DEVICES, json.encodeToString(ListSerializer(String.serializer()), ids))
        _settings.value = _settings.value.copy(selectedGoveeDeviceIds = ids)
    }

    fun setAlwaysOnTop(alwaysOnTop: Boolean) {
        preferences.putBoolean(KEY_ALWAYS_ON_TOP, alwaysOnTop)
        _settings.value = _settings.value.copy(isAlwaysOnTop = alwaysOnTop)
    }

    fun replaceAll(settings: AtmosphereSettings) {
        preferences.put(KEY_BASE_URL, settings.connection.baseUrl)
        preferences.put(KEY_TOKEN, settings.connection.token)
        preferences.put(KEY_HUE_HOST, settings.hue.bridgeHost)
        preferences.put(KEY_HUE_KEY, settings.hue.applicationKey)
        preferences.put(KEY_GOVEE_DEVICES, json.encodeToString(ListSerializer(GoveeDevice.serializer()), settings.goveeDevices))
        preferences.put(KEY_HUE_LIGHTS, json.encodeToString(ListSerializer(HueLight.serializer()), settings.hueLights))
        preferences.put(KEY_SCENES, json.encodeToString(ListSerializer(AtmosphereScene.serializer()), settings.scenes))
        preferences.put(KEY_MOODS, json.encodeToString(ListSerializer(AtmosphereMood.serializer()), settings.moods))
        preferences.put(KEY_SELECTED_HUE_LIGHTS, json.encodeToString(ListSerializer(String.serializer()), settings.selectedHueLightIds))
        preferences.put(KEY_SELECTED_GOVEE_DEVICES, json.encodeToString(ListSerializer(String.serializer()), settings.selectedGoveeDeviceIds))
        preferences.putBoolean(KEY_ALWAYS_ON_TOP, settings.isAlwaysOnTop)
        _settings.value = settings
    }

    private fun read(): AtmosphereSettings {
        return AtmosphereSettings(
            connection = HomeAssistantConnection(
                baseUrl = preferences.get(KEY_BASE_URL, ""),
                token = preferences.get(KEY_TOKEN, ""),
            ),
            hue = HueConnection(
                bridgeHost = preferences.get(KEY_HUE_HOST, ""),
                applicationKey = preferences.get(KEY_HUE_KEY, ""),
            ),
            goveeDevices = readGoveeDevices(),
            hueLights = readHueLights(),
            scenes = readScenes(),
            moods = readMoods(),
            selectedHueLightIds = readStringIds(KEY_SELECTED_HUE_LIGHTS),
            selectedGoveeDeviceIds = readStringIds(KEY_SELECTED_GOVEE_DEVICES),
            isAlwaysOnTop = preferences.getBoolean(KEY_ALWAYS_ON_TOP, false),
        )
    }

    private fun readScenes(): List<AtmosphereScene> {
        val raw = preferences.get(KEY_SCENES, "[]")
        return try {
            json.decodeFromString(ListSerializer(AtmosphereScene.serializer()), raw)
                .sortedBy { it.sortOrder }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun readGoveeDevices(): List<GoveeDevice> {
        val raw = preferences.get(KEY_GOVEE_DEVICES, "[]")
        return try {
            json.decodeFromString(ListSerializer(GoveeDevice.serializer()), raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun readHueLights(): List<HueLight> {
        val raw = preferences.get(KEY_HUE_LIGHTS, "[]")
        return try {
            json.decodeFromString(ListSerializer(HueLight.serializer()), raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun readMoods(): List<AtmosphereMood> {
        val raw = preferences.get(KEY_MOODS, "[]")
        return try {
            json.decodeFromString(ListSerializer(AtmosphereMood.serializer()), raw)
                .sortedBy { it.sortOrder }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun readStringIds(key: String): List<String> {
        val raw = preferences.get(key, "[]")
        return try {
            json.decodeFromString(ListSerializer(String.serializer()), raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    companion object {
        const val PREF_NODE = "io.github.kmbisset89.worldweaver.atmosphere"
        private const val KEY_BASE_URL = "home_assistant_base_url"
        private const val KEY_TOKEN = "home_assistant_token"
        private const val KEY_HUE_HOST = "hue_bridge_host"
        private const val KEY_HUE_KEY = "hue_application_key"
        private const val KEY_GOVEE_DEVICES = "govee_devices"
        private const val KEY_HUE_LIGHTS = "hue_lights"
        private const val KEY_SCENES = "atmosphere_scenes"
        private const val KEY_MOODS = "atmosphere_moods"
        private const val KEY_SELECTED_HUE_LIGHTS = "selected_hue_light_ids"
        private const val KEY_SELECTED_GOVEE_DEVICES = "selected_govee_device_ids"
        private const val KEY_ALWAYS_ON_TOP = "atmosphere_always_on_top"

        private val json = Json {
            ignoreUnknownKeys = true
        }
    }
}
