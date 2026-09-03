package net.tactware.worldweaver.ui.encounters

import net.tactware.worldweaver.domain.CombatState
import net.tactware.worldweaver.domain.EncounterDifficulty
import net.tactware.worldweaver.domain.EncounterParticipantSource
import net.tactware.worldweaver.domain.EncounterStatus
import net.tactware.worldweaver.domain.EncounterTurnDirection
import net.tactware.worldweaver.domain.FifthEditionCondition
import net.tactware.worldweaver.ui.maps.TerrainPaintKind

internal sealed interface EncountersInteraction {
    data object ScreenStarted : EncountersInteraction
    data object RetrySelected : EncountersInteraction
    data object CreateWorldSelected : EncountersInteraction
    data object CreateCampaignSelected : EncountersInteraction
    data object NewEncounterSelected : EncountersInteraction
    data class EncounterSelected(val encounterId: String) : EncountersInteraction
    data class EncounterOpened(val encounterId: String) : EncountersInteraction
    data class DeleteEncounterSelected(val encounterId: String) : EncountersInteraction
    data object DeleteConfirmed : EncountersInteraction
    data object DeleteCancelled : EncountersInteraction
    data class StatusFilterSelected(val status: EncounterStatus?) : EncountersInteraction
    data class LinkedLocationSelected(val locationId: String) : EncountersInteraction
    data class OpenMapSelected(val battleMapId: String) : EncountersInteraction
    data class StartEncounterSelected(val encounterId: String) : EncountersInteraction
    data class EndEncounterSelected(val encounterId: String) : EncountersInteraction
    data class EndOutcomeChanged(val outcomeNote: String) : EncountersInteraction
    data object EndConfirmed : EncountersInteraction
    data object EndCancelled : EncountersInteraction
    data object LibrarySelected : EncountersInteraction
    data class TurnAdvanced(
        val encounterId: String,
        val direction: EncounterTurnDirection,
    ) : EncountersInteraction
    data class ParticipantSelected(val participantId: String) : EncountersInteraction
    data class SheetSelected(
        val source: EncounterParticipantSource,
        val sourceId: String?,
    ) : EncountersInteraction
    data class CombatAmountChanged(val amount: String) : EncountersInteraction
    data class DamageApplied(val encounterId: String, val participantId: String) : EncountersInteraction
    data class HealApplied(val encounterId: String, val participantId: String) : EncountersInteraction
    data class TemporaryHitPointsSet(
        val encounterId: String,
        val participantId: String,
    ) : EncountersInteraction
    data class ConditionToggled(
        val encounterId: String,
        val participantId: String,
        val condition: FifthEditionCondition,
    ) : EncountersInteraction
    data class ConditionRemoved(
        val encounterId: String,
        val participantId: String,
        val condition: String,
    ) : EncountersInteraction
    data class CombatStateSelected(
        val encounterId: String,
        val participantId: String,
        val state: CombatState,
    ) : EncountersInteraction
    data class PlayerVisibilityToggled(
        val encounterId: String,
        val participantId: String,
        val visibleToPlayers: Boolean,
    ) : EncountersInteraction
    data class AttacksUsedSelected(
        val encounterId: String,
        val participantId: String,
        val count: Int,
    ) : EncountersInteraction
    data class AttacksAllowedSelected(
        val encounterId: String,
        val participantId: String,
        val count: Int,
    ) : EncountersInteraction
    data class BonusActionSet(
        val encounterId: String,
        val participantId: String,
        val used: Boolean,
    ) : EncountersInteraction
    data class ReactionSet(
        val encounterId: String,
        val participantId: String,
        val used: Boolean,
    ) : EncountersInteraction
    data class DeathSaveSuccessSelected(
        val participantId: String,
        val successes: Int,
    ) : EncountersInteraction
    data class DeathSaveFailureSelected(
        val participantId: String,
        val failures: Int,
    ) : EncountersInteraction
    data class InitiativeRollEntered(
        val encounterId: String,
        val participantId: String,
        val roll: String,
    ) : EncountersInteraction
    data class InitiativeBonusEntered(
        val encounterId: String,
        val participantId: String,
        val bonus: String,
    ) : EncountersInteraction
    data class InitiativeRolled(
        val encounterId: String,
        val participantId: String,
    ) : EncountersInteraction
    data class RollAllInitiativeSelected(
        val encounterId: String?,
        val overwriteExisting: Boolean,
    ) : EncountersInteraction
    data class EditorBattleMapSelected(val battleMapId: String?) : EncountersInteraction
    data class EditorNameChanged(val name: String) : EncountersInteraction
    data class EditorNotesChanged(val notes: String) : EncountersInteraction
    data class EditorDifficultySelected(val difficulty: EncounterDifficulty) : EncountersInteraction
    data class EditorLocationSelected(val locationId: String?) : EncountersInteraction
    data class EditorNamelessNameChanged(val name: String) : EncountersInteraction
    data class EditorNamelessGroupCountChanged(val groupCount: String) : EncountersInteraction
    data object EditorNamelessSwarmToggled : EncountersInteraction
    data class EditorRosterSearchChanged(val query: String) : EncountersInteraction
    data object EditorNamelessAdded : EncountersInteraction
    data object EditorPartyAdded : EncountersInteraction
    data class EditorWorldPersonAdded(val personId: String) : EncountersInteraction
    data class EditorCampaignPersonAdded(val personId: String) : EncountersInteraction
    data class EditorOwnerCompanionsAdded(
        val ownerPersonId: String,
        val ownerSource: EncounterParticipantSource,
    ) : EncountersInteraction
    data class EditorParticipantRemoved(val index: Int) : EncountersInteraction
    data class EditorParticipantInitiativeChanged(val index: Int, val roll: String) : EncountersInteraction
    data object EditorSaved : EncountersInteraction
    data object EditorDismissed : EncountersInteraction
    data class MapCellSelected(val x: Double, val y: Double) : EncountersInteraction
    data class TokenSelected(val participantId: String) : EncountersInteraction
    data class MovementSpeedChanged(val speed: String) : EncountersInteraction
    data object MovementCleared : EncountersInteraction
    data object MeasureToggled : EncountersInteraction
    data object MeasureCleared : EncountersInteraction
    data object FogToggled : EncountersInteraction
    data object FogRevealBrushSelected : EncountersInteraction
    data object FogHideBrushSelected : EncountersInteraction
    data object FogRevealAllSelected : EncountersInteraction
    data object FogHideAllSelected : EncountersInteraction
    data class TerrainPaintSelected(val kind: TerrainPaintKind?) : EncountersInteraction
    data object ItemDropToggled : EncountersInteraction
    data class ItemNameChanged(val name: String) : EncountersInteraction
    data class ItemSelected(val itemId: String) : EncountersInteraction
    data object ItemRemoved : EncountersInteraction
    data object PlayerViewSelected : EncountersInteraction
    data object PlayerViewClosed : EncountersInteraction
}
