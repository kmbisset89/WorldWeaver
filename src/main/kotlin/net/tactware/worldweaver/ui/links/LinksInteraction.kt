package net.tactware.worldweaver.ui.links

import net.tactware.worldweaver.domain.RelationshipType

internal sealed interface LinksInteraction {
    data object ScreenStarted : LinksInteraction
    data object RetrySelected : LinksInteraction
    data object CreateWorldSelected : LinksInteraction
    data class NodeSelected(val nodeId: String) : LinksInteraction
    data class NodeOpened(val nodeId: String) : LinksInteraction
    data object SelectionCleared : LinksInteraction
    data class SearchQueryChanged(val query: String) : LinksInteraction
    data object IsolateVisibilityToggled : LinksInteraction
    data object MembershipEdgesToggled : LinksInteraction
    data class RelationshipTypeFilterToggled(val type: RelationshipType) : LinksInteraction
}
