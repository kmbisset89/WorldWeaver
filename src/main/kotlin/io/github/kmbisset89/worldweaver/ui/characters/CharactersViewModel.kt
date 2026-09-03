package io.github.kmbisset89.worldweaver.ui.characters

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import io.github.kmbisset89.worldweaver.core.AppCoroutineScope
import io.github.kmbisset89.worldweaver.domain.AbilityScoreMethod
import io.github.kmbisset89.worldweaver.domain.AbilityScores
import io.github.kmbisset89.worldweaver.domain.ActiveContextDetails
import io.github.kmbisset89.worldweaver.domain.AddWorldPersonToCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.ClearPersonAvatarUseCase
import io.github.kmbisset89.worldweaver.domain.ClearVoiceClipUseCase
import io.github.kmbisset89.worldweaver.domain.CampaignPerson
import io.github.kmbisset89.worldweaver.domain.CampaignPersonDraft
import io.github.kmbisset89.worldweaver.domain.ClassLevel
import io.github.kmbisset89.worldweaver.domain.CompanionKind
import io.github.kmbisset89.worldweaver.domain.CreateCampaignPersonUseCase
import io.github.kmbisset89.worldweaver.domain.CreateFactionMembershipUseCase
import io.github.kmbisset89.worldweaver.domain.CreatePersonCompanionUseCase
import io.github.kmbisset89.worldweaver.domain.CreatePersonRelationshipUseCase
import io.github.kmbisset89.worldweaver.domain.CreateWorldPersonFromSrdMonsterUseCase
import io.github.kmbisset89.worldweaver.domain.CreateWorldPersonUseCase
import io.github.kmbisset89.worldweaver.domain.FifthEditionPickerCatalog
import io.github.kmbisset89.worldweaver.domain.FifthEditionPickerCatalogResolver
import io.github.kmbisset89.worldweaver.domain.GameSystem
import io.github.kmbisset89.worldweaver.domain.Pathfinder2EFeat
import io.github.kmbisset89.worldweaver.domain.Pathfinder2EReference
import io.github.kmbisset89.worldweaver.domain.Pathfinder2ESheet
import io.github.kmbisset89.worldweaver.domain.Pathfinder2ESkill
import io.github.kmbisset89.worldweaver.domain.Pathfinder2ESpell
import io.github.kmbisset89.worldweaver.domain.Pathfinder2ESkillRank
import io.github.kmbisset89.worldweaver.domain.PersonSheet
import io.github.kmbisset89.worldweaver.domain.DeathSaves
import io.github.kmbisset89.worldweaver.domain.DeleteCampaignPersonUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteFactionMembershipUseCase
import io.github.kmbisset89.worldweaver.domain.DeletePersonCompanionUseCase
import io.github.kmbisset89.worldweaver.domain.DeletePersonRelationshipUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteWorldPersonUseCase
import io.github.kmbisset89.worldweaver.domain.FifthEditionSheet
import io.github.kmbisset89.worldweaver.domain.FifthEditionSkill
import io.github.kmbisset89.worldweaver.domain.FifthEditionSkillCatalog
import io.github.kmbisset89.worldweaver.domain.FifthEditionSpellSlot
import io.github.kmbisset89.worldweaver.domain.GenerateRandomNpcUseCase
import io.github.kmbisset89.worldweaver.domain.InventoryItem
import io.github.kmbisset89.worldweaver.domain.Lore
import io.github.kmbisset89.worldweaver.domain.Faction
import io.github.kmbisset89.worldweaver.domain.FactionMembership
import io.github.kmbisset89.worldweaver.domain.ObserveActiveContextDetailsUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveFactionMembershipsUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveFactionsForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveFifthEditionPickerCatalogUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveLoreForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.ObservePeopleForActiveContextUseCase
import io.github.kmbisset89.worldweaver.domain.ObservePersonCompanionsUseCase
import io.github.kmbisset89.worldweaver.domain.ObservePersonRelationshipsUseCase
import io.github.kmbisset89.worldweaver.domain.PersonCompanion
import io.github.kmbisset89.worldweaver.domain.ObserveQuestsForActiveCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.Quest
import io.github.kmbisset89.worldweaver.domain.QuestLinkKind
import io.github.kmbisset89.worldweaver.domain.PeopleSnapshot
import io.github.kmbisset89.worldweaver.domain.PersonFeature
import io.github.kmbisset89.worldweaver.domain.PersonKind
import io.github.kmbisset89.worldweaver.domain.PersonAvatarFileStore
import io.github.kmbisset89.worldweaver.domain.PersonRef
import io.github.kmbisset89.worldweaver.domain.SetPersonAvatarUseCase
import io.github.kmbisset89.worldweaver.domain.SrdMonsterEntry
import io.github.kmbisset89.worldweaver.domain.SetVoiceClipUseCase
import io.github.kmbisset89.worldweaver.domain.VoiceClipFileStore
import io.github.kmbisset89.worldweaver.domain.VoiceClipPlayer
import io.github.kmbisset89.worldweaver.domain.VoiceClipRecorder
import io.github.kmbisset89.worldweaver.domain.VoiceClipRef
import io.github.kmbisset89.worldweaver.domain.PersonRelationship
import io.github.kmbisset89.worldweaver.domain.PersonSpell
import io.github.kmbisset89.worldweaver.domain.RelationshipType
import io.github.kmbisset89.worldweaver.domain.UpdateCampaignPersonUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateWorldPersonUseCase
import io.github.kmbisset89.worldweaver.domain.WorldPerson
import io.github.kmbisset89.worldweaver.domain.WorldPersonDraft

