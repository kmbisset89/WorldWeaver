package io.github.kmbisset89.worldweaver.ui.locations

import io.github.kmbisset89.worldweaver.domain.LocationType

internal sealed interface LocationsInteraction {
    data object ScreenStarted : LocationsInteraction
    data object RetrySelected : LocationsInteraction
    data object CreateWorldSelected : LocationsInteraction
    data object NewLocationSelected : LocationsInteraction
    data class LocationSelected(val locationId: String) : LocationsInteraction
    data class LocationOpened(val locationId: String) : LocationsInteraction
    data class BreadcrumbSelected(val locationId: String) : LocationsInteraction
    data class EditLocationSelected(val locationId: String) : LocationsInteraction
    data class DeleteLocationSelected(val locationId: String) : LocationsInteraction
    data object DeleteConfirmed : LocationsInteraction
    data object DeleteCancelled : LocationsInteraction
    data object BlockReasonDismissed : LocationsInteraction
    data class SearchQueryChanged(val query: String) : LocationsInteraction
    data class TypeFilterSelected(val type: LocationType?) : LocationsInteraction
    data class EditorNameChanged(val name: String) : LocationsInteraction
    data class EditorTypeSelected(val type: LocationType) : LocationsInteraction
    data class EditorParentSelected(val parentLocationId: String?) : LocationsInteraction
    data class EditorDescriptionChanged(val description: String) : LocationsInteraction
    data class EditorClimateChanged(val climate: String) : LocationsInteraction
    data class EditorTerrainChanged(val terrain: String) : LocationsInteraction
    data class EditorGovernmentChanged(val government: String) : LocationsInteraction
    data class EditorLandmarksChanged(val landmarks: String) : LocationsInteraction
    data class EditorHistoryChanged(val history: String) : LocationsInteraction
    data class EditorNotesChanged(val notes: String) : LocationsInteraction
    data object EditorSaved : LocationsInteraction
    data object EditorDismissed : LocationsInteraction
    data class OverlayPartyPresenceChanged(val hasPartyPresence: Boolean) : LocationsInteraction
    data class OverlayNotesChanged(val notes: String) : LocationsInteraction
    data object OverlaySaved : LocationsInteraction
    data class AttachedLoreSelected(val loreId: String) : LocationsInteraction
    data class AttachedQuestSelected(val questId: String) : LocationsInteraction
    data class VoiceClipAttached(val path: String) : LocationsInteraction
    data object VoiceClipRecordToggled : LocationsInteraction
    data object VoiceClipPlayToggled : LocationsInteraction
    data object VoiceClipRemoved : LocationsInteraction
}
