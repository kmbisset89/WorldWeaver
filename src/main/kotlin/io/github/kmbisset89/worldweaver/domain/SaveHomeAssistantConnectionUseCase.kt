package io.github.kmbisset89.worldweaver.domain

internal class SaveHomeAssistantConnectionUseCase(
    private val parser: HomeAssistantConnectionParser,
    private val store: AtmosphereSettingsStore,
) {
    sealed interface Result {
        data object Saved : Result
        data object BlankUrl : Result
        data object BlankToken : Result
        data object InvalidUrl : Result
    }

    operator fun invoke(baseUrl: String, token: String): Result {
        val parsed = parser.parse(baseUrl, token)
        val connection = when (parsed) {
            is HomeAssistantConnectionParser.Result.Valid -> parsed.connection
            HomeAssistantConnectionParser.Result.BlankUrl -> return Result.BlankUrl
            HomeAssistantConnectionParser.Result.BlankToken -> return Result.BlankToken
            HomeAssistantConnectionParser.Result.InvalidUrl -> return Result.InvalidUrl
        }
        store.setConnection(connection)
        return Result.Saved
    }
}
