package io.github.kmbisset89.worldweaver.domain

internal class SaveHueConnectionUseCase(
    private val parser: HueBridgeHostParser,
    private val store: AtmosphereSettingsStore,
) {
    sealed interface Result {
        data object Saved : Result
        data object BlankHost : Result
        data object InvalidHost : Result
        data object BlankKey : Result
    }

    operator fun invoke(bridgeHost: String, applicationKey: String): Result {
        val host = when (val parsed = parser.parse(bridgeHost)) {
            is HueBridgeHostParser.Result.Valid -> parsed.host
            HueBridgeHostParser.Result.Blank -> return Result.BlankHost
            HueBridgeHostParser.Result.Invalid -> return Result.InvalidHost
        }
        val key = applicationKey.trim()
        if (key.isEmpty()) {
            return Result.BlankKey
        }
        store.setHueConnection(HueConnection(bridgeHost = host, applicationKey = key))
        return Result.Saved
    }
}
