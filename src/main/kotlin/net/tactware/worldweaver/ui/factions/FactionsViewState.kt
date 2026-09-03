package net.tactware.worldweaver.ui.factions

import net.tactware.worldweaver.domain.Faction

internal sealed class FactionsViewState {
    data object Loading : FactionsViewState()

    data class Error(
        val message: String,
        val canRetry: Boolean,
    ) : FactionsViewState()

    data object NoActiveWorld : FactionsViewState()

    data class Empty(
        val worldName: String,
        val editor: FactionEditorState?,
    ) : FactionsViewState()

    data class Content(
        val worldName: String,
        val factions: List<Faction>,
        val selectedFaction: Faction?,
        val members: List<MemberRow>,
        val editor: FactionEditorState?,
        val pendingDelete: PendingDelete?,
        val blockDeleteReason: String?,
    ) : FactionsViewState()

    data class MemberRow(
        val membershipId: String,
        val personName: String,
        val role: String,
        val notes: String,
    )

    data class FactionEditorState(
        val factionId: String?,
        val name: String,
        val description: String,
        val goals: String,
        val notes: String,
        val nameError: String?,
    )

    data class PendingDelete(
        val factionId: String,
        val factionName: String,
    )
}
