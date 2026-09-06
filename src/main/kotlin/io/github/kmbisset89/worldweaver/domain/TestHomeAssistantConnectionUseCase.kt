package io.github.kmbisset89.worldweaver.domain

internal class TestHomeAssistantConnectionUseCase(
    private val parser: HomeAssistantConnectionParser,
    private val client: HomeAssistantClient,
) {
    sealed interface Result {
        data object Connected : Result
        data object BlankUrl : Result
        data object BlankToken : Result
        data object InvalidUrl : Result
        data object Unauthorized : Result
        data class Unreachable(val message: String) : Result
    }

    suspend operator fun invoke(baseUrl: String, token: String): Result {
        val parsed = parser.parse(baseUrl, token)
        val connection = when (parsed) {
            is HomeAssistantConnectionParser.Result.Valid -> parsed.connection
            HomeAssistantConnectionParser.Result.BlankUrl -> return Result.BlankUrl
            HomeAssistantConnectionParser.Result.BlankToken -> return Result.BlankToken
            HomeAssistantConnectionParser.Result.InvalidUrl -> return Result.InvalidUrl
        }
        return when (val response = client.testConnection(connection)) {
            HomeAssistantClient.ConnectionResult.Connected -> Result.Connected
            HomeAssistantClient.ConnectionResult.Unauthorized -> Result.Unauthorized
            is HomeAssistantClient.ConnectionResult.Unreachable -> Result.Unreachable(response.message)
        }
    }
}
