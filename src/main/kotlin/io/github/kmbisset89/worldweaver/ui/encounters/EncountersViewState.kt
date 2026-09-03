package io.github.kmbisset89.worldweaver.ui.encounters

import io.github.kmbisset89.worldweaver.domain.BattleMap
import io.github.kmbisset89.worldweaver.domain.DeathSaves
import io.github.kmbisset89.worldweaver.domain.Encounter
import io.github.kmbisset89.worldweaver.domain.EncounterDifficulty
import io.github.kmbisset89.worldweaver.domain.EncounterParticipant
import io.github.kmbisset89.worldweaver.domain.EncounterParticipantSource
import io.github.kmbisset89.worldweaver.domain.EncounterStatus
import io.github.kmbisset89.worldweaver.domain.FifthEditionCondition
import io.github.kmbisset89.worldweaver.domain.GridCell
import io.github.kmbisset89.worldweaver.domain.GridDistance
import io.github.kmbisset89.worldweaver.domain.Location
import io.github.kmbisset89.worldweaver.ui.maps.BattleMapBoardToken
import io.github.kmbisset89.worldweaver.ui.maps.TerrainPaintKind

internal sealed class EncountersViewState {
    data object Loading : EncountersViewState()

    data class Error(
        val message: String,
        val canRetry: Boolean,
    ) : EncountersViewState()

    data object NoActiveWorld : EncountersViewState()

    data object NoActiveCampaign : EncountersViewState()

    data class Empty(
        val worldName: String,
        val campaignName: String,
        val setup: EncounterSetupState?,
    ) : EncountersViewState()

    data class Content(
        val worldName: String,
        val campaignName: String,
        val encounters: List<Encounter>,
        val selectedEncounter: Encounter?,
        val statusFilter: EncounterStatus?,
        val locationName: String?,
        val battleMapName: String?,
        val setup: EncounterSetupState?,
        val pendingDelete: PendingDelete?,
        val startWarning: String?,
    ) : EncountersViewState()

    data class Running(
        val worldName: String,
        val campaignName: String,
        val encounter: Encounter,
        val locationName: String?,
        val battleMapName: String?,
        val battleMap: BattleMap?,
        val initiativeOrder: List<EncounterParticipant>,
        val currentTurnParticipantId: String?,
        val selectedParticipantId: String?,
        val combatAmount: String,
        val availableConditions: List<FifthEditionCondition>,
        val deathSaves: DeathSaves?,
        val tokens: List<BattleMapBoardToken>,
        val selectedTokenName: String?,
        val unplacedTokenCount: Int,
        val movementSpeedText: String,
        val movementOrigin: GridCell?,
        val reachableCells: List<GridCell>,
        val measureEnabled: Boolean,
        val measureOrigin: GridCell?,
        val measureDestination: GridCell?,
        val measureDistance: GridDistance?,
        val fogPaintEnabled: Boolean,
        val fogRevealBrush: Boolean,
        val terrainPaint: TerrainPaintKind?,
        val itemDropEnabled: Boolean,
        val itemNameText: String,
        val selectedItemId: String?,
        val selectedItemName: String?,
        val playerViewOpen: Boolean,
        val pendingEnd: PendingEnd?,
    ) : EncountersViewState()

    data class EncounterSetupState(
        val encounterId: String?,
        val name: String,
        val locationId: String?,
        val locationOptions: List<Location>,
        val battleMapId: String?,
        val battleMapOptions: List<BattleMap>,
        val difficulty: EncounterDifficulty,
        val notes: String,
        val participants: List<EncounterParticipant>,
        val namelessName: String,
        val namelessGroupCount: String,
        val namelessAsSwarm: Boolean,
        val rosterSearch: String,
        val worldPersonOptions: List<PersonOption>,
        val campaignPersonOptions: List<PersonOption>,
        val companionSuggestions: List<CompanionSuggestion>,
        val nameError: String?,
        val missingInitiativeCount: Int,
    )

    data class PersonOption(
        val id: String,
        val name: String,
        val source: EncounterParticipantSource,
        val alreadyAdded: Boolean,
        val initiativeBonus: Int,
        val armorClass: Int,
        val hitPoints: Int,
        val maxHitPoints: Int,
        val temporaryHitPoints: Int,
    )

    data class CompanionSuggestion(
        val ownerPersonId: String,
        val ownerSource: EncounterParticipantSource,
        val ownerName: String,
        val companions: List<PersonOption>,
    )

    data class PendingDelete(
        val encounterId: String,
        val encounterName: String,
    )

    data class PendingEnd(
        val encounterId: String,
        val encounterName: String,
        val outcomeNote: String,
    )
}
