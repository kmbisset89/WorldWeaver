package io.github.kmbisset89.worldweaver.domain

internal enum class CelestialMoonPhase(
    val displayName: String,
) {
    New("New"),
    WaxingCrescent("Waxing crescent"),
    FirstQuarter("First quarter"),
    WaxingGibbous("Waxing gibbous"),
    Full("Full"),
    WaningGibbous("Waning gibbous"),
    LastQuarter("Last quarter"),
    WaningCrescent("Waning crescent"),
    ;
}
