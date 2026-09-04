package io.github.kmbisset89.worldweaver.ui.quests

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
import io.github.kmbisset89.worldweaver.domain.ActiveContextDetails
import io.github.kmbisset89.worldweaver.domain.AwardPartyExperienceUseCase
import io.github.kmbisset89.worldweaver.domain.AwardPartyLevelUseCase
import io.github.kmbisset89.worldweaver.domain.CreateQuestUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteQuestUseCase
import io.github.kmbisset89.worldweaver.domain.Location
import io.github.kmbisset89.worldweaver.domain.LevelingMode
import io.github.kmbisset89.worldweaver.domain.Lore
import io.github.kmbisset89.worldweaver.domain.ObserveActiveContextDetailsUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveLocationsForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveLoreForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.ObservePeopleForActiveContextUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveQuestsForActiveCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveSessionsForActiveCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.PeopleSnapshot
import io.github.kmbisset89.worldweaver.domain.PersonKind
import io.github.kmbisset89.worldweaver.domain.Quest
import io.github.kmbisset89.worldweaver.domain.QuestDraft
import io.github.kmbisset89.worldweaver.domain.QuestLink
import io.github.kmbisset89.worldweaver.domain.QuestLinkKind
import io.github.kmbisset89.worldweaver.domain.QuestObjective
import io.github.kmbisset89.worldweaver.domain.QuestObjectiveStatus
import io.github.kmbisset89.worldweaver.domain.QuestStatus
import io.github.kmbisset89.worldweaver.domain.Session
import io.github.kmbisset89.worldweaver.domain.UpdateQuestUseCase
import io.github.kmbisset89.worldweaver.ui.advancement.AdvancementPrompt

