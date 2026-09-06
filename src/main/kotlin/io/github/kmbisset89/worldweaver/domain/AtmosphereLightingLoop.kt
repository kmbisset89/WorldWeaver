package io.github.kmbisset89.worldweaver.domain

/**
 * A looping table lighting cue that oscillates between colors until the GM stops it.
 */
internal enum class AtmosphereLightingLoop(
    val displayName: String,
) {
    Fire("Fire"),
    Water("Water"),
    Candle("Candle"),
    Storm("Storm"),
    Ice("Ice"),
    Arcane("Arcane"),
    Forest("Forest"),
    Lava("Lava"),
}
