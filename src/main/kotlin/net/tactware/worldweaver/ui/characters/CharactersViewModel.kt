package net.tactware.worldweaver.ui.characters

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
import net.tactware.worldweaver.core.AppCoroutineScope
import net.tactware.worldweaver.domain.AbilityScoreMethod
import net.tactware.worldweaver.domain.AbilityScores
import net.tactware.worldweaver.domain.ActiveContextDetails
import net.tactware.worldweaver.domain.AddWorldPersonToCampaignUseCase
import net.tactware.worldweaver.domain.ClearPersonAvatarUseCase
import net.tactware.worldweaver.domain.ClearVoiceClipUseCase
import net.tactware.worldweaver.domain.CampaignPerson
import net.tactware.worldweaver.domain.CampaignPersonDraft
import net.tactware.worldweaver.domain.ClassLevel
import net.tactware.worldweaver.domain.CompanionKind
import net.tactware.worldweaver.domain.CreateCampaignPersonUseCase
import net.tactware.worldweaver.domain.CreatePersonCompanionUseCase
import net.tactware.worldweaver.domain.CreatePersonRelationshipUseCase
import net.tactware.worldweaver.domain.CreateWorldPersonUseCase
import net.tactware.worldweaver.domain.DeathSaves
import net.tactware.worldweaver.domain.DeleteCampaignPersonUseCase
import net.tactware.worldweaver.domain.DeletePersonCompanionUseCase
import net.tactware.worldweaver.domain.DeletePersonRelationshipUseCase
import net.tactware.worldweaver.domain.DeleteWorldPersonUseCase
import net.tactware.worldweaver.domain.FifthEditionSheet
import net.tactware.worldweaver.domain.GenerateRandomNpcUseCase
import net.tactware.worldweaver.domain.InventoryItem
import net.tactware.worldweaver.domain.Lore
import net.tactware.worldweaver.domain.ObserveActiveContextDetailsUseCase
import net.tactware.worldweaver.domain.ObserveLoreForActiveWorldUseCase
import net.tactware.worldweaver.domain.ObservePeopleForActiveContextUseCase
import net.tactware.worldweaver.domain.ObservePersonCompanionsUseCase
import net.tactware.worldweaver.domain.ObservePersonRelationshipsUseCase
import net.tactware.worldweaver.domain.PersonCompanion
import net.tactware.worldweaver.domain.ObserveQuestsForActiveCampaignUseCase
import net.tactware.worldweaver.domain.Quest
import net.tactware.worldweaver.domain.QuestLinkKind
import net.tactware.worldweaver.domain.PeopleSnapshot
import net.tactware.worldweaver.domain.PersonFeature
import net.tactware.worldweaver.domain.PersonKind
import net.tactware.worldweaver.domain.PersonAvatarFileStore
import net.tactware.worldweaver.domain.PersonRef
import net.tactware.worldweaver.domain.SetPersonAvatarUseCase
import net.tactware.worldweaver.domain.SetVoiceClipUseCase
import net.tactware.worldweaver.domain.VoiceClipFileStore
import net.tactware.worldweaver.domain.VoiceClipPlayer
import net.tactware.worldweaver.domain.VoiceClipRecorder
import net.tactware.worldweaver.domain.VoiceClipRef
import net.tactware.worldweaver.domain.PersonRelationship
import net.tactware.worldweaver.domain.PersonSpell
import net.tactware.worldweaver.domain.RelationshipType
import net.tactware.worldweaver.domain.UpdateCampaignPersonUseCase
import net.tactware.worldweaver.domain.UpdateWorldPersonUseCase
import net.tactware.worldweaver.domain.WorldPerson
import net.tactware.worldweaver.domain.WorldPersonDraft

