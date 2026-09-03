package net.tactware.worldweaver.ui.factions

internal sealed interface FactionsInteraction {
    data object ScreenStarted : FactionsInteraction
    data object RetrySelected : FactionsInteraction
    data object CreateWorldSelected : FactionsInteraction
    data object NewFactionSelected : FactionsInteraction
    data class FactionSelected(val factionId: String) : FactionsInteraction
    data class FactionOpened(val factionId: String) : FactionsInteraction
    data class EditFactionSelected(val factionId: String) : FactionsInteraction
    data class DeleteFactionSelected(val factionId: String) : FactionsInteraction
    data object DeleteConfirmed : FactionsInteraction
    data object DeleteCancelled : FactionsInteraction
    data object BlockReasonDismissed : FactionsInteraction
    data class MemberRemoved(val membershipId: String) : FactionsInteraction
    data class EditorNameChanged(val name: String) : FactionsInteraction
    data class EditorDescriptionChanged(val description: String) : FactionsInteraction
    data class EditorGoalsChanged(val goals: String) : FactionsInteraction
    data class EditorNotesChanged(val notes: String) : FactionsInteraction
    data object EditorSaved : FactionsInteraction
    data object EditorDismissed : FactionsInteraction
}
