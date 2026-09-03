package io.github.kmbisset89.worldweaver.ui.sheet

internal sealed interface CharacterSheetViewEffect {
    data class OpenEditor(
        val key: CharacterSheetViewState.PersonKey,
    ) : CharacterSheetViewEffect
}
