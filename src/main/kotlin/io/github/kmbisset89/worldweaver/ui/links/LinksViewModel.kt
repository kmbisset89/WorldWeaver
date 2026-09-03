package io.github.kmbisset89.worldweaver.ui.links

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import io.github.kmbisset89.worldweaver.core.AppCoroutineScope
import io.github.kmbisset89.worldweaver.domain.ObserveRelationshipWebUseCase
import io.github.kmbisset89.worldweaver.domain.PersonKind
import io.github.kmbisset89.worldweaver.domain.PersonRef
import io.github.kmbisset89.worldweaver.domain.RelationshipType
import io.github.kmbisset89.worldweaver.domain.RelationshipWeb
import io.github.kmbisset89.worldweaver.domain.RelationshipWebFactory
import io.github.kmbisset89.worldweaver.domain.RelationshipWebSnapshot
import io.github.kmbisset89.worldweaver.ui.characters.CharactersViewState
import io.github.kmbisset89.worldweaver.ui.characters.PersonMembership

internal class LinksViewModel(
    private val appScope: AppCoroutineScope,
    private val observeRelationshipWeb: ObserveRelationshipWebUseCase,
    private val relationshipWebFactory: RelationshipWebFactory,
    private val layoutFactory: RelationshipWebLayoutFactory,
) {
    private val _state = MutableStateFlow<LinksViewState>(LinksViewState.Loading)
    val state: StateFlow<LinksViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<LinksViewEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<LinksViewEffect> = _effects.asSharedFlow()

    private var observeJob: Job? = null
    private var latestSnapshot: RelationshipWebSnapshot? = null
    private var selectedNodeId: String? = null
    private var searchQuery: String = ""
    private var showIsolates: Boolean = false
    private var showMemberships: Boolean = true
    private var enabledRelationshipTypes: Set<RelationshipType> = RelationshipType.entries.toSet()

    init {
        observe()
    }

    fun onInteraction(interaction: LinksInteraction) {
        when (interaction) {
            LinksInteraction.ScreenStarted -> Unit
            LinksInteraction.RetrySelected -> observe()
            LinksInteraction.CreateWorldSelected -> {
                _effects.tryEmit(LinksViewEffect.OpenWorlds)
            }
            is LinksInteraction.NodeSelected -> selectNode(interaction.nodeId)
            is LinksInteraction.NodeOpened -> openNode(interaction.nodeId)
            LinksInteraction.SelectionCleared -> selectNode(null)
            is LinksInteraction.SearchQueryChanged -> {
                searchQuery = interaction.query
                render()
            }
            LinksInteraction.IsolateVisibilityToggled -> {
                showIsolates = !showIsolates
                render()
            }
            LinksInteraction.MembershipEdgesToggled -> {
                showMemberships = !showMemberships
                render()
            }
            is LinksInteraction.RelationshipTypeFilterToggled -> {
                enabledRelationshipTypes = toggledTypes(interaction.type)
                render()
            }
        }
    }

    private fun observe() {
        observeJob?.cancel()
        _state.value = LinksViewState.Loading
        observeJob = appScope.scope.launch {
            observeRelationshipWeb()
                .catch { error ->
                    _state.value = LinksViewState.Error(
                        message = error.message ?: "Could not load the relationship web",
                        canRetry = true,
                    )
                }
                .collect { snapshot ->
                    latestSnapshot = snapshot
                    render()
                }
        }
    }

    private fun render() {
        val snapshot = latestSnapshot ?: return
        val world = snapshot.details.world
        if (world == null) {
            selectedNodeId = null
            _state.value = LinksViewState.NoActiveWorld
            return
        }
        val filtered = relationshipWebFactory.filter(
            web = snapshot.web,
            includeMemberships = showMemberships,
            enabledRelationshipTypes = enabledRelationshipTypes,
            includeIsolates = showIsolates,
        )
        val nodes = filtered.nodes.map(::toNode)
        val edges = filtered.edges.map(::toEdge)
        val visibleIds = nodes.map { it.id }.toSet()
        if (selectedNodeId !in visibleIds) {
            selectedNodeId = null
        }
        if (nodes.isEmpty()) {
            _state.value = LinksViewState.Empty(
                worldName = world.name,
                campaignName = snapshot.details.campaign?.name,
                searchQuery = searchQuery,
                showIsolates = showIsolates,
                showMemberships = showMemberships,
                enabledRelationshipTypes = enabledRelationshipTypes,
                hiddenByFilters = snapshot.web.nodes.isNotEmpty() || snapshot.web.edges.isNotEmpty(),
            )
            return
        }
        val inspectorNode = nodes.firstOrNull { it.id == selectedNodeId }
        _state.value = LinksViewState.Content(
            worldName = world.name,
            campaignName = snapshot.details.campaign?.name,
            nodes = nodes,
            edges = edges,
            positions = layoutFactory.create(nodes, edges),
            selectedNodeId = selectedNodeId,
            inspector = inspectorNode?.let { node -> inspectorFor(node, nodes, edges) },
            searchQuery = searchQuery,
            showIsolates = showIsolates,
            showMemberships = showMemberships,
            enabledRelationshipTypes = enabledRelationshipTypes,
        )
    }

    private fun selectNode(nodeId: String?) {
        if (selectedNodeId == nodeId) {
            return
        }
        selectedNodeId = nodeId
        render()
    }

    private fun openNode(nodeId: String) {
        val node = (_state.value as? LinksViewState.Content)
            ?.nodes
            ?.firstOrNull { it.id == nodeId }
            ?: return
        when (node.kind) {
            LinksViewState.NodeKind.Faction -> {
                val factionId = node.factionId ?: return
                _effects.tryEmit(LinksViewEffect.OpenFaction(factionId))
            }
            LinksViewState.NodeKind.WorldPerson,
            LinksViewState.NodeKind.CampaignPerson,
            -> {
                val personId = node.personId ?: return
                val membership = node.personMembership ?: return
                _effects.tryEmit(
                    LinksViewEffect.OpenPerson(
                        CharactersViewState.PersonKey(
                            membership = membership,
                            id = personId,
                        )
                    )
                )
            }
        }
    }

    private fun toNode(node: RelationshipWeb.Node): LinksViewState.Node {
        return when (node) {
            is RelationshipWeb.Node.Person -> LinksViewState.Node(
                id = node.id,
                name = node.name,
                kind = when (node.ref) {
                    is PersonRef.World -> LinksViewState.NodeKind.WorldPerson
                    is PersonRef.Campaign -> LinksViewState.NodeKind.CampaignPerson
                },
                personKind = node.kind,
                personMembership = when (node.ref) {
                    is PersonRef.World -> PersonMembership.WorldLibrary
                    is PersonRef.Campaign -> PersonMembership.ThisCampaign
                },
                personId = node.ref.id,
                factionId = null,
            )
            is RelationshipWeb.Node.Faction -> LinksViewState.Node(
                id = node.id,
                name = node.name,
                kind = LinksViewState.NodeKind.Faction,
                personKind = null,
                personMembership = null,
                personId = null,
                factionId = node.factionId,
            )
        }
    }

    private fun toEdge(edge: RelationshipWeb.Edge): LinksViewState.Edge {
        return when (edge) {
            is RelationshipWeb.Edge.Relationship -> LinksViewState.Edge(
                id = edge.id,
                fromId = edge.fromId,
                toId = edge.toId,
                kind = LinksViewState.EdgeKind.Relationship,
                label = edgeLabel(edge),
                relationshipType = edge.type,
            )
            is RelationshipWeb.Edge.Membership -> LinksViewState.Edge(
                id = edge.id,
                fromId = edge.fromId,
                toId = edge.toId,
                kind = LinksViewState.EdgeKind.Membership,
                label = edge.label,
                relationshipType = null,
            )
        }
    }

    private fun edgeLabel(edge: RelationshipWeb.Edge.Relationship): String {
        val lean = edge.factionLeanName?.takeIf { it.isNotBlank() }
        return if (lean == null) {
            edge.type.displayName
        } else {
            "${edge.type.displayName} · $lean"
        }
    }

    private fun inspectorFor(
        node: LinksViewState.Node,
        nodes: List<LinksViewState.Node>,
        edges: List<LinksViewState.Edge>,
    ): LinksViewState.Inspector {
        val names = nodes.associate { it.id to it.name }
        val incident = edges
            .filter { it.fromId == node.id || it.toId == node.id }
            .map { edge ->
                LinksViewState.InspectorEdge(
                    id = edge.id,
                    label = inspectorLabel(node.id, edge, names),
                )
            }
            .sortedBy { it.label.lowercase() }
        return LinksViewState.Inspector(
            nodeId = node.id,
            name = node.name,
            subtitle = nodeSubtitle(node),
            edges = incident,
        )
    }

    private fun inspectorLabel(
        nodeId: String,
        edge: LinksViewState.Edge,
        names: Map<String, String>,
    ): String {
        val otherId = if (edge.fromId == nodeId) edge.toId else edge.fromId
        val otherName = names[otherId] ?: "Unknown"
        return when (edge.kind) {
            LinksViewState.EdgeKind.Membership -> {
                if (edge.fromId == nodeId) {
                    val role = edge.label.takeIf { it.isNotBlank() && it != "Member" }
                    if (role == null) "Member of $otherName" else "Member of $otherName ($role)"
                } else {
                    val role = edge.label.takeIf { it.isNotBlank() && it != "Member" }
                    if (role == null) otherName else "$otherName ($role)"
                }
            }
            LinksViewState.EdgeKind.Relationship -> {
                if (edge.fromId == nodeId) {
                    "${edge.label} → $otherName"
                } else {
                    "$otherName → ${edge.label}"
                }
            }
        }
    }

    private fun nodeSubtitle(node: LinksViewState.Node): String {
        return when (node.kind) {
            LinksViewState.NodeKind.Faction -> "Faction"
            LinksViewState.NodeKind.WorldPerson -> worldPersonSubtitle(node.personKind)
            LinksViewState.NodeKind.CampaignPerson -> campaignPersonSubtitle(node.personKind)
        }
    }

    private fun worldPersonSubtitle(kind: PersonKind?): String {
        return when (kind) {
            PersonKind.PlayerCharacter -> "World library · PC"
            PersonKind.Monster -> "World library · Monster"
            PersonKind.Npc, null -> "World library · NPC"
        }
    }

    private fun campaignPersonSubtitle(kind: PersonKind?): String {
        return when (kind) {
            PersonKind.PlayerCharacter -> "Campaign · PC"
            PersonKind.Monster -> "Campaign · Monster"
            PersonKind.Npc, null -> "Campaign · NPC"
        }
    }

    private fun toggledTypes(type: RelationshipType): Set<RelationshipType> {
        return if (type in enabledRelationshipTypes) {
            enabledRelationshipTypes - type
        } else {
            enabledRelationshipTypes + type
        }
    }
}
