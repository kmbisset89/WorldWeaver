package io.github.kmbisset89.worldweaver.domain

/**
 * Calls a Home Assistant instance to test connectivity, list scenes, and activate scenes.
 */
internal interface HomeAssistantClient {
    suspend fun testConnection(connection: HomeAssistantConnection): ConnectionResult

    suspend fun listScenes(connection: HomeAssistantConnection): SceneListResult

    suspend fun activateScene(
        connection: HomeAssistantConnection,
        entityId: String,
    ): ActivateResult

    sealed interface ConnectionResult {
        data object Connected : ConnectionResult
        data object Unauthorized : ConnectionResult
        data class Unreachable(val message: String) : ConnectionResult
    }

    sealed interface SceneListResult {
        data class Scenes(val scenes: List<HomeAssistantScene>) : SceneListResult
        data object Unauthorized : SceneListResult
        data class Failed(val message: String) : SceneListResult
    }

    sealed interface ActivateResult {
        data object Activated : ActivateResult
        data object Unauthorized : ActivateResult
        data class Failed(val message: String) : ActivateResult
    }
}
