package io.github.kmbisset89.worldweaver.ui

internal sealed interface AppViewEffect {
    data object ExitRequested : AppViewEffect
}
