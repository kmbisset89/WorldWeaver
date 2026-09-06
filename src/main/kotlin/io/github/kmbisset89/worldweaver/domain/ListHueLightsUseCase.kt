package io.github.kmbisset89.worldweaver.domain

internal class ListHueLightsUseCase(
    private val store: AtmosphereSettingsStore,
    private val client: HueClient,
) {
    sealed interface Result {
        data class Listed(val lights: List<HueLight>) : Result
        data object NotConfigured : Result
        data object Unauthorized : Result
        data class Failed(val message: String) : Result
    }

    suspend operator fun invoke(): Result {
        val connection = store.settings.value.hue
        if (!connection.isConfigured) {
            return Result.NotConfigured
        }
        return when (val response = client.listLights(connection)) {
            is HueClient.LightListResult.Lights -> {
                store.setHueLights(response.lights)
                Result.Listed(response.lights)
            }
            HueClient.LightListResult.Unauthorized -> Result.Unauthorized
            is HueClient.LightListResult.Failed -> Result.Failed(response.message)
        }
    }
}
