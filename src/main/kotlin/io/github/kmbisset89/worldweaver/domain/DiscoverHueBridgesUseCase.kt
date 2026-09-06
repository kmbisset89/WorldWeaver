package io.github.kmbisset89.worldweaver.domain

internal class DiscoverHueBridgesUseCase(
    private val client: HueClient,
) {
    sealed interface Result {
        data class Found(val bridges: List<HueBridge>) : Result
        data class Failed(val message: String) : Result
    }

    suspend operator fun invoke(): Result {
        return when (val response = client.discoverBridges()) {
            is HueClient.DiscoverResult.Bridges -> Result.Found(response.bridges)
            is HueClient.DiscoverResult.Failed -> Result.Failed(response.message)
        }
    }
}
