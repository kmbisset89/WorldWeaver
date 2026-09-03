package io.github.kmbisset89.worldweaver.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Simple navigation state management for the desktop application.
 * Manages screen transitions and back stack.
 */
internal class NavigationState {
    var currentScreen by mutableStateOf(Screen.HOME)
        private set

    private val backStack = mutableListOf<Screen>()

    fun navigateTo(screen: Screen) {
        backStack.add(currentScreen)
        currentScreen = screen
    }

    fun goBack() {
        if (backStack.isNotEmpty()) {
            currentScreen = backStack.removeLast()
        }
    }

    fun navigateToRoot(screen: Screen) {
        backStack.clear()
        currentScreen = screen
    }

    fun canGoBack(): Boolean = backStack.isNotEmpty()

    fun peekBack(): Screen? = backStack.lastOrNull()
}
