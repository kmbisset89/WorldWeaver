package io.github.kmbisset89.worldweaver.ui

import io.github.kmbisset89.worldweaver.ui.navigation.Screen

internal sealed interface AppInteraction {
    data class ScreenSelected(val screen: Screen) : AppInteraction
    data object ThemeModeCycled : AppInteraction
    data object NavDensityToggled : AppInteraction
    data object SignOutSelected : AppInteraction
}
