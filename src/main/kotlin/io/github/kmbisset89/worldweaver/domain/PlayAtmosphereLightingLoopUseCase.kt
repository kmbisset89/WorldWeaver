package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext

/**
 * Oscillates selected Hue and Govee lights through a live lighting loop until the caller cancels.
 */
internal class PlayAtmosphereLightingLoopUseCase(
    private val applyLook: ApplyAtmosphereLookUseCase,
    private val calculator: AtmosphereLightingLoopCalculator = AtmosphereLightingLoopCalculator(),
) {
    sealed interface Result {
        data object NoTargets : Result
        data class Failed(val message: String) : Result
    }

    suspend operator fun invoke(
        loop: AtmosphereLightingLoop,
        hueLightIds: List<String> = emptyList(),
        goveeDeviceIds: List<String> = emptyList(),
        from: LightingLook? = null,
    ): Result {
        val lights = hueLightIds.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        val devices = goveeDeviceIds.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        if (lights.isEmpty() && devices.isEmpty()) {
            return Result.NoTargets
        }
        var previous = from
        var elapsedMs = 0L
        val sampleMs = AtmosphereLightingLoopCalculator.SAMPLE_INTERVAL_MS
        while (true) {
            coroutineContext.ensureActive()
            val look = calculator.lookAt(loop, elapsedMs)
            when (
                val result = applyLook(
                    hueLightIds = lights,
                    goveeDeviceIds = devices,
                    powerOn = look.powerOn,
                    brightness = look.brightness,
                    colorHex = look.toColorHex(),
                    transitionDurationMs = sampleMs,
                    from = previous,
                )
            ) {
                ApplyAtmosphereLookUseCase.Result.Applied -> Unit
                is ApplyAtmosphereLookUseCase.Result.Partial -> Unit
                ApplyAtmosphereLookUseCase.Result.NoTargets -> return Result.NoTargets
                is ApplyAtmosphereLookUseCase.Result.Failed -> return Result.Failed(result.message)
            }
            previous = look
            delay(sampleMs.toLong())
            elapsedMs += sampleMs
        }
    }
}
