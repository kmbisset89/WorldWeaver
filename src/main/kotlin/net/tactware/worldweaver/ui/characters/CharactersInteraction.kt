package net.tactware.worldweaver.ui.characters

import net.tactware.worldweaver.domain.AbilityScoreMethod
import net.tactware.worldweaver.domain.CompanionKind
import net.tactware.worldweaver.domain.PersonKind
import net.tactware.worldweaver.domain.RelationshipType

internal sealed interface CharactersInteraction {
    data object ScreenStarted : CharactersInteraction
    data object RetrySelected : CharactersInteraction
    data object CreateWorldSelected : CharactersInteraction
    data object NewPersonSelected : CharactersInteraction
    data object RandomNpcSelected : CharactersInteraction
    data class PersonSelected(val key: CharactersViewState.PersonKey) : CharactersInteraction
    data class PersonOpened(val key: CharactersViewState.PersonKey) : CharactersInteraction
    data class EditPersonSelected(val key: CharactersViewState.PersonKey) : CharactersInteraction
    data class DeletePersonSelected(val key: CharactersViewState.PersonKey) : CharactersInteraction
    data object DeleteConfirmed : CharactersInteraction
    data object DeleteCancelled : CharactersInteraction
    data object BlockReasonDismissed : CharactersInteraction
    data class AddToCampaignSelected(val worldPersonId: String) : CharactersInteraction
    data class SearchQueryChanged(val query: String) : CharactersInteraction
    data class KindFilterSelected(val kind: PersonKind?) : CharactersInteraction
    data class MembershipFilterSelected(val membership: PersonMembership?) : CharactersInteraction
    data class AvatarImageChosen(val path: String) : CharactersInteraction
    data object AvatarRemoved : CharactersInteraction
    data class VoiceClipAttached(val path: String) : CharactersInteraction
    data object VoiceClipRecordToggled : CharactersInteraction
    data object VoiceClipPlayToggled : CharactersInteraction
    data object VoiceClipRemoved : CharactersInteraction
    data class OverlayHitPointsChanged(val value: String) : CharactersInteraction
    data class OverlayNotesChanged(val value: String) : CharactersInteraction
    data object OverlaySaved : CharactersInteraction
    data class AttachedLoreSelected(val loreId: String) : CharactersInteraction
    data class AttachedQuestSelected(val questId: String) : CharactersInteraction
    data object RelationshipEditorOpened : CharactersInteraction
    data object RelationshipEditorDismissed : CharactersInteraction
    data class RelationshipTargetSelected(val key: CharactersViewState.PersonKey) : CharactersInteraction
    data class RelationshipTypeSelected(val type: RelationshipType) : CharactersInteraction
    data class RelationshipDescriptionChanged(val description: String) : CharactersInteraction
    data class RelationshipFactionChanged(val factionLean: String) : CharactersInteraction
    data object RelationshipSaved : CharactersInteraction
    data class RelationshipDeleted(val relationshipId: String) : CharactersInteraction
    data class EditorMembershipSelected(val membership: PersonMembership) : CharactersInteraction
    data class EditorKindSelected(val kind: PersonKind) : CharactersInteraction
    data class EditorNameChanged(val name: String) : CharactersInteraction
    data class EditorDescriptionChanged(val description: String) : CharactersInteraction
    data class EditorRaceChanged(val race: String) : CharactersInteraction
    data object EditorClassLevelAdded : CharactersInteraction
    data class EditorClassLevelRemoved(val index: Int) : CharactersInteraction
    data class EditorClassNameChanged(val index: Int, val className: String) : CharactersInteraction
    data class EditorSubclassChanged(val index: Int, val subclass: String) : CharactersInteraction
    data class EditorClassLevelChanged(val index: Int, val level: String) : CharactersInteraction
    data class EditorStrengthChanged(val value: String) : CharactersInteraction
    data class EditorDexterityChanged(val value: String) : CharactersInteraction
    data class EditorConstitutionChanged(val value: String) : CharactersInteraction
    data class EditorIntelligenceChanged(val value: String) : CharactersInteraction
    data class EditorWisdomChanged(val value: String) : CharactersInteraction
    data class EditorCharismaChanged(val value: String) : CharactersInteraction
    data class EditorHitPointsChanged(val value: String) : CharactersInteraction
    data class EditorMaxHitPointsChanged(val value: String) : CharactersInteraction
    data class EditorTemporaryHitPointsChanged(val value: String) : CharactersInteraction
    data class EditorArmorClassChanged(val value: String) : CharactersInteraction
    data class EditorWalkSpeedChanged(val value: String) : CharactersInteraction
    data class EditorDeathSuccessesChanged(val value: String) : CharactersInteraction
    data class EditorDeathFailuresChanged(val value: String) : CharactersInteraction
    data object EditorItemAdded : CharactersInteraction
    data class EditorItemRemoved(val index: Int) : CharactersInteraction
    data class EditorItemNameChanged(val index: Int, val name: String) : CharactersInteraction
    data class EditorItemQuantityChanged(val index: Int, val quantity: String) : CharactersInteraction
    data class EditorItemNotesChanged(val index: Int, val notes: String) : CharactersInteraction
    data object EditorFeatureAdded : CharactersInteraction
    data class EditorFeatureRemoved(val index: Int) : CharactersInteraction
    data class EditorFeatureNameChanged(val index: Int, val name: String) : CharactersInteraction
    data class EditorFeatureDescriptionChanged(val index: Int, val description: String) : CharactersInteraction
    data object EditorSpellAdded : CharactersInteraction
    data class EditorSpellRemoved(val index: Int) : CharactersInteraction
    data class EditorSpellNameChanged(val index: Int, val name: String) : CharactersInteraction
    data class EditorSpellLevelChanged(val index: Int, val level: String) : CharactersInteraction
    data class EditorSpellPreparedChanged(val index: Int, val prepared: Boolean) : CharactersInteraction
    data class EditorNotesChanged(val notes: String) : CharactersInteraction
    data class EditorOverlayHitPointsChanged(val value: String) : CharactersInteraction
    data class EditorOverlayNotesChanged(val notes: String) : CharactersInteraction
    data object EditorSaved : CharactersInteraction
    data object EditorDismissed : CharactersInteraction
    data class GeneratorMethodSelected(val method: AbilityScoreMethod) : CharactersInteraction
    data object GeneratorRolled : CharactersInteraction
    data object GeneratorSaved : CharactersInteraction
    data object GeneratorDismissed : CharactersInteraction
    data object CompanionEditorOpened : CharactersInteraction
    data object CompanionEditorDismissed : CharactersInteraction
    data class CompanionKindSelected(val kind: CompanionKind) : CharactersInteraction
    data class CompanionUseExistingChanged(val useExisting: Boolean) : CharactersInteraction
    data class CompanionTargetSelected(val key: CharactersViewState.PersonKey) : CharactersInteraction
    data class CompanionNameChanged(val name: String) : CharactersInteraction
    data class CompanionCreatureChanged(val creature: String) : CharactersInteraction
    data object CompanionSaved : CharactersInteraction
    data class CompanionDeleted(val companionId: String) : CharactersInteraction
    data class WizardMembershipSelected(val membership: PersonMembership) : CharactersInteraction
    data class WizardKindSelected(val kind: PersonKind) : CharactersInteraction
    data class WizardNameChanged(val name: String) : CharactersInteraction
    data class WizardDescriptionChanged(val description: String) : CharactersInteraction
    data class WizardRaceChanged(val race: String) : CharactersInteraction
    data object WizardClassLevelAdded : CharactersInteraction
    data class WizardClassLevelRemoved(val index: Int) : CharactersInteraction
    data class WizardClassNameChanged(val index: Int, val className: String) : CharactersInteraction
    data class WizardSubclassChanged(val index: Int, val subclass: String) : CharactersInteraction
    data class WizardClassLevelChanged(val index: Int, val level: String) : CharactersInteraction
    data class WizardStrengthChanged(val value: String) : CharactersInteraction
    data class WizardDexterityChanged(val value: String) : CharactersInteraction
    data class WizardConstitutionChanged(val value: String) : CharactersInteraction
    data class WizardIntelligenceChanged(val value: String) : CharactersInteraction
    data class WizardWisdomChanged(val value: String) : CharactersInteraction
    data class WizardCharismaChanged(val value: String) : CharactersInteraction
    data class WizardHitPointsChanged(val value: String) : CharactersInteraction
    data class WizardMaxHitPointsChanged(val value: String) : CharactersInteraction
    data class WizardArmorClassChanged(val value: String) : CharactersInteraction
    data class WizardWalkSpeedChanged(val value: String) : CharactersInteraction
    data object WizardCompanionAdded : CharactersInteraction
    data class WizardCompanionRemoved(val index: Int) : CharactersInteraction
    data class WizardCompanionKindSelected(
        val index: Int,
        val kind: CompanionKind,
    ) : CharactersInteraction
    data class WizardCompanionUseExistingChanged(
        val index: Int,
        val useExisting: Boolean,
    ) : CharactersInteraction
    data class WizardCompanionTargetSelected(
        val index: Int,
        val key: CharactersViewState.PersonKey,
    ) : CharactersInteraction
    data class WizardCompanionNameChanged(val index: Int, val name: String) : CharactersInteraction
    data class WizardCompanionCreatureChanged(
        val index: Int,
        val creature: String,
    ) : CharactersInteraction
    data object WizardNextSelected : CharactersInteraction
    data object WizardBackSelected : CharactersInteraction
    data object WizardSaved : CharactersInteraction
    data object WizardDismissed : CharactersInteraction
}
