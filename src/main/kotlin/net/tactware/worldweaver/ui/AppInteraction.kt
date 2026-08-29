package net.tactware.worldweaver.ui

import net.tactware.worldweaver.ui.navigation.Screen

internal sealed interface AppInteraction {
    data class ScreenSelected(val screen: Screen) : AppInteraction
    data object ThemeModeCycled : AppInteraction
    data object NotificationsToggled : AppInteraction
    data object NotificationsDismissed : AppInteraction
    data class NotificationSelected(val id: String) : AppInteraction
    data object SignOutSelected : AppInteraction
}
