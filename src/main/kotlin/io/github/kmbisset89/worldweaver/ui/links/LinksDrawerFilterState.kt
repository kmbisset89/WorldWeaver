package io.github.kmbisset89.worldweaver.ui.links

import io.github.kmbisset89.worldweaver.domain.RelationshipType

internal data class LinksDrawerFilterState(
    val showIsolates: Boolean,
    val showMemberships: Boolean,
    val enabledRelationshipTypes: Set<RelationshipType>,
)
