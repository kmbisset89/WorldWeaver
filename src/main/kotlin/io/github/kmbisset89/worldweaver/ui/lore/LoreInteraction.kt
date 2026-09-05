package io.github.kmbisset89.worldweaver.ui.lore

import io.github.kmbisset89.worldweaver.domain.LoreCategory

internal sealed interface LoreInteraction {
    data object ScreenStarted : LoreInteraction
    data object RetrySelected : LoreInteraction
    data object CreateWorldSelected : LoreInteraction
    data object NewLoreSelected : LoreInteraction
    data class LoreSelected(val loreId: String) : LoreInteraction
    data class LoreOpened(val loreId: String) : LoreInteraction
    data class EditLoreSelected(val loreId: String) : LoreInteraction
    data class DeleteLoreSelected(val loreId: String) : LoreInteraction
    data object DeleteConfirmed : LoreInteraction
    data object DeleteCancelled : LoreInteraction
    data class CategoryFilterSelected(val category: LoreCategory?) : LoreInteraction
    data class RelatedLoreSelected(val loreId: String) : LoreInteraction
    data class ObservedOnSelected(val observanceId: String) : LoreInteraction
    data class HintRevealToggled(val secretId: String, val hintId: String) : LoreInteraction
    data class EditorTitleChanged(val title: String) : LoreInteraction
    data class EditorContentChanged(val content: String) : LoreInteraction
    data class EditorCategorySelected(val category: LoreCategory) : LoreInteraction
    data class EditorTagsChanged(val tags: String) : LoreInteraction
    data class EditorRelatedToggled(val loreId: String) : LoreInteraction
    data class EditorLocationSelected(val locationId: String?) : LoreInteraction
    data class EditorCharacterSelected(val characterId: String?) : LoreInteraction
    data object EditorSecretAdded : LoreInteraction
    data class EditorSecretRemoved(val index: Int) : LoreInteraction
    data class EditorSecretTitleChanged(val index: Int, val title: String) : LoreInteraction
    data class EditorSecretBodyChanged(val index: Int, val secret: String) : LoreInteraction
    data class EditorHintAdded(val secretIndex: Int) : LoreInteraction
    data class EditorHintRemoved(val secretIndex: Int, val hintIndex: Int) : LoreInteraction
    data class EditorHintTextChanged(
        val secretIndex: Int,
        val hintIndex: Int,
        val text: String,
    ) : LoreInteraction
    data object EditorSaved : LoreInteraction
    data object EditorDismissed : LoreInteraction
}
