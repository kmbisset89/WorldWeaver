package io.github.kmbisset89.worldweaver.ui.worlds

internal sealed interface WorldsViewEffect {
    data object OpenOneShotWizard : WorldsViewEffect
    data class Exported(val worldName: String) : WorldsViewEffect
    data class Imported(val worldName: String) : WorldsViewEffect
    data class Failed(val message: String) : WorldsViewEffect
}
