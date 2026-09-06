package io.github.kmbisset89.worldweaver.domain

internal class TestHueConnectionUseCase(
    private val parser: HueBridgeHostParser,
    private val client: HueClient,
) {
    sealed interface Result {
        data object Connected : Result
        data object BlankHost : Result
        data object InvalidHost : Result
        data object BlankKey : Result
        data object Unauthorized : Result
        data class Unreachable(val message: String) : Result
    }

    suspend operator fun invoke(bridgeHost: String, applicationKey: String): Result {
        val host = when (val parsed = parser.parse(bridgeHost)) {
            is HueBridgeHostParser.Result.Valid -> parsed.host
            HueBridgeHostParser.Result.Blank -> return Result.BlankHost
            HueBridgeHostParser.Result.Invalid -> return Result.InvalidHost
        }
        val key = applicationKey.trim()
        if (key.isEmpty()) {
            return Result.BlankKey
        }
        return when (val response = client.testConnection(HueConnection(host, key))) {
            HueClient.ConnectionResult.Connected -> Result.Connected
            HueClient.ConnectionResult.Unauthorized -> Result.Unauthorized
            is HueClient.ConnectionResult.Unreachable -> Result.Unreachable(response.message)
        }
    }
}
