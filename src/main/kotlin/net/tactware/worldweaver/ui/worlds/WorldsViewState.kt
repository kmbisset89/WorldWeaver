package net.tactware.worldweaver.ui.worlds

import net.tactware.worldweaver.domain.World

internal sealed class WorldsViewState {
    data object Loading : WorldsViewState()

    data class Error(
        val message: String,
        val canRetry: Boolean,
    ) : WorldsViewState()

    data class Empty(
        val editor: WorldEditorState?,
        val isTransferring: Boolean = false,
    ) : WorldsViewState()

    data class Content(
        val worlds: List<World>,
        val activeWorldId: String?,
        val editor: WorldEditorState?,
        val pendingDelete: PendingDelete?,
        val blockDeleteReason: String?,
        val isTransferring: Boolean = false,
    ) : WorldsViewState()

    data class WorldEditorState(
        val worldId: String?,
        val name: String,
        val description: String,
        val nameError: String?,
    )

    data class PendingDelete(
        val worldId: String,
        val worldName: String,
    )
}