internal class QuestsViewModel(
    private val appScope: AppCoroutineScope,
    private val observeActiveContextDetails: ObserveActiveContextDetailsUseCase,
    private val observeQuests: ObserveQuestsForActiveCampaignUseCase,
    private val observeLocations: ObserveLocationsForActiveWorldUseCase,
    private val observeLore: ObserveLoreForActiveWorldUseCase,
    private val observePeople: ObservePeopleForActiveContextUseCase,
    private val observeSessions: ObserveSessionsForActiveCampaignUseCase,
    private val createQuest: CreateQuestUseCase,
    private val updateQuest: UpdateQuestUseCase,
    private val deleteQuest: DeleteQuestUseCase,
    private val awardPartyLevel: AwardPartyLevelUseCase,
    private val awardPartyExperience: AwardPartyExperienceUseCase,
) {
    private val _state = MutableStateFlow<QuestsViewState>(QuestsViewState.Loading)
    val state: StateFlow<QuestsViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<QuestsViewEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<QuestsViewEffect> = _effects.asSharedFlow()

    private var observeJob: Job? = null
    private var openCreateOnNextLoad = false
    private var statusFilter: QuestStatus? = null
    private var selectedQuestId: String? = null
    private var latestQuests: List<Quest> = emptyList()
    private var latestLocations: List<Location> = emptyList()
    private var latestLore: List<Lore> = emptyList()
    private var latestPeople: PeopleSnapshot = PeopleSnapshot(emptyList(), emptyList())
    private var latestSessions: List<Session> = emptyList()
    private var latestWorldName: String = ""
    private var latestCampaignName: String = ""
    private var latestCampaignId: String? = null
    private var latestLevelingMode = LevelingMode.Milestone
    private var advancementPrompt: AdvancementPrompt? = null

    init {
        observe()
    }

    fun onInteraction(interaction: QuestsInteraction) {
        when (interaction) {
            QuestsInteraction.ScreenStarted -> Unit
            QuestsInteraction.RetrySelected -> observe()
            QuestsInteraction.CreateWorldSelected -> _effects.tryEmit(QuestsViewEffect.OpenWorlds)
            QuestsInteraction.CreateCampaignSelected -> _effects.tryEmit(QuestsViewEffect.OpenCampaigns)
            QuestsInteraction.NewQuestSelected -> openCreateEditor()
            is QuestsInteraction.QuestSelected,
            is QuestsInteraction.QuestOpened,
            -> selectQuest(questIdFrom(interaction))
            is QuestsInteraction.EditQuestSelected -> openEditEditor(interaction.questId)
            is QuestsInteraction.DeleteQuestSelected -> requestDelete(interaction.questId)
            QuestsInteraction.DeleteConfirmed -> confirmDelete()
            QuestsInteraction.DeleteCancelled -> updatePendingDelete(null)
            is QuestsInteraction.StatusFilterSelected -> {
                statusFilter = interaction.status
                refreshContent()
            }
            is QuestsInteraction.QuestStatusSelected -> changeQuestStatus(
                interaction.questId,
                interaction.status,
            )
            is QuestsInteraction.ObjectiveStatusSelected -> changeObjectiveStatus(
                interaction.questId,
                interaction.objectiveId,
                interaction.status,
            )
            is QuestsInteraction.LinkedLoreSelected -> {
                _effects.tryEmit(QuestsViewEffect.OpenLore(interaction.loreId))
            }
            is QuestsInteraction.LinkedPersonSelected -> {
                _effects.tryEmit(QuestsViewEffect.OpenCharacters)
            }
            is QuestsInteraction.LinkedSessionSelected -> {
                _effects.tryEmit(QuestsViewEffect.OpenSession(interaction.sessionId))
            }
            is QuestsInteraction.LinkedLocationSelected -> {
                _effects.tryEmit(QuestsViewEffect.OpenLocations)
            }
            is QuestsInteraction.EditorTitleChanged -> updateEditor { editor ->
                editor?.copy(title = interaction.title, titleError = null)
            }
            is QuestsInteraction.EditorSummaryChanged -> updateEditor { editor ->
                editor?.copy(summary = interaction.summary)
            }
            is QuestsInteraction.EditorStatusSelected -> updateEditor { editor ->
                editor?.copy(status = interaction.status)
            }
            is QuestsInteraction.EditorLocationSelected -> updateEditor { editor ->
                editor?.copy(locationId = interaction.locationId)
            }
            QuestsInteraction.EditorObjectiveAdded -> updateEditor { editor ->
                editor?.copy(
                    objectives = editor.objectives + QuestsViewState.ObjectiveEditorState(
                        id = "",
                        title = "",
                        status = QuestObjectiveStatus.Open,
                    ),
                )
            }
            is QuestsInteraction.EditorObjectiveRemoved -> updateEditor { editor ->
                editor?.copy(
                    objectives = editor.objectives.filterIndexed { index, _ ->
                        index != interaction.index
                    },
                )
            }
            is QuestsInteraction.EditorObjectiveTitleChanged -> updateEditor { editor ->
                editor?.copy(
                    objectives = editor.objectives.mapIndexed { index, objective ->
                        if (index == interaction.index) {
                            objective.copy(title = interaction.title)
                        } else {
                            objective
                        }
                    },
                )
            }
            is QuestsInteraction.EditorObjectiveStatusSelected -> updateEditor { editor ->
                editor?.copy(
                    objectives = editor.objectives.mapIndexed { index, objective ->
                        if (index == interaction.index) {
                            objective.copy(status = interaction.status)
                        } else {
                            objective
                        }
                    },
                )
            }
            is QuestsInteraction.EditorLoreToggled -> updateEditor { editor ->
                editor?.copy(loreIds = toggleId(editor.loreIds, interaction.loreId))
            }
            is QuestsInteraction.EditorWorldPersonToggled -> updateEditor { editor ->
                editor?.copy(worldPersonIds = toggleId(editor.worldPersonIds, interaction.personId))
            }
            is QuestsInteraction.EditorCampaignPersonToggled -> updateEditor { editor ->
                editor?.copy(
                    campaignPersonIds = toggleId(editor.campaignPersonIds, interaction.personId),
                )
            }
            is QuestsInteraction.EditorSessionToggled -> updateEditor { editor ->
                editor?.copy(sessionIds = toggleId(editor.sessionIds, interaction.sessionId))
            }
            QuestsInteraction.EditorSaved -> saveEditor()
            QuestsInteraction.EditorDismissed -> updateEditor { null }
            QuestsInteraction.AdvancementDismissed -> dismissAdvancement()
            QuestsInteraction.AwardLevelConfirmed -> confirmAwardLevel()
            is QuestsInteraction.AwardExperienceAmountChanged -> {
                val current = advancementPrompt
                if (current is AdvancementPrompt.AwardExperience) {
                    advancementPrompt = current.copy(
                        amountText = interaction.value,
                        amountError = null,
                    )
                    refreshAdvancementPrompt()
                }
            }
            QuestsInteraction.AwardExperienceConfirmed -> confirmAwardExperience()
        }
    }

    private fun questIdFrom(interaction: QuestsInteraction): String {
        return when (interaction) {
            is QuestsInteraction.QuestSelected -> interaction.questId
            is QuestsInteraction.QuestOpened -> interaction.questId
            else -> ""
        }
    }

    private fun observe() {
        observeJob?.cancel()
        _state.value = QuestsViewState.Loading
        observeJob = appScope.scope.launch {
            combine(
                combine(
                    observeActiveContextDetails(),
                    observeQuests(),
                    observeLocations(),
                ) { details, quests, locations ->
                    Triple(details, quests, locations)
                },
                combine(
                    observeLore(),
                    observePeople(),
                    observeSessions(),
                ) { lore, people, sessions ->
                    Triple(lore, people, sessions)
                },
            ) { first, second ->
                LoadedSnapshot(
                    details = first.first,
                    quests = first.second,
                    locations = first.third,
                    lore = second.first,
                    people = second.second,
                    sessions = second.third,
                )
            }
                .catch { error ->
                    _state.value = QuestsViewState.Error(
                        message = error.message ?: "Could not load quests",
                        canRetry = true,
                    )
                }
                .collect { snapshot ->
                    applyLoaded(snapshot)
                }
        }
    }

    private fun applyLoaded(snapshot: LoadedSnapshot) {
        val world = snapshot.details.world
        if (world == null) {
            clearLatest()
            _state.value = QuestsViewState.NoActiveWorld
            return
        }
        val campaign = snapshot.details.campaign
        if (campaign == null) {
            clearLatest()
            latestWorldName = world.name
            _state.value = QuestsViewState.NoActiveCampaign
            return
        }
        latestQuests = snapshot.quests
        latestLocations = snapshot.locations
        latestLore = snapshot.lore
        latestPeople = snapshot.people
        latestSessions = snapshot.sessions
        latestWorldName = world.name
        latestCampaignName = campaign.name
        latestCampaignId = campaign.id
        latestLevelingMode = campaign.levelingMode
        val current = _state.value
        val editor = if (openCreateOnNextLoad) {
            openCreateOnNextLoad = false
            createEditor()
        } else {
            editorFrom(current)?.let { refreshEditorOptions(it) }
        }
        if (snapshot.quests.isEmpty()) {
            selectedQuestId = null
            _state.value = QuestsViewState.Empty(
                worldName = world.name,
                campaignName = campaign.name,
                editor = editor,
            )
            return
        }
        val selected = selectedFrom(snapshot.quests) ?: snapshot.quests.first()
        selectedQuestId = selected.id
        _state.value = contentState(
            selected = selected,
            editor = editor,
            pendingDelete = pendingDeleteFrom(current),
        )
    }

    private fun refreshContent() {
        val current = _state.value
        if (current !is QuestsViewState.Content) {
            return
        }
        val selected = selectedFrom(latestQuests) ?: latestQuests.firstOrNull()
        _state.value = contentState(
            selected = selected,
            editor = current.editor,
            pendingDelete = current.pendingDelete,
        )
    }

    private fun contentState(
        selected: Quest?,
        editor: QuestsViewState.QuestEditorState?,
        pendingDelete: QuestsViewState.PendingDelete?,
    ): QuestsViewState.Content {
        val visible = latestQuests.filter { quest ->
            statusFilter == null || quest.status == statusFilter
        }
        return QuestsViewState.Content(
            worldName = latestWorldName,
            campaignName = latestCampaignName,
            quests = visible,
            selectedQuest = selected,
            statusFilter = statusFilter,
            locationName = selected?.locationId?.let { locationId ->
                latestLocations.firstOrNull { it.id == locationId }?.name
            },
            links = linkRows(selected),
            editor = editor,
            pendingDelete = pendingDelete,
            advancementPrompt = advancementPrompt,
        )
    }

    private fun linkRows(selected: Quest?): List<QuestsViewState.QuestLinkRow> {
        if (selected == null) {
            return emptyList()
        }
        return selected.links.map { link ->
            val label = when (link.kind) {
                QuestLinkKind.LORE -> latestLore.firstOrNull { it.id == link.targetId }?.title
                QuestLinkKind.WORLD_PERSON -> {
                    latestPeople.worldPeople.firstOrNull { it.id == link.targetId }?.name
                }
                QuestLinkKind.CAMPAIGN_PERSON -> {
                    latestPeople.campaignPeople.firstOrNull { it.id == link.targetId }?.name
                }
                QuestLinkKind.SESSION -> latestSessions.firstOrNull { it.id == link.targetId }?.name
            }
            QuestsViewState.QuestLinkRow(
                kind = link.kind,
                targetId = link.targetId,
                label = label ?: "Missing link",
                missing = label == null,
            )
        }
    }

    private fun selectQuest(questId: String) {
        if (questId.isEmpty() || selectedQuestId == questId) {
            return
        }
        selectedQuestId = questId
        refreshContent()
    }

    private fun openCreateEditor() {
        when (val current = _state.value) {
            is QuestsViewState.Empty -> {
                _state.value = current.copy(editor = createEditor())
            }
            is QuestsViewState.Content -> {
                _state.value = current.copy(editor = createEditor())
            }
            QuestsViewState.Loading, is QuestsViewState.Error -> {
                openCreateOnNextLoad = true
            }
            QuestsViewState.NoActiveWorld, QuestsViewState.NoActiveCampaign -> Unit
        }
    }

    private fun openEditEditor(questId: String) {
        val quest = latestQuests.firstOrNull { it.id == questId } ?: return
        val editor = editorFromQuest(quest)
        when (val current = _state.value) {
            is QuestsViewState.Content -> _state.value = current.copy(editor = editor)
            else -> Unit
        }
    }

    private fun requestDelete(questId: String) {
        val quest = latestQuests.firstOrNull { it.id == questId } ?: return
        updatePendingDelete(
            QuestsViewState.PendingDelete(
                questId = quest.id,
                questTitle = quest.title,
            )
        )
    }

    private fun confirmDelete() {
        val pending = pendingDeleteFrom(_state.value) ?: return
        appScope.scope.launch {
            deleteQuest(pending.questId)
            if (selectedQuestId == pending.questId) {
                selectedQuestId = null
            }
            updatePendingDelete(null)
        }
    }

    private fun changeQuestStatus(questId: String, status: QuestStatus) {
        val quest = latestQuests.firstOrNull { it.id == questId } ?: return
        val alreadyCompleted = quest.status == QuestStatus.Completed
        appScope.scope.launch {
            updateQuest(quest.id, draftFromQuest(quest.copy(status = status)))
            if (status == QuestStatus.Completed && !alreadyCompleted) {
                showAdvancementPrompt()
            }
        }
    }

    private fun changeObjectiveStatus(
        questId: String,
        objectiveId: String,
        status: QuestObjectiveStatus,
    ) {
        val quest = latestQuests.firstOrNull { it.id == questId } ?: return
        val objectives = quest.objectives.map { objective ->
            if (objective.id == objectiveId) objective.copy(status = status) else objective
        }
        appScope.scope.launch {
            updateQuest(quest.id, draftFromQuest(quest.copy(objectives = objectives)))
        }
    }

    private fun saveEditor() {
        val editor = editorFrom(_state.value) ?: return
        if (editor.title.trim().isEmpty()) {
            updateEditor { current -> current?.copy(titleError = "Title is required") }
            return
        }
        val draft = draftFrom(editor)
        appScope.scope.launch {
            val result = if (editor.questId == null) {
                createQuest(draft)
            } else {
                updateQuest(editor.questId, draft)
            }
            when (result) {
                is CreateQuestUseCase.Result.Created -> {
                    selectedQuestId = result.quest.id
                    updateEditor { null }
                }
                CreateQuestUseCase.Result.InvalidTitle,
                UpdateQuestUseCase.Result.InvalidTitle,
                -> updateEditor { current -> current?.copy(titleError = "Title is required") }
                CreateQuestUseCase.Result.NoActiveCampaign,
                CreateQuestUseCase.Result.InvalidLocation,
                UpdateQuestUseCase.Result.InvalidLocation,
                UpdateQuestUseCase.Result.Updated,
                UpdateQuestUseCase.Result.NotFound,
                -> updateEditor { null }
            }
        }
    }

    private fun createEditor(): QuestsViewState.QuestEditorState {
        return QuestsViewState.QuestEditorState(
            questId = null,
            title = "",
            summary = "",
            status = QuestStatus.Active,
            locationId = null,
            locationOptions = latestLocations,
            objectives = emptyList(),
            loreIds = emptyList(),
            loreOptions = latestLore,
            worldPersonIds = emptyList(),
            campaignPersonIds = emptyList(),
            personOptions = personOptions(),
            sessionIds = emptyList(),
            sessionOptions = latestSessions,
            titleError = null,
        )
    }

    private fun editorFromQuest(quest: Quest): QuestsViewState.QuestEditorState {
        return QuestsViewState.QuestEditorState(
            questId = quest.id,
            title = quest.title,
            summary = quest.summary,
            status = quest.status,
            locationId = quest.locationId,
            locationOptions = latestLocations,
            objectives = quest.objectives.map { objective ->
                QuestsViewState.ObjectiveEditorState(
                    id = objective.id,
                    title = objective.title,
                    status = objective.status,
                )
            },
            loreIds = quest.links.filter { it.kind == QuestLinkKind.LORE }.map { it.targetId },
            loreOptions = latestLore,
            worldPersonIds = quest.links
                .filter { it.kind == QuestLinkKind.WORLD_PERSON }
                .map { it.targetId },
            campaignPersonIds = quest.links
                .filter { it.kind == QuestLinkKind.CAMPAIGN_PERSON }
                .map { it.targetId },
            personOptions = personOptions(),
            sessionIds = quest.links.filter { it.kind == QuestLinkKind.SESSION }.map { it.targetId },
            sessionOptions = latestSessions,
            titleError = null,
        )
    }

    private fun refreshEditorOptions(
        editor: QuestsViewState.QuestEditorState,
    ): QuestsViewState.QuestEditorState {
        return editor.copy(
            locationOptions = latestLocations,
            loreOptions = latestLore,
            personOptions = personOptions(),
            sessionOptions = latestSessions,
        )
    }

    private fun draftFrom(editor: QuestsViewState.QuestEditorState): QuestDraft {
        val links = editor.loreIds.map { id ->
            QuestLink(id = "", kind = QuestLinkKind.LORE, targetId = id)
        } + editor.worldPersonIds.map { id ->
            QuestLink(id = "", kind = QuestLinkKind.WORLD_PERSON, targetId = id)
        } + editor.campaignPersonIds.map { id ->
            QuestLink(id = "", kind = QuestLinkKind.CAMPAIGN_PERSON, targetId = id)
        } + editor.sessionIds.map { id ->
            QuestLink(id = "", kind = QuestLinkKind.SESSION, targetId = id)
        }
        return QuestDraft(
            title = editor.title,
            summary = editor.summary,
            status = editor.status,
            locationId = editor.locationId,
            objectives = editor.objectives.map { objective ->
                QuestObjective(
                    id = objective.id,
                    title = objective.title,
                    status = objective.status,
                )
            },
            links = links,
        )
    }

    private fun draftFromQuest(quest: Quest): QuestDraft {
        return QuestDraft(
            title = quest.title,
            summary = quest.summary,
            status = quest.status,
            locationId = quest.locationId,
            objectives = quest.objectives,
            links = quest.links,
        )
    }

    private fun personOptions(): List<QuestsViewState.PersonOption> {
        val world = latestPeople.worldPeople.map { person ->
            QuestsViewState.PersonOption(
                id = person.id,
                name = "${person.name} (world)",
                worldOwned = true,
            )
        }
        val campaign = latestPeople.campaignPeople.map { person ->
            QuestsViewState.PersonOption(
                id = person.id,
                name = "${person.name} (campaign)",
                worldOwned = false,
            )
        }
        return (world + campaign).sortedBy { it.name.lowercase() }
    }

    private fun toggleId(ids: List<String>, id: String): List<String> {
        return if (id in ids) ids.filterNot { it == id } else ids + id
    }

    private fun updateEditor(
        transform: (QuestsViewState.QuestEditorState?) -> QuestsViewState.QuestEditorState?,
    ) {
        when (val current = _state.value) {
            is QuestsViewState.Empty -> {
                _state.value = current.copy(editor = transform(current.editor))
            }
            is QuestsViewState.Content -> {
                _state.value = current.copy(editor = transform(current.editor))
            }
            else -> Unit
        }
    }

    private fun updatePendingDelete(pendingDelete: QuestsViewState.PendingDelete?) {
        val current = _state.value
        if (current is QuestsViewState.Content) {
            _state.value = current.copy(pendingDelete = pendingDelete)
        }
    }

    private fun selectedFrom(quests: List<Quest>): Quest? {
        return selectedQuestId?.let { id -> quests.firstOrNull { it.id == id } }
    }

    private fun editorFrom(state: QuestsViewState): QuestsViewState.QuestEditorState? {
        return when (state) {
            is QuestsViewState.Empty -> state.editor
            is QuestsViewState.Content -> state.editor
            else -> null
        }
    }

    private fun pendingDeleteFrom(state: QuestsViewState): QuestsViewState.PendingDelete? {
        return (state as? QuestsViewState.Content)?.pendingDelete
    }

    private fun clearLatest() {
        latestQuests = emptyList()
        latestLocations = emptyList()
        latestLore = emptyList()
        latestPeople = PeopleSnapshot(emptyList(), emptyList())
        latestSessions = emptyList()
        selectedQuestId = null
        latestCampaignId = null
        latestLevelingMode = LevelingMode.Milestone
        advancementPrompt = null
    }

    private fun showAdvancementPrompt() {
        val partySize = latestPeople.campaignPeople.count { it.kind == PersonKind.PlayerCharacter }
        advancementPrompt = promptFor(latestLevelingMode, partySize)
        refreshAdvancementPrompt()
    }

    private fun dismissAdvancement() {
        advancementPrompt = null
        refreshAdvancementPrompt()
    }

    private fun confirmAwardLevel() {
        val campaignId = latestCampaignId ?: return
        appScope.scope.launch {
            awardPartyLevel(campaignId)
            advancementPrompt = null
            refreshAdvancementPrompt()
        }
    }

    private fun confirmAwardExperience() {
        val current = advancementPrompt as? AdvancementPrompt.AwardExperience ?: return
        val amount = current.amountText.toIntOrNull()
        if (amount == null || amount <= 0) {
            advancementPrompt = current.copy(amountError = "Enter a positive number")
            refreshAdvancementPrompt()
            return
        }
        val campaignId = latestCampaignId ?: return
        appScope.scope.launch {
            awardPartyExperience(campaignId, amount)
            advancementPrompt = null
            refreshAdvancementPrompt()
        }
    }

    private fun refreshAdvancementPrompt() {
        when (val current = _state.value) {
            is QuestsViewState.Content -> {
                _state.value = current.copy(advancementPrompt = advancementPrompt)
            }
            else -> Unit
        }
    }

    private fun promptFor(mode: LevelingMode, partySize: Int): AdvancementPrompt? {
        if (partySize == 0) {
            return null
        }
        return when (mode) {
            LevelingMode.Milestone -> AdvancementPrompt.AwardLevel
            LevelingMode.Experience -> AdvancementPrompt.AwardExperience(
                amountText = "",
                amountError = null,
            )
        }
    }

    private data class LoadedSnapshot(
        val details: ActiveContextDetails,
        val quests: List<Quest>,
        val locations: List<Location>,
        val lore: List<Lore>,
        val people: PeopleSnapshot,
        val sessions: List<Session>,
    )
}
