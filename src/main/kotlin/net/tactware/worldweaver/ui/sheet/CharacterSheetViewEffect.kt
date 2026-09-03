package net.tactware.worldweaver.ui.sheet

internal sealed interface CharacterSheetViewEffect {
    data class OpenEditor(
        val key: CharacterSheetViewState.PersonKey,
    ) : CharacterSheetViewEffect
}
