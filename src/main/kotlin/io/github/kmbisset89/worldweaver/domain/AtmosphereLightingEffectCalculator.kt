package io.github.kmbisset89.worldweaver.domain

/**
 * Builds timed color pulses for a one-shot table lighting cue, ending on the current look.
 */
internal class AtmosphereLightingEffectCalculator {
    data class Frame(
        val holdMs: Long,
        val look: LightingLook,
        val transitionMs: Int = 0,
    )

    fun calculate(
        effect: AtmosphereLightingEffect,
        current: LightingLook,
    ): List<Frame> {
        val frames = when (effect) {
            AtmosphereLightingEffect.Lightning -> lightning(current)
            AtmosphereLightingEffect.CriticalStrike -> criticalStrike(current)
            AtmosphereLightingEffect.Fireball -> fireball()
            AtmosphereLightingEffect.Heal -> heal()
            AtmosphereLightingEffect.HolyLight -> holyLight()
            AtmosphereLightingEffect.Poison -> poison()
            AtmosphereLightingEffect.Darkness -> darkness(current)
            AtmosphereLightingEffect.ArcaneBurst -> arcaneBurst()
        }
        return frames + Frame(holdMs = 0L, look = current, transitionMs = restoreMs(effect))
    }

    private fun lightning(current: LightingLook): List<Frame> {
        return listOf(
            flash(WHITE, brightness = 100, holdMs = 40L),
            dimmed(current, brightness = 8, holdMs = 55L),
            flash(WHITE, brightness = 100, holdMs = 25L),
            dimmed(current, brightness = 12, holdMs = 70L),
            flash(WHITE, brightness = 85, holdMs = 35L),
        )
    }

    private fun criticalStrike(current: LightingLook): List<Frame> {
        return listOf(
            flash(STRIKE_RED, brightness = 100, holdMs = 90L),
            dimmed(current.copy(red = STRIKE_RED.red, green = STRIKE_RED.green, blue = STRIKE_RED.blue), 55, 80L),
            flash(STRIKE_FLASH, brightness = 100, holdMs = 90L),
        )
    }

    private fun fireball(): List<Frame> {
        return listOf(
            flash(EMBER, brightness = 100, holdMs = 80L, transitionMs = 40),
            flash(FLAME, brightness = 100, holdMs = 110L, transitionMs = 60),
            flash(EMBER, brightness = 75, holdMs = 80L, transitionMs = 50),
        )
    }

    private fun heal(): List<Frame> {
        return listOf(
            flash(HEAL_SOFT, brightness = 70, holdMs = 220L, transitionMs = 180),
            flash(HEAL_BRIGHT, brightness = 92, holdMs = 200L, transitionMs = 140),
        )
    }

    private fun holyLight(): List<Frame> {
        return listOf(
            flash(HALO, brightness = 100, holdMs = 280L, transitionMs = 160),
            flash(WHITE, brightness = 100, holdMs = 140L, transitionMs = 80),
        )
    }

    private fun poison(): List<Frame> {
        return listOf(
            flash(VENOM, brightness = 82, holdMs = 100L),
            flash(VENOM_DARK, brightness = 48, holdMs = 120L),
            flash(VENOM_BRIGHT, brightness = 90, holdMs = 100L),
        )
    }

    private fun darkness(current: LightingLook): List<Frame> {
        return listOf(
            Frame(
                holdMs = 480L,
                look = current.copy(powerOn = true, brightness = 3),
                transitionMs = 180,
            ),
        )
    }

    private fun arcaneBurst(): List<Frame> {
        return listOf(
            flash(ARCANE, brightness = 100, holdMs = 70L),
            flash(ARCANE_FLASH, brightness = 100, holdMs = 80L),
            flash(ARCANE_DEEP, brightness = 88, holdMs = 70L),
        )
    }

    private fun flash(
        color: Rgb,
        brightness: Int,
        holdMs: Long,
        transitionMs: Int = 0,
    ): Frame {
        return Frame(
            holdMs = holdMs,
            look = LightingLook(
                powerOn = true,
                brightness = brightness.coerceIn(1, 100),
                red = color.red,
                green = color.green,
                blue = color.blue,
            ),
            transitionMs = transitionMs,
        )
    }

    private fun dimmed(current: LightingLook, brightness: Int, holdMs: Long): Frame {
        return Frame(
            holdMs = holdMs,
            look = current.copy(
                powerOn = true,
                brightness = brightness.coerceIn(1, 100),
            ),
            transitionMs = 0,
        )
    }

    private fun restoreMs(effect: AtmosphereLightingEffect): Int {
        return when (effect) {
            AtmosphereLightingEffect.Lightning -> 280
            AtmosphereLightingEffect.CriticalStrike -> 350
            AtmosphereLightingEffect.Fireball -> 400
            AtmosphereLightingEffect.Heal -> 520
            AtmosphereLightingEffect.HolyLight -> 620
            AtmosphereLightingEffect.Poison -> 360
            AtmosphereLightingEffect.Darkness -> 900
            AtmosphereLightingEffect.ArcaneBurst -> 360
        }
    }

    private data class Rgb(
        val red: Int,
        val green: Int,
        val blue: Int,
    )

    private companion object {
        val WHITE = Rgb(255, 255, 255)
        val STRIKE_RED = Rgb(196, 30, 58)
        val STRIKE_FLASH = Rgb(255, 42, 42)
        val EMBER = Rgb(224, 90, 28)
        val FLAME = Rgb(255, 154, 26)
        val HEAL_SOFT = Rgb(110, 224, 138)
        val HEAL_BRIGHT = Rgb(168, 240, 176)
        val HALO = Rgb(255, 244, 194)
        val VENOM = Rgb(107, 143, 58)
        val VENOM_DARK = Rgb(61, 92, 26)
        val VENOM_BRIGHT = Rgb(143, 191, 64)
        val ARCANE = Rgb(123, 75, 255)
        val ARCANE_FLASH = Rgb(196, 163, 255)
        val ARCANE_DEEP = Rgb(74, 42, 255)
    }
}
