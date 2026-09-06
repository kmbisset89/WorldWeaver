package io.github.kmbisset89.worldweaver.domain

/**
 * Calls a local Philips Hue bridge to pair, list lights and scenes, recall scenes, and set per-light color.
 */
internal interface HueClient {
    suspend fun discoverBridges(): DiscoverResult

    suspend fun testConnection(connection: HueConnection): ConnectionResult

    suspend fun pair(bridgeHost: String, deviceName: String): PairResult

    suspend fun listScenes(connection: HueConnection): SceneListResult

    suspend fun listLights(connection: HueConnection): LightListResult

    suspend fun activateScene(
        connection: HueConnection,
        sceneId: String,
        groupId: String,
        lightIds: List<String> = emptyList(),
    ): ActivateResult

    suspend fun applyColor(
        connection: HueConnection,
        lightIds: List<String>,
        powerOn: Boolean,
        brightness: Int,
        red: Int,
        green: Int,
        blue: Int,
    ): ActivateResult

    sealed interface DiscoverResult {
        data class Bridges(val bridges: List<HueBridge>) : DiscoverResult
        data class Failed(val message: String) : DiscoverResult
    }

    sealed interface ConnectionResult {
        data object Connected : ConnectionResult
        data object Unauthorized : ConnectionResult
        data class Unreachable(val message: String) : ConnectionResult
    }

    sealed interface PairResult {
        data class Paired(val applicationKey: String) : PairResult
        data object LinkButtonNotPressed : PairResult
        data class Failed(val message: String) : PairResult
    }

    sealed interface SceneListResult {
        data class Scenes(val scenes: List<HueScene>) : SceneListResult
        data object Unauthorized : SceneListResult
        data class Failed(val message: String) : SceneListResult
    }

    sealed interface LightListResult {
        data class Lights(val lights: List<HueLight>) : LightListResult
        data object Unauthorized : LightListResult
        data class Failed(val message: String) : LightListResult
    }

    sealed interface ActivateResult {
        data object Activated : ActivateResult
        data object Unauthorized : ActivateResult
        data class Failed(val message: String) : ActivateResult
    }
}
