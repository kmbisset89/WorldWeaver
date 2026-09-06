package io.github.kmbisset89.worldweaver.domain

import kotlin.math.pow

/**
 * Converts sRGB 0–255 channels to CIE xy chromaticity for Hue CLIP v2 light PUTs.
 */
internal class HueRgbXyTransformer {
    data class Xy(
        val x: Double,
        val y: Double,
    )

    fun transform(red: Int, green: Int, blue: Int): Xy {
        val r = gamma(red)
        val g = gamma(green)
        val b = gamma(blue)
        val x = r * 0.664511 + g * 0.154324 + b * 0.162028
        val y = r * 0.283881 + g * 0.668433 + b * 0.047685
        val z = r * 0.000088 + g * 0.072310 + b * 0.986039
        val sum = x + y + z
        if (sum <= 0.0) {
            return Xy(D65_X, D65_Y)
        }
        return Xy(x = roundChromaticity(x / sum), y = roundChromaticity(y / sum))
    }

    private fun gamma(channel: Int): Double {
        val value = channel.coerceIn(0, 255) / 255.0
        return if (value > 0.04045) {
            ((value + 0.055) / 1.055).pow(2.4)
        } else {
            value / 12.92
        }
    }

    private fun roundChromaticity(value: Double): Double {
        return (value * 10_000.0).toInt() / 10_000.0
    }

    private companion object {
        const val D65_X = 0.3127
        const val D65_Y = 0.3290
    }
}
