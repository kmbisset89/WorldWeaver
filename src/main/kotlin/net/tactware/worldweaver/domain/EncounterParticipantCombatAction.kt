package net.tactware.worldweaver.domain

internal sealed interface EncounterParticipantCombatAction {
    data class Damage(val amount: Int) : EncounterParticipantCombatAction
    data class Heal(val amount: Int) : EncounterParticipantCombatAction
    data class SetTemporaryHitPoints(val amount: Int) : EncounterParticipantCombatAction
    data class SetConditions(val conditions: List<String>) : EncounterParticipantCombatAction
    data class SetCombatState(val state: CombatState) : EncounterParticipantCombatAction
    data class SetVisibleToPlayers(val visible: Boolean) : EncounterParticipantCombatAction
    data class SetInitiative(
        val roll: Int?,
        val bonus: Int,
    ) : EncounterParticipantCombatAction
    data object RollInitiative : EncounterParticipantCombatAction
    data class SetAttacksUsed(val count: Int) : EncounterParticipantCombatAction
    data class SetAttacksAllowed(val count: Int) : EncounterParticipantCombatAction
    data class SetBonusActionUsed(val used: Boolean) : EncounterParticipantCombatAction
    data class SetReactionUsed(val used: Boolean) : EncounterParticipantCombatAction
}
