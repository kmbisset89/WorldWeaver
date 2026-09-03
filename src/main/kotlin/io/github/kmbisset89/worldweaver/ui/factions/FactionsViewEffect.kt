package io.github.kmbisset89.worldweaver.ui.factions

internal sealed interface FactionsViewEffect {
    data object OpenWorlds : FactionsViewEffect
}
