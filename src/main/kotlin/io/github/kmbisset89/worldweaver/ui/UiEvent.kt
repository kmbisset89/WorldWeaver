package io.github.kmbisset89.worldweaver.ui

/**
 * One-shot UI feedback events consumed by the root snackbar host.
 */
internal sealed class UiEvent {
    abstract val message: String

    data class Success(override val message: String) : UiEvent()
    data class Error(override val message: String) : UiEvent()
    data class Info(override val message: String) : UiEvent()
}
