package io.github.kmbisset89.worldweaver.domain

internal class PairHueBridgeUseCase(
    private val parser: HueBridgeHostParser,
    private val client: HueClient,
    private val store: AtmosphereSettingsStore,
) {
    sealed interface Result {
        data object Paired : Result
        data object BlankHost : Result
        data object InvalidHost : Result
        data object LinkButtonNotPressed : Result
        data class Failed(val message: String) : Result
    }

    suspend operator fun invoke(bridgeHost: String, deviceName: String): Result {
        val host = when (val parsed = parser.parse(bridgeHost)) {
            is HueBridgeHostParser.Result.Valid -> parsed.host
            HueBridgeHostParser.Result.Blank -> return Result.BlankHost
            HueBridgeHostParser.Result.Invalid -> return Result.InvalidHost
        }
        return when (val response = client.pair(host, deviceName)) {
            is HueClient.PairResult.Paired -> {
                store.setHueConnection(HueConnection(bridgeHost = host, applicationKey = response.applicationKey))
                Result.Paired
            }
            HueClient.PairResult.LinkButtonNotPressed -> Result.LinkButtonNotPressed
            is HueClient.PairResult.Failed -> Result.Failed(response.message)
        }
    }
}
