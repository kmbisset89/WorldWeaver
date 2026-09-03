package io.github.kmbisset89.worldweaver.ui.sheet

internal sealed interface CharacterSheetInteraction {
    data class SheetOpened(
        val key: CharacterSheetViewState.PersonKey,
    ) : CharacterSheetInteraction

    data object UnavailableOpened : CharacterSheetInteraction

    data object SheetDismissed : CharacterSheetInteraction

    data object RetrySelected : CharacterSheetInteraction

    data class DeathSaveSuccessesSelected(
        val count: Int,
    ) : CharacterSheetInteraction

    data class DeathSaveFailuresSelected(
        val count: Int,
    ) : CharacterSheetInteraction

    data class DyingSelected(
        val count: Int,
    ) : CharacterSheetInteraction

    data class WoundedSelected(
        val count: Int,
    ) : CharacterSheetInteraction

    data object EditSelected : CharacterSheetInteraction
}
