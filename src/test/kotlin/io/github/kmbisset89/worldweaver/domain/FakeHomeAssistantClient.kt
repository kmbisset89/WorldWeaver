package io.github.kmbisset89.worldweaver.domain

internal class FakeHomeAssistantClient : HomeAssistantClient {
    var connectionResult: HomeAssistantClient.ConnectionResult =
        HomeAssistantClient.ConnectionResult.Connected
    var sceneListResult: HomeAssistantClient.SceneListResult =
        HomeAssistantClient.SceneListResult.Scenes(emptyList())
    var activateResult: HomeAssistantClient.ActivateResult =
        HomeAssistantClient.ActivateResult.Activated
    var lastActivatedEntityId: String? = null
    var lastConnection: HomeAssistantConnection? = null

    override suspend fun testConnection(
        connection: HomeAssistantConnection,
    ): HomeAssistantClient.ConnectionResult {
        lastConnection = connection
        return connectionResult
    }

    override suspend fun listScenes(
        connection: HomeAssistantConnection,
    ): HomeAssistantClient.SceneListResult {
        lastConnection = connection
        return sceneListResult
    }

    override suspend fun activateScene(
        connection: HomeAssistantConnection,
        entityId: String,
    ): HomeAssistantClient.ActivateResult {
        lastConnection = connection
        lastActivatedEntityId = entityId
        return activateResult
    }
}
