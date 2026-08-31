package net.tactware.worldweaver.ui.worlds

internal sealed interface WorldsInteraction {
    data object ScreenStarted : WorldsInteraction
    data object RetrySelected : WorldsInteraction
    data object NewWorldSelected : WorldsInteraction
    data class WorldSelected(val worldId: String) : WorldsInteraction
    data class EditWorldSelected(val worldId: String) : WorldsInteraction
    data class DeleteWorldSelected(val worldId: String) : WorldsInteraction
    data object DeleteConfirmed : WorldsInteraction
    data object DeleteCancelled : WorldsInteraction
    data class EditorNameChanged(val name: String) : WorldsInteraction
    data class EditorDescriptionChanged(val description: String) : WorldsInteraction
    data object EditorSaved : WorldsInteraction
    data object EditorDismissed : WorldsInteraction
    data object BlockReasonDismissed : WorldsInteraction
    data class ExportWorldSelected(val worldId: String) : WorldsInteraction
    data class ExportPathChosen(val worldId: String, val path: String) : WorldsInteraction
    data object ImportWorldSelected : WorldsInteraction
    data class ImportPathChosen(val path: String) : WorldsInteraction
}
