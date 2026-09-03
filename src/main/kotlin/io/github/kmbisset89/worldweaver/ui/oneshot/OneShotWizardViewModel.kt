package io.github.kmbisset89.worldweaver.ui.oneshot

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import io.github.kmbisset89.worldweaver.core.AppCoroutineScope
import io.github.kmbisset89.worldweaver.domain.CreateOneShotUseCase
import io.github.kmbisset89.worldweaver.domain.OneShotAnswers
import io.github.kmbisset89.worldweaver.domain.OneShotDraft
import io.github.kmbisset89.worldweaver.domain.OneShotDraftFactory
import io.github.kmbisset89.worldweaver.domain.OneShotTemplateCatalog

internal class OneShotWizardViewModel(
    private val appScope: AppCoroutineScope,
    private val createOneShot: CreateOneShotUseCase,
    private val catalog: OneShotTemplateCatalog = OneShotTemplateCatalog(),
    private val draftFactory: OneShotDraftFactory = OneShotDraftFactory(catalog),
) {
    private val _state = MutableStateFlow<OneShotWizardViewState>(initialState())
    val state: StateFlow<OneShotWizardViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<OneShotWizardViewEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<OneShotWizardViewEffect> = _effects.asSharedFlow()

    fun onInteraction(interaction: OneShotWizardInteraction) {
        when (interaction) {
            OneShotWizardInteraction.ScreenStarted -> reset()
            OneShotWizardInteraction.BackSelected -> goBack()
            OneShotWizardInteraction.NextSelected -> goNext()
            OneShotWizardInteraction.Saved -> save()
            OneShotWizardInteraction.Dismissed -> {
                reset()
                _effects.tryEmit(OneShotWizardViewEffect.Dismissed)
            }
            is OneShotWizardInteraction.WorldNameChanged -> updateAnswers { answers ->
                answers.copy(worldName = interaction.value)
            }
            is OneShotWizardInteraction.CampaignNameChanged -> updateAnswers { answers ->
                answers.copy(campaignName = interaction.value)
            }
            is OneShotWizardInteraction.GameSystemSelected -> updateAnswers { answers ->
                answers.copy(gameSystem = interaction.gameSystem)
            }
            is OneShotWizardInteraction.LoglineChanged -> updateAnswers { answers ->
                answers.copy(logline = interaction.value)
            }
            is OneShotWizardInteraction.GenreSelected -> updateAnswers { answers ->
                draftFactory.applyGenre(answers, interaction.id)
            }
            is OneShotWizardInteraction.ToneSelected -> updateAnswers { answers ->
                draftFactory.applyTone(answers, interaction.id)
            }
            is OneShotWizardInteraction.HookSelected -> updateAnswers { answers ->
                draftFactory.applyHook(answers, interaction.id)
            }
            is OneShotWizardInteraction.HookChanged -> updateAnswers { answers ->
                answers.copy(hook = interaction.value)
            }
            is OneShotWizardInteraction.RealmNameChanged -> updateAnswers { answers ->
                answers.copy(realmName = interaction.value)
            }
            is OneShotWizardInteraction.RegionNameChanged -> updateAnswers { answers ->
                answers.copy(regionName = interaction.value)
            }
            is OneShotWizardInteraction.SettlementNameChanged -> updateAnswers { answers ->
                answers.copy(settlementName = interaction.value)
            }
            is OneShotWizardInteraction.SiteTypeSelected -> updateAnswers { answers ->
                draftFactory.applySiteType(answers, interaction.id)
            }
            is OneShotWizardInteraction.OpeningSiteNameChanged -> updateAnswers { answers ->
                answers.copy(openingSiteName = interaction.value)
            }
            is OneShotWizardInteraction.OpeningSiteDescriptionChanged -> updateAnswers { answers ->
                answers.copy(openingSiteDescription = interaction.value)
            }
            is OneShotWizardInteraction.MiddleSiteNameChanged -> updateAnswers { answers ->
                answers.copy(middleSiteName = interaction.value)
            }
            is OneShotWizardInteraction.MiddleSiteDescriptionChanged -> updateAnswers { answers ->
                answers.copy(middleSiteDescription = interaction.value)
            }
            is OneShotWizardInteraction.ClimaxSiteNameChanged -> updateAnswers { answers ->
                answers.copy(climaxSiteName = interaction.value)
            }
            is OneShotWizardInteraction.ClimaxSiteDescriptionChanged -> updateAnswers { answers ->
                answers.copy(climaxSiteDescription = interaction.value)
            }
            is OneShotWizardInteraction.PatronNameChanged -> updateAnswers { answers ->
                answers.copy(patronName = interaction.value)
            }
            is OneShotWizardInteraction.PatronDescriptionChanged -> updateAnswers { answers ->
                answers.copy(patronDescription = interaction.value)
            }
            is OneShotWizardInteraction.VillainNameChanged -> updateAnswers { answers ->
                answers.copy(villainName = interaction.value)
            }
            is OneShotWizardInteraction.VillainDescriptionChanged -> updateAnswers { answers ->
                answers.copy(villainDescription = interaction.value)
            }
            is OneShotWizardInteraction.VillainKindSelected -> updateAnswers { answers ->
                answers.copy(villainKind = interaction.kind)
            }
            is OneShotWizardInteraction.VillainTypeSelected -> updateAnswers { answers ->
                draftFactory.applyVillainType(answers, interaction.id)
            }
            is OneShotWizardInteraction.AllyNameChanged -> updateAnswers { answers ->
                answers.copy(allyName = interaction.value)
            }
            is OneShotWizardInteraction.AllyDescriptionChanged -> updateAnswers { answers ->
                answers.copy(allyDescription = interaction.value)
            }
            is OneShotWizardInteraction.FactionNameChanged -> updateAnswers { answers ->
                answers.copy(factionName = interaction.value)
            }
            is OneShotWizardInteraction.FactionDescriptionChanged -> updateAnswers { answers ->
                answers.copy(factionDescription = interaction.value)
            }
            is OneShotWizardInteraction.QuestTitleChanged -> updateAnswers { answers ->
                answers.copy(questTitle = interaction.value)
            }
            is OneShotWizardInteraction.StakesChanged -> updateAnswers { answers ->
                answers.copy(stakes = interaction.value)
            }
            is OneShotWizardInteraction.ObjectiveChanged -> updateObjective(
                interaction.index,
                interaction.value,
            )
            is OneShotWizardInteraction.ObjectiveChipSelected -> updateAnswers { answers ->
                draftFactory.applyObjectiveChip(answers, interaction.id)
            }
            is OneShotWizardInteraction.TwistChanged -> updateAnswers { answers ->
                answers.copy(twist = interaction.value)
            }
            is OneShotWizardInteraction.SessionNameChanged -> updateAnswers { answers ->
                answers.copy(sessionName = interaction.value)
            }
            is OneShotWizardInteraction.OpeningSceneTitleChanged -> updateAnswers { answers ->
                answers.copy(openingSceneTitle = interaction.value)
            }
            is OneShotWizardInteraction.MiddleSceneTitleChanged -> updateAnswers { answers ->
                answers.copy(middleSceneTitle = interaction.value)
            }
            is OneShotWizardInteraction.ClimaxSceneTitleChanged -> updateAnswers { answers ->
                answers.copy(climaxSceneTitle = interaction.value)
            }
            is OneShotWizardInteraction.IncludeEncounterChanged -> updateAnswers { answers ->
                answers.copy(includeEncounter = interaction.include)
            }
            is OneShotWizardInteraction.EncounterNameChanged -> updateAnswers { answers ->
                answers.copy(
                    encounterName = interaction.value,
                    includeEncounter = answers.includeEncounter || interaction.value.isNotBlank(),
                )
            }
            is OneShotWizardInteraction.EncounterDifficultySelected -> updateAnswers { answers ->
                answers.copy(
                    encounterDifficulty = interaction.difficulty,
                    includeEncounter = true,
                )
            }
        }
    }

    private fun reset() {
        _state.value = initialState()
    }

    private fun goBack() {
        val current = content() ?: return
        if (current.isSaving) {
            return
        }
        val previous = current.step.previous() ?: return
        _state.value = current.copy(step = previous, saveError = null)
    }

    private fun goNext() {
        val current = content() ?: return
        if (current.isSaving) {
            return
        }
        val errors = validate(current.step, current.answers)
        if (errors.hasError()) {
            _state.value = current.copy(
                worldNameError = errors.worldNameError,
                campaignNameError = errors.campaignNameError,
                siteNameError = errors.siteNameError,
                questTitleError = errors.questTitleError,
            )
            return
        }
        val next = current.step.next() ?: return
        val answers = if (next == OneShotWizardViewState.Step.TablePlan) {
            prefillScenes(current.answers)
        } else {
            current.answers
        }
        _state.value = current.copy(
            step = next,
            answers = answers,
            worldNameError = null,
            campaignNameError = null,
            siteNameError = null,
            questTitleError = null,
            saveError = null,
            reviewItems = if (next == OneShotWizardViewState.Step.Review) {
                reviewItems(draftFactory.create(answers))
            } else {
                current.reviewItems
            },
        )
    }

    private fun save() {
        val current = content() ?: return
        if (current.isSaving) {
            return
        }
        val errors = validate(OneShotWizardViewState.Step.Review, current.answers)
        if (errors.hasError()) {
            _state.value = current.copy(
                worldNameError = errors.worldNameError,
                campaignNameError = errors.campaignNameError,
                siteNameError = errors.siteNameError,
                questTitleError = errors.questTitleError,
            )
            return
        }
        _state.value = current.copy(isSaving = true, saveError = null)
        val draft = draftFactory.create(current.answers)
        appScope.scope.launch {
            when (val result = createOneShot(draft)) {
                is CreateOneShotUseCase.Result.Created -> {
                    reset()
                    _effects.tryEmit(OneShotWizardViewEffect.Completed)
                }
                is CreateOneShotUseCase.Result.Failed -> {
                    val after = content() ?: return@launch
                    _state.value = after.copy(
                        isSaving = false,
                        saveError = "${result.step.name}: ${result.message}",
                    )
                }
            }
        }
    }

    private fun updateAnswers(transform: (OneShotAnswers) -> OneShotAnswers) {
        val current = content() ?: return
        if (current.isSaving) {
            return
        }
        _state.value = current.copy(
            answers = transform(current.answers),
            worldNameError = null,
            campaignNameError = null,
            siteNameError = null,
            questTitleError = null,
            saveError = null,
        )
    }

    private fun updateObjective(index: Int, value: String) {
        updateAnswers { answers ->
            when (index) {
                0 -> answers.copy(objective1 = value)
                1 -> answers.copy(objective2 = value)
                2 -> answers.copy(objective3 = value)
                3 -> answers.copy(objective4 = value)
                else -> answers
            }
        }
    }

    private fun validate(
        step: OneShotWizardViewState.Step,
        answers: OneShotAnswers,
    ): FieldErrors {
        val checkIdentity = step == OneShotWizardViewState.Step.Identity ||
            step == OneShotWizardViewState.Step.Review
        val checkPlaces = step == OneShotWizardViewState.Step.Places ||
            step == OneShotWizardViewState.Step.Review
        val checkQuest = step == OneShotWizardViewState.Step.Conflict ||
            step == OneShotWizardViewState.Step.Review
        return FieldErrors(
            worldNameError = if (checkIdentity && answers.worldName.isBlank()) {
                "Name is required"
            } else {
                null
            },
            campaignNameError = if (checkIdentity && answers.campaignName.isBlank()) {
                "Name is required"
            } else {
                null
            },
            siteNameError = if (checkPlaces && noSiteNamed(answers)) {
                "Name at least one adventure site"
            } else {
                null
            },
            questTitleError = if (checkQuest && answers.questTitle.isBlank()) {
                "Title is required"
            } else {
                null
            },
        )
    }

    private fun noSiteNamed(answers: OneShotAnswers): Boolean {
        return answers.openingSiteName.isBlank() &&
            answers.middleSiteName.isBlank() &&
            answers.climaxSiteName.isBlank()
    }

    private fun prefillScenes(answers: OneShotAnswers): OneShotAnswers {
        return answers.copy(
            openingSceneTitle = answers.openingSceneTitle.ifBlank { answers.openingSiteName },
            middleSceneTitle = answers.middleSceneTitle.ifBlank { answers.middleSiteName },
            climaxSceneTitle = answers.climaxSceneTitle.ifBlank { answers.climaxSiteName },
            sessionName = answers.sessionName.ifBlank { answers.campaignName },
        )
    }

    private fun reviewItems(draft: OneShotDraft): List<String> {
        val items = mutableListOf(
            "World: ${draft.worldName}",
            "Campaign: ${draft.campaignName}",
            "Places: ${draft.realmName} / ${draft.regionName} / ${draft.settlementName}",
        )
        draft.sites.forEach { site ->
            items += "Site (${site.role.name.lowercase()}): ${site.name}"
        }
        draft.people.forEach { person ->
            items += "${person.role.name}: ${person.name}"
        }
        draft.faction?.let { faction ->
            items += "Faction: ${faction.name}"
        }
        if (draft.loreContent.isNotBlank()) {
            items += "Lore: ${draft.loreTitle}"
        }
        items += "Quest: ${draft.questTitle}"
        items += "Session: ${draft.sessionName}"
        draft.encounterName?.let { name ->
            items += "Encounter: $name"
        }
        return items
    }

    private fun initialState(): OneShotWizardViewState.Content {
        return OneShotWizardViewState.Content(
            step = OneShotWizardViewState.Step.Identity,
            answers = OneShotAnswers(),
            genres = catalog.genreChips().map(::toChip),
            tones = catalog.toneChips().map(::toChip),
            hooks = catalog.hookChips().map(::toChip),
            siteTypes = catalog.siteTypeChips().map(::toChip),
            villainTypes = catalog.villainTypeChips().map(::toChip),
            objectives = catalog.objectiveChips().map(::toChip),
            worldNameError = null,
            campaignNameError = null,
            siteNameError = null,
            questTitleError = null,
            saveError = null,
            isSaving = false,
            reviewItems = emptyList(),
        )
    }

    private fun toChip(chip: OneShotTemplateCatalog.Chip): OneShotWizardViewState.Chip {
        return OneShotWizardViewState.Chip(id = chip.id, label = chip.label)
    }

    private fun content(): OneShotWizardViewState.Content? {
        return _state.value as? OneShotWizardViewState.Content
    }

    private data class FieldErrors(
        val worldNameError: String?,
        val campaignNameError: String?,
        val siteNameError: String?,
        val questTitleError: String?,
    ) {
        fun hasError(): Boolean {
            return worldNameError != null ||
                campaignNameError != null ||
                siteNameError != null ||
                questTitleError != null
        }
    }
}
