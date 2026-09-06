package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.delay

/**
 * Plays a one-shot lighting cue on selected Hue and Govee lights, then restores the current look.
 */
internal class PlayAtmosphereLightingEffectUseCase(
    private val applyLook: ApplyAtmosphereLookUseCase,
    private val calculator: AtmosphereLightingEffectCalculator = AtmosphereLightingEffectCalculator(),
) {
    sealed interface Result {
        data object Played : Result
        data class Partial(val message: String) : Result
        data object NoTargets : Result
        data class Failed(val message: String) : Result
    }

    suspend operator fun invoke(
        effect: AtmosphereLightingEffect,
        currentLook: LightingLook,
        hueLightIds: List<String> = emptyList(),
        goveeDeviceIds: List<String> = emptyList(),
    ): Result {
        val lights = hueLightIds.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        val devices = goveeDeviceIds.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        if (lights.isEmpty() && devices.isEmpty()) {
            return Result.NoTargets
        }
        val frames = calculator.calculate(effect, currentLook)
        var previous: LightingLook? = currentLook
        val errors = mutableListOf<String>()
        var anySuccess = false
        for (frame in frames) {
            when (
                val result = applyLook(
                    hueLightIds = lights,
                    goveeDeviceIds = devices,
                    powerOn = frame.look.powerOn,
                    brightness = frame.look.brightness,
                    colorHex = frame.look.toColorHex(),
                    transitionDurationMs = frame.transitionMs,
                    from = previous,
                )
            ) {
                ApplyAtmosphereLookUseCase.Result.Applied -> anySuccess = true
                is ApplyAtmosphereLookUseCase.Result.Partial -> {
                    anySuccess = true
                    errors.add(result.message)
                }
                ApplyAtmosphereLookUseCase.Result.NoTargets -> return Result.NoTargets
                is ApplyAtmosphereLookUseCase.Result.Failed -> errors.add(result.message)
            }
            previous = frame.look
            if (frame.holdMs > 0L) {
                delay(frame.holdMs)
            }
        }
        return when {
            anySuccess && errors.isEmpty() -> Result.Played
            anySuccess -> Result.Partial(errors.distinct().joinToString(" "))
            else -> Result.Failed(errors.joinToString(" ").ifBlank { "Could not play that effect" })
        }
    }
}
