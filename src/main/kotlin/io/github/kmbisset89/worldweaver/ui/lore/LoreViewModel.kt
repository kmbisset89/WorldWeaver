package io.github.kmbisset89.worldweaver.ui.lore

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
import io.github.kmbisset89.worldweaver.domain.CreateLoreUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteLoreUseCase
import io.github.kmbisset89.worldweaver.domain.Location
import io.github.kmbisset89.worldweaver.domain.Lore
import io.github.kmbisset89.worldweaver.domain.LoreCategory
import io.github.kmbisset89.worldweaver.domain.LoreDraft
import io.github.kmbisset89.worldweaver.domain.LoreHint
import io.github.kmbisset89.worldweaver.domain.LoreSecret
import io.github.kmbisset89.worldweaver.domain.ObserveActiveContextDetailsUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveLocationsForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveLoreForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.ObservePeopleForActiveContextUseCase
import io.github.kmbisset89.worldweaver.domain.PeopleSnapshot
import io.github.kmbisset89.worldweaver.domain.UpdateLoreUseCase

internal class LoreViewModel(
    private val appScope: AppCoroutineScope,
    private val observeActiveContextDetails: ObserveActiveContextDetailsUseCase,
    private val observeLore: ObserveLoreForActiveWorldUseCase,
    private val observeLocations: ObserveLocationsForActiveWorldUseCase,
    private val observePeople: ObservePeopleForActiveContextUseCase,
    private val createLore: CreateLoreUseCase,
    private val updateLore: UpdateLoreUseCase,
    private val deleteLore: DeleteLoreUseCase,
) {
    private val _state = MutableStateFlow<LoreViewState>(LoreViewState.Loading)
    val state: StateFlow<LoreViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<LoreViewEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<LoreViewEffect> = _effects.asSharedFlow()

    private var observeJob: Job? = null
    private var openCreateOnNextLoad = false
    private var categoryFilter: LoreCategory? = null
    private var selectedLoreId: String? = null
    private var latestLore: List<Lore> = emptyList()
    private var latestLocations: List<Location> = emptyList()
    private var latestPeople: PeopleSnapshot = PeopleSnapshot(emptyList(), emptyList())
    private var latestWorldName: String = ""

    init {
        observe()
    }

    fun onInteraction(interaction: LoreInteraction) {
        when (interaction) {
            LoreInteraction.ScreenStarted -> Unit
            LoreInteraction.RetrySelected -> observe()
            LoreInteraction.CreateWorldSelected -> {
                _effects.tryEmit(LoreViewEffect.OpenWorlds)
            }
            LoreInteraction.NewLoreSelected -> openCreateEditor()
            is LoreInteraction.LoreSelected,
            is LoreInteraction.RelatedLoreSelected,
            -> selectLore(selectedId(interaction))
            is LoreInteraction.LoreOpened -> selectLore(interaction.loreId)
            is LoreInteraction.EditLoreSelected -> openEditEditor(interaction.loreId)
            is LoreInteraction.DeleteLoreSelected -> requestDelete(interaction.loreId)
            LoreInteraction.DeleteConfirmed -> confirmDelete()
            LoreInteraction.DeleteCancelled -> updatePendingDelete(null)
            is LoreInteraction.CategoryFilterSelected -> {
                categoryFilter = interaction.category
                refreshContent()
            }
            is LoreInteraction.HintRevealToggled -> toggleHint(interaction.secretId, interaction.hintId)
            is LoreInteraction.EditorTitleChanged -> updateEditor { editor ->
                editor?.copy(title = interaction.title, titleError = null)
            }
            is LoreInteraction.EditorContentChanged -> updateEditor { editor ->
                editor?.copy(content = interaction.content, contentError = null)
            }
            is LoreInteraction.EditorCategorySelected -> updateEditor { editor ->
                editor?.copy(category = interaction.category)
            }
            is LoreInteraction.EditorTagsChanged -> updateEditor { editor ->
                editor?.copy(tagsText = interaction.tags)
            }
            is LoreInteraction.EditorRelatedToggled -> updateEditor { editor ->
                editor?.let { toggleRelated(it, interaction.loreId) }
            }
            is LoreInteraction.EditorLocationSelected -> updateEditor { editor ->
                editor?.copy(locationId = interaction.locationId)
            }
            is LoreInteraction.EditorCharacterSelected -> updateEditor { editor ->
                editor?.copy(characterId = interaction.characterId)
            }
            LoreInteraction.EditorSecretAdded -> updateEditor { editor ->
                editor?.copy(secrets = editor.secrets + emptySecret())
            }
            is LoreInteraction.EditorSecretRemoved -> updateEditor { editor ->
                editor?.copy(secrets = editor.secrets.filterIndexed { index, _ ->
                    index != interaction.index
                })
            }
            is LoreInteraction.EditorSecretTitleChanged -> updateEditor { editor ->
                editor?.copy(
                    secrets = editor.secrets.mapIndexed { index, secret ->
                        if (index == interaction.index) {
                            secret.copy(title = interaction.title)
                        } else {
                            secret
                        }
                    },
                )
            }
            is LoreInteraction.EditorSecretBodyChanged -> updateEditor { editor ->
                editor?.copy(
                    secrets = editor.secrets.mapIndexed { index, secret ->
                        if (index == interaction.index) {
                            secret.copy(secret = interaction.secret)
                        } else {
                            secret
                        }
                    },
                )
            }
            is LoreInteraction.EditorHintAdded -> updateEditor { editor ->
                editor?.copy(secrets = addHint(editor.secrets, interaction.secretIndex))
            }
            is LoreInteraction.EditorHintRemoved -> updateEditor { editor ->
                editor?.copy(
                    secrets = removeHint(editor.secrets, interaction.secretIndex, interaction.hintIndex),
                )
            }
            is LoreInteraction.EditorHintTextChanged -> updateEditor { editor ->
                editor?.copy(
                    secrets = changeHintText(
                        editor.secrets,
                        interaction.secretIndex,
                        interaction.hintIndex,
                        interaction.text,
                    ),
                )
            }
            LoreInteraction.EditorSaved -> saveEditor()
            LoreInteraction.EditorDismissed -> updateEditor { null }
        }
    }

    private fun selectedId(interaction: LoreInteraction): String {
        return when (interaction) {
            is LoreInteraction.LoreSelected -> interaction.loreId
            is LoreInteraction.RelatedLoreSelected -> interaction.loreId
            else -> ""
        }
    }

    private fun observe() {
        observeJob?.cancel()
        _state.value = LoreViewState.Loading
        observeJob = appScope.scope.launch {
            combine(
                observeActiveContextDetails(),
                observeLore(),
                observeLocations(),
                observePeople(),
            ) { details, lore, locations, people ->
                LoadedSnapshot(details, lore, locations, people)
            }
                .catch { error ->
                    _state.value = LoreViewState.Error(
                        message = error.message ?: "Could not load lore",
                        canRetry = true,
                    )
                }
                .collect { snapshot ->
                    applyLoaded(
                        snapshot.details,
                        snapshot.lore,
                        snapshot.locations,
                        snapshot.people,
                    )
                }
        }
    }

    private fun applyLoaded(
        details: ActiveContextDetails,
        lore: List<Lore>,
        locations: List<Location>,
        people: PeopleSnapshot,
    ) {
        val world = details.world
        if (world == null) {
            latestLore = emptyList()
            latestLocations = emptyList()
            latestPeople = PeopleSnapshot(emptyList(), emptyList())
            selectedLoreId = null
            _state.value = LoreViewState.NoActiveWorld
            return
        }
        latestLore = lore
        latestLocations = locations
        latestPeople = people
        latestWorldName = world.name
        val current = _state.value
        val editor = if (openCreateOnNextLoad) {
            openCreateOnNextLoad = false
            createEditor()
        } else {
            editorFrom(current)?.let { refreshEditorOptions(it) }
        }
        if (lore.isEmpty()) {
            selectedLoreId = null
            _state.value = LoreViewState.Empty(
                worldName = world.name,
                editor = editor,
            )
            return
        }
        val selected = selectedFrom(lore) ?: lore.first()
        selectedLoreId = selected.id
        _state.value = contentState(
            selected = selected,
            editor = editor,
            pendingDelete = pendingDeleteFrom(current),
        )
    }

    private fun refreshContent() {
        val current = _state.value
        if (current !is LoreViewState.Content) {
            return
        }
        val selected = selectedFrom(latestLore) ?: latestLore.firstOrNull()
        _state.value = contentState(
            selected = selected,
            editor = current.editor,
            pendingDelete = current.pendingDelete,
        )
    }

    private fun contentState(
        selected: Lore?,
        editor: LoreViewState.LoreEditorState?,
        pendingDelete: LoreViewState.PendingDelete?,
    ): LoreViewState.Content {
        val visible = latestLore.filter { entry ->
            categoryFilter == null || entry.category == categoryFilter
        }
        return LoreViewState.Content(
            worldName = latestWorldName,
            groups = groupLore(visible),
            selectedLore = selected,
            relatedLinks = relatedLinks(selected),
            attachedLocationName = selected?.locationId?.let { locationId ->
                latestLocations.firstOrNull { it.id == locationId }?.name
            },
            attachedCharacterName = selected?.characterId?.let { characterId ->
                characterName(characterId)
            },
            categoryFilter = categoryFilter,
            editor = editor,
            pendingDelete = pendingDelete,
        )
    }

    private fun groupLore(entries: List<Lore>): List<LoreViewState.LoreGroup> {
        return LoreCategory.entries.mapNotNull { category ->
            val inCategory = entries.filter { it.category == category }
            if (inCategory.isEmpty()) {
                null
            } else {
                LoreViewState.LoreGroup(category = category, entries = inCategory)
            }
        }
    }

    private fun relatedLinks(selected: Lore?): List<LoreViewState.RelatedLink> {
        if (selected == null) {
            return emptyList()
        }
        val byId = latestLore.associateBy { it.id }
        return selected.relatedEntryIds.map { id ->
            val match = byId[id]
            LoreViewState.RelatedLink(
                loreId = id,
                title = match?.title ?: "Missing entry",
                missing = match == null,
            )
        }
    }

    private fun selectLore(loreId: String) {
        if (loreId.isEmpty() || selectedLoreId == loreId) {
            return
        }
        selectedLoreId = loreId
        refreshContent()
    }

    private fun openCreateEditor() {
        when (val current = _state.value) {
            is LoreViewState.Empty -> {
                _state.value = current.copy(editor = createEditor())
            }
            is LoreViewState.Content -> {
                _state.value = current.copy(editor = createEditor())
            }
            LoreViewState.Loading, is LoreViewState.Error -> {
                openCreateOnNextLoad = true
            }
            LoreViewState.NoActiveWorld -> Unit
        }
    }

    private fun openEditEditor(loreId: String) {
        val lore = latestLore.firstOrNull { it.id == loreId } ?: return
        val editor = LoreViewState.LoreEditorState(
            loreId = lore.id,
            title = lore.title,
            content = lore.content,
            category = lore.category,
            tagsText = lore.tags.joinToString("\n"),
            relatedEntryIds = lore.relatedEntryIds,
            relatedOptions = latestLore.filter { it.id != lore.id },
            locationId = lore.locationId,
            locationOptions = latestLocations,
            characterId = lore.characterId,
            characterOptions = characterOptions(),
            secrets = lore.secrets.map { secret ->
                LoreViewState.SecretEditorState(
                    id = secret.id,
                    title = secret.title,
                    secret = secret.secret,
                    hints = secret.hints.map { hint ->
                        LoreViewState.HintEditorState(
                            id = hint.id,
                            text = hint.text,
                            revealed = hint.revealed,
                        )
                    },
                )
            },
            titleError = null,
            contentError = null,
        )
        when (val current = _state.value) {
            is LoreViewState.Content -> _state.value = current.copy(editor = editor)
            else -> Unit
        }
    }

    private fun requestDelete(loreId: String) {
        val lore = latestLore.firstOrNull { it.id == loreId } ?: return
        updatePendingDelete(
            LoreViewState.PendingDelete(
                loreId = lore.id,
                loreTitle = lore.title,
            )
        )
    }

    private fun confirmDelete() {
        val pending = pendingDeleteFrom(_state.value) ?: return
        appScope.scope.launch {
            deleteLore(pending.loreId)
            if (selectedLoreId == pending.loreId) {
                selectedLoreId = null
            }
            updatePendingDelete(null)
        }
    }

    private fun toggleHint(secretId: String, hintId: String) {
        val selected = selectedFrom(latestLore) ?: return
        val updatedSecrets = selected.secrets.map { secret ->
            if (secret.id != secretId) {
                secret
            } else {
                secret.copy(
                    hints = secret.hints.map { hint ->
                        if (hint.id == hintId) {
                            hint.copy(revealed = !hint.revealed)
                        } else {
                            hint
                        }
                    },
                )
            }
        }
        appScope.scope.launch {
            updateLore(
                selected.id,
                draftFromLore(selected.copy(secrets = updatedSecrets)),
            )
        }
    }

    private fun saveEditor() {
        val editor = editorFrom(_state.value) ?: return
        val titleBlank = editor.title.trim().isEmpty()
        val contentBlank = editor.content.trim().isEmpty()
        if (titleBlank || contentBlank) {
            updateEditor { current ->
                current?.copy(
                    titleError = if (titleBlank) "Title is required" else null,
                    contentError = if (contentBlank) "Content is required" else null,
                )
            }
            return
        }
        val draft = draftFrom(editor)
        appScope.scope.launch {
            val result = if (editor.loreId == null) {
                createLore(draft)
            } else {
                updateLore(editor.loreId, draft)
            }
            when (result) {
                is CreateLoreUseCase.Result.Created -> {
                    selectedLoreId = result.lore.id
                    updateEditor { null }
                }
                CreateLoreUseCase.Result.InvalidTitle,
                UpdateLoreUseCase.Result.InvalidTitle,
                -> updateEditor { current -> current?.copy(titleError = "Title is required") }
                CreateLoreUseCase.Result.InvalidContent,
                UpdateLoreUseCase.Result.InvalidContent,
                -> updateEditor { current -> current?.copy(contentError = "Content is required") }
                CreateLoreUseCase.Result.NoActiveWorld,
                CreateLoreUseCase.Result.InvalidLocation,
                UpdateLoreUseCase.Result.InvalidLocation,
                UpdateLoreUseCase.Result.Updated,
                UpdateLoreUseCase.Result.NotFound,
                -> updateEditor { null }
            }
        }
    }

    private fun createEditor(): LoreViewState.LoreEditorState {
        return LoreViewState.LoreEditorState(
            loreId = null,
            title = "",
            content = "",
            category = LoreCategory.History,
            tagsText = "",
            relatedEntryIds = emptyList(),
            relatedOptions = latestLore,
            locationId = null,
            locationOptions = latestLocations,
            characterId = null,
            characterOptions = characterOptions(),
            secrets = emptyList(),
            titleError = null,
            contentError = null,
        )
    }

    private fun refreshEditorOptions(
        editor: LoreViewState.LoreEditorState,
    ): LoreViewState.LoreEditorState {
        return editor.copy(
            relatedOptions = latestLore.filter { it.id != editor.loreId },
            locationOptions = latestLocations,
            characterOptions = characterOptions(),
        )
    }

    private fun toggleRelated(
        editor: LoreViewState.LoreEditorState,
        loreId: String,
    ): LoreViewState.LoreEditorState {
        val related = if (loreId in editor.relatedEntryIds) {
            editor.relatedEntryIds.filterNot { it == loreId }
        } else {
            editor.relatedEntryIds + loreId
        }
        return editor.copy(relatedEntryIds = related)
    }

    private fun emptySecret(): LoreViewState.SecretEditorState {
        return LoreViewState.SecretEditorState(
            id = "",
            title = "",
            secret = "",
            hints = emptyList(),
        )
    }

    private fun addHint(
        secrets: List<LoreViewState.SecretEditorState>,
        secretIndex: Int,
    ): List<LoreViewState.SecretEditorState> {
        return secrets.mapIndexed { index, secret ->
            if (index == secretIndex) {
                secret.copy(
                    hints = secret.hints + LoreViewState.HintEditorState(
                        id = "",
                        text = "",
                        revealed = false,
                    ),
                )
            } else {
                secret
            }
        }
    }

    private fun removeHint(
        secrets: List<LoreViewState.SecretEditorState>,
        secretIndex: Int,
        hintIndex: Int,
    ): List<LoreViewState.SecretEditorState> {
        return secrets.mapIndexed { index, secret ->
            if (index == secretIndex) {
                secret.copy(
                    hints = secret.hints.filterIndexed { current, _ -> current != hintIndex },
                )
            } else {
                secret
            }
        }
    }

    private fun changeHintText(
        secrets: List<LoreViewState.SecretEditorState>,
        secretIndex: Int,
        hintIndex: Int,
        text: String,
    ): List<LoreViewState.SecretEditorState> {
        return secrets.mapIndexed { index, secret ->
            if (index == secretIndex) {
                secret.copy(
                    hints = secret.hints.mapIndexed { current, hint ->
                        if (current == hintIndex) hint.copy(text = text) else hint
                    },
                )
            } else {
                secret
            }
        }
    }

    private fun draftFrom(editor: LoreViewState.LoreEditorState): LoreDraft {
        return LoreDraft(
            title = editor.title,
            content = editor.content,
            category = editor.category,
            tags = editor.tagsText.lines(),
            relatedEntryIds = editor.relatedEntryIds,
            secrets = editor.secrets.map { secret ->
                LoreSecret(
                    id = secret.id,
                    title = secret.title,
                    secret = secret.secret,
                    hints = secret.hints.map { hint ->
                        LoreHint(
                            id = hint.id,
                            text = hint.text,
                            revealed = hint.revealed,
                        )
                    },
                )
            },
            locationId = editor.locationId,
            characterId = editor.characterId,
        )
    }

    private fun draftFromLore(lore: Lore): LoreDraft {
        return LoreDraft(
            title = lore.title,
            content = lore.content,
            category = lore.category,
            tags = lore.tags,
            relatedEntryIds = lore.relatedEntryIds,
            secrets = lore.secrets,
            locationId = lore.locationId,
            characterId = lore.characterId,
        )
    }

    private fun updateEditor(
        transform: (LoreViewState.LoreEditorState?) -> LoreViewState.LoreEditorState?,
    ) {
        when (val current = _state.value) {
            is LoreViewState.Empty -> {
                _state.value = current.copy(editor = transform(current.editor))
            }
            is LoreViewState.Content -> {
                _state.value = current.copy(editor = transform(current.editor))
            }
            else -> Unit
        }
    }

    private fun updatePendingDelete(pendingDelete: LoreViewState.PendingDelete?) {
        val current = _state.value
        if (current is LoreViewState.Content) {
            _state.value = current.copy(pendingDelete = pendingDelete)
        }
    }

    private fun selectedFrom(lore: List<Lore>): Lore? {
        return selectedLoreId?.let { id -> lore.firstOrNull { it.id == id } }
    }

    private fun editorFrom(state: LoreViewState): LoreViewState.LoreEditorState? {
        return when (state) {
            is LoreViewState.Empty -> state.editor
            is LoreViewState.Content -> state.editor
            else -> null
        }
    }

    private fun pendingDeleteFrom(state: LoreViewState): LoreViewState.PendingDelete? {
        return (state as? LoreViewState.Content)?.pendingDelete
    }

    private fun characterOptions(): List<LoreViewState.CharacterOption> {
        val world = latestPeople.worldPeople.map { person ->
            LoreViewState.CharacterOption(id = person.id, name = "${person.name} (world)")
        }
        val campaign = latestPeople.campaignPeople.map { person ->
            LoreViewState.CharacterOption(id = person.id, name = "${person.name} (campaign)")
        }
        return (world + campaign).sortedBy { it.name.lowercase() }
    }

    private fun characterName(characterId: String): String? {
        latestPeople.worldPeople.firstOrNull { it.id == characterId }?.let { return it.name }
        latestPeople.campaignPeople.firstOrNull { it.id == characterId }?.let { return it.name }
        return null
    }

    private data class LoadedSnapshot(
        val details: ActiveContextDetails,
        val lore: List<Lore>,
        val locations: List<Location>,
        val people: PeopleSnapshot,
    )
}
