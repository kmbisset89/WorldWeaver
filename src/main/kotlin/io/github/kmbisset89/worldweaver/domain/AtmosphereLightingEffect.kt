package io.github.kmbisset89.worldweaver.domain

/**
 * A one-shot table lighting cue that plays over the current look, then restores it.
 */
internal enum class AtmosphereLightingEffect(
    val displayName: String,
) {
    Lightning("Lightning"),
    CriticalStrike("Critical strike"),
    Fireball("Fireball"),
    Heal("Heal"),
    HolyLight("Holy light"),
    Poison("Poison"),
    Darkness("Darkness"),
    ArcaneBurst("Arcane burst"),
}
