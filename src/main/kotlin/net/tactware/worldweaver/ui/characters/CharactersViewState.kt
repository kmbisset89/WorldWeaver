package net.tactware.worldweaver.ui.characters

import net.tactware.worldweaver.domain.AbilityScoreMethod
import net.tactware.worldweaver.domain.CompanionKind
import net.tactware.worldweaver.domain.CreatureSize
import net.tactware.worldweaver.domain.FifthEditionPickerCatalog
import net.tactware.worldweaver.domain.FifthEditionSheet
import net.tactware.worldweaver.domain.Pathfinder2ESkillRank
import net.tactware.worldweaver.domain.PersonKind
import net.tactware.worldweaver.domain.PersonSheet
import net.tactware.worldweaver.domain.RandomNpcDraft
import net.tactware.worldweaver.domain.RelationshipType
import net.tactware.worldweaver.domain.SrdMonsterEntry

internal sealed class CharactersViewState {
    data object Loading : CharactersViewState()

    data class Error(
        val message: String,
        val canRetry: Boolean,
    ) : CharactersViewState()

    data object NoActiveWorld : CharactersViewState()

    data class Empty(
        val worldName: String,
        val campaignName: String?,
        val editor: CharacterEditorState?,
        val pathfinderEditor: PathfinderEditorState?,
        val generator: GeneratorState?,
        val wizard: CreationWizardState?,
        val pathfinderWizard: PathfinderWizardState?,
        val pickerCatalog: FifthEditionPickerCatalog,
        val srdMonsterPicker: List<SrdMonsterEntry>?,
        val worldGameSystemIsFifthEdition: Boolean,
    ) : CharactersViewState()

    data class Content(
        val worldName: String,
        val campaignName: String?,
        val people: List<PersonRow>,
        val selected: SelectedPerson?,
        val kindFilter: PersonKind?,
        val membershipFilter: PersonMembership?,
        val searchQuery: String,
        val editor: CharacterEditorState?,
        val pathfinderEditor: PathfinderEditorState?,
        val generator: GeneratorState?,
        val wizard: CreationWizardState?,
        val pathfinderWizard: PathfinderWizardState?,
        val relationshipEditor: RelationshipEditorState?,
        val membershipEditor: MembershipEditorState?,
        val companionEditor: CompanionEditorState?,
        val pendingDelete: PendingDelete?,
        val blockDeleteReason: String?,
        val pickerCatalog: FifthEditionPickerCatalog,
        val srdMonsterPicker: List<SrdMonsterEntry>?,
        val worldGameSystemIsFifthEdition: Boolean,
    ) : CharactersViewState()

    data class PersonKey(
        val membership: PersonMembership,
        val id: String,
    )

    data class PersonRow(
        val key: PersonKey,
        val name: String,
        val kind: PersonKind,
        val subtitle: String,
        val avatarPath: String?,
    )

    data class SelectedPerson(
        val key: PersonKey,
        val kind: PersonKind,
        val name: String,
        val description: String,
        val sheet: PersonSheet,
        val overlayHitPoints: String,
        val overlayNotes: String,
        val isWorldReference: Boolean,
        val canAddToCampaign: Boolean,
        val relationships: List<RelationshipRow>,
        val memberships: List<MembershipRow>,
        val factionOptions: List<FactionOption>,
        val companions: List<CompanionRow>,
        val attachedLore: List<AttachedLore>,
        val attachedQuests: List<AttachedQuest>,
        val relationshipTargets: List<RelationshipTarget>,
        val avatarPath: String?,
        val voiceClipPath: String?,
        val isRecordingVoice: Boolean,
        val isPlayingVoice: Boolean,
    )

    data class RelationshipRow(
        val id: String,
        val label: String,
        val type: RelationshipType,
        val description: String,
        val factionName: String?,
    )

    data class MembershipRow(
        val id: String,
        val factionName: String,
        val role: String,
        val notes: String,
    )

    data class FactionOption(
        val id: String,
        val name: String,
    )

    data class RelationshipTarget(
        val key: PersonKey,
        val name: String,
    )

    data class CompanionRow(
        val id: String,
        val key: PersonKey,
        val name: String,
        val kind: CompanionKind,
    )

    data class CompanionTarget(
        val key: PersonKey,
        val name: String,
    )

    enum class CreationStep {
        Identity,
        RaceAndClass,
        Abilities,
        Companions,
        Review,
    }

    data class CreationWizardState(
        val step: CreationStep,
        val membership: PersonMembership,
        val canChangeMembership: Boolean,
        val hasActiveCampaign: Boolean,
        val kind: PersonKind,
        val name: String,
        val description: String,
        val race: String,
        val classLevels: List<ClassLevelEditor>,
        val strength: String,
        val dexterity: String,
        val constitution: String,
        val intelligence: String,
        val wisdom: String,
        val charisma: String,
        val hitPoints: String,
        val maxHitPoints: String,
        val armorClass: String,
        val walkSpeed: String,
        val companions: List<CompanionDraftEditor>,
        val companionTargets: List<CompanionTarget>,
        val nameError: String?,
        val membershipError: String?,
        val companionError: String?,
    )