internal class CharactersViewModel(
    private val appScope: AppCoroutineScope,
    private val observeActiveContextDetails: ObserveActiveContextDetailsUseCase,
    private val observePeople: ObservePeopleForActiveContextUseCase,
    private val observeRelationships: ObservePersonRelationshipsUseCase,
    private val observeCompanions: ObservePersonCompanionsUseCase,
    private val observeLore: ObserveLoreForActiveWorldUseCase,
    private val observeQuests: ObserveQuestsForActiveCampaignUseCase,
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
    private val createPersonRelationship: CreatePersonRelationshipUseCase,
    private val deletePersonRelationship: DeletePersonRelationshipUseCase,
    private val createPersonCompanion: CreatePersonCompanionUseCase,
    private val deletePersonCompanion: DeletePersonCompanionUseCase,
) {
    private val _state = MutableStateFlow<CharactersViewState>(CharactersViewState.Loading)
    val state: StateFlow<CharactersViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<CharactersViewEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<CharactersViewEffect> = _effects.asSharedFlow()

    private var observeJob: Job? = null
    private var openCreateOnNextLoad = false
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
    private var latestWorldName: String = ""
    private var latestCampaignName: String? = null
    private var hasActiveCampaign = false
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
            is CharactersInteraction.RelationshipFactionChanged -> updateRelationshipEditor { editor ->
                editor.copy(factionLean = interaction.factionLean)
            }
            CharactersInteraction.RelationshipSaved -> saveRelationship()
            is CharactersInteraction.RelationshipDeleted -> deleteRelationship(interaction.relationshipId)
            is CharactersInteraction.EditorMembershipSelected -> updateEditor { editor ->
                editor?.let { changeMembership(it, interaction.membership) }
            }
            is CharactersInteraction.EditorKindSelected -> updateEditor { editor ->
                editor?.copy(kind = interaction.kind)
            }
            is CharactersInteraction.EditorNameChanged -> updateEditor { editor ->
                editor?.copy(name = interaction.name, nameError = null)
            }
            is CharactersInteraction.EditorDescriptionChanged -> updateEditor { editor ->
                editor?.copy(description = interaction.description)
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
            is CharactersInteraction.EditorStrengthChanged -> updateEditor { editor ->
                editor?.copy(strength = interaction.value)
            }
            is CharactersInteraction.EditorDexterityChanged -> updateEditor { editor ->
                editor?.copy(dexterity = interaction.value)
            }
            is CharactersInteraction.EditorConstitutionChanged -> updateEditor { editor ->
                editor?.copy(constitution = interaction.value)
            }
            is CharactersInteraction.EditorIntelligenceChanged -> updateEditor { editor ->
                editor?.copy(intelligence = interaction.value)
            }
            is CharactersInteraction.EditorWisdomChanged -> updateEditor { editor ->
                editor?.copy(wisdom = interaction.value)
            }
            is CharactersInteraction.EditorCharismaChanged -> updateEditor { editor ->
                editor?.copy(charisma = interaction.value)
            }
            is CharactersInteraction.EditorHitPointsChanged -> updateEditor { editor ->
                editor?.copy(hitPoints = interaction.value)
            }
            is CharactersInteraction.EditorMaxHitPointsChanged -> updateEditor { editor ->
                editor?.copy(maxHitPoints = interaction.value)
            }
            is CharactersInteraction.EditorTemporaryHitPointsChanged -> updateEditor { editor ->
                editor?.copy(temporaryHitPoints = interaction.value)
            }
            is CharactersInteraction.EditorArmorClassChanged -> updateEditor { editor ->
                editor?.copy(armorClass = interaction.value)
            }
            is CharactersInteraction.EditorWalkSpeedChanged -> updateEditor { editor ->
                editor?.copy(walkSpeed = interaction.value.filter { it.isDigit() }.take(3))
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
                spell.copy(name = interaction.name)
            }
            is CharactersInteraction.EditorSpellLevelChanged -> updateSpell(interaction.index) { spell ->
                spell.copy(levelText = interaction.level)
            }
            is CharactersInteraction.EditorSpellPreparedChanged -> updateSpell(interaction.index) { spell ->
                spell.copy(prepared = interaction.prepared)
            }
            is CharactersInteraction.EditorNotesChanged -> updateEditor { editor ->
                editor?.copy(notes = interaction.notes)
            }
            is CharactersInteraction.EditorOverlayHitPointsChanged -> updateEditor { editor ->
                editor?.copy(overlayHitPoints = interaction.value)
            }
            is CharactersInteraction.EditorOverlayNotesChanged -> updateEditor { editor ->
                editor?.copy(overlayNotes = interaction.notes)
            }
            CharactersInteraction.EditorSaved -> saveEditor()
            CharactersInteraction.EditorDismissed -> updateEditor { null }
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
            is CharactersInteraction.WizardMembershipSelected -> updateWizard { wizard ->
                changeWizardMembership(wizard, interaction.membership)
            }
            is CharactersInteraction.WizardKindSelected -> updateWizard { wizard ->
                wizard.copy(kind = interaction.kind)
            }
            is CharactersInteraction.WizardNameChanged -> updateWizard { wizard ->
                wizard.copy(name = interaction.name, nameError = null)
            }
            is CharactersInteraction.WizardDescriptionChanged -> updateWizard { wizard ->
                wizard.copy(description = interaction.description)
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
            is CharactersInteraction.WizardStrengthChanged -> updateWizard { wizard ->
                wizard.copy(strength = interaction.value)
            }
            is CharactersInteraction.WizardDexterityChanged -> updateWizard { wizard ->
                wizard.copy(dexterity = interaction.value)
            }
            is CharactersInteraction.WizardConstitutionChanged -> updateWizard { wizard ->
                wizard.copy(constitution = interaction.value)
            }
            is CharactersInteraction.WizardIntelligenceChanged -> updateWizard { wizard ->
                wizard.copy(intelligence = interaction.value)
            }
            is CharactersInteraction.WizardWisdomChanged -> updateWizard { wizard ->
                wizard.copy(wisdom = interaction.value)
            }
            is CharactersInteraction.WizardCharismaChanged -> updateWizard { wizard ->
                wizard.copy(charisma = interaction.value)
            }
            is CharactersInteraction.WizardHitPointsChanged -> updateWizard { wizard ->
                wizard.copy(hitPoints = interaction.value)
            }
            is CharactersInteraction.WizardMaxHitPointsChanged -> updateWizard { wizard ->
                wizard.copy(maxHitPoints = interaction.value)
            }
            is CharactersInteraction.WizardArmorClassChanged -> updateWizard { wizard ->
                wizard.copy(armorClass = interaction.value)
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
            CharactersInteraction.WizardNextSelected -> advanceWizard()
            CharactersInteraction.WizardBackSelected -> rewindWizard()
            CharactersInteraction.WizardSaved -> saveWizard()
            CharactersInteraction.WizardDismissed -> updateWizardState(null)
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
                observeCompanions(),
            ) { load, companions ->
                LoadedSnapshot(
                    load.details,
                    load.people,
                    load.relationships,
                    companions,
                    load.lore,
                    load.quests,
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
        latestWorldName = world.name
        latestCampaignName = details.campaign?.name
        hasActiveCampaign = details.campaign != null
        val current = _state.value
        val editor = editorFrom(current)
        val wizard = if (openCreateOnNextLoad) {
            openCreateOnNextLoad = false
            createWizard()
        } else {
            wizardFrom(current)?.copy(companionTargets = companionTargets(null))
        }
        val generator = generatorFrom(current)
        if (people.worldPeople.isEmpty() && people.campaignPeople.isEmpty()) {
            selectedKey = null
            _state.value = CharactersViewState.Empty(
                worldName = world.name,
                campaignName = details.campaign?.name,
                editor = editor,
                generator = generator,
                wizard = wizard,
            )
            return
        }
        _state.value = contentState(
            editor = editor,
            generator = generator,
            wizard = wizard,
            relationshipEditor = relationshipEditorFrom(current)?.copy(
                targets = relationshipTargets(selectedKey),
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
            generator = current.generator,
            wizard = current.wizard,
            relationshipEditor = current.relationshipEditor,
            companionEditor = current.companionEditor,
            pendingDelete = current.pendingDelete,
            blockDeleteReason = current.blockDeleteReason,
        )
    }

    private fun contentState(
        editor: CharactersViewState.CharacterEditorState?,
        generator: CharactersViewState.GeneratorState?,
        wizard: CharactersViewState.CreationWizardState?,
        relationshipEditor: CharactersViewState.RelationshipEditorState?,
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
            generator = generator,
            wizard = wizard,
            relationshipEditor = relationshipEditor,
            companionEditor = companionEditor,
            pendingDelete = pendingDelete,
            blockDeleteReason = blockDeleteReason,
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
        sheet: FifthEditionSheet,
        isReference: Boolean = false,
    ): String {
        val membershipLabel = if (isReference) {
            "Campaign reference"
        } else {
            membership.displayName
        }
        val race = sheet.race.takeIf { it.isNotBlank() }
        val level = "Lv ${sheet.totalLevel()}"
        return listOfNotNull(kind.displayName, membershipLabel, race, level).joinToString(" · ")
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

    private fun resolvedSheet(person: CampaignPerson): FifthEditionSheet {
        val worldId = person.worldPersonId ?: return person.sheet
        return latestWorldPeople.firstOrNull { it.id == worldId }?.sheet ?: person.sheet
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
                    factionLean = relationship.factionLean,
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
        val wizard = createWizard()
        when (val current = _state.value) {
            is CharactersViewState.Empty -> _state.value = current.copy(wizard = wizard)
            is CharactersViewState.Content -> _state.value = current.copy(wizard = wizard)
            CharactersViewState.Loading, is CharactersViewState.Error -> {
                openCreateOnNextLoad = true
            }
            CharactersViewState.NoActiveWorld -> Unit
        }
    }

    private fun openEditEditor(key: CharactersViewState.PersonKey) {
        val editor = when (key.membership) {
            PersonMembership.WorldLibrary -> {
                val person = latestWorldPeople.firstOrNull { it.id == key.id } ?: return
                editorFromPerson(
                    personId = person.id,
                    membership = PersonMembership.WorldLibrary,
                    isWorldReference = false,
                    kind = person.kind,
                    name = person.name,
                    description = person.description,
                    sheet = person.sheet,
                    overlayHitPoints = "",
                    overlayNotes = "",
                )
            }
            PersonMembership.ThisCampaign -> {
                val person = latestCampaignPeople.firstOrNull { it.id == key.id } ?: return
                editorFromPerson(
                    personId = person.id,
                    membership = PersonMembership.ThisCampaign,
                    isWorldReference = person.isWorldReference(),
                    kind = person.kind,
                    name = person.name,
                    description = person.description,
                    sheet = resolvedSheet(person),
                    overlayHitPoints = person.overlayHitPoints?.toString().orEmpty(),
                    overlayNotes = person.overlayNotes,
                )
            }
        }
        when (val current = _state.value) {
            is CharactersViewState.Content -> _state.value = current.copy(editor = editor)
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
                factionLean = "",
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
                factionLean = editor.factionLean,
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
        companionEditor: CharactersViewState.CompanionEditorState? =
            companionEditorFrom(_state.value),
    ) {
        when (val current = _state.value) {
            is CharactersViewState.Content -> {
                _state.value = current.copy(
                    pendingDelete = pendingDelete,
                    blockDeleteReason = blockDeleteReason,
                    relationshipEditor = relationshipEditor,
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

    private fun createWizard(): CharactersViewState.CreationWizardState {
        return CharactersViewState.CreationWizardState(
            step = CharactersViewState.CreationStep.Identity,
            membership = PersonMembership.WorldLibrary,
            canChangeMembership = hasActiveCampaign,
            hasActiveCampaign = hasActiveCampaign,
            kind = PersonKind.Npc,
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

    private fun companionEditorFrom(
        state: CharactersViewState,
    ): CharactersViewState.CompanionEditorState? {
        return (state as? CharactersViewState.Content)?.companionEditor
    }

    private data class PeopleLoad(
        val details: ActiveContextDetails,
        val people: PeopleSnapshot,
        val relationships: List<PersonRelationship>,
        val lore: List<Lore>,
        val quests: List<Quest>,
    )

    private data class LoadedSnapshot(
        val details: ActiveContextDetails,
        val people: PeopleSnapshot,
        val relationships: List<PersonRelationship>,
        val companions: List<PersonCompanion>,
        val lore: List<Lore>,
        val quests: List<Quest>,
    )
}
