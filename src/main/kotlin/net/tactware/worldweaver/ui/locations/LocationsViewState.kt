package net.tactware.worldweaver.ui.locations

import net.tactware.worldweaver.domain.Location
import net.tactware.worldweaver.domain.LocationType

internal sealed class LocationsViewState {
    data object Loading : LocationsViewState()

    data class Error(
        val message: String,
        val canRetry: Boolean,
    ) : LocationsViewState()

    data object NoActiveWorld : LocationsViewState()

    data class Empty(
        val worldName: String,
        val editor: LocationEditorState?,
    ) : LocationsViewState()

    data class Content(
        val worldName: String,
        val campaignName: String?,
        val locations: List<Location>,
        val visibleTree: List<LocationTreeNode>,
        val selectedLocation: Location?,
        val breadcrumbs: List<Location>,
        val searchQuery: String,
        val typeFilter: LocationType?,
        val overlay: OverlayState?,
        val attachedLore: List<AttachedLore>,
        val attachedQuests: List<AttachedQuest>,
        val voiceClipPath: String?,
        val isRecordingVoice: Boolean,
        val isPlayingVoice: Boolean,
        val editor: LocationEditorState?,
        val pendingDelete: PendingDelete?,
        val blockDeleteReason: String?,
    ) : LocationsViewState()

    data class LocationTreeNode(
        val location: Location,
        val children: List<LocationTreeNode>,
    )

    data class LocationEditorState(
        val locationId: String?,
        val type: LocationType,
        val parentLocationId: String?,
        val parentOptions: List<Location>,
        val name: String,
        val description: String,
        val climate: String,
        val terrain: String,
        val government: String,
        val landmarksText: String,
        val history: String,
        val notes: String,
        val nameError: String?,
        val parentError: String?,
    )

    data class OverlayState(
        val campaignName: String,
        val hasPartyPresence: Boolean,
        val notes: String,
    )

    data class AttachedLore(
        val loreId: String,
        val title: String,
    )

    data class AttachedQuest(
        val questId: String,
        val title: String,
    )

    data class PendingDelete(
        val locationId: String,
        val locationName: String,
    )
}