    data class CompanionDraftEditor(
        val kind: CompanionKind,
        val useExisting: Boolean,
        val existingKey: PersonKey?,
        val newName: String,
        val newCreature: String,
    )

    data class CompanionEditorState(
        val kind: CompanionKind,
        val useExisting: Boolean,
        val existingKey: PersonKey?,
        val newName: String,
        val newCreature: String,
        val targets: List<CompanionTarget>,
        val error: String?,
    )

    data class AttachedLore(
        val loreId: String,
        val title: String,
    )

    data class AttachedQuest(
        val questId: String,
        val title: String,
    )

    data class CharacterEditorState(
        val personId: String?,
        val membership: PersonMembership,
        val isWorldReference: Boolean,
        val canChangeMembership: Boolean,
        val hasActiveCampaign: Boolean,
        val kind: PersonKind,
        val name: String,
        val description: String,
        val race: String,
        val classLevels: List<ClassLevelEditor>,
        val strength: String,
        val dexterity: String,
        val constitution: String,
        val intelligence: String,
        val wisdom: String,
        val charisma: String,
        val hitPoints: String,
        val maxHitPoints: String,
        val temporaryHitPoints: String,
        val armorClass: String,
        val walkSpeed: String,
        val creatureSize: CreatureSize,
        val concentratingSpell: String,
        val skills: List<SkillEditor>,
        val spellSlots: List<SpellSlotEditor>,
        val deathSuccesses: String,
        val deathFailures: String,
        val items: List<ItemEditor>,
        val features: List<FeatureEditor>,
        val spells: List<SpellEditor>,
        val notes: String,
        val overlayHitPoints: String,
        val overlayNotes: String,
        val nameError: String?,
        val membershipError: String?,
    )

    data class ClassLevelEditor(
        val className: String,
        val subclass: String,
        val levelText: String,
    )

    data class ItemEditor(
        val name: String,
        val quantityText: String,
        val notes: String,
    )

    data class FeatureEditor(
        val name: String,
        val description: String,
    )

    data class SpellEditor(
        val name: String,
        val levelText: String,
        val prepared: Boolean,
    )

    data class SkillEditor(
        val name: String,
        val ability: String,
        val proficient: Boolean,
    )

    data class SpellSlotEditor(
        val levelText: String,
        val maximumText: String,
        val usedText: String,
    )

    data class GeneratorState(
        val method: AbilityScoreMethod,
        val draft: RandomNpcDraft?,
    )

    data class RelationshipEditorState(
        val target: PersonKey?,
        val type: RelationshipType,
        val description: String,
        val factionId: String?,
        val factions: List<FactionOption>,
        val targets: List<RelationshipTarget>,
        val targetError: String?,
    )

    data class MembershipEditorState(
        val factionId: String?,
        val role: String,
        val factions: List<FactionOption>,
        val factionError: String?,
    )

    data class PendingDelete(
        val key: PersonKey,
        val name: String,
    )

    enum class PathfinderCreationStep {
        Identity,
        AncestryAndClass,
        Attributes,
        Skills,
        Review,
    }

    data class PathfinderEditorState(
        val personId: String?,
        val membership: PersonMembership,
        val isWorldReference: Boolean,
        val canChangeMembership: Boolean,
        val hasActiveCampaign: Boolean,
        val kind: PersonKind,
        val name: String,
        val description: String,
        val ancestry: String,
        val heritage: String,
        val background: String,
        val className: String,
        val subclass: String,
        val levelText: String,
        val strength: String,
        val dexterity: String,
        val constitution: String,
        val intelligence: String,
        val wisdom: String,
        val charisma: String,
        val hitPoints: String,
        val maxHitPoints: String,
        val temporaryHitPoints: String,
        val armorClass: String,
        val perception: String,
        val landSpeed: String,
        val dying: String,
        val wounded: String,
        val skills: List<PathfinderSkillEditor>,
        val feats: List<PathfinderFeatEditor>,
        val spells: List<PathfinderSpellEditor>,
        val notes: String,
        val overlayHitPoints: String,
        val overlayNotes: String,
        val nameError: String?,
        val membershipError: String?,
    )

    data class PathfinderWizardState(
        val step: PathfinderCreationStep,
        val membership: PersonMembership,
        val canChangeMembership: Boolean,
        val hasActiveCampaign: Boolean,
        val kind: PersonKind,
        val name: String,
        val description: String,
        val ancestry: String,
        val heritage: String,
        val background: String,
        val className: String,
        val subclass: String,
        val levelText: String,
        val strength: String,
        val dexterity: String,
        val constitution: String,
        val intelligence: String,
        val wisdom: String,
        val charisma: String,
        val hitPoints: String,
        val maxHitPoints: String,
        val armorClass: String,
        val perception: String,
        val landSpeed: String,
        val skills: List<PathfinderSkillEditor>,
        val nameError: String?,
        val membershipError: String?,
    )

    data class PathfinderSkillEditor(
        val name: String,
        val rank: Pathfinder2ESkillRank,
    )

    data class PathfinderFeatEditor(
        val name: String,
        val type: String,
        val description: String,
    )

    data class PathfinderSpellEditor(
        val name: String,
        val rankText: String,
        val prepared: Boolean,
    )
}
