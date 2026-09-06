package io.github.kmbisset89.worldweaver.domain

internal class ActivateAtmosphereSceneUseCase(
    private val store: AtmosphereSettingsStore,
    private val homeAssistant: HomeAssistantClient,
    private val hue: HueClient,
    private val applyLook: ApplyAtmosphereLookUseCase,
) {
    sealed interface Result {
        data object Activated : Result
        data class Partial(val message: String) : Result
        data object NotFound : Result
        data object NoTargets : Result
        data class Failed(val message: String) : Result
    }

    suspend operator fun invoke(
        sceneId: String,
        transitionDurationMs: Int = 0,
        fromLook: LightingLook? = null,
    ): Result {
        val settings = store.settings.value
        val scene = settings.scenes.firstOrNull { it.id == sceneId } ?: return Result.NotFound
        if (!scene.hasAnyTarget) {
            return Result.NoTargets
        }
        val errors = mutableListOf<String>()
        var anySuccess = false
        if (scene.hasHomeAssistant) {
            when (val result = activateHomeAssistant(settings, scene)) {
                ProviderResult.Success -> anySuccess = true
                is ProviderResult.Error -> errors.add(result.message)
            }
        }
        if (scene.hasHue && scene.hueSceneId.isNotBlank()) {
            when (val result = activateHueScene(settings, scene)) {
                ProviderResult.Success -> anySuccess = true
                is ProviderResult.Error -> errors.add(result.message)
            }
        }
        val lookHueIds = if (scene.hueSceneId.isBlank()) scene.hueLightIds else emptyList()
        if (lookHueIds.isNotEmpty() || scene.hasGovee) {
            when (
                val result = applyLook(
                    hueLightIds = lookHueIds,
                    goveeDeviceIds = scene.goveeDeviceIds,
                    powerOn = scene.goveePowerOn,
                    brightness = scene.goveeBrightness,
                    colorHex = scene.goveeColorHex,
                    transitionDurationMs = transitionDurationMs,
                    from = fromLook,
                )
            ) {
                ApplyAtmosphereLookUseCase.Result.Applied -> anySuccess = true
                is ApplyAtmosphereLookUseCase.Result.Partial -> {
                    anySuccess = true
                    errors.add(result.message)
                }
                ApplyAtmosphereLookUseCase.Result.NoTargets -> errors.add("Pick Hue or Govee lights for that look")
                is ApplyAtmosphereLookUseCase.Result.Failed -> errors.add(result.message)
            }
        }
        return when {
            anySuccess && errors.isEmpty() -> Result.Activated
            anySuccess -> Result.Partial(errors.joinToString(" "))
            else -> Result.Failed(errors.joinToString(" ").ifBlank { "Could not activate that scene" })
        }
    }

    private suspend fun activateHomeAssistant(
        settings: AtmosphereSettings,
        scene: AtmosphereScene,
    ): ProviderResult {
        if (!settings.connection.isConfigured) {
            return ProviderResult.Error("Home Assistant is not connected.")
        }
        return when (val response = homeAssistant.activateScene(settings.connection, scene.entityId)) {
            HomeAssistantClient.ActivateResult.Activated -> ProviderResult.Success
            HomeAssistantClient.ActivateResult.Unauthorized -> ProviderResult.Error("Home Assistant rejected the token.")
            is HomeAssistantClient.ActivateResult.Failed -> ProviderResult.Error(response.message)
        }
    }

    private suspend fun activateHueScene(
        settings: AtmosphereSettings,
        scene: AtmosphereScene,
    ): ProviderResult {
        if (!settings.hue.isConfigured) {
            return ProviderResult.Error("Philips Hue is not connected.")
        }
        return hueResult(
            hue.activateScene(
                connection = settings.hue,
                sceneId = scene.hueSceneId,
                groupId = scene.hueGroupId,
                lightIds = scene.hueLightIds,
            ),
        )
    }

    private fun hueResult(response: HueClient.ActivateResult): ProviderResult {
        return when (response) {
            HueClient.ActivateResult.Activated -> ProviderResult.Success
            HueClient.ActivateResult.Unauthorized -> ProviderResult.Error("Hue rejected the application key.")
            is HueClient.ActivateResult.Failed -> ProviderResult.Error(response.message)
        }
    }

    private sealed interface ProviderResult {
        data object Success : ProviderResult
        data class Error(val message: String) : ProviderResult
    }
}
