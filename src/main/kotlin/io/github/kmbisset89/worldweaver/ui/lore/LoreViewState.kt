package io.github.kmbisset89.worldweaver.ui.lore

import io.github.kmbisset89.worldweaver.domain.Location
import io.github.kmbisset89.worldweaver.domain.Lore
import io.github.kmbisset89.worldweaver.domain.LoreCategory

internal sealed class LoreViewState {
    data object Loading : LoreViewState()

    data class Error(
        val message: String,
        val canRetry: Boolean,
    ) : LoreViewState()

    data object NoActiveWorld : LoreViewState()

    data class Empty(
        val worldName: String,
        val editor: LoreEditorState?,
    ) : LoreViewState()

    data class Content(
        val worldName: String,
        val groups: List<LoreGroup>,
        val selectedLore: Lore?,
        val relatedLinks: List<RelatedLink>,
        val observedOn: List<ObservedOnLink>,
        val attachedLocationName: String?,
        val attachedCharacterName: String?,
        val categoryFilter: LoreCategory?,
        val editor: LoreEditorState?,
        val pendingDelete: PendingDelete?,
    ) : LoreViewState()

    data class LoreGroup(
        val category: LoreCategory,
        val entries: List<Lore>,
    )

    data class RelatedLink(
        val loreId: String,
        val title: String,
        val missing: Boolean,
    )

    data class ObservedOnLink(
        val observanceId: String,
        val name: String,
        val dateLabel: String,
    )

    data class LoreEditorState(
        val loreId: String?,
        val title: String,
        val content: String,
        val category: LoreCategory,
        val tagsText: String,
        val relatedEntryIds: List<String>,
        val relatedOptions: List<Lore>,
        val locationId: String?,
        val locationOptions: List<Location>,
        val characterId: String?,
        val characterOptions: List<CharacterOption>,
        val secrets: List<SecretEditorState>,
        val titleError: String?,
        val contentError: String?,
    )

    data class SecretEditorState(
        val id: String,
        val title: String,
        val secret: String,
        val hints: List<HintEditorState>,
    )

    data class HintEditorState(
        val id: String,
        val text: String,
        val revealed: Boolean,
    )

    data class CharacterOption(
        val id: String,
        val name: String,
    )

    data class PendingDelete(
        val loreId: String,
        val loreTitle: String,
    )
}
