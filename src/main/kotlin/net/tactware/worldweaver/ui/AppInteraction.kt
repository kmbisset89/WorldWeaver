package net.tactware.worldweaver.ui

import net.tactware.worldweaver.ui.navigation.Screen

internal sealed interface AppInteraction {
    data class ScreenSelected(val screen: Screen) : AppInteraction
    data object ThemeModeCycled : AppInteraction
    data object NavDensityToggled : AppInteraction
    data object SignOutSelected : AppInteraction
}
