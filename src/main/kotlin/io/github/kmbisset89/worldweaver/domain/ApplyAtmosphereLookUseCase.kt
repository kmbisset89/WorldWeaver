package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

/**
 * Applies a shared color and brightness to selected Hue and Govee lights.
 *
 * Hue fades in hardware when [transitionDurationMs] is greater than zero. Govee
 * has no local duration, so a supplied [from] look is stepped through in software.
 */
internal class ApplyAtmosphereLookUseCase(
    private val store: AtmosphereSettingsStore,
    private val hue: HueClient,
    private val govee: GoveeLightingClient,
    private val colorParser: GoveeColorHexParser = GoveeColorHexParser(),
    private val transitionCalculator: LightingTransitionCalculator = LightingTransitionCalculator(),
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
        transitionDurationMs: Int = 0,
        from: LightingLook? = null,
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
        val duration = transitionDurationMs.coerceIn(0, LightingTransitionCalculator.MAX_DURATION_MS)
        return coroutineScope {
            val hueResult = async {
                if (lights.isEmpty()) {
                    null
                } else {
                    applyHue(lights, powerOn, brightness, rgb, duration)
                }
            }
            val goveeResult = async {
                if (devices.isEmpty()) {
                    null
                } else {
                    applyGovee(devices, powerOn, brightness, rgb, duration, from)
                }
            }
            combine(hueResult.await(), goveeResult.await())
        }
    }

    private suspend fun applyHue(
        lightIds: List<String>,
        powerOn: Boolean,
        brightness: Int,
        rgb: GoveeColorHexParser.Rgb,
        transitionDurationMs: Int,
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
                transitionDurationMs = transitionDurationMs,
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
        transitionDurationMs: Int,
        from: LightingLook?,
    ): ProviderResult {
        val devices = store.settings.value.goveeDevices.filter { it.deviceId in deviceIds }
        if (devices.isEmpty()) {
            return ProviderResult.Error("Scan for Govee lights before applying that look.")
        }
        val to = LightingLook(
            powerOn = powerOn,
            brightness = brightness,
            red = rgb.red,
            green = rgb.green,
            blue = rgb.blue,
        )
        val frames = if (from != null && transitionDurationMs > 0) {
            transitionCalculator.calculate(from, to, transitionDurationMs)
        } else {
            listOf(LightingTransitionCalculator.Frame(delayFromStartMs = 0L, look = to))
        }
        var elapsed = 0L
        var anySuccess = false
        val errors = mutableListOf<String>()
        for (frame in frames) {
            val wait = frame.delayFromStartMs - elapsed
            if (wait > 0L) {
                delay(wait)
            }
            elapsed = frame.delayFromStartMs
            val command = GoveeLightCommand(
                powerOn = frame.look.powerOn,
                brightness = frame.look.brightness,
                red = frame.look.red,
                green = frame.look.green,
                blue = frame.look.blue,
            )
            when (val response = govee.apply(devices, command)) {
                GoveeLightingClient.ApplyResult.Applied -> anySuccess = true
                is GoveeLightingClient.ApplyResult.Failed -> errors.add(response.message)
            }
        }
        return when {
            anySuccess && errors.isEmpty() -> ProviderResult.Success
            anySuccess -> ProviderResult.Error(errors.joinToString(" "))
            else -> ProviderResult.Error(errors.joinToString(" ").ifBlank { "Could not reach Govee lights" })
        }
    }

    private fun combine(hueResult: ProviderResult?, goveeResult: ProviderResult?): Result {
        val results = listOfNotNull(hueResult, goveeResult)
        val errors = results.filterIsInstance<ProviderResult.Error>().map { it.message }
        val anySuccess = results.any { it is ProviderResult.Success }
        return when {
            anySuccess && errors.isEmpty() -> Result.Applied
            anySuccess -> Result.Partial(errors.joinToString(" "))
            else -> Result.Failed(errors.joinToString(" ").ifBlank { "Could not apply that look" })
        }
    }

    private sealed interface ProviderResult {
        data object Success : ProviderResult
        data class Error(val message: String) : ProviderResult
    }
}
