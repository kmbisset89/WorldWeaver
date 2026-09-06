package io.github.kmbisset89.worldweaver.domain

internal class ListHueScenesUseCase(
    private val store: AtmosphereSettingsStore,
    private val client: HueClient,
) {
    sealed interface Result {
        data class Listed(val scenes: List<HueScene>) : Result
        data object NotConfigured : Result
        data object Unauthorized : Result
        data class Failed(val message: String) : Result
    }

    suspend operator fun invoke(): Result {
        val connection = store.settings.value.hue
        if (!connection.isConfigured) {
            return Result.NotConfigured
        }
        return when (val response = client.listScenes(connection)) {
            is HueClient.SceneListResult.Scenes -> Result.Listed(response.scenes)
            HueClient.SceneListResult.Unauthorized -> Result.Unauthorized
            is HueClient.SceneListResult.Failed -> Result.Failed(response.message)
        }
    }
}
