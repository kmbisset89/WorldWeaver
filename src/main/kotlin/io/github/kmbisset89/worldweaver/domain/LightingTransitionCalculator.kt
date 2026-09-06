package io.github.kmbisset89.worldweaver.domain

import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Builds timed frames that ease from one table look to another.
 *
 * Color is interpolated in linear light after sRGB decoding so midpoints stay
 * perceptually even. Brightness uses the same easing curve. Hue can fade in
 * hardware; Govee has no local duration, so callers play these frames in order.
 */
internal class LightingTransitionCalculator {
    data class Frame(
        val delayFromStartMs: Long,
        val look: LightingLook,
    )

    enum class Easing {
        Linear,
        EaseInOut,
    }

    fun calculate(
        from: LightingLook,
        to: LightingLook,
        durationMs: Int,
        stepIntervalMs: Int = DEFAULT_STEP_INTERVAL_MS,
        easing: Easing = Easing.EaseInOut,
    ): List<Frame> {
        val duration = durationMs.coerceAtLeast(0)
        if (duration == 0 || from == to) {
            return listOf(Frame(delayFromStartMs = 0L, look = to))
        }
        val interval = stepIntervalMs.coerceAtLeast(1)
        val steps = (duration / interval).coerceAtLeast(1)
        return (1..steps).map { step ->
            val t = step.toDouble() / steps.toDouble()
            Frame(
                delayFromStartMs = (duration.toDouble() * t).roundToInt().toLong(),
                look = mix(from, to, t, easing),
            )
        }
    }

    /**
     * Interpolates [from] toward [to] at [t] in `0..1` using linear-light color mixing.
     */
    fun mix(
        from: LightingLook,
        to: LightingLook,
        t: Double,
        easing: Easing = Easing.Linear,
    ): LightingLook {
        return interpolate(from, to, ease(t, easing))
    }

    private fun interpolate(from: LightingLook, to: LightingLook, t: Double): LightingLook {
        val fromBrightness = if (from.powerOn) from.brightness else 0
        val toBrightness = if (to.powerOn) to.brightness else 0
        val brightness = lerpInt(fromBrightness, toBrightness, t)
        val fromRed = if (from.powerOn) from.red else 0
        val fromGreen = if (from.powerOn) from.green else 0
        val fromBlue = if (from.powerOn) from.blue else 0
        val toRed = if (to.powerOn) to.red else 0
        val toGreen = if (to.powerOn) to.green else 0
        val toBlue = if (to.powerOn) to.blue else 0
        val red = encode(lerp(linearize(fromRed), linearize(toRed), t))
        val green = encode(lerp(linearize(fromGreen), linearize(toGreen), t))
        val blue = encode(lerp(linearize(fromBlue), linearize(toBlue), t))
        val powerOn = if (t >= 1.0) {
            to.powerOn
        } else {
            brightness > 0 || to.powerOn
        }
        return LightingLook(
            powerOn = powerOn,
            brightness = if (powerOn) brightness.coerceIn(1, 100) else 1,
            red = red,
            green = green,
            blue = blue,
        )
    }

    private fun ease(t: Double, easing: Easing): Double {
        val clamped = t.coerceIn(0.0, 1.0)
        return when (easing) {
            Easing.Linear -> clamped
            Easing.EaseInOut -> clamped * clamped * (3.0 - 2.0 * clamped)
        }
    }

    private fun lerp(from: Double, to: Double, t: Double): Double {
        return from + (to - from) * t
    }

    private fun lerpInt(from: Int, to: Int, t: Double): Int {
        return lerp(from.toDouble(), to.toDouble(), t).roundToInt()
    }

    private fun linearize(channel: Int): Double {
        val value = channel.coerceIn(0, 255) / 255.0
        return if (value <= 0.04045) {
            value / 12.92
        } else {
            ((value + 0.055) / 1.055).pow(2.4)
        }
    }

    private fun encode(linear: Double): Int {
        val value = linear.coerceIn(0.0, 1.0)
        val srgb = if (value <= 0.0031308) {
            12.92 * value
        } else {
            1.055 * value.pow(1.0 / 2.4) - 0.055
        }
        return (srgb * 255.0).roundToInt().coerceIn(0, 255)
    }

    companion object {
        const val DEFAULT_DURATION_MS = 400
        const val MAX_DURATION_MS = 3_000
        const val PREVIEW_CAP_MS = 300
        const val DEFAULT_STEP_INTERVAL_MS = 80
    }
}
