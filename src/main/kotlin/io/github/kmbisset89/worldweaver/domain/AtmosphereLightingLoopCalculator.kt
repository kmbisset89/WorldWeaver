package io.github.kmbisset89.worldweaver.domain

import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Samples a looping lighting cue at an elapsed time by oscillating through color waypoints.
 *
 * Color is mixed in linear light. A faster sine flicker can vary brightness so fire and candle
 * keep moving even between waypoints.
 */
internal class AtmosphereLightingLoopCalculator(
    private val mixer: LightingTransitionCalculator = LightingTransitionCalculator(),
) {
    fun lookAt(loop: AtmosphereLightingLoop, elapsedMs: Long): LightingLook {
        val spec = specFor(loop)
        val waypoints = spec.waypoints
        val period = spec.periodMs.coerceAtLeast(1)
        val cycle = Math.floorMod(elapsedMs, period.toLong()).toDouble() / period.toDouble()
        val position = cycle * waypoints.size
        val index = position.toInt().coerceIn(0, waypoints.lastIndex)
        val nextIndex = (index + 1) % waypoints.size
        val localT = (position - index).coerceIn(0.0, 1.0)
        val mixed = mixer.mix(
            from = waypoints[index],
            to = waypoints[nextIndex],
            t = localT,
            easing = LightingTransitionCalculator.Easing.EaseInOut,
        )
        return flicker(mixed, elapsedMs, spec)
    }

    private fun flicker(look: LightingLook, elapsedMs: Long, spec: LoopSpec): LightingLook {
        if (spec.flickerPeriodMs <= 0 || spec.flickerAmount <= 0.0) {
            return look
        }
        val wave = 0.5 + 0.5 * sin(2.0 * PI * elapsedMs.toDouble() / spec.flickerPeriodMs.toDouble())
        val scale = 1.0 + spec.flickerAmount * (2.0 * wave - 1.0)
        return look.copy(brightness = (look.brightness * scale).roundToInt().coerceIn(1, 100))
    }

    private fun specFor(loop: AtmosphereLightingLoop): LoopSpec {
        return when (loop) {
            AtmosphereLightingLoop.Fire -> LoopSpec(
                periodMs = 2_400,
                flickerPeriodMs = 280,
                flickerAmount = 0.14,
                waypoints = listOf(
                    look(196, 65, 10, 42),
                    look(224, 90, 28, 60),
                    look(255, 154, 26, 78),
                    look(232, 106, 18, 52),
                ),
            )
            AtmosphereLightingLoop.Water -> LoopSpec(
                periodMs = 3_600,
                waypoints = listOf(
                    look(22, 58, 95, 38),
                    look(31, 111, 139, 54),
                    look(60, 166, 200, 64),
                    look(42, 122, 154, 48),
                ),
            )
            AtmosphereLightingLoop.Candle -> LoopSpec(
                periodMs = 2_000,
                flickerPeriodMs = 180,
                flickerAmount = 0.10,
                waypoints = listOf(
                    look(227, 155, 90, 40),
                    look(240, 194, 122, 54),
                    look(224, 90, 28, 46),
                    look(227, 155, 90, 44),
                ),
            )
            AtmosphereLightingLoop.Storm -> LoopSpec(
                periodMs = 3_200,
                flickerPeriodMs = 420,
                flickerAmount = 0.08,
                waypoints = listOf(
                    look(44, 51, 88, 28),
                    look(74, 78, 138, 48),
                    look(107, 122, 184, 40),
                    look(168, 184, 220, 62),
                ),
            )
            AtmosphereLightingLoop.Ice -> LoopSpec(
                periodMs = 3_800,
                waypoints = listOf(
                    look(168, 212, 232, 50),
                    look(232, 244, 255, 74),
                    look(126, 200, 224, 58),
                    look(197, 232, 245, 64),
                ),
            )
            AtmosphereLightingLoop.Arcane -> LoopSpec(
                periodMs = 2_800,
                waypoints = listOf(
                    look(74, 42, 255, 48),
                    look(123, 75, 255, 70),
                    look(196, 163, 255, 58),
                    look(90, 53, 208, 54),
                ),
            )
            AtmosphereLightingLoop.Forest -> LoopSpec(
                periodMs = 3_400,
                waypoints = listOf(
                    look(45, 74, 56, 36),
                    look(61, 107, 79, 50),
                    look(90, 143, 98, 58),
                    look(61, 107, 79, 44),
                ),
            )
            AtmosphereLightingLoop.Lava -> LoopSpec(
                periodMs = 1_800,
                flickerPeriodMs = 220,
                flickerAmount = 0.16,
                waypoints = listOf(
                    look(139, 26, 0, 52),
                    look(224, 90, 28, 82),
                    look(255, 154, 26, 96),
                    look(196, 30, 58, 70),
                ),
            )
        }
    }

    private fun look(red: Int, green: Int, blue: Int, brightness: Int): LightingLook {
        return LightingLook(
            powerOn = true,
            brightness = brightness,
            red = red,
            green = green,
            blue = blue,
        )
    }

    private data class LoopSpec(
        val periodMs: Int,
        val waypoints: List<LightingLook>,
        val flickerPeriodMs: Int = 0,
        val flickerAmount: Double = 0.0,
    )

    companion object {
        const val SAMPLE_INTERVAL_MS = 250
    }
}