internal class CharactersViewModel(
    private val appScope: AppCoroutineScope,
    private val observeActiveContextDetails: ObserveActiveContextDetailsUseCase,
    private val observePeople: ObservePeopleForActiveContextUseCase,
    private val observeRelationships: ObservePersonRelationshipsUseCase,
    private val observeCompanions: ObservePersonCompanionsUseCase,
    private val observeLore: ObserveLoreForActiveWorldUseCase,
    private val observeQuests: ObserveQuestsForActiveCampaignUseCase,
    private val observeFactions: ObserveFactionsForActiveWorldUseCase,
    private val observeMemberships: ObserveFactionMembershipsUseCase,
    private val createWorldPerson: CreateWorldPersonUseCase,
    private val updateWorldPerson: UpdateWorldPersonUseCase,
    private val deleteWorldPerson: DeleteWorldPersonUseCase,
    private val createCampaignPerson: CreateCampaignPersonUseCase,
    private val updateCampaignPerson: UpdateCampaignPersonUseCase,
    private val deleteCampaignPerson: DeleteCampaignPersonUseCase,
    private val addWorldPersonToCampaign: AddWorldPersonToCampaignUseCase,
    private val setPersonAvatar: SetPersonAvatarUseCase,
    private val clearPersonAvatar: ClearPersonAvatarUseCase,
    private val avatarFileStore: PersonAvatarFileStore,
    private val setVoiceClip: SetVoiceClipUseCase,
    private val clearVoiceClip: ClearVoiceClipUseCase,
    private val voiceClipFileStore: VoiceClipFileStore,
    private val voiceClipRecorder: VoiceClipRecorder,
    private val voiceClipPlayer: VoiceClipPlayer,
    private val generateRandomNpc: GenerateRandomNpcUseCase,
    private val observePickerCatalog: ObserveFifthEditionPickerCatalogUseCase,
    private val createFromSrdMonster: CreateWorldPersonFromSrdMonsterUseCase,
    private val createPersonRelationship: CreatePersonRelationshipUseCase,
    private val deletePersonRelationship: DeletePersonRelationshipUseCase,
    private val createFactionMembership: CreateFactionMembershipUseCase,
    private val deleteFactionMembership: DeleteFactionMembershipUseCase,
    private val createPersonCompanion: CreatePersonCompanionUseCase,
    private val deletePersonCompanion: DeletePersonCompanionUseCase,
) {
    private val _state = MutableStateFlow<CharactersViewState>(CharactersViewState.Loading)
    val state: StateFlow<CharactersViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<CharactersViewEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<CharactersViewEffect> = _effects.asSharedFlow()

    private var observeJob: Job? = null
    private var pendingCreate: PendingCreate? = null
    private var searchQuery = ""
    private var kindFilter: PersonKind? = null
    private var membershipFilter: PersonMembership? = null
    private var selectedKey: CharactersViewState.PersonKey? = null
    private var latestWorldPeople: List<WorldPerson> = emptyList()
    private var latestCampaignPeople: List<CampaignPerson> = emptyList()
    private var latestRelationships: List<PersonRelationship> = emptyList()
    private var latestCompanions: List<PersonCompanion> = emptyList()
    private var latestLore: List<Lore> = emptyList()
    private var latestQuests: List<Quest> = emptyList()
    private var latestFactions: List<Faction> = emptyList()
    private var latestMemberships: List<FactionMembership> = emptyList()
    private var latestPickerCatalog: FifthEditionPickerCatalog =
        FifthEditionPickerCatalogResolver().resolve(null)
    private var srdMonsterPickerOpen = false
    private var latestWorldName: String = ""
    private var latestCampaignName: String? = null
    private var hasActiveCampaign = false
    private var latestWorldSystem = GameSystem.FifthEdition
    private var latestCampaignSystem = GameSystem.FifthEdition
    private var isRecordingVoice = false
    private var isPlayingVoice = false

    init {
        observe()
    }

    fun onInteraction(interaction: CharactersInteraction) {
        when (interaction) {
            CharactersInteraction.ScreenStarted -> Unit
            CharactersInteraction.RetrySelected -> observe()
            CharactersInteraction.CreateWorldSelected -> {
                _effects.tryEmit(CharactersViewEffect.OpenWorlds)
            }
            CharactersInteraction.NewPersonSelected -> openCreateWizard()
            CharactersInteraction.NewPlayerCharacterSelected -> openCreatePcWizard()
            CharactersInteraction.RandomNpcSelected -> openGenerator()
            is CharactersInteraction.PersonSelected,
            is CharactersInteraction.PersonOpened,
            -> {
                val nextKey = personKeyFrom(interaction)
                if (selectedKey != nextKey) {
                    stopVoiceSession()
                }
                selectedKey = nextKey
                refreshContent()
            }
            is CharactersInteraction.SheetSelected -> {
                _effects.tryEmit(CharactersViewEffect.OpenSheet(interaction.key))
            }
            is CharactersInteraction.EditPersonSelected -> openEditEditor(interaction.key)
            is CharactersInteraction.DeletePersonSelected -> requestDelete(interaction.key)
            CharactersInteraction.DeleteConfirmed -> confirmDelete()
            CharactersInteraction.DeleteCancelled -> updateContentOverlays(pendingDelete = null)
            CharactersInteraction.BlockReasonDismissed -> updateContentOverlays(blockDeleteReason = null)
            is CharactersInteraction.AddToCampaignSelected -> addToCampaign(interaction.worldPersonId)
            is CharactersInteraction.AvatarImageChosen -> saveAvatar(interaction.path)
            CharactersInteraction.AvatarRemoved -> removeAvatar()
            is CharactersInteraction.VoiceClipAttached -> saveVoiceClip(interaction.path)
            CharactersInteraction.VoiceClipRecordToggled -> toggleVoiceRecord()
            CharactersInteraction.VoiceClipPlayToggled -> toggleVoicePlay()
            CharactersInteraction.VoiceClipRemoved -> removeVoiceClip()
            is CharactersInteraction.SearchQueryChanged -> {
                searchQuery = interaction.query
                refreshContent()
            }
            is CharactersInteraction.KindFilterSelected -> {
                kindFilter = interaction.kind
                refreshContent()
            }
            is CharactersInteraction.MembershipFilterSelected -> {
                membershipFilter = interaction.membership
                refreshContent()
            }
            is CharactersInteraction.OverlayHitPointsChanged -> updateSelectedOverlay { selected ->
                selected.copy(overlayHitPoints = interaction.value)
            }
            is CharactersInteraction.OverlayNotesChanged -> updateSelectedOverlay { selected ->
                selected.copy(overlayNotes = interaction.value)
            }
            CharactersInteraction.OverlaySaved -> saveOverlay()
            is CharactersInteraction.AttachedLoreSelected -> {
                _effects.tryEmit(CharactersViewEffect.OpenLore(interaction.loreId))
            }
            is CharactersInteraction.AttachedQuestSelected -> {
                _effects.tryEmit(CharactersViewEffect.OpenQuest(interaction.questId))
            }
            CharactersInteraction.RelationshipEditorOpened -> openRelationshipEditor()
            CharactersInteraction.RelationshipEditorDismissed -> updateContentOverlays(
                relationshipEditor = null,
            )
            is CharactersInteraction.RelationshipTargetSelected -> updateRelationshipEditor { editor ->
                editor.copy(target = interaction.key, targetError = null)
            }
            is CharactersInteraction.RelationshipTypeSelected -> updateRelationshipEditor { editor ->
                editor.copy(type = interaction.type)
            }
            is CharactersInteraction.RelationshipDescriptionChanged -> updateRelationshipEditor { editor ->
                editor.copy(description = interaction.description)
            }
            is CharactersInteraction.RelationshipFactionSelected -> updateRelationshipEditor { editor ->
                editor.copy(factionId = interaction.factionId)
            }
            CharactersInteraction.RelationshipSaved -> saveRelationship()
            is CharactersInteraction.RelationshipDeleted -> deleteRelationship(interaction.relationshipId)
            CharactersInteraction.MembershipEditorOpened -> openMembershipEditor()
            CharactersInteraction.MembershipEditorDismissed -> updateContentOverlays(
                membershipEditor = null,
            )
            is CharactersInteraction.MembershipFactionSelected -> updateMembershipEditor { editor ->
                editor.copy(factionId = interaction.factionId, factionError = null)
            }
            is CharactersInteraction.MembershipRoleChanged -> updateMembershipEditor { editor ->
                editor.copy(role = interaction.role)
            }
            CharactersInteraction.MembershipSaved -> saveMembership()
            is CharactersInteraction.MembershipDeleted -> deleteMembership(interaction.membershipId)
            is CharactersInteraction.EditorMembershipSelected -> {
                updateEditor { editor ->
                    editor?.let { changeMembership(it, interaction.membership) }
                }
                updatePathfinderEditor { editor ->
                    editor?.let { changePathfinderMembership(it, interaction.membership) }
                }
            }
            is CharactersInteraction.EditorKindSelected -> {
                updateEditor { editor -> editor?.copy(kind = interaction.kind) }
                updatePathfinderEditor { editor -> editor?.copy(kind = interaction.kind) }
            }
            is CharactersInteraction.EditorNameChanged -> {
                updateEditor { editor -> editor?.copy(name = interaction.name, nameError = null) }
                updatePathfinderEditor { editor ->
                    editor?.copy(name = interaction.name, nameError = null)
                }
            }
            is CharactersInteraction.EditorDescriptionChanged -> {
                updateEditor { editor -> editor?.copy(description = interaction.description) }
                updatePathfinderEditor { editor ->
                    editor?.copy(description = interaction.description)
                }
            }
            is CharactersInteraction.EditorRaceChanged -> updateEditor { editor ->
                editor?.copy(race = interaction.race)
            }
            CharactersInteraction.EditorClassLevelAdded -> updateEditor { editor ->
                editor?.copy(
                    classLevels = editor.classLevels + CharactersViewState.ClassLevelEditor(
                        className = "",
                        subclass = "",
                        levelText = "1",
                    ),
                )
            }
            is CharactersInteraction.EditorClassLevelRemoved -> updateEditor { editor ->
                editor?.copy(
                    classLevels = editor.classLevels.filterIndexed { index, _ ->
                        index != interaction.index
                    },
                )
            }
            is CharactersInteraction.EditorClassNameChanged -> updateClassLevel(interaction.index) { level ->
                level.copy(className = interaction.className, subclass = "")
            }
            is CharactersInteraction.EditorSubclassChanged -> updateClassLevel(interaction.index) { level ->
                level.copy(subclass = interaction.subclass)
            }
            is CharactersInteraction.EditorClassLevelChanged -> updateClassLevel(interaction.index) { level ->
                level.copy(levelText = interaction.level)
            }
            is CharactersInteraction.EditorStrengthChanged -> {
                updateEditor { editor -> editor?.copy(strength = interaction.value) }
                updatePathfinderEditor { editor -> editor?.copy(strength = interaction.value) }
            }
            is CharactersInteraction.EditorDexterityChanged -> {
                updateEditor { editor -> editor?.copy(dexterity = interaction.value) }
                updatePathfinderEditor { editor -> editor?.copy(dexterity = interaction.value) }
            }
            is CharactersInteraction.EditorConstitutionChanged -> {
                updateEditor { editor -> editor?.copy(constitution = interaction.value) }
                updatePathfinderEditor { editor -> editor?.copy(constitution = interaction.value) }
            }
            is CharactersInteraction.EditorIntelligenceChanged -> {
                updateEditor { editor -> editor?.copy(intelligence = interaction.value) }
                updatePathfinderEditor { editor -> editor?.copy(intelligence = interaction.value) }
            }
            is CharactersInteraction.EditorWisdomChanged -> {
                updateEditor { editor -> editor?.copy(wisdom = interaction.value) }
                updatePathfinderEditor { editor -> editor?.copy(wisdom = interaction.value) }
            }
            is CharactersInteraction.EditorCharismaChanged -> {
                updateEditor { editor -> editor?.copy(charisma = interaction.value) }
                updatePathfinderEditor { editor -> editor?.copy(charisma = interaction.value) }
            }
            is CharactersInteraction.EditorHitPointsChanged -> {
                updateEditor { editor -> editor?.copy(hitPoints = interaction.value) }
                updatePathfinderEditor { editor -> editor?.copy(hitPoints = interaction.value) }
            }
            is CharactersInteraction.EditorMaxHitPointsChanged -> {
                updateEditor { editor -> editor?.copy(maxHitPoints = interaction.value) }
                updatePathfinderEditor { editor -> editor?.copy(maxHitPoints = interaction.value) }
            }
            is CharactersInteraction.EditorTemporaryHitPointsChanged -> {
                updateEditor { editor -> editor?.copy(temporaryHitPoints = interaction.value) }
                updatePathfinderEditor { editor ->
                    editor?.copy(temporaryHitPoints = interaction.value)
                }
            }
            is CharactersInteraction.EditorArmorClassChanged -> {
                updateEditor { editor -> editor?.copy(armorClass = interaction.value) }
                updatePathfinderEditor { editor -> editor?.copy(armorClass = interaction.value) }
            }
            is CharactersInteraction.EditorWalkSpeedChanged -> updateEditor { editor ->
                editor?.copy(walkSpeed = interaction.value.filter { it.isDigit() }.take(3))
            }
            is CharactersInteraction.EditorCreatureSizeSelected -> updateEditor { editor ->
                editor?.copy(creatureSize = interaction.size)
            }
            is CharactersInteraction.EditorConcentratingSpellChanged -> updateEditor { editor ->
                editor?.copy(concentratingSpell = interaction.value)
            }
            is CharactersInteraction.EditorSkillProficiencyToggled -> updateEditor { editor ->
                editor?.copy(
                    skills = editor.skills.map { skill ->
                        if (skill.name == interaction.name) {
                            skill.copy(proficient = !skill.proficient)
                        } else {
                            skill
                        }
                    },
                )
            }
            CharactersInteraction.EditorSpellSlotAdded -> updateEditor { editor ->
                editor?.copy(
                    spellSlots = editor.spellSlots + CharactersViewState.SpellSlotEditor(
                        levelText = (editor.spellSlots.size + 1).toString(),
                        maximumText = "1",
                        usedText = "0",
                    ),
                )
            }
            is CharactersInteraction.EditorSpellSlotRemoved -> updateEditor { editor ->
                editor?.copy(
                    spellSlots = editor.spellSlots.filterIndexed { index, _ ->
                        index != interaction.index
                    },
                )
            }
            is CharactersInteraction.EditorSpellSlotLevelChanged -> updateEditor { editor ->
                editor?.copy(
                    spellSlots = editor.spellSlots.mapIndexed { index, slot ->
                        if (index == interaction.index) {
                            slot.copy(levelText = interaction.value.filter { it.isDigit() }.take(1))
                        } else {
                            slot
                        }
                    },
                )
            }
            is CharactersInteraction.EditorSpellSlotMaximumChanged -> updateEditor { editor ->
                editor?.copy(
                    spellSlots = editor.spellSlots.mapIndexed { index, slot ->
                        if (index == interaction.index) {
                            slot.copy(maximumText = interaction.value.filter { it.isDigit() }.take(2))
                        } else {
                            slot
                        }
                    },
                )
            }
            is CharactersInteraction.EditorSpellSlotUsedChanged -> updateEditor { editor ->
                editor?.copy(
                    spellSlots = editor.spellSlots.mapIndexed { index, slot ->
                        if (index == interaction.index) {
                            slot.copy(usedText = interaction.value.filter { it.isDigit() }.take(2))
                        } else {
                            slot
                        }
                    },
                )
            }
            is CharactersInteraction.EditorDeathSuccessesChanged -> updateEditor { editor ->
                editor?.copy(deathSuccesses = interaction.value)
            }
            is CharactersInteraction.EditorDeathFailuresChanged -> updateEditor { editor ->
                editor?.copy(deathFailures = interaction.value)
            }
            CharactersInteraction.EditorItemAdded -> updateEditor { editor ->
                editor?.copy(
                    items = editor.items + CharactersViewState.ItemEditor("", "1", ""),
                )
            }
            is CharactersInteraction.EditorItemRemoved -> updateEditor { editor ->
                editor?.copy(
                    items = editor.items.filterIndexed { index, _ -> index != interaction.index },
                )
            }
            is CharactersInteraction.EditorItemNameChanged -> updateItem(interaction.index) { item ->
                item.copy(name = interaction.name)
            }
            is CharactersInteraction.EditorItemQuantityChanged -> updateItem(interaction.index) { item ->
                item.copy(quantityText = interaction.quantity)
            }
            is CharactersInteraction.EditorItemNotesChanged -> updateItem(interaction.index) { item ->
                item.copy(notes = interaction.notes)
            }
            CharactersInteraction.EditorFeatureAdded -> updateEditor { editor ->
                editor?.copy(
                    features = editor.features + CharactersViewState.FeatureEditor("", ""),
                )
            }
            is CharactersInteraction.EditorFeatureRemoved -> updateEditor { editor ->
                editor?.copy(
                    features = editor.features.filterIndexed { index, _ ->
                        index != interaction.index
                    },
                )
            }
            is CharactersInteraction.EditorFeatureNameChanged -> updateFeature(interaction.index) { feature ->
                feature.copy(name = interaction.name)
            }
            is CharactersInteraction.EditorFeatureDescriptionChanged -> updateFeature(interaction.index) { feature ->
                feature.copy(description = interaction.description)
            }
            CharactersInteraction.EditorSpellAdded -> updateEditor { editor ->
                editor?.copy(
                    spells = editor.spells + CharactersViewState.SpellEditor("", "0", false),
                )
            }
            is CharactersInteraction.EditorSpellRemoved -> updateEditor { editor ->
                editor?.copy(
                    spells = editor.spells.filterIndexed { index, _ -> index != interaction.index },
                )
            }
            is CharactersInteraction.EditorSpellNameChanged -> updateSpell(interaction.index) { spell ->
                val level = latestPickerCatalog.spellLevelFor(interaction.name)
                spell.copy(
                    name = interaction.name,
                    levelText = level?.toString() ?: spell.levelText,
                )
            }
            is CharactersInteraction.EditorSpellLevelChanged -> updateSpell(interaction.index) { spell ->
                spell.copy(levelText = interaction.level)
            }
            is CharactersInteraction.EditorSpellPreparedChanged -> updateSpell(interaction.index) { spell ->
                spell.copy(prepared = interaction.prepared)
            }
            is CharactersInteraction.EditorNotesChanged -> {
                updateEditor { editor -> editor?.copy(notes = interaction.notes) }
                updatePathfinderEditor { editor -> editor?.copy(notes = interaction.notes) }
            }
            is CharactersInteraction.EditorOverlayHitPointsChanged -> {
                updateEditor { editor -> editor?.copy(overlayHitPoints = interaction.value) }
                updatePathfinderEditor { editor -> editor?.copy(overlayHitPoints = interaction.value) }
            }
            is CharactersInteraction.EditorOverlayNotesChanged -> {
                updateEditor { editor -> editor?.copy(overlayNotes = interaction.notes) }
                updatePathfinderEditor { editor -> editor?.copy(overlayNotes = interaction.notes) }
            }
            CharactersInteraction.EditorSaved -> {
                if (pathfinderEditorFrom(_state.value) != null) {
                    savePathfinderEditor()
                } else {
                    saveEditor()
                }
            }
            CharactersInteraction.EditorDismissed -> {
                updateEditor { null }
                updatePathfinderEditor { null }
            }
            is CharactersInteraction.GeneratorMethodSelected -> updateGenerator { generator ->
                generator.copy(method = interaction.method)
            }
            CharactersInteraction.GeneratorRolled -> rollGenerator()
            CharactersInteraction.GeneratorSaved -> saveGenerator()
            CharactersInteraction.GeneratorDismissed -> updateGeneratorState(null)
            CharactersInteraction.CompanionEditorOpened -> openCompanionEditor()
            CharactersInteraction.CompanionEditorDismissed -> updateContentOverlays(
                companionEditor = null,
            )
            is CharactersInteraction.CompanionKindSelected -> updateCompanionEditor { editor ->
                editor.copy(kind = interaction.kind)
            }
            is CharactersInteraction.CompanionUseExistingChanged -> updateCompanionEditor { editor ->
                editor.copy(useExisting = interaction.useExisting, error = null)
            }
            is CharactersInteraction.CompanionTargetSelected -> updateCompanionEditor { editor ->
                editor.copy(existingKey = interaction.key, error = null)
            }
            is CharactersInteraction.CompanionNameChanged -> updateCompanionEditor { editor ->
                editor.copy(newName = interaction.name, error = null)
            }
            is CharactersInteraction.CompanionCreatureChanged -> updateCompanionEditor { editor ->
                editor.copy(newCreature = interaction.creature)
            }
            CharactersInteraction.CompanionSaved -> saveCompanionEditor()
            is CharactersInteraction.CompanionDeleted -> deleteCompanion(interaction.companionId)
            is CharactersInteraction.WizardMembershipSelected -> {
                applyWizardMembership(interaction.membership)
            }
            is CharactersInteraction.WizardKindSelected -> {
                updateWizard { wizard -> wizard.copy(kind = interaction.kind) }
                updatePathfinderWizard { wizard -> wizard.copy(kind = interaction.kind) }
            }
            is CharactersInteraction.WizardNameChanged -> {
                updateWizard { wizard -> wizard.copy(name = interaction.name, nameError = null) }
                updatePathfinderWizard { wizard ->
                    wizard.copy(name = interaction.name, nameError = null)
                }
            }
            is CharactersInteraction.WizardDescriptionChanged -> {
                updateWizard { wizard -> wizard.copy(description = interaction.description) }
                updatePathfinderWizard { wizard ->
                    wizard.copy(description = interaction.description)
                }
            }
            is CharactersInteraction.WizardRaceChanged -> updateWizard { wizard ->
                wizard.copy(race = interaction.race)
            }
            CharactersInteraction.WizardClassLevelAdded -> updateWizard { wizard ->
                wizard.copy(
                    classLevels = wizard.classLevels + CharactersViewState.ClassLevelEditor(
                        className = "",
                        subclass = "",
                        levelText = "1",
                    ),
                )
            }
            is CharactersInteraction.WizardClassLevelRemoved -> updateWizard { wizard ->
                wizard.copy(
                    classLevels = wizard.classLevels.filterIndexed { index, _ ->
                        index != interaction.index
                    },
                )
            }
            is CharactersInteraction.WizardClassNameChanged -> updateWizardClassLevel(
                interaction.index,
            ) { level ->
                level.copy(className = interaction.className, subclass = "")
            }
            is CharactersInteraction.WizardSubclassChanged -> updateWizardClassLevel(
                interaction.index,
            ) { level ->
                level.copy(subclass = interaction.subclass)
            }
            is CharactersInteraction.WizardClassLevelChanged -> updateWizardClassLevel(
                interaction.index,
            ) { level ->
                level.copy(levelText = interaction.level)
            }
            is CharactersInteraction.WizardStrengthChanged -> {
                updateWizard { wizard -> wizard.copy(strength = interaction.value) }
                updatePathfinderWizard { wizard -> wizard.copy(strength = interaction.value) }
            }
            is CharactersInteraction.WizardDexterityChanged -> {
                updateWizard { wizard -> wizard.copy(dexterity = interaction.value) }
                updatePathfinderWizard { wizard -> wizard.copy(dexterity = interaction.value) }
            }
            is CharactersInteraction.WizardConstitutionChanged -> {
                updateWizard { wizard -> wizard.copy(constitution = interaction.value) }
                updatePathfinderWizard { wizard -> wizard.copy(constitution = interaction.value) }
            }
            is CharactersInteraction.WizardIntelligenceChanged -> {
                updateWizard { wizard -> wizard.copy(intelligence = interaction.value) }
                updatePathfinderWizard { wizard -> wizard.copy(intelligence = interaction.value) }
            }
            is CharactersInteraction.WizardWisdomChanged -> {
                updateWizard { wizard -> wizard.copy(wisdom = interaction.value) }
                updatePathfinderWizard { wizard -> wizard.copy(wisdom = interaction.value) }
            }
            is CharactersInteraction.WizardCharismaChanged -> {
                updateWizard { wizard -> wizard.copy(charisma = interaction.value) }
                updatePathfinderWizard { wizard -> wizard.copy(charisma = interaction.value) }
            }
            is CharactersInteraction.WizardHitPointsChanged -> {
                updateWizard { wizard -> wizard.copy(hitPoints = interaction.value) }
                updatePathfinderWizard { wizard -> wizard.copy(hitPoints = interaction.value) }
            }
            is CharactersInteraction.WizardMaxHitPointsChanged -> {
                updateWizard { wizard -> wizard.copy(maxHitPoints = interaction.value) }
                updatePathfinderWizard { wizard -> wizard.copy(maxHitPoints = interaction.value) }
            }
            is CharactersInteraction.WizardArmorClassChanged -> {
                updateWizard { wizard -> wizard.copy(armorClass = interaction.value) }
                updatePathfinderWizard { wizard -> wizard.copy(armorClass = interaction.value) }
            }
            is CharactersInteraction.WizardWalkSpeedChanged -> updateWizard { wizard ->
                wizard.copy(walkSpeed = interaction.value.filter { it.isDigit() }.take(3))
            }
            CharactersInteraction.WizardCompanionAdded -> updateWizard { wizard ->
                wizard.copy(
                    companions = wizard.companions + emptyCompanionDraft(),
                    companionError = null,
                )
            }
            is CharactersInteraction.WizardCompanionRemoved -> updateWizard { wizard ->
                wizard.copy(
                    companions = wizard.companions.filterIndexed { index, _ ->
                        index != interaction.index
                    },
                    companionError = null,
                )
            }
            is CharactersInteraction.WizardCompanionKindSelected -> updateWizardCompanion(
                interaction.index,
            ) { draft ->
                draft.copy(kind = interaction.kind)
            }
            is CharactersInteraction.WizardCompanionUseExistingChanged -> updateWizardCompanion(
                interaction.index,
            ) { draft ->
                draft.copy(useExisting = interaction.useExisting)
            }
            is CharactersInteraction.WizardCompanionTargetSelected -> updateWizardCompanion(
                interaction.index,
            ) { draft ->
                draft.copy(existingKey = interaction.key)
            }
            is CharactersInteraction.WizardCompanionNameChanged -> updateWizardCompanion(
                interaction.index,
            ) { draft ->
                draft.copy(newName = interaction.name)
            }
            is CharactersInteraction.WizardCompanionCreatureChanged -> updateWizardCompanion(
                interaction.index,
            ) { draft ->
                draft.copy(newCreature = interaction.creature)
            }
            CharactersInteraction.WizardNextSelected -> {
                if (pathfinderWizardFrom(_state.value) != null) {
                    advancePathfinderWizard()
                } else {
                    advanceWizard()
                }
            }
            CharactersInteraction.WizardBackSelected -> {
                if (pathfinderWizardFrom(_state.value) != null) {
                    rewindPathfinderWizard()
                } else {
                    rewindWizard()
                }
            }
            CharactersInteraction.WizardSaved -> {
                if (pathfinderWizardFrom(_state.value) != null) {
                    savePathfinderWizard()
                } else {
                    saveWizard()
                }
            }
            CharactersInteraction.WizardDismissed -> {
                updateWizardState(null)
                updatePathfinderWizardState(null)
            }
            CharactersInteraction.SrdMonsterImportOpened -> openSrdMonsterPicker()
            CharactersInteraction.SrdMonsterImportDismissed -> setSrdMonsterPicker(open = false)
            is CharactersInteraction.SrdMonsterSelected -> addSrdMonster(interaction.name)
            is CharactersInteraction.PathfinderAncestryChanged -> {
                updatePathfinderEditor { editor ->
                    editor?.copy(ancestry = interaction.ancestry, heritage = "")
                }
                updatePathfinderWizard { wizard ->
                    wizard.copy(ancestry = interaction.ancestry, heritage = "")
                }
            }
            is CharactersInteraction.PathfinderHeritageChanged -> {
                updatePathfinderEditor { editor -> editor?.copy(heritage = interaction.heritage) }
                updatePathfinderWizard { wizard -> wizard.copy(heritage = interaction.heritage) }
            }
            is CharactersInteraction.PathfinderBackgroundChanged -> {
                updatePathfinderEditor { editor ->
                    editor?.copy(background = interaction.background)
                }
                updatePathfinderWizard { wizard -> wizard.copy(background = interaction.background) }
            }
            is CharactersInteraction.PathfinderClassChanged -> {
                updatePathfinderEditor { editor ->
                    editor?.copy(className = interaction.className, subclass = "")
                }
                updatePathfinderWizard { wizard ->
                    wizard.copy(className = interaction.className, subclass = "")
                }
            }
            is CharactersInteraction.PathfinderSubclassChanged -> {
                updatePathfinderEditor { editor -> editor?.copy(subclass = interaction.subclass) }
                updatePathfinderWizard { wizard -> wizard.copy(subclass = interaction.subclass) }
            }
            is CharactersInteraction.PathfinderLevelChanged -> {
                updatePathfinderEditor { editor -> editor?.copy(levelText = interaction.level) }
                updatePathfinderWizard { wizard -> wizard.copy(levelText = interaction.level) }
            }
            is CharactersInteraction.PathfinderPerceptionChanged -> {
                updatePathfinderEditor { editor -> editor?.copy(perception = interaction.value) }
                updatePathfinderWizard { wizard -> wizard.copy(perception = interaction.value) }
            }
            is CharactersInteraction.PathfinderLandSpeedChanged -> {
                updatePathfinderEditor { editor -> editor?.copy(landSpeed = interaction.value) }
                updatePathfinderWizard { wizard -> wizard.copy(landSpeed = interaction.value) }
            }
            is CharactersInteraction.PathfinderDyingChanged -> updatePathfinderEditor { editor ->
                editor?.copy(dying = interaction.value)
            }
            is CharactersInteraction.PathfinderWoundedChanged -> updatePathfinderEditor { editor ->
                editor?.copy(wounded = interaction.value)
            }
            CharactersInteraction.PathfinderSkillAdded -> {
                updatePathfinderEditor { editor ->
                    editor?.copy(
                        skills = editor.skills + CharactersViewState.PathfinderSkillEditor(
                            name = "",
                            rank = Pathfinder2ESkillRank.Untrained,
                        ),
                    )
                }
                updatePathfinderWizard { wizard ->
                    wizard.copy(
                        skills = wizard.skills + CharactersViewState.PathfinderSkillEditor(
                            name = "",
                            rank = Pathfinder2ESkillRank.Untrained,
                        ),
                    )
                }
            }
            is CharactersInteraction.PathfinderSkillRemoved -> {
                updatePathfinderEditor { editor ->
                    editor?.copy(
                        skills = editor.skills.filterIndexed { index, _ -> index != interaction.index },
                    )
                }
                updatePathfinderWizard { wizard ->
                    wizard.copy(
                        skills = wizard.skills.filterIndexed { index, _ -> index != interaction.index },
                    )
                }
            }
            is CharactersInteraction.PathfinderSkillNameChanged -> {
                updatePathfinderSkill(interaction.index) { skill -> skill.copy(name = interaction.name) }
            }
            is CharactersInteraction.PathfinderSkillRankChanged -> {
                updatePathfinderSkill(interaction.index) { skill -> skill.copy(rank = interaction.rank) }
            }
            CharactersInteraction.PathfinderFeatAdded -> updatePathfinderEditor { editor ->
                editor?.copy(
                    feats = editor.feats + CharactersViewState.PathfinderFeatEditor(
                        name = "",
                        type = "",
                        description = "",
                    ),
                )
            }
            is CharactersInteraction.PathfinderFeatRemoved -> updatePathfinderEditor { editor ->
                editor?.copy(
                    feats = editor.feats.filterIndexed { index, _ -> index != interaction.index },
                )
            }
            is CharactersInteraction.PathfinderFeatNameChanged -> updatePathfinderFeat(
                interaction.index,
            ) { feat ->
                feat.copy(name = interaction.name)
            }
            is CharactersInteraction.PathfinderFeatTypeChanged -> updatePathfinderFeat(
                interaction.index,
            ) { feat ->
                feat.copy(type = interaction.type)
            }
            is CharactersInteraction.PathfinderFeatDescriptionChanged -> updatePathfinderFeat(
                interaction.index,
            ) { feat ->
                feat.copy(description = interaction.description)
            }
            CharactersInteraction.PathfinderSpellAdded -> updatePathfinderEditor { editor ->
                editor?.copy(
                    spells = editor.spells + CharactersViewState.PathfinderSpellEditor(
                        name = "",
                        rankText = "1",
                        prepared = false,
                    ),
                )
            }
            is CharactersInteraction.PathfinderSpellRemoved -> updatePathfinderEditor { editor ->
                editor?.copy(
                    spells = editor.spells.filterIndexed { index, _ -> index != interaction.index },
                )
            }
            is CharactersInteraction.PathfinderSpellNameChanged -> updatePathfinderSpell(
                interaction.index,
            ) { spell ->
                spell.copy(name = interaction.name)
            }
            is CharactersInteraction.PathfinderSpellRankChanged -> updatePathfinderSpell(
                interaction.index,
            ) { spell ->
                spell.copy(rankText = interaction.rank)
            }
            is CharactersInteraction.PathfinderSpellPreparedChanged -> updatePathfinderSpell(
                interaction.index,
            ) { spell ->
                spell.copy(prepared = interaction.prepared)
            }
        }
    }

    private fun observe() {
        observeJob?.cancel()
        _state.value = CharactersViewState.Loading
        observeJob = appScope.scope.launch {
            combine(
                combine(
                    observeActiveContextDetails(),
                    observePeople(),
                    observeRelationships(),
                    observeLore(),
                    observeQuests(),
                ) { details, people, relationships, lore, quests ->
                    PeopleLoad(details, people, relationships, lore, quests)
                },
                combine(
                    observeCompanions(),
                    observeFactions(),
                    observeMemberships(),
                    observePickerCatalog(),
                ) { companions, factions, memberships, pickerCatalog ->
                    ExtraLoad(companions, factions, memberships, pickerCatalog)
                },
            ) { load, extra ->
                LoadedSnapshot(
                    load.details,
                    load.people,
                    load.relationships,
                    extra.companions,
                    load.lore,
                    load.quests,
                    extra.factions,
                    extra.memberships,
                    extra.pickerCatalog,
                )
            }
                .catch { error ->
                    _state.value = CharactersViewState.Error(
                        message = error.message ?: "Could not load characters",
                        canRetry = true,
                    )
                }
                .collect { snapshot ->
                    applyLoaded(
                        snapshot.details,
                        snapshot.people,
                        snapshot.relationships,
                        snapshot.companions,
                        snapshot.lore,
                        snapshot.quests,
                        snapshot.factions,
                        snapshot.memberships,
                        snapshot.pickerCatalog,
                    )
                }
        }
    }

    private fun applyLoaded(
        details: ActiveContextDetails,
        people: PeopleSnapshot,
        relationships: List<PersonRelationship>,
        companions: List<PersonCompanion>,
        lore: List<Lore>,
        quests: List<Quest>,
        factions: List<Faction>,
        memberships: List<FactionMembership>,
        pickerCatalog: FifthEditionPickerCatalog,
    ) {
        val world = details.world
        if (world == null) {
            latestWorldPeople = emptyList()
            latestCampaignPeople = emptyList()
            selectedKey = null
            _state.value = CharactersViewState.NoActiveWorld
            return
        }
        latestWorldPeople = people.worldPeople
        latestCampaignPeople = people.campaignPeople
        latestRelationships = relationships
        latestCompanions = companions
        latestLore = lore
        latestQuests = quests
        latestFactions = factions
        latestMemberships = memberships
        latestPickerCatalog = pickerCatalog
        latestWorldName = world.name
        latestCampaignName = details.campaign?.name
        latestWorldSystem = world.defaultGameSystem
        latestCampaignSystem = details.campaign?.resolvedGameSystem(world.defaultGameSystem)
            ?: world.defaultGameSystem
        hasActiveCampaign = details.campaign != null
        val current = _state.value
        val editor = editorFrom(current)
        val pending = pendingCreate
        val pendingWizard = pendingWizardFrom(pending)
        if (pendingWizard != null || pending != PendingCreate.PlayerCharacter) {
            pendingCreate = null
        }
        val wizard = if (pendingWizard != null) {
            pendingWizard.fifthEdition
        } else {
            wizardFrom(current)?.copy(companionTargets = companionTargets(null))
        }
        val pathfinderWizard = if (pendingWizard != null) {
            pendingWizard.pathfinder
        } else {
            pathfinderWizardFrom(current)
        }
        val generator = generatorFrom(current)
        if (people.worldPeople.isEmpty() && people.campaignPeople.isEmpty()) {
            selectedKey = null
            _state.value = CharactersViewState.Empty(
                worldName = world.name,
                campaignName = details.campaign?.name,
                editor = editor,
                pathfinderEditor = pathfinderEditorFrom(current),
                generator = generator,
                wizard = wizard,
                pathfinderWizard = pathfinderWizard,
                pickerCatalog = latestPickerCatalog,
                srdMonsterPicker = srdMonsterPicker(),
                worldGameSystemIsFifthEdition = latestWorldSystem == GameSystem.FifthEdition,
            )
            return
        }
        _state.value = contentState(
            editor = editor,
            pathfinderEditor = pathfinderEditorFrom(current),
            generator = generator,
            wizard = wizard,
            pathfinderWizard = pathfinderWizard,
            relationshipEditor = relationshipEditorFrom(current)?.copy(
                targets = relationshipTargets(selectedKey),
                factions = factionOptions(),
            ),
            membershipEditor = membershipEditorFrom(current)?.copy(
                factions = availableMembershipFactions(selectedKey),
            ),
            companionEditor = companionEditorFrom(current)?.copy(
                targets = companionTargets(selectedKey),
            ),
            pendingDelete = pendingDeleteFrom(current),
            blockDeleteReason = blockReasonFrom(current),
        )
    }

    private fun refreshContent() {
        val current = _state.value
        if (current !is CharactersViewState.Content) {
            return
        }
        _state.value = contentState(
            editor = current.editor,
            pathfinderEditor = current.pathfinderEditor,
            generator = current.generator,
            wizard = current.wizard,
            pathfinderWizard = current.pathfinderWizard,
            relationshipEditor = current.relationshipEditor,
            membershipEditor = current.membershipEditor,
            companionEditor = current.companionEditor,
            pendingDelete = current.pendingDelete,
            blockDeleteReason = current.blockDeleteReason,
        )
    }

    private fun contentState(
        editor: CharactersViewState.CharacterEditorState?,
        pathfinderEditor: CharactersViewState.PathfinderEditorState?,
        generator: CharactersViewState.GeneratorState?,
        wizard: CharactersViewState.CreationWizardState?,
        pathfinderWizard: CharactersViewState.PathfinderWizardState?,
        relationshipEditor: CharactersViewState.RelationshipEditorState?,
        membershipEditor: CharactersViewState.MembershipEditorState?,
        companionEditor: CharactersViewState.CompanionEditorState?,
        pendingDelete: CharactersViewState.PendingDelete?,
        blockDeleteReason: String?,
    ): CharactersViewState.Content {
        val rows = visibleRows()
        val matched = selectedKey?.let { key -> selectedPerson(key) }
        val selected = matched ?: rows.firstOrNull()?.let { selectedPerson(it.key) }
        if (matched != null || selectedKey == null) {
            selectedKey = selected?.key
        }
        return CharactersViewState.Content(
            worldName = latestWorldName,
            campaignName = latestCampaignName,
            people = rows,
            selected = selected,
            kindFilter = kindFilter,
            membershipFilter = membershipFilter,
            searchQuery = searchQuery,
            editor = editor,
            pathfinderEditor = pathfinderEditor,
            generator = generator,
            wizard = wizard,
            pathfinderWizard = pathfinderWizard,
            relationshipEditor = relationshipEditor,
            membershipEditor = membershipEditor,
            companionEditor = companionEditor,
            pendingDelete = pendingDelete,
            blockDeleteReason = blockDeleteReason,
            pickerCatalog = latestPickerCatalog,
            srdMonsterPicker = srdMonsterPicker(),
            worldGameSystemIsFifthEdition = latestWorldSystem == GameSystem.FifthEdition,
        )
    }

    private fun visibleRows(): List<CharactersViewState.PersonRow> {
        val worldRows = latestWorldPeople.map { person ->
            CharactersViewState.PersonRow(
                key = CharactersViewState.PersonKey(PersonMembership.WorldLibrary, person.id),
                name = person.name,
                kind = person.kind,
                subtitle = rowSubtitle(person.kind, PersonMembership.WorldLibrary, person.sheet),
                avatarPath = avatarFileStore.pathIfPresent(PersonRef.World(person.id)),
            )
        }
        val campaignRows = latestCampaignPeople.map { person ->
            val membership = PersonMembership.ThisCampaign
            val sheet = resolvedSheet(person)
            CharactersViewState.PersonRow(
                key = CharactersViewState.PersonKey(membership, person.id),
                name = person.name,
                kind = person.kind,
                subtitle = rowSubtitle(person.kind, membership, sheet, person.isWorldReference()),
                avatarPath = avatarPathForCampaign(person),
            )
        }
        return (worldRows + campaignRows)
            .filter { row -> kindFilter == null || row.kind == kindFilter }
            .filter { row -> membershipFilter == null || row.key.membership == membershipFilter }
            .filter { row ->
                searchQuery.isBlank() || row.name.contains(searchQuery, ignoreCase = true)
            }
            .sortedBy { it.name.lowercase() }
    }

    private fun rowSubtitle(
        kind: PersonKind,
        membership: PersonMembership,
        sheet: PersonSheet,
        isReference: Boolean = false,
    ): String {
        val membershipLabel = if (isReference) {
            "Campaign reference"
        } else {
            membership.displayName
        }
        val lineage = sheet.lineageLabel().takeIf { it.isNotBlank() }
        val level = "Lv ${sheet.totalLevel()}"
        return listOfNotNull(kind.displayName, membershipLabel, lineage, level).joinToString(" · ")
    }

    private fun personKeyFrom(interaction: CharactersInteraction): CharactersViewState.PersonKey {
        return when (interaction) {
            is CharactersInteraction.PersonSelected -> interaction.key
            is CharactersInteraction.PersonOpened -> interaction.key
            else -> CharactersViewState.PersonKey(PersonMembership.WorldLibrary, "")
        }
    }

    private fun selectedPerson(key: CharactersViewState.PersonKey): CharactersViewState.SelectedPerson? {
        return when (key.membership) {
            PersonMembership.WorldLibrary -> {
                val person = latestWorldPeople.firstOrNull { it.id == key.id } ?: return null
                val alreadyAdded = latestCampaignPeople.any { it.worldPersonId == person.id }
                CharactersViewState.SelectedPerson(
                    key = key,
                    kind = person.kind,
                    name = person.name,
                    description = person.description,
                    sheet = person.sheet,
                    overlayHitPoints = "",
                    overlayNotes = "",
                    isWorldReference = false,
                    canAddToCampaign = hasActiveCampaign && !alreadyAdded,
                    relationships = relationshipsFor(PersonRef.World(person.id)),
                    memberships = membershipsFor(key, null),
                    factionOptions = factionOptions(),
                    companions = companionsFor(PersonRef.World(person.id)),
                    attachedLore = attachedLore(person.id),
                    attachedQuests = attachedQuests(
                        worldPersonId = person.id,
                        campaignPersonId = null,
                    ),
                    relationshipTargets = relationshipTargets(key),
                    avatarPath = avatarFileStore.pathIfPresent(PersonRef.World(person.id)),
                    voiceClipPath = voiceClipFileStore.pathIfPresent(VoiceClipRef.WorldPerson(person.id)),
                    isRecordingVoice = isRecordingVoice,
                    isPlayingVoice = isPlayingVoice,
                )
            }
            PersonMembership.ThisCampaign -> {
                val person = latestCampaignPeople.firstOrNull { it.id == key.id } ?: return null
                CharactersViewState.SelectedPerson(
                    key = key,
                    kind = person.kind,
                    name = person.name,
                    description = person.description,
                    sheet = resolvedSheet(person),
                    overlayHitPoints = person.overlayHitPoints?.toString().orEmpty(),
                    overlayNotes = person.overlayNotes,
                    isWorldReference = person.isWorldReference(),
                    canAddToCampaign = false,
                    relationships = relationshipsFor(PersonRef.Campaign(person.id)),
                    memberships = membershipsFor(key, person),
                    factionOptions = factionOptions(),
                    companions = companionsFor(PersonRef.Campaign(person.id)),
                    attachedLore = attachedLore(person.id, person.worldPersonId),
                    attachedQuests = attachedQuests(
                        worldPersonId = person.worldPersonId,
                        campaignPersonId = person.id,
                    ),
                    relationshipTargets = relationshipTargets(key),
                    avatarPath = avatarPathForCampaign(person),
                    voiceClipPath = voicePathForCampaign(person),
                    isRecordingVoice = isRecordingVoice,
                    isPlayingVoice = isPlayingVoice,
                )
            }
        }
    }

    private fun avatarPathForCampaign(person: CampaignPerson): String? {
        return avatarFileStore.pathIfPresent(PersonRef.Campaign(person.id))
            ?: person.worldPersonId?.let { worldId ->
                avatarFileStore.pathIfPresent(PersonRef.World(worldId))
            }
    }

    private fun voicePathForCampaign(person: CampaignPerson): String? {
        return voiceClipFileStore.pathIfPresent(VoiceClipRef.CampaignPerson(person.id))
            ?: person.worldPersonId?.let { worldId ->
                voiceClipFileStore.pathIfPresent(VoiceClipRef.WorldPerson(worldId))
            }
    }

    private fun voiceClipRefFrom(key: CharactersViewState.PersonKey): VoiceClipRef {
        return when (key.membership) {
            PersonMembership.WorldLibrary -> VoiceClipRef.WorldPerson(key.id)
            PersonMembership.ThisCampaign -> VoiceClipRef.CampaignPerson(key.id)
        }
    }

    private fun saveVoiceClip(path: String) {
        val key = selectedKey ?: return
        val file = java.io.File(path)
        if (!file.isFile) {
            return
        }
        stopVoicePlayback()
        if (isRecordingVoice) {
            voiceClipRecorder.stop()
            isRecordingVoice = false
        }
        appScope.scope.launch {
            setVoiceClip(voiceClipRefFrom(key), file.readBytes())
            refreshContent()
        }
    }

    private fun toggleVoiceRecord() {
        val key = selectedKey ?: return
        if (isRecordingVoice) {
            val wav = voiceClipRecorder.stop()
            isRecordingVoice = false
            if (wav != null) {
                appScope.scope.launch {
                    setVoiceClip(voiceClipRefFrom(key), wav)
                    refreshContent()
                }
            } else {
                refreshContent()
            }
            return
        }
        stopVoicePlayback()
        isRecordingVoice = voiceClipRecorder.start()
        refreshContent()
    }

    private fun toggleVoicePlay() {
        val key = selectedKey ?: return
        if (isPlayingVoice) {
            stopVoicePlayback()
            refreshContent()
            return
        }
        val path = when (key.membership) {
            PersonMembership.WorldLibrary -> {
                voiceClipFileStore.pathIfPresent(VoiceClipRef.WorldPerson(key.id))
            }
            PersonMembership.ThisCampaign -> {
                latestCampaignPeople.firstOrNull { it.id == key.id }?.let(::voicePathForCampaign)
            }
        } ?: return
        isPlayingVoice = voiceClipPlayer.play(path) {
            appScope.scope.launch {
                isPlayingVoice = false
                refreshContent()
            }
        }
        refreshContent()
    }

    private fun removeVoiceClip() {
        val key = selectedKey ?: return
        stopVoicePlayback()
        appScope.scope.launch {
            clearVoiceClip(voiceClipRefFrom(key))
            refreshContent()
        }
    }

    private fun stopVoiceSession() {
        if (isRecordingVoice) {
            voiceClipRecorder.stop()
            isRecordingVoice = false
        }
        stopVoicePlayback()
    }

    private fun stopVoicePlayback() {
        if (isPlayingVoice) {
            voiceClipPlayer.stop()
            isPlayingVoice = false
        }
    }

    private fun saveAvatar(path: String) {
        val key = selectedKey ?: return
        val file = java.io.File(path)
        if (!file.isFile) {
            return
        }
        appScope.scope.launch {
            setPersonAvatar(personRefFrom(key), file.readBytes())
        }
    }

    private fun removeAvatar() {
        val key = selectedKey ?: return
        appScope.scope.launch {
            clearPersonAvatar(personRefFrom(key))
        }
    }

    private fun personRefFrom(key: CharactersViewState.PersonKey): PersonRef {
        return when (key.membership) {
            PersonMembership.WorldLibrary -> PersonRef.World(key.id)
            PersonMembership.ThisCampaign -> PersonRef.Campaign(key.id)
        }
    }

    private fun resolvedSheet(person: CampaignPerson): PersonSheet {
        val worldId = person.worldPersonId ?: return person.sheet
        return latestWorldPeople.firstOrNull { it.id == worldId }?.sheet ?: person.sheet
    }

    private fun systemFor(membership: PersonMembership): GameSystem {
        return when (membership) {
            PersonMembership.WorldLibrary -> latestWorldSystem
            PersonMembership.ThisCampaign -> latestCampaignSystem
        }
    }

    private fun relationshipsFor(ref: PersonRef): List<CharactersViewState.RelationshipRow> {
        return latestRelationships
            .filter { relationship ->
                sameRef(relationship.from, ref) || sameRef(relationship.to, ref)
            }
            .map { relationship ->
                val other = if (sameRef(relationship.from, ref)) relationship.to else relationship.from
                CharactersViewState.RelationshipRow(
                    id = relationship.id,
                    label = personName(other),
                    type = relationship.type,
                    description = relationship.description,
                    factionName = relationship.factionId?.let { factionId ->
                        latestFactions.firstOrNull { it.id == factionId }?.name
                    },
                )
            }
    }

    private fun relationshipTargets(
        selected: CharactersViewState.PersonKey?,
    ): List<CharactersViewState.RelationshipTarget> {
        val worldTargets = latestWorldPeople
            .filter { person ->
                selected?.membership != PersonMembership.WorldLibrary || selected.id != person.id
            }
            .map { person ->
                CharactersViewState.RelationshipTarget(
                    key = CharactersViewState.PersonKey(PersonMembership.WorldLibrary, person.id),
                    name = "${person.name} (world)",
                )
            }
        val campaignTargets = latestCampaignPeople
            .filter { person ->
                selected?.membership != PersonMembership.ThisCampaign || selected.id != person.id
            }
            .map { person ->
                CharactersViewState.RelationshipTarget(
                    key = CharactersViewState.PersonKey(PersonMembership.ThisCampaign, person.id),
                    name = "${person.name} (campaign)",
                )
            }
        return (worldTargets + campaignTargets).sortedBy { it.name.lowercase() }
    }

    private fun attachedQuests(
        worldPersonId: String?,
        campaignPersonId: String?,
    ): List<CharactersViewState.AttachedQuest> {
        return latestQuests.filter { quest ->
            quest.links.any { link ->
                (link.kind == QuestLinkKind.WORLD_PERSON && link.targetId == worldPersonId) ||
                    (link.kind == QuestLinkKind.CAMPAIGN_PERSON && link.targetId == campaignPersonId)
            }
        }.map { quest ->
            CharactersViewState.AttachedQuest(questId = quest.id, title = quest.title)
        }
    }

    private fun attachedLore(vararg personIds: String?): List<CharactersViewState.AttachedLore> {
        val ids = personIds.filterNotNull().toSet()
        if (ids.isEmpty()) {
            return emptyList()
        }
        return latestLore.filter { it.characterId in ids }.map { lore ->
            CharactersViewState.AttachedLore(loreId = lore.id, title = lore.title)
        }
    }

    private fun personName(ref: PersonRef): String {
        return when (ref) {
            is PersonRef.World -> latestWorldPeople.firstOrNull { it.id == ref.id }?.name
            is PersonRef.Campaign -> latestCampaignPeople.firstOrNull { it.id == ref.id }?.name
        } ?: "Unknown person"
    }

    private fun sameRef(left: PersonRef, right: PersonRef): Boolean {
        return left.id == right.id && left::class == right::class
    }

    private fun toRef(key: CharactersViewState.PersonKey): PersonRef {
        return when (key.membership) {
            PersonMembership.WorldLibrary -> PersonRef.World(key.id)
            PersonMembership.ThisCampaign -> PersonRef.Campaign(key.id)
        }
    }

    private fun openCreateWizard() {
        openWizard(PendingCreate.Person)
    }

    private fun openCreatePcWizard() {
        openWizard(PendingCreate.PlayerCharacter)
    }

    private fun openWizard(pending: PendingCreate) {
        when (_state.value) {
            is CharactersViewState.Empty,
            is CharactersViewState.Content,
            -> {
                if (pending == PendingCreate.PlayerCharacter && !hasActiveCampaign) {
                    pendingCreate = pending
                } else {
                    showCreateWizard(pending)
                }
            }
            CharactersViewState.Loading, is CharactersViewState.Error -> {
                pendingCreate = pending
            }
            CharactersViewState.NoActiveWorld -> Unit
        }
    }

    private fun showCreateWizard(pending: PendingCreate) {
        val pendingWizard = pendingWizardFrom(pending) ?: return
        when (val current = _state.value) {
            is CharactersViewState.Empty -> {
                _state.value = current.copy(
                    wizard = pendingWizard.fifthEdition,
                    pathfinderWizard = pendingWizard.pathfinder,
                )
            }
            is CharactersViewState.Content -> {
                _state.value = current.copy(
                    wizard = pendingWizard.fifthEdition,
                    pathfinderWizard = pendingWizard.pathfinder,
                )
            }
            else -> Unit
        }
    }

    private fun pendingWizardFrom(pending: PendingCreate?): PendingWizard? {
        if (pending == null) {
            return null
        }
        if (pending == PendingCreate.PlayerCharacter && !hasActiveCampaign) {
            return null
        }
        val membership = pending.membership()
        val kind = pending.kind()
        return when (systemFor(membership)) {
            GameSystem.FifthEdition -> PendingWizard(
                fifthEdition = createWizard(membership, kind),
                pathfinder = null,
            )
            GameSystem.Pathfinder2E -> PendingWizard(
                fifthEdition = null,
                pathfinder = createPathfinderWizard(membership, kind),
            )
        }
    }

    private fun openEditEditor(key: CharactersViewState.PersonKey) {
        val personId: String
        val membership: PersonMembership
        val isWorldReference: Boolean
        val kind: PersonKind
        val name: String
        val description: String
        val sheet: PersonSheet
        val overlayHitPoints: String
        val overlayNotes: String
        when (key.membership) {
            PersonMembership.WorldLibrary -> {
                val person = latestWorldPeople.firstOrNull { it.id == key.id } ?: return
                personId = person.id
                membership = PersonMembership.WorldLibrary
                isWorldReference = false
                kind = person.kind
                name = person.name
                description = person.description
                sheet = person.sheet
                overlayHitPoints = ""
                overlayNotes = ""
            }
            PersonMembership.ThisCampaign -> {
                val person = latestCampaignPeople.firstOrNull { it.id == key.id } ?: return
                personId = person.id
                membership = PersonMembership.ThisCampaign
                isWorldReference = person.isWorldReference()
                kind = person.kind
                name = person.name
                description = person.description
                sheet = resolvedSheet(person)
                overlayHitPoints = person.overlayHitPoints?.toString().orEmpty()
                overlayNotes = person.overlayNotes
            }
        }
        when (val current = _state.value) {
            is CharactersViewState.Content -> {
                _state.value = when (sheet) {
                    is FifthEditionSheet -> current.copy(
                        editor = editorFromPerson(
                            personId = personId,
                            membership = membership,
                            isWorldReference = isWorldReference,
                            kind = kind,
                            name = name,
                            description = description,
                            sheet = sheet,
                            overlayHitPoints = overlayHitPoints,
                            overlayNotes = overlayNotes,
                        ),
                        pathfinderEditor = null,
                    )
                    is Pathfinder2ESheet -> current.copy(
                        editor = null,
                        pathfinderEditor = pathfinderEditorFromPerson(
                            personId = personId,
                            membership = membership,
                            isWorldReference = isWorldReference,
                            kind = kind,
                            name = name,
                            description = description,
                            sheet = sheet,
                            overlayHitPoints = overlayHitPoints,
                            overlayNotes = overlayNotes,
                        ),
                    )
                }
            }
            else -> Unit
        }
    }

    private fun editorFromPerson(
        personId: String?,
        membership: PersonMembership,
        isWorldReference: Boolean,
        kind: PersonKind,
        name: String,
        description: String,
        sheet: FifthEditionSheet,
        overlayHitPoints: String,
        overlayNotes: String,
    ): CharactersViewState.CharacterEditorState {
        return CharactersViewState.CharacterEditorState(
            personId = personId,
            membership = membership,
            isWorldReference = isWorldReference,
            canChangeMembership = personId == null && hasActiveCampaign,
            hasActiveCampaign = hasActiveCampaign,
            kind = kind,
            name = name,
            description = description,
            race = sheet.race,
            classLevels = sheet.classLevels.map { level ->
                CharactersViewState.ClassLevelEditor(
                    className = level.className,
                    subclass = level.subclass,
                    levelText = level.level.toString(),
                )
            },
            strength = sheet.abilityScores.strength.toString(),
            dexterity = sheet.abilityScores.dexterity.toString(),
            constitution = sheet.abilityScores.constitution.toString(),
            intelligence = sheet.abilityScores.intelligence.toString(),
            wisdom = sheet.abilityScores.wisdom.toString(),
            charisma = sheet.abilityScores.charisma.toString(),
            hitPoints = sheet.hitPoints.toString(),
            maxHitPoints = sheet.maxHitPoints.toString(),
            temporaryHitPoints = sheet.temporaryHitPoints.toString(),
            armorClass = sheet.armorClass.toString(),
            walkSpeed = sheet.walkSpeed.toString(),
            creatureSize = sheet.creatureSize,
            concentratingSpell = sheet.concentratingSpell,
            skills = skillEditorsFrom(sheet),
            spellSlots = sheet.spellSlots.map { slot ->
                CharactersViewState.SpellSlotEditor(
                    levelText = slot.level.toString(),
                    maximumText = slot.maximum.toString(),
                    usedText = slot.used.toString(),
                )
            },
            deathSuccesses = sheet.deathSaves.successes.toString(),
            deathFailures = sheet.deathSaves.failures.toString(),
            items = sheet.items.map { item ->
                CharactersViewState.ItemEditor(
                    name = item.name,
                    quantityText = item.quantity.toString(),
                    notes = item.notes,
                )
            },
            features = sheet.features.map { feature ->
                CharactersViewState.FeatureEditor(
                    name = feature.name,
                    description = feature.description,
                )
            },
            spells = sheet.spells.map { spell ->
                CharactersViewState.SpellEditor(
                    name = spell.name,
                    levelText = spell.level.toString(),
                    prepared = spell.prepared,
                )
            },
            notes = sheet.notes,
            overlayHitPoints = overlayHitPoints,
            overlayNotes = overlayNotes,
            nameError = null,
            membershipError = null,
        )
    }

    private fun pathfinderEditorFromPerson(
        personId: String?,
        membership: PersonMembership,
        isWorldReference: Boolean,
        kind: PersonKind,
        name: String,
        description: String,
        sheet: Pathfinder2ESheet,
        overlayHitPoints: String,
        overlayNotes: String,
    ): CharactersViewState.PathfinderEditorState {
        return CharactersViewState.PathfinderEditorState(
            personId = personId,
            membership = membership,
            isWorldReference = isWorldReference,
            canChangeMembership = personId == null && hasActiveCampaign,
            hasActiveCampaign = hasActiveCampaign,
            kind = kind,
            name = name,
            description = description,
            ancestry = sheet.ancestry,
            heritage = sheet.heritage,
            background = sheet.background,
            className = sheet.className,
            subclass = sheet.subclass,
            levelText = sheet.level.toString(),
            strength = sheet.abilityScores.strength.toString(),
            dexterity = sheet.abilityScores.dexterity.toString(),
            constitution = sheet.abilityScores.constitution.toString(),
            intelligence = sheet.abilityScores.intelligence.toString(),
            wisdom = sheet.abilityScores.wisdom.toString(),
            charisma = sheet.abilityScores.charisma.toString(),
            hitPoints = sheet.hitPoints.toString(),
            maxHitPoints = sheet.maxHitPoints.toString(),
            temporaryHitPoints = sheet.temporaryHitPoints.toString(),
            armorClass = sheet.armorClass.toString(),
            perception = sheet.perception.toString(),
            landSpeed = sheet.landSpeed.toString(),
            dying = sheet.dying.toString(),
            wounded = sheet.wounded.toString(),
            skills = if (sheet.skills.isEmpty()) {
                defaultPathfinderSkills()
            } else {
                sheet.skills.map { skill ->
                    CharactersViewState.PathfinderSkillEditor(
                        name = skill.name,
                        rank = skill.rank,
                    )
                }
            },
            feats = sheet.feats.map { feat ->
                CharactersViewState.PathfinderFeatEditor(
                    name = feat.name,
                    type = feat.type,
                    description = feat.description,
                )
            },
            spells = sheet.spells.map { spell ->
                CharactersViewState.PathfinderSpellEditor(
                    name = spell.name,
                    rankText = spell.rank.toString(),
                    prepared = spell.prepared,
                )
            },
            notes = sheet.notes,
            overlayHitPoints = overlayHitPoints,
            overlayNotes = overlayNotes,
            nameError = null,
            membershipError = null,
        )
    }

    private fun changeMembership(
        editor: CharactersViewState.CharacterEditorState,
        membership: PersonMembership,
    ): CharactersViewState.CharacterEditorState {
        val kind = when (membership) {
            PersonMembership.WorldLibrary -> {
                if (editor.kind == PersonKind.PlayerCharacter) PersonKind.Npc else editor.kind
            }
            PersonMembership.ThisCampaign -> editor.kind
        }
        return editor.copy(
            membership = membership,
            kind = kind,
            membershipError = null,
        )
    }

    private fun changePathfinderMembership(
        editor: CharactersViewState.PathfinderEditorState,
        membership: PersonMembership,
    ): CharactersViewState.PathfinderEditorState {
        return editor.copy(
            membership = membership,
            kind = kindForMembership(membership, editor.kind),
            membershipError = null,
        )
    }

    private fun kindForMembership(
        membership: PersonMembership,
        kind: PersonKind,
    ): PersonKind {
        return when (membership) {
            PersonMembership.WorldLibrary -> {
                if (kind == PersonKind.PlayerCharacter) PersonKind.Npc else kind
            }
            PersonMembership.ThisCampaign -> kind
        }
    }

    private fun applyWizardMembership(membership: PersonMembership) {
        val nextSystem = systemFor(membership)
        val pathfinder = pathfinderWizardFrom(_state.value)
        val fifth = wizardFrom(_state.value)
        when {
            pathfinder != null && nextSystem == GameSystem.Pathfinder2E -> {
                updatePathfinderWizard { wizard ->
                    wizard.copy(
                        membership = membership,
                        kind = kindForMembership(membership, wizard.kind),
                        membershipError = null,
                    )
                }
            }
            fifth != null && nextSystem == GameSystem.FifthEdition -> {
                updateWizard { wizard -> changeWizardMembership(wizard, membership) }
            }
            pathfinder != null && nextSystem == GameSystem.FifthEdition -> {
                updatePathfinderWizardState(null)
                updateWizardState(
                    createWizard().copy(
                        membership = membership,
                        kind = kindForMembership(membership, pathfinder.kind),
                        name = pathfinder.name,
                        description = pathfinder.description,
                        strength = pathfinder.strength,
                        dexterity = pathfinder.dexterity,
                        constitution = pathfinder.constitution,
                        intelligence = pathfinder.intelligence,
                        wisdom = pathfinder.wisdom,
                        charisma = pathfinder.charisma,
                        hitPoints = pathfinder.hitPoints,
                        maxHitPoints = pathfinder.maxHitPoints,
                        armorClass = pathfinder.armorClass,
                    ),
                )
            }
            fifth != null && nextSystem == GameSystem.Pathfinder2E -> {
                updateWizardState(null)
                updatePathfinderWizardState(
                    createPathfinderWizard().copy(
                        membership = membership,
                        kind = kindForMembership(membership, fifth.kind),
                        name = fifth.name,
                        description = fifth.description,
                        strength = fifth.strength,
                        dexterity = fifth.dexterity,
                        constitution = fifth.constitution,
                        intelligence = fifth.intelligence,
                        wisdom = fifth.wisdom,
                        charisma = fifth.charisma,
                        hitPoints = fifth.hitPoints,
                        maxHitPoints = fifth.maxHitPoints,
                        armorClass = fifth.armorClass,
                    ),
                )
            }
        }
    }

    private fun saveEditor() {
        val editor = editorFrom(_state.value) ?: return
        if (editor.name.trim().isEmpty()) {
            updateEditor { current -> current?.copy(nameError = "Name is required") }
            return
        }
        if (editor.membership == PersonMembership.ThisCampaign && !hasActiveCampaign) {
            updateEditor { current ->
                current?.copy(membershipError = "Select a campaign first")
            }
            return
        }
        val sheet = sheetFrom(editor)
        appScope.scope.launch {
            val failed = when {
                editor.personId == null && editor.membership == PersonMembership.WorldLibrary -> {
                    createWorldPerson(
                        WorldPersonDraft(
                            kind = editor.kind,
                            name = editor.name,
                            description = editor.description,
                            sheet = sheet,
                        )
                    ) !is CreateWorldPersonUseCase.Result.Created
                }
                editor.personId == null -> {
                    createCampaignPerson(
                        CampaignPersonDraft(
                            kind = editor.kind,
                            name = editor.name,
                            description = editor.description,
                            sheet = sheet,
                            overlayHitPoints = editor.overlayHitPoints.toIntOrNull(),
                            overlayNotes = editor.overlayNotes,
                        )
                    ) !is CreateCampaignPersonUseCase.Result.Created
                }
                editor.membership == PersonMembership.WorldLibrary -> {
                    updateWorldPerson(
                        editor.personId,
                        WorldPersonDraft(
                            kind = editor.kind,
                            name = editor.name,
                            description = editor.description,
                            sheet = sheet,
                        )
                    ) !is UpdateWorldPersonUseCase.Result.Updated
                }
                else -> {
                    updateCampaignPerson(
                        editor.personId,
                        CampaignPersonDraft(
                            kind = editor.kind,
                            name = editor.name,
                            description = editor.description,
                            sheet = sheet,
                            overlayHitPoints = editor.overlayHitPoints.toIntOrNull(),
                            overlayNotes = editor.overlayNotes,
                        )
                    ) !is UpdateCampaignPersonUseCase.Result.Updated
                }
            }
            if (failed) {
                updateEditor { current -> current?.copy(nameError = "Could not save this person") }
            } else {
                updateEditor { null }
            }
        }
    }

    private fun savePathfinderEditor() {
        val editor = pathfinderEditorFrom(_state.value) ?: return
        if (editor.name.trim().isEmpty()) {
            updatePathfinderEditor { current -> current?.copy(nameError = "Name is required") }
            return
        }
        if (editor.membership == PersonMembership.ThisCampaign && !hasActiveCampaign) {
            updatePathfinderEditor { current ->
                current?.copy(membershipError = "Select a campaign first")
            }
            return
        }
        val sheet = sheetFrom(editor)
        appScope.scope.launch {
            val failed = when {
                editor.personId == null && editor.membership == PersonMembership.WorldLibrary -> {
                    createWorldPerson(
                        WorldPersonDraft(
                            kind = editor.kind,
                            name = editor.name,
                            description = editor.description,
                            sheet = sheet,
                        )
                    ) !is CreateWorldPersonUseCase.Result.Created
                }
                editor.personId == null -> {
                    createCampaignPerson(
                        CampaignPersonDraft(
                            kind = editor.kind,
                            name = editor.name,
                            description = editor.description,
                            sheet = sheet,
                            overlayHitPoints = editor.overlayHitPoints.toIntOrNull(),
                            overlayNotes = editor.overlayNotes,
                        )
                    ) !is CreateCampaignPersonUseCase.Result.Created
                }
                editor.membership == PersonMembership.WorldLibrary -> {
                    updateWorldPerson(
                        editor.personId,
                        WorldPersonDraft(
                            kind = editor.kind,
                            name = editor.name,
                            description = editor.description,
                            sheet = sheet,
                        )
                    ) !is UpdateWorldPersonUseCase.Result.Updated
                }
                else -> {
                    updateCampaignPerson(
                        editor.personId,
                        CampaignPersonDraft(
                            kind = editor.kind,
                            name = editor.name,
                            description = editor.description,
                            sheet = sheet,
                            overlayHitPoints = editor.overlayHitPoints.toIntOrNull(),
                            overlayNotes = editor.overlayNotes,
                        )
                    ) !is UpdateCampaignPersonUseCase.Result.Updated
                }
            }
            if (failed) {
                updatePathfinderEditor { current ->
                    current?.copy(nameError = "Could not save this person")
                }
            } else {
                updatePathfinderEditor { null }
            }
        }
    }

    private fun skillEditorsFrom(sheet: FifthEditionSheet): List<CharactersViewState.SkillEditor> {
        return FifthEditionSkillCatalog.skills.map { catalogSkill ->
            val stored = sheet.skills.firstOrNull { skill ->
                skill.name.equals(catalogSkill.name, ignoreCase = true)
            }
            CharactersViewState.SkillEditor(
                name = catalogSkill.name,
                ability = catalogSkill.ability,
                proficient = stored?.proficient == true,
            )
        }
    }

    private fun sheetFrom(editor: CharactersViewState.CharacterEditorState): FifthEditionSheet {
        return FifthEditionSheet(
            race = editor.race,
            classLevels = editor.classLevels.mapNotNull { level ->
                val className = level.className.trim()
                if (className.isEmpty()) {
                    null
                } else {
                    ClassLevel(
                        className = className,
                        subclass = level.subclass.trim(),
                        level = level.levelText.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                    )
                }
            },
            abilityScores = AbilityScores(
                strength = editor.strength.toIntOrNull() ?: 10,
                dexterity = editor.dexterity.toIntOrNull() ?: 10,
                constitution = editor.constitution.toIntOrNull() ?: 10,
                intelligence = editor.intelligence.toIntOrNull() ?: 10,
                wisdom = editor.wisdom.toIntOrNull() ?: 10,
                charisma = editor.charisma.toIntOrNull() ?: 10,
            ),
            hitPoints = editor.hitPoints.toIntOrNull() ?: 10,
            maxHitPoints = editor.maxHitPoints.toIntOrNull() ?: 10,
            temporaryHitPoints = editor.temporaryHitPoints.toIntOrNull() ?: 0,
            armorClass = editor.armorClass.toIntOrNull() ?: 10,
            walkSpeed = editor.walkSpeed.toIntOrNull()?.coerceAtLeast(0) ?: 30,
            deathSaves = DeathSaves(
                successes = editor.deathSuccesses.toIntOrNull()?.coerceIn(0, 3) ?: 0,
                failures = editor.deathFailures.toIntOrNull()?.coerceIn(0, 3) ?: 0,
            ),
            items = editor.items.mapNotNull { item ->
                val name = item.name.trim()
                if (name.isEmpty()) {
                    null
                } else {
                    InventoryItem(
                        name = name,
                        quantity = item.quantityText.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                        notes = item.notes.trim(),
                    )
                }
            },
            features = editor.features.mapNotNull { feature ->
                val name = feature.name.trim()
                if (name.isEmpty()) {
                    null
                } else {
                    PersonFeature(name = name, description = feature.description.trim())
                }
            },
            spells = editor.spells.mapNotNull { spell ->
                val name = spell.name.trim()
                if (name.isEmpty()) {
                    null
                } else {
                    PersonSpell(
                        name = name,
                        level = spell.levelText.toIntOrNull()?.coerceAtLeast(0) ?: 0,
                        prepared = spell.prepared,
                    )
                }
            },
            notes = editor.notes,
            skills = editor.skills.map { skill ->
                FifthEditionSkill(
                    name = skill.name,
                    ability = skill.ability,
                    proficient = skill.proficient,
                )
            },
            spellSlots = editor.spellSlots.mapNotNull { slot ->
                val level = slot.levelText.toIntOrNull()?.coerceIn(1, 9) ?: return@mapNotNull null
                val maximum = slot.maximumText.toIntOrNull()?.coerceAtLeast(0) ?: 0
                if (maximum <= 0) {
                    null
                } else {
                    FifthEditionSpellSlot(
                        level = level,
                        maximum = maximum,
                        used = slot.usedText.toIntOrNull()?.coerceIn(0, maximum) ?: 0,
                    )
                }
            },
            concentratingSpell = editor.concentratingSpell.trim(),
            creatureSize = editor.creatureSize,
        )
    }

    private fun sheetFrom(editor: CharactersViewState.PathfinderEditorState): Pathfinder2ESheet {
        return Pathfinder2ESheet(
            ancestry = editor.ancestry,
            heritage = editor.heritage,
            background = editor.background,
            className = editor.className,
            subclass = editor.subclass,
            level = editor.levelText.toIntOrNull()?.coerceAtLeast(1) ?: 1,
            abilityScores = AbilityScores(
                strength = editor.strength.toIntOrNull() ?: 10,
                dexterity = editor.dexterity.toIntOrNull() ?: 10,
                constitution = editor.constitution.toIntOrNull() ?: 10,
                intelligence = editor.intelligence.toIntOrNull() ?: 10,
                wisdom = editor.wisdom.toIntOrNull() ?: 10,
                charisma = editor.charisma.toIntOrNull() ?: 10,
            ),
            hitPoints = editor.hitPoints.toIntOrNull() ?: 16,
            maxHitPoints = editor.maxHitPoints.toIntOrNull() ?: 16,
            temporaryHitPoints = editor.temporaryHitPoints.toIntOrNull() ?: 0,
            armorClass = editor.armorClass.toIntOrNull() ?: 15,
            perception = editor.perception.toIntOrNull() ?: 3,
            landSpeed = editor.landSpeed.toIntOrNull()?.coerceAtLeast(0) ?: 25,
            skills = editor.skills.mapNotNull { skill ->
                val name = skill.name.trim()
                if (name.isEmpty()) {
                    null
                } else {
                    Pathfinder2ESkill(name = name, rank = skill.rank)
                }
            },
            feats = editor.feats.mapNotNull { feat ->
                val name = feat.name.trim()
                if (name.isEmpty()) {
                    null
                } else {
                    Pathfinder2EFeat(
                        name = name,
                        type = feat.type.trim(),
                        description = feat.description.trim(),
                    )
                }
            },
            spells = editor.spells.mapNotNull { spell ->
                val name = spell.name.trim()
                if (name.isEmpty()) {
                    null
                } else {
                    Pathfinder2ESpell(
                        name = name,
                        rank = spell.rankText.toIntOrNull()?.coerceIn(0, 10) ?: 1,
                        prepared = spell.prepared,
                    )
                }
            },
            notes = editor.notes,
            dying = editor.dying.toIntOrNull()?.coerceAtLeast(0) ?: 0,
            wounded = editor.wounded.toIntOrNull()?.coerceAtLeast(0) ?: 0,
        )
    }

    private fun requestDelete(key: CharactersViewState.PersonKey) {
        val name = when (key.membership) {
            PersonMembership.WorldLibrary -> latestWorldPeople.firstOrNull { it.id == key.id }?.name
            PersonMembership.ThisCampaign -> latestCampaignPeople.firstOrNull { it.id == key.id }?.name
        } ?: return
        updateContentOverlays(
            pendingDelete = CharactersViewState.PendingDelete(key = key, name = name),
        )
    }

    private fun confirmDelete() {
        val pending = pendingDeleteFrom(_state.value) ?: return
        appScope.scope.launch {
            when (pending.key.membership) {
                PersonMembership.WorldLibrary -> {
                    when (val result = deleteWorldPerson(pending.key.id)) {
                        DeleteWorldPersonUseCase.Result.Deleted -> {
                            if (selectedKey == pending.key) {
                                selectedKey = null
                            }
                            updateContentOverlays(pendingDelete = null, blockDeleteReason = null)
                        }
                        is DeleteWorldPersonUseCase.Result.Blocked -> {
                            updateContentOverlays(
                                pendingDelete = null,
                                blockDeleteReason = "This person is still referenced by " +
                                    "${result.referenceCount} campaign " +
                                    if (result.referenceCount == 1) "record." else "records.",
                            )
                        }
                        DeleteWorldPersonUseCase.Result.NotFound -> {
                            updateContentOverlays(pendingDelete = null)
                        }
                    }
                }
                PersonMembership.ThisCampaign -> {
                    deleteCampaignPerson(pending.key.id)
                    if (selectedKey == pending.key) {
                        selectedKey = null
                    }
                    updateContentOverlays(pendingDelete = null)
                }
            }
        }
    }

    private fun addToCampaign(worldPersonId: String) {
        appScope.scope.launch {
            addWorldPersonToCampaign(worldPersonId)
        }
    }

    private fun saveOverlay() {
        val selected = (_state.value as? CharactersViewState.Content)?.selected ?: return
        if (!selected.isWorldReference) {
            return
        }
        val person = latestCampaignPeople.firstOrNull { it.id == selected.key.id } ?: return
        appScope.scope.launch {
            updateCampaignPerson(
                person.id,
                CampaignPersonDraft(
                    kind = person.kind,
                    name = person.name,
                    description = person.description,
                    sheet = person.sheet,
                    overlayHitPoints = selected.overlayHitPoints.toIntOrNull(),
                    overlayNotes = selected.overlayNotes,
                ),
            )
        }
    }

    private fun openRelationshipEditor() {
        val selected = selectedKey ?: return
        updateContentOverlays(
            relationshipEditor = CharactersViewState.RelationshipEditorState(
                target = null,
                type = RelationshipType.Ally,
                description = "",
                factionId = null,
                factions = factionOptions(),
                targets = relationshipTargets(selected),
                targetError = null,
            ),
        )
    }

    private fun saveRelationship() {
        val selected = selectedKey ?: return
        val editor = relationshipEditorFrom(_state.value) ?: return
        val target = editor.target
        if (target == null) {
            updateRelationshipEditor { current -> current.copy(targetError = "Pick a person") }
            return
        }
        appScope.scope.launch {
            val result = createPersonRelationship(
                from = toRef(selected),
                to = toRef(target),
                type = editor.type,
                description = editor.description,
                factionId = editor.factionId,
            )
            if (result is CreatePersonRelationshipUseCase.Result.Created) {
                updateContentOverlays(relationshipEditor = null)
            } else {
                updateRelationshipEditor { current ->
                    current.copy(targetError = "Could not create that relationship")
                }
            }
        }
    }

    private fun deleteRelationship(relationshipId: String) {
        appScope.scope.launch {
            deletePersonRelationship(relationshipId)
        }
    }

    private fun openGenerator() {
        if (latestWorldSystem != GameSystem.FifthEdition) {
            return
        }
        val generator = CharactersViewState.GeneratorState(
            method = AbilityScoreMethod.FourD6DropLowest,
            draft = null,
        )
        when (val current = _state.value) {
            is CharactersViewState.Empty -> _state.value = current.copy(generator = generator)
            is CharactersViewState.Content -> _state.value = current.copy(generator = generator)
            else -> Unit
        }
    }

    private fun rollGenerator() {
        val generator = generatorFrom(_state.value) ?: return
        val draft = generateRandomNpc(generator.method)
        updateGenerator { current -> current.copy(draft = draft) }
    }

    private fun saveGenerator() {
        val draft = generatorFrom(_state.value)?.draft ?: return
        appScope.scope.launch {
            val result = createWorldPerson(
                WorldPersonDraft(
                    kind = PersonKind.Npc,
                    name = draft.name,
                    description = "",
                    sheet = FifthEditionSheet.empty().copy(
                        race = draft.race,
                        abilityScores = draft.abilityScores,
                    ),
                )
            )
            if (result is CreateWorldPersonUseCase.Result.Created) {
                selectedKey = CharactersViewState.PersonKey(
                    PersonMembership.WorldLibrary,
                    result.person.id,
                )
                updateGeneratorState(null)
            }
        }
    }

    private fun updateClassLevel(
        index: Int,
        transform: (CharactersViewState.ClassLevelEditor) -> CharactersViewState.ClassLevelEditor,
    ) {
        updateEditor { editor ->
            editor?.copy(
                classLevels = editor.classLevels.mapIndexed { current, level ->
                    if (current == index) transform(level) else level
                },
            )
        }
    }

    private fun updateItem(
        index: Int,
        transform: (CharactersViewState.ItemEditor) -> CharactersViewState.ItemEditor,
    ) {
        updateEditor { editor ->
            editor?.copy(
                items = editor.items.mapIndexed { current, item ->
                    if (current == index) transform(item) else item
                },
            )
        }
    }

    private fun updateFeature(
        index: Int,
        transform: (CharactersViewState.FeatureEditor) -> CharactersViewState.FeatureEditor,
    ) {
        updateEditor { editor ->
            editor?.copy(
                features = editor.features.mapIndexed { current, feature ->
                    if (current == index) transform(feature) else feature
                },
            )
        }
    }

    private fun updateSpell(
        index: Int,
        transform: (CharactersViewState.SpellEditor) -> CharactersViewState.SpellEditor,
    ) {
        updateEditor { editor ->
            editor?.copy(
                spells = editor.spells.mapIndexed { current, spell ->
                    if (current == index) transform(spell) else spell
                },
            )
        }
    }

    private fun updateEditor(
        transform: (CharactersViewState.CharacterEditorState?) -> CharactersViewState.CharacterEditorState?,
    ) {
        when (val current = _state.value) {
            is CharactersViewState.Empty -> {
                _state.value = current.copy(editor = transform(current.editor))
            }
            is CharactersViewState.Content -> {
                _state.value = current.copy(editor = transform(current.editor))
            }
            else -> Unit
        }
    }

    private fun updatePathfinderEditor(
        transform: (CharactersViewState.PathfinderEditorState?) -> CharactersViewState.PathfinderEditorState?,
    ) {
        when (val current = _state.value) {
            is CharactersViewState.Empty -> {
                _state.value = current.copy(pathfinderEditor = transform(current.pathfinderEditor))
            }
            is CharactersViewState.Content -> {
                _state.value = current.copy(pathfinderEditor = transform(current.pathfinderEditor))
            }
            else -> Unit
        }
    }

    private fun updatePathfinderSkill(
        index: Int,
        transform: (CharactersViewState.PathfinderSkillEditor) -> CharactersViewState.PathfinderSkillEditor,
    ) {
        updatePathfinderEditor { editor ->
            editor?.copy(
                skills = editor.skills.mapIndexed { current, skill ->
                    if (current == index) transform(skill) else skill
                },
            )
        }
        updatePathfinderWizard { wizard ->
            wizard.copy(
                skills = wizard.skills.mapIndexed { current, skill ->
                    if (current == index) transform(skill) else skill
                },
            )
        }
    }

    private fun updatePathfinderFeat(
        index: Int,
        transform: (CharactersViewState.PathfinderFeatEditor) -> CharactersViewState.PathfinderFeatEditor,
    ) {
        updatePathfinderEditor { editor ->
            editor?.copy(
                feats = editor.feats.mapIndexed { current, feat ->
                    if (current == index) transform(feat) else feat
                },
            )
        }
    }

    private fun updatePathfinderSpell(
        index: Int,
        transform: (CharactersViewState.PathfinderSpellEditor) -> CharactersViewState.PathfinderSpellEditor,
    ) {
        updatePathfinderEditor { editor ->
            editor?.copy(
                spells = editor.spells.mapIndexed { current, spell ->
                    if (current == index) transform(spell) else spell
                },
            )
        }
    }

    private fun updateGenerator(
        transform: (CharactersViewState.GeneratorState) -> CharactersViewState.GeneratorState,
    ) {
        when (val current = _state.value) {
            is CharactersViewState.Empty -> {
                current.generator?.let { _state.value = current.copy(generator = transform(it)) }
            }
            is CharactersViewState.Content -> {
                current.generator?.let { _state.value = current.copy(generator = transform(it)) }
            }
            else -> Unit
        }
    }

    private fun updateGeneratorState(generator: CharactersViewState.GeneratorState?) {
        when (val current = _state.value) {
            is CharactersViewState.Empty -> _state.value = current.copy(generator = generator)
            is CharactersViewState.Content -> _state.value = current.copy(generator = generator)
            else -> Unit
        }
    }

    private fun updateSelectedOverlay(
        transform: (CharactersViewState.SelectedPerson) -> CharactersViewState.SelectedPerson,
    ) {
        val current = _state.value
        if (current is CharactersViewState.Content && current.selected != null) {
            _state.value = current.copy(selected = transform(current.selected))
        }
    }

    private fun updateRelationshipEditor(
        transform: (CharactersViewState.RelationshipEditorState) -> CharactersViewState.RelationshipEditorState,
    ) {
        val current = _state.value
        if (current is CharactersViewState.Content && current.relationshipEditor != null) {
            _state.value = current.copy(relationshipEditor = transform(current.relationshipEditor))
        }
    }

    private fun updateContentOverlays(
        pendingDelete: CharactersViewState.PendingDelete? = pendingDeleteFrom(_state.value),
        blockDeleteReason: String? = blockReasonFrom(_state.value),
        relationshipEditor: CharactersViewState.RelationshipEditorState? =
            relationshipEditorFrom(_state.value),
        membershipEditor: CharactersViewState.MembershipEditorState? =
            membershipEditorFrom(_state.value),
        companionEditor: CharactersViewState.CompanionEditorState? =
            companionEditorFrom(_state.value),
    ) {
        when (val current = _state.value) {
            is CharactersViewState.Content -> {
                _state.value = current.copy(
                    pendingDelete = pendingDelete,
                    blockDeleteReason = blockDeleteReason,
                    relationshipEditor = relationshipEditor,
                    membershipEditor = membershipEditor,
                    companionEditor = companionEditor,
                )
            }
            else -> Unit
        }
    }

    private fun editorFrom(state: CharactersViewState): CharactersViewState.CharacterEditorState? {
        return when (state) {
            is CharactersViewState.Empty -> state.editor
            is CharactersViewState.Content -> state.editor
            else -> null
        }
    }

    private fun pathfinderEditorFrom(
        state: CharactersViewState,
    ): CharactersViewState.PathfinderEditorState? {
        return when (state) {
            is CharactersViewState.Empty -> state.pathfinderEditor
            is CharactersViewState.Content -> state.pathfinderEditor
            else -> null
        }
    }

    private fun generatorFrom(state: CharactersViewState): CharactersViewState.GeneratorState? {
        return when (state) {
            is CharactersViewState.Empty -> state.generator
            is CharactersViewState.Content -> state.generator
            else -> null
        }
    }

    private fun relationshipEditorFrom(
        state: CharactersViewState,
    ): CharactersViewState.RelationshipEditorState? {
        return (state as? CharactersViewState.Content)?.relationshipEditor
    }

    private fun membershipEditorFrom(
        state: CharactersViewState,
    ): CharactersViewState.MembershipEditorState? {
        return (state as? CharactersViewState.Content)?.membershipEditor
    }

    private fun pendingDeleteFrom(state: CharactersViewState): CharactersViewState.PendingDelete? {
        return (state as? CharactersViewState.Content)?.pendingDelete
    }

    private fun blockReasonFrom(state: CharactersViewState): String? {
        return (state as? CharactersViewState.Content)?.blockDeleteReason
    }

    private fun companionsFor(ref: PersonRef): List<CharactersViewState.CompanionRow> {
        return latestCompanions
            .filter { link -> sameRef(link.owner, ref) }
            .map { link ->
                CharactersViewState.CompanionRow(
                    id = link.id,
                    key = keyFrom(link.companion),
                    name = personName(link.companion),
                    kind = link.kind,
                )
            }
    }

    private fun companionTargets(
        selected: CharactersViewState.PersonKey?,
    ): List<CharactersViewState.CompanionTarget> {
        val worldTargets = latestWorldPeople
            .filter { person -> person.kind != PersonKind.PlayerCharacter }
            .filter { person ->
                selected?.membership != PersonMembership.WorldLibrary || selected.id != person.id
            }
            .map { person ->
                CharactersViewState.CompanionTarget(
                    key = CharactersViewState.PersonKey(PersonMembership.WorldLibrary, person.id),
                    name = "${person.name} (world)",
                )
            }
        val campaignTargets = latestCampaignPeople
            .filter { person -> person.kind != PersonKind.PlayerCharacter }
            .filter { person ->
                selected?.membership != PersonMembership.ThisCampaign || selected.id != person.id
            }
            .map { person ->
                CharactersViewState.CompanionTarget(
                    key = CharactersViewState.PersonKey(PersonMembership.ThisCampaign, person.id),
                    name = "${person.name} (campaign)",
                )
            }
        return (worldTargets + campaignTargets).sortedBy { it.name.lowercase() }
    }

    private fun keyFrom(ref: PersonRef): CharactersViewState.PersonKey {
        return when (ref) {
            is PersonRef.World -> CharactersViewState.PersonKey(PersonMembership.WorldLibrary, ref.id)
            is PersonRef.Campaign -> CharactersViewState.PersonKey(PersonMembership.ThisCampaign, ref.id)
        }
    }

    private fun createWizard(
        membership: PersonMembership = PersonMembership.WorldLibrary,
        kind: PersonKind = PersonKind.Npc,
    ): CharactersViewState.CreationWizardState {
        return CharactersViewState.CreationWizardState(
            step = CharactersViewState.CreationStep.Identity,
            membership = membership,
            canChangeMembership = hasActiveCampaign,
            hasActiveCampaign = hasActiveCampaign,
            kind = kindForMembership(membership, kind),
            name = "",
            description = "",
            race = "",
            classLevels = emptyList(),
            strength = "10",
            dexterity = "10",
            constitution = "10",
            intelligence = "10",
            wisdom = "10",
            charisma = "10",
            hitPoints = "10",
            maxHitPoints = "10",
            armorClass = "10",
            walkSpeed = "30",
            companions = emptyList(),
            companionTargets = companionTargets(null),
            nameError = null,
            membershipError = null,
            companionError = null,
        )
    }

    private fun createPathfinderWizard(
        membership: PersonMembership = PersonMembership.WorldLibrary,
        kind: PersonKind = PersonKind.Npc,
    ): CharactersViewState.PathfinderWizardState {
        val empty = Pathfinder2ESheet.empty()
        return CharactersViewState.PathfinderWizardState(
            step = CharactersViewState.PathfinderCreationStep.Identity,
            membership = membership,
            canChangeMembership = hasActiveCampaign,
            hasActiveCampaign = hasActiveCampaign,
            kind = kindForMembership(membership, kind),
            name = "",
            description = "",
            ancestry = "",
            heritage = "",
            background = "",
            className = "",
            subclass = "",
            levelText = "1",
            strength = empty.abilityScores.strength.toString(),
            dexterity = empty.abilityScores.dexterity.toString(),
            constitution = empty.abilityScores.constitution.toString(),
            intelligence = empty.abilityScores.intelligence.toString(),
            wisdom = empty.abilityScores.wisdom.toString(),
            charisma = empty.abilityScores.charisma.toString(),
            hitPoints = empty.hitPoints.toString(),
            maxHitPoints = empty.maxHitPoints.toString(),
            armorClass = empty.armorClass.toString(),
            perception = empty.perception.toString(),
            landSpeed = empty.landSpeed.toString(),
            skills = defaultPathfinderSkills(),
            nameError = null,
            membershipError = null,
        )
    }

    private fun defaultPathfinderSkills(): List<CharactersViewState.PathfinderSkillEditor> {
        return Pathfinder2EReference.skills.map { name ->
            CharactersViewState.PathfinderSkillEditor(
                name = name,
                rank = Pathfinder2ESkillRank.Untrained,
            )
        }
    }

    private fun emptyCompanionDraft(): CharactersViewState.CompanionDraftEditor {
        return CharactersViewState.CompanionDraftEditor(
            kind = CompanionKind.Familiar,
            useExisting = false,
            existingKey = null,
            newName = "",
            newCreature = "",
        )
    }

    private fun changeWizardMembership(
        wizard: CharactersViewState.CreationWizardState,
        membership: PersonMembership,
    ): CharactersViewState.CreationWizardState {
        val kind = when (membership) {
            PersonMembership.WorldLibrary -> {
                if (wizard.kind == PersonKind.PlayerCharacter) PersonKind.Npc else wizard.kind
            }
            PersonMembership.ThisCampaign -> wizard.kind
        }
        return wizard.copy(
            membership = membership,
            kind = kind,
            membershipError = null,
        )
    }

    private fun advanceWizard() {
        val wizard = wizardFrom(_state.value) ?: return
        if (wizard.step == CharactersViewState.CreationStep.Identity) {
            if (wizard.name.trim().isEmpty()) {
                updateWizard { current -> current.copy(nameError = "Name is required") }
                return
            }
            if (wizard.membership == PersonMembership.ThisCampaign && !hasActiveCampaign) {
                updateWizard { current ->
                    current.copy(membershipError = "Select a campaign first")
                }
                return
            }
        }
        if (wizard.step == CharactersViewState.CreationStep.Companions &&
            !companionsAreValid(wizard)
        ) {
            return
        }
        val next = nextStep(wizard.step) ?: return
        updateWizard { current -> current.copy(step = next, companionError = null) }
    }

    private fun rewindWizard() {
        val wizard = wizardFrom(_state.value) ?: return
        val previous = previousStep(wizard.step) ?: return
        updateWizard { current -> current.copy(step = previous, companionError = null) }
    }

    private fun nextStep(
        step: CharactersViewState.CreationStep,
    ): CharactersViewState.CreationStep? {
        val steps = CharactersViewState.CreationStep.entries
        val index = steps.indexOf(step)
        return steps.getOrNull(index + 1)
    }

    private fun previousStep(
        step: CharactersViewState.CreationStep,
    ): CharactersViewState.CreationStep? {
        val steps = CharactersViewState.CreationStep.entries
        val index = steps.indexOf(step)
        return steps.getOrNull(index - 1)
    }

    private fun companionsAreValid(wizard: CharactersViewState.CreationWizardState): Boolean {
        val invalid = wizard.companions.any { draft ->
            if (draft.useExisting) {
                draft.existingKey == null
            } else {
                draft.newName.trim().isEmpty()
            }
        }
        if (invalid) {
            updateWizard { current ->
                current.copy(companionError = "Each companion needs a name or a linked person")
            }
        }
        return !invalid
    }

    private fun saveWizard() {
        val wizard = wizardFrom(_state.value) ?: return
        if (wizard.name.trim().isEmpty()) {
            updateWizard { current ->
                current.copy(
                    step = CharactersViewState.CreationStep.Identity,
                    nameError = "Name is required",
                )
            }
            return
        }
        if (wizard.membership == PersonMembership.ThisCampaign && !hasActiveCampaign) {
            updateWizard { current ->
                current.copy(
                    step = CharactersViewState.CreationStep.Identity,
                    membershipError = "Select a campaign first",
                )
            }
            return
        }
        if (!companionsAreValid(wizard)) {
            updateWizard { current ->
                current.copy(step = CharactersViewState.CreationStep.Companions)
            }
            return
        }
        appScope.scope.launch {
            val ownerRef = createWizardOwner(wizard)
            if (ownerRef == null) {
                updateWizard { current -> current.copy(nameError = "Could not save this person") }
                return@launch
            }
            val failed = wizard.companions.any { draft ->
                !saveWizardCompanion(ownerRef, wizard.membership, draft)
            }
            if (failed) {
                updateWizard { current ->
                    current.copy(
                        step = CharactersViewState.CreationStep.Companions,
                        companionError = "Could not save one or more companions",
                    )
                }
            } else {
                selectedKey = keyFrom(ownerRef)
                updateWizardState(null)
            }
        }
    }

    private suspend fun createWizardOwner(
        wizard: CharactersViewState.CreationWizardState,
    ): PersonRef? {
        val sheet = sheetFromWizard(wizard)
        return when (wizard.membership) {
            PersonMembership.WorldLibrary -> {
                val result = createWorldPerson(
                    WorldPersonDraft(
                        kind = wizard.kind,
                        name = wizard.name,
                        description = wizard.description,
                        sheet = sheet,
                    )
                )
                if (result is CreateWorldPersonUseCase.Result.Created) {
                    PersonRef.World(result.person.id)
                } else {
                    null
                }
            }
            PersonMembership.ThisCampaign -> {
                val result = createCampaignPerson(
                    CampaignPersonDraft(
                        kind = wizard.kind,
                        name = wizard.name,
                        description = wizard.description,
                        sheet = sheet,
                        overlayHitPoints = null,
                        overlayNotes = "",
                    )
                )
                if (result is CreateCampaignPersonUseCase.Result.Created) {
                    PersonRef.Campaign(result.person.id)
                } else {
                    null
                }
            }
        }
    }

    private suspend fun saveWizardCompanion(
        owner: PersonRef,
        ownerMembership: PersonMembership,
        draft: CharactersViewState.CompanionDraftEditor,
    ): Boolean {
        val companionRef = if (draft.useExisting) {
            val key = draft.existingKey ?: return false
            resolveCompanionRef(key, ownerMembership)
        } else {
            createCompanionPerson(ownerMembership, draft)
        } ?: return false
        return createPersonCompanion(
            owner = owner,
            companion = companionRef,
            kind = draft.kind,
        ) is CreatePersonCompanionUseCase.Result.Created
    }

    private suspend fun resolveCompanionRef(
        key: CharactersViewState.PersonKey,
        ownerMembership: PersonMembership,
    ): PersonRef? {
        return when (key.membership) {
            PersonMembership.WorldLibrary -> {
                if (ownerMembership == PersonMembership.ThisCampaign) {
                    val existing = latestCampaignPeople.firstOrNull { it.worldPersonId == key.id }
                    if (existing != null) {
                        return PersonRef.Campaign(existing.id)
                    }
                    when (val result = addWorldPersonToCampaign(key.id)) {
                        is AddWorldPersonToCampaignUseCase.Result.Added -> {
                            PersonRef.Campaign(result.person.id)
                        }
                        else -> PersonRef.World(key.id)
                    }
                } else {
                    PersonRef.World(key.id)
                }
            }
            PersonMembership.ThisCampaign -> PersonRef.Campaign(key.id)
        }
    }

    private suspend fun createCompanionPerson(
        ownerMembership: PersonMembership,
        draft: CharactersViewState.CompanionDraftEditor,
    ): PersonRef? {
        val name = draft.newName.trim()
        if (name.isEmpty()) {
            return null
        }
        val sheet = FifthEditionSheet.empty().copy(race = draft.newCreature.trim())
        return when (ownerMembership) {
            PersonMembership.WorldLibrary -> {
                val result = createWorldPerson(
                    WorldPersonDraft(
                        kind = PersonKind.Monster,
                        name = name,
                        description = "",
                        sheet = sheet,
                    )
                )
                if (result is CreateWorldPersonUseCase.Result.Created) {
                    PersonRef.World(result.person.id)
                } else {
                    null
                }
            }
            PersonMembership.ThisCampaign -> {
                val result = createCampaignPerson(
                    CampaignPersonDraft(
                        kind = PersonKind.Monster,
                        name = name,
                        description = "",
                        sheet = sheet,
                        overlayHitPoints = null,
                        overlayNotes = "",
                    )
                )
                if (result is CreateCampaignPersonUseCase.Result.Created) {
                    PersonRef.Campaign(result.person.id)
                } else {
                    null
                }
            }
        }
    }

    private fun sheetFromWizard(wizard: CharactersViewState.CreationWizardState): FifthEditionSheet {
        return FifthEditionSheet.empty().copy(
            race = wizard.race,
            classLevels = wizard.classLevels.mapNotNull { level ->
                val className = level.className.trim()
                if (className.isEmpty()) {
                    null
                } else {
                    ClassLevel(
                        className = className,
                        subclass = level.subclass.trim(),
                        level = level.levelText.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                    )
                }
            },
            abilityScores = AbilityScores(
                strength = wizard.strength.toIntOrNull() ?: 10,
                dexterity = wizard.dexterity.toIntOrNull() ?: 10,
                constitution = wizard.constitution.toIntOrNull() ?: 10,
                intelligence = wizard.intelligence.toIntOrNull() ?: 10,
                wisdom = wizard.wisdom.toIntOrNull() ?: 10,
                charisma = wizard.charisma.toIntOrNull() ?: 10,
            ),
            hitPoints = wizard.hitPoints.toIntOrNull() ?: 10,
            maxHitPoints = wizard.maxHitPoints.toIntOrNull() ?: 10,
            armorClass = wizard.armorClass.toIntOrNull() ?: 10,
            walkSpeed = wizard.walkSpeed.toIntOrNull()?.coerceAtLeast(0) ?: 30,
        )
    }

    private fun sheetFromPathfinderWizard(
        wizard: CharactersViewState.PathfinderWizardState,
    ): Pathfinder2ESheet {
        val empty = Pathfinder2ESheet.empty()
        return empty.copy(
            ancestry = wizard.ancestry,
            heritage = wizard.heritage,
            background = wizard.background,
            className = wizard.className,
            subclass = wizard.subclass,
            level = wizard.levelText.toIntOrNull()?.coerceAtLeast(1) ?: 1,
            abilityScores = AbilityScores(
                strength = wizard.strength.toIntOrNull() ?: empty.abilityScores.strength,
                dexterity = wizard.dexterity.toIntOrNull() ?: empty.abilityScores.dexterity,
                constitution = wizard.constitution.toIntOrNull() ?: empty.abilityScores.constitution,
                intelligence = wizard.intelligence.toIntOrNull() ?: empty.abilityScores.intelligence,
                wisdom = wizard.wisdom.toIntOrNull() ?: empty.abilityScores.wisdom,
                charisma = wizard.charisma.toIntOrNull() ?: empty.abilityScores.charisma,
            ),
            hitPoints = wizard.hitPoints.toIntOrNull() ?: empty.hitPoints,
            maxHitPoints = wizard.maxHitPoints.toIntOrNull() ?: empty.maxHitPoints,
            armorClass = wizard.armorClass.toIntOrNull() ?: empty.armorClass,
            perception = wizard.perception.toIntOrNull() ?: empty.perception,
            landSpeed = wizard.landSpeed.toIntOrNull()?.coerceAtLeast(0) ?: empty.landSpeed,
            skills = wizard.skills.mapNotNull { skill ->
                val name = skill.name.trim()
                if (name.isEmpty()) {
                    null
                } else {
                    Pathfinder2ESkill(name = name, rank = skill.rank)
                }
            },
        )
    }

    private fun advancePathfinderWizard() {
        val wizard = pathfinderWizardFrom(_state.value) ?: return
        if (wizard.step == CharactersViewState.PathfinderCreationStep.Identity) {
            if (wizard.name.trim().isEmpty()) {
                updatePathfinderWizard { current -> current.copy(nameError = "Name is required") }
                return
            }
            if (wizard.membership == PersonMembership.ThisCampaign && !hasActiveCampaign) {
                updatePathfinderWizard { current ->
                    current.copy(membershipError = "Select a campaign first")
                }
                return
            }
        }
        val next = when (wizard.step) {
            CharactersViewState.PathfinderCreationStep.Identity -> {
                CharactersViewState.PathfinderCreationStep.AncestryAndClass
            }
            CharactersViewState.PathfinderCreationStep.AncestryAndClass -> {
                CharactersViewState.PathfinderCreationStep.Attributes
            }
            CharactersViewState.PathfinderCreationStep.Attributes -> {
                CharactersViewState.PathfinderCreationStep.Skills
            }
            CharactersViewState.PathfinderCreationStep.Skills -> {
                CharactersViewState.PathfinderCreationStep.Review
            }
            CharactersViewState.PathfinderCreationStep.Review -> {
                CharactersViewState.PathfinderCreationStep.Review
            }
        }
        updatePathfinderWizard { current -> current.copy(step = next) }
    }

    private fun rewindPathfinderWizard() {
        val wizard = pathfinderWizardFrom(_state.value) ?: return
        val previous = when (wizard.step) {
            CharactersViewState.PathfinderCreationStep.Identity -> {
                CharactersViewState.PathfinderCreationStep.Identity
            }
            CharactersViewState.PathfinderCreationStep.AncestryAndClass -> {
                CharactersViewState.PathfinderCreationStep.Identity
            }
            CharactersViewState.PathfinderCreationStep.Attributes -> {
                CharactersViewState.PathfinderCreationStep.AncestryAndClass
            }
            CharactersViewState.PathfinderCreationStep.Skills -> {
                CharactersViewState.PathfinderCreationStep.Attributes
            }
            CharactersViewState.PathfinderCreationStep.Review -> {
                CharactersViewState.PathfinderCreationStep.Skills
            }
        }
        updatePathfinderWizard { current -> current.copy(step = previous) }
    }

    private fun savePathfinderWizard() {
        val wizard = pathfinderWizardFrom(_state.value) ?: return
        if (wizard.name.trim().isEmpty()) {
            updatePathfinderWizard { current ->
                current.copy(
                    step = CharactersViewState.PathfinderCreationStep.Identity,
                    nameError = "Name is required",
                )
            }
            return
        }
        if (wizard.membership == PersonMembership.ThisCampaign && !hasActiveCampaign) {
            updatePathfinderWizard { current ->
                current.copy(
                    step = CharactersViewState.PathfinderCreationStep.Identity,
                    membershipError = "Select a campaign first",
                )
            }
            return
        }
        appScope.scope.launch {
            val ownerRef = createPathfinderWizardOwner(wizard)
            if (ownerRef == null) {
                updatePathfinderWizard { current ->
                    current.copy(nameError = "Could not save this person")
                }
                return@launch
            }
            selectedKey = keyFrom(ownerRef)
            updatePathfinderWizardState(null)
        }
    }

    private suspend fun createPathfinderWizardOwner(
        wizard: CharactersViewState.PathfinderWizardState,
    ): PersonRef? {
        val sheet = sheetFromPathfinderWizard(wizard)
        return when (wizard.membership) {
            PersonMembership.WorldLibrary -> {
                val result = createWorldPerson(
                    WorldPersonDraft(
                        kind = wizard.kind,
                        name = wizard.name,
                        description = wizard.description,
                        sheet = sheet,
                    )
                )
                if (result is CreateWorldPersonUseCase.Result.Created) {
                    PersonRef.World(result.person.id)
                } else {
                    null
                }
            }
            PersonMembership.ThisCampaign -> {
                val result = createCampaignPerson(
                    CampaignPersonDraft(
                        kind = wizard.kind,
                        name = wizard.name,
                        description = wizard.description,
                        sheet = sheet,
                        overlayHitPoints = null,
                        overlayNotes = "",
                    )
                )
                if (result is CreateCampaignPersonUseCase.Result.Created) {
                    PersonRef.Campaign(result.person.id)
                } else {
                    null
                }
            }
        }
    }

    private fun openCompanionEditor() {
        val selected = selectedKey ?: return
        updateContentOverlays(
            companionEditor = CharactersViewState.CompanionEditorState(
                kind = CompanionKind.Familiar,
                useExisting = true,
                existingKey = null,
                newName = "",
                newCreature = "",
                targets = companionTargets(selected),
                error = null,
            ),
        )
    }

    private fun saveCompanionEditor() {
        val selected = selectedKey ?: return
        val editor = companionEditorFrom(_state.value) ?: return
        appScope.scope.launch {
            val companionRef = if (editor.useExisting) {
                val key = editor.existingKey
                if (key == null) {
                    updateCompanionEditor { current -> current.copy(error = "Pick a person") }
                    return@launch
                }
                resolveCompanionRef(key, selected.membership)
            } else {
                if (editor.newName.trim().isEmpty()) {
                    updateCompanionEditor { current -> current.copy(error = "Name is required") }
                    return@launch
                }
                createCompanionPerson(
                    selected.membership,
                    CharactersViewState.CompanionDraftEditor(
                        kind = editor.kind,
                        useExisting = false,
                        existingKey = null,
                        newName = editor.newName,
                        newCreature = editor.newCreature,
                    ),
                )
            }
            if (companionRef == null) {
                updateCompanionEditor { current ->
                    current.copy(error = "Could not save that companion")
                }
                return@launch
            }
            val result = createPersonCompanion(
                owner = toRef(selected),
                companion = companionRef,
                kind = editor.kind,
            )
            if (result is CreatePersonCompanionUseCase.Result.Created) {
                updateContentOverlays(companionEditor = null)
            } else {
                updateCompanionEditor { current ->
                    current.copy(error = "Could not link that companion")
                }
            }
        }
    }

    private fun deleteCompanion(companionId: String) {
        appScope.scope.launch {
            deletePersonCompanion(companionId)
        }
    }

    private fun updateWizardClassLevel(
        index: Int,
        transform: (CharactersViewState.ClassLevelEditor) -> CharactersViewState.ClassLevelEditor,
    ) {
        updateWizard { wizard ->
            wizard.copy(
                classLevels = wizard.classLevels.mapIndexed { current, level ->
                    if (current == index) transform(level) else level
                },
            )
        }
    }

    private fun updateWizardCompanion(
        index: Int,
        transform: (CharactersViewState.CompanionDraftEditor) -> CharactersViewState.CompanionDraftEditor,
    ) {
        updateWizard { wizard ->
            wizard.copy(
                companions = wizard.companions.mapIndexed { current, draft ->
                    if (current == index) transform(draft) else draft
                },
                companionError = null,
            )
        }
    }

    private fun updateWizard(
        transform: (CharactersViewState.CreationWizardState) -> CharactersViewState.CreationWizardState,
    ) {
        when (val current = _state.value) {
            is CharactersViewState.Empty -> {
                current.wizard?.let { _state.value = current.copy(wizard = transform(it)) }
            }
            is CharactersViewState.Content -> {
                current.wizard?.let { _state.value = current.copy(wizard = transform(it)) }
            }
            else -> Unit
        }
    }

    private fun updateWizardState(wizard: CharactersViewState.CreationWizardState?) {
        when (val current = _state.value) {
            is CharactersViewState.Empty -> _state.value = current.copy(wizard = wizard)
            is CharactersViewState.Content -> _state.value = current.copy(wizard = wizard)
            else -> Unit
        }
    }

    private fun updateCompanionEditor(
        transform: (CharactersViewState.CompanionEditorState) -> CharactersViewState.CompanionEditorState,
    ) {
        val current = _state.value
        if (current is CharactersViewState.Content && current.companionEditor != null) {
            _state.value = current.copy(companionEditor = transform(current.companionEditor))
        }
    }

    private fun wizardFrom(state: CharactersViewState): CharactersViewState.CreationWizardState? {
        return when (state) {
            is CharactersViewState.Empty -> state.wizard
            is CharactersViewState.Content -> state.wizard
            else -> null
        }
    }

    private fun pathfinderWizardFrom(
        state: CharactersViewState,
    ): CharactersViewState.PathfinderWizardState? {
        return when (state) {
            is CharactersViewState.Empty -> state.pathfinderWizard
            is CharactersViewState.Content -> state.pathfinderWizard
            else -> null
        }
    }

    private fun updatePathfinderWizard(
        transform: (CharactersViewState.PathfinderWizardState) -> CharactersViewState.PathfinderWizardState,
    ) {
        when (val current = _state.value) {
            is CharactersViewState.Empty -> {
                current.pathfinderWizard?.let {
                    _state.value = current.copy(pathfinderWizard = transform(it))
                }
            }
            is CharactersViewState.Content -> {
                current.pathfinderWizard?.let {
                    _state.value = current.copy(pathfinderWizard = transform(it))
                }
            }
            else -> Unit
        }
    }

    private fun updatePathfinderWizardState(wizard: CharactersViewState.PathfinderWizardState?) {
        when (val current = _state.value) {
            is CharactersViewState.Empty -> _state.value = current.copy(pathfinderWizard = wizard)
            is CharactersViewState.Content -> _state.value = current.copy(pathfinderWizard = wizard)
            else -> Unit
        }
    }

    private fun companionEditorFrom(
        state: CharactersViewState,
    ): CharactersViewState.CompanionEditorState? {
        return (state as? CharactersViewState.Content)?.companionEditor
    }

    private fun openMembershipEditor() {
        val selected = selectedKey ?: return
        updateContentOverlays(
            membershipEditor = CharactersViewState.MembershipEditorState(
                factionId = null,
                role = "",
                factions = availableMembershipFactions(selected),
                factionError = null,
            ),
        )
    }

    private fun saveMembership() {
        val selected = selectedKey ?: return
        val editor = membershipEditorFrom(_state.value) ?: return
        val factionId = editor.factionId
        if (factionId == null) {
            updateMembershipEditor { current -> current.copy(factionError = "Pick a faction") }
            return
        }
        appScope.scope.launch {
            val result = createFactionMembership(
                person = membershipRef(selected),
                factionId = factionId,
                role = editor.role,
                notes = "",
            )
            if (result is CreateFactionMembershipUseCase.Result.Created) {
                updateContentOverlays(membershipEditor = null)
            } else {
                updateMembershipEditor { current ->
                    current.copy(factionError = "Could not add that membership")
                }
            }
        }
    }

    private fun deleteMembership(membershipId: String) {
        appScope.scope.launch {
            deleteFactionMembership(membershipId)
        }
    }

    private fun updateMembershipEditor(
        transform: (CharactersViewState.MembershipEditorState) -> CharactersViewState.MembershipEditorState,
    ) {
        val current = _state.value
        if (current is CharactersViewState.Content && current.membershipEditor != null) {
            _state.value = current.copy(membershipEditor = transform(current.membershipEditor))
        }
    }

    private fun membershipsFor(
        key: CharactersViewState.PersonKey,
        campaignPerson: CampaignPerson?,
    ): List<CharactersViewState.MembershipRow> {
        val ref = membershipRef(key, campaignPerson)
        return latestMemberships
            .filter { membership -> sameRef(membership.person, ref) }
            .map { membership ->
                CharactersViewState.MembershipRow(
                    id = membership.id,
                    factionName = latestFactions.firstOrNull { it.id == membership.factionId }?.name
                        ?: "Unknown faction",
                    role = membership.role,
                    notes = membership.notes,
                )
            }
    }

    private fun membershipRef(
        key: CharactersViewState.PersonKey,
        campaignPerson: CampaignPerson? = latestCampaignPeople.firstOrNull { it.id == key.id },
    ): PersonRef {
        return when (key.membership) {
            PersonMembership.WorldLibrary -> PersonRef.World(key.id)
            PersonMembership.ThisCampaign -> {
                val worldPersonId = campaignPerson?.worldPersonId
                if (worldPersonId != null) {
                    PersonRef.World(worldPersonId)
                } else {
                    PersonRef.Campaign(key.id)
                }
            }
        }
    }

    private fun factionOptions(): List<CharactersViewState.FactionOption> {
        return latestFactions.map { faction ->
            CharactersViewState.FactionOption(id = faction.id, name = faction.name)
        }
    }

    private fun availableMembershipFactions(
        selected: CharactersViewState.PersonKey?,
    ): List<CharactersViewState.FactionOption> {
        if (selected == null) {
            return factionOptions()
        }
        val memberFactionIds = latestMemberships
            .filter { membership -> sameRef(membership.person, membershipRef(selected)) }
            .map { it.factionId }
            .toSet()
        return factionOptions().filter { option -> option.id !in memberFactionIds }
    }

    private fun openSrdMonsterPicker() {
        if (latestWorldSystem != GameSystem.FifthEdition || latestPickerCatalog.monsters.isEmpty()) {
            return
        }
        setSrdMonsterPicker(open = true)
    }

    private fun addSrdMonster(name: String) {
        appScope.scope.launch {
            when (createFromSrdMonster(name)) {
                is CreateWorldPersonFromSrdMonsterUseCase.Result.Created -> {
                    setSrdMonsterPicker(open = false)
                }
                CreateWorldPersonFromSrdMonsterUseCase.Result.NotFound -> {
                    setSrdMonsterPicker(open = false)
                }
                CreateWorldPersonFromSrdMonsterUseCase.Result.NoActiveWorld,
                CreateWorldPersonFromSrdMonsterUseCase.Result.InvalidName -> Unit
            }
        }
    }

    private fun setSrdMonsterPicker(open: Boolean) {
        srdMonsterPickerOpen = open
        val picker = srdMonsterPicker()
        when (val current = _state.value) {
            is CharactersViewState.Empty -> {
                _state.value = current.copy(srdMonsterPicker = picker)
            }
            is CharactersViewState.Content -> {
                _state.value = current.copy(srdMonsterPicker = picker)
            }
            else -> Unit
        }
    }

    private fun srdMonsterPicker(): List<SrdMonsterEntry>? {
        if (!srdMonsterPickerOpen) {
            return null
        }
        return latestPickerCatalog.monsters
    }

    private data class PeopleLoad(
        val details: ActiveContextDetails,
        val people: PeopleSnapshot,
        val relationships: List<PersonRelationship>,
        val lore: List<Lore>,
        val quests: List<Quest>,
    )

    private data class ExtraLoad(
        val companions: List<PersonCompanion>,
        val factions: List<Faction>,
        val memberships: List<FactionMembership>,
        val pickerCatalog: FifthEditionPickerCatalog,
    )

    private data class LoadedSnapshot(
        val details: ActiveContextDetails,
        val people: PeopleSnapshot,
        val relationships: List<PersonRelationship>,
        val companions: List<PersonCompanion>,
        val lore: List<Lore>,
        val quests: List<Quest>,
        val factions: List<Faction>,
        val memberships: List<FactionMembership>,
        val pickerCatalog: FifthEditionPickerCatalog,
    )

    private data class PendingWizard(
        val fifthEdition: CharactersViewState.CreationWizardState?,
        val pathfinder: CharactersViewState.PathfinderWizardState?,
    )

    private enum class PendingCreate {
        Person,
        PlayerCharacter,
        ;

        fun membership(): PersonMembership {
            return when (this) {
                Person -> PersonMembership.WorldLibrary
                PlayerCharacter -> PersonMembership.ThisCampaign
            }
        }

        fun kind(): PersonKind {
            return when (this) {
                Person -> PersonKind.Npc
                PlayerCharacter -> PersonKind.PlayerCharacter
            }
        }
    }
}
