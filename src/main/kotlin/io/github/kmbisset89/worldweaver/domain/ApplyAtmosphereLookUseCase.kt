package io.github.kmbisset89.worldweaver.domain

/**
 * Applies a shared color and brightness to selected Hue and Govee lights.
 */
internal class ApplyAtmosphereLookUseCase(
    private val store: AtmosphereSettingsStore,
    private val hue: HueClient,
    private val govee: GoveeLightingClient,
    private val colorParser: GoveeColorHexParser = GoveeColorHexParser(),
) {
    sealed interface Result {
        data object Applied : Result
        data class Partial(val message: String) : Result
        data object NoTargets : Result
        data class Failed(val message: String) : Result
    }

    suspend operator fun invoke(
        hueLightIds: List<String> = emptyList(),
        goveeDeviceIds: List<String> = emptyList(),
        powerOn: Boolean,
        brightness: Int,
        colorHex: String,
    ): Result {
        val lights = hueLightIds.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        val devices = goveeDeviceIds.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        if (lights.isEmpty() && devices.isEmpty()) {
            return Result.NoTargets
        }
        val rgb = if (powerOn) {
            colorParser.parse(colorHex) ?: return Result.Failed("That color is not valid.")
        } else {
            GoveeColorHexParser.Rgb(0, 0, 0)
        }
        val errors = mutableListOf<String>()
        var anySuccess = false
        if (lights.isNotEmpty()) {
            when (val result = applyHue(lights, powerOn, brightness, rgb)) {
                ProviderResult.Success -> anySuccess = true
                is ProviderResult.Error -> errors.add(result.message)
            }
        }
        if (devices.isNotEmpty()) {
            when (val result = applyGovee(devices, powerOn, brightness, rgb)) {
                ProviderResult.Success -> anySuccess = true
                is ProviderResult.Error -> errors.add(result.message)
            }
        }
        return when {
            anySuccess && errors.isEmpty() -> Result.Applied
            anySuccess -> Result.Partial(errors.joinToString(" "))
            else -> Result.Failed(errors.joinToString(" ").ifBlank { "Could not apply that look" })
        }
    }

    private suspend fun applyHue(
        lightIds: List<String>,
        powerOn: Boolean,
        brightness: Int,
        rgb: GoveeColorHexParser.Rgb,
    ): ProviderResult {
        val settings = store.settings.value
        if (!settings.hue.isConfigured) {
            return ProviderResult.Error("Philips Hue is not connected.")
        }
        return when (
            val response = hue.applyColor(
                connection = settings.hue,
                lightIds = lightIds,
                powerOn = powerOn,
                brightness = brightness,
                red = rgb.red,
                green = rgb.green,
                blue = rgb.blue,
            )
        ) {
            HueClient.ActivateResult.Activated -> ProviderResult.Success
            HueClient.ActivateResult.Unauthorized -> ProviderResult.Error("Hue rejected the application key.")
            is HueClient.ActivateResult.Failed -> ProviderResult.Error(response.message)
        }
    }

    private suspend fun applyGovee(
        deviceIds: List<String>,
        powerOn: Boolean,
        brightness: Int,
        rgb: GoveeColorHexParser.Rgb,
    ): ProviderResult {
        val devices = store.settings.value.goveeDevices.filter { it.deviceId in deviceIds }
        if (devices.isEmpty()) {
            return ProviderResult.Error("Scan for Govee lights before applying that look.")
        }
        val command = GoveeLightCommand(
            powerOn = powerOn,
            brightness = brightness,
            red = rgb.red,
            green = rgb.green,
            blue = rgb.blue,
        )
        return when (val response = govee.apply(devices, command)) {
            GoveeLightingClient.ApplyResult.Applied -> ProviderResult.Success
            is GoveeLightingClient.ApplyResult.Failed -> ProviderResult.Error(response.message)
        }
    }

    private sealed interface ProviderResult {
        data object Success : ProviderResult
        data class Error(val message: String) : ProviderResult
    }
}
