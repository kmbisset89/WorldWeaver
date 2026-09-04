package io.github.kmbisset89.worldweaver.ui.sheet

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
import io.github.kmbisset89.worldweaver.domain.AbilityScores
import io.github.kmbisset89.worldweaver.domain.CampaignPerson
import io.github.kmbisset89.worldweaver.domain.CampaignPersonDraft
import io.github.kmbisset89.worldweaver.domain.DeathSaves
import io.github.kmbisset89.worldweaver.domain.FifthEditionSheet
import io.github.kmbisset89.worldweaver.domain.FifthEditionSkillCatalog
import io.github.kmbisset89.worldweaver.domain.LevelingMode
import io.github.kmbisset89.worldweaver.domain.ObserveActiveContextDetailsUseCase
import io.github.kmbisset89.worldweaver.domain.ObservePeopleForActiveContextUseCase
import io.github.kmbisset89.worldweaver.domain.Pathfinder2ESheet
import io.github.kmbisset89.worldweaver.domain.PeopleSnapshot
import io.github.kmbisset89.worldweaver.domain.PersonAvatarFileStore
import io.github.kmbisset89.worldweaver.domain.PersonKind
import io.github.kmbisset89.worldweaver.domain.PersonRef
import io.github.kmbisset89.worldweaver.domain.PersonSheet
import io.github.kmbisset89.worldweaver.domain.UpdateCampaignPersonDeathSavesUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateCampaignPersonUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateWorldPersonUseCase
import io.github.kmbisset89.worldweaver.domain.WorldPerson
import io.github.kmbisset89.worldweaver.domain.WorldPersonDraft
import io.github.kmbisset89.worldweaver.ui.characters.PersonMembership

internal class CharacterSheetViewModel(
    private val appScope: AppCoroutineScope,
    private val observePeople: ObservePeopleForActiveContextUseCase,
    private val updateWorldPerson: UpdateWorldPersonUseCase,
    private val updateCampaignPerson: UpdateCampaignPersonUseCase,
    private val updateDeathSaves: UpdateCampaignPersonDeathSavesUseCase,
    private val avatarFileStore: PersonAvatarFileStore,
    private val observeActiveContextDetails: ObserveActiveContextDetailsUseCase,
) {
    private val _state = MutableStateFlow<CharacterSheetViewState>(CharacterSheetViewState.Hidden)
    val state: StateFlow<CharacterSheetViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<CharacterSheetViewEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<CharacterSheetViewEffect> = _effects.asSharedFlow()

    private var observeJob: Job? = null
    private var openedKey: CharacterSheetViewState.PersonKey? = null
    private var showingUnavailable = false
    private var latestPeople = PeopleSnapshot(emptyList(), emptyList())
    private var latestLevelingMode = LevelingMode.Milestone

    init {
        observe()
    }

    fun onInteraction(interaction: CharacterSheetInteraction) {
        when (interaction) {
            is CharacterSheetInteraction.SheetOpened -> openSheet(interaction.key)
            CharacterSheetInteraction.UnavailableOpened -> showUnavailable(
                "This combatant has no linked person.",
            )
            CharacterSheetInteraction.SheetDismissed -> dismiss()
            CharacterSheetInteraction.RetrySelected -> observe()
            is CharacterSheetInteraction.DeathSaveSuccessesSelected -> writeDeathSaves { current ->
                current.withSuccesses(interaction.count)
            }
            is CharacterSheetInteraction.DeathSaveFailuresSelected -> writeDeathSaves { current ->
                current.withFailures(interaction.count)
            }
            is CharacterSheetInteraction.DyingSelected -> writePathfinderCondition(
                dying = interaction.count,
                wounded = null,
            )
            is CharacterSheetInteraction.WoundedSelected -> writePathfinderCondition(
                dying = null,
                wounded = interaction.count,
            )
            CharacterSheetInteraction.EditSelected -> openEditor()
        }
    }

    private fun openSheet(key: CharacterSheetViewState.PersonKey) {
        openedKey = key
        showingUnavailable = false
        if (_state.value is CharacterSheetViewState.Hidden) {
            _state.value = CharacterSheetViewState.Loading
        }
        publish()
    }

    private fun showUnavailable(message: String) {
        openedKey = null
        showingUnavailable = true
        _state.value = CharacterSheetViewState.Unavailable(message)
    }

    private fun dismiss() {
        openedKey = null
        showingUnavailable = false
        _state.value = CharacterSheetViewState.Hidden
    }

    private fun openEditor() {
        val key = openedKey ?: return
        _effects.tryEmit(CharacterSheetViewEffect.OpenEditor(key))
    }

    private fun observe() {
        observeJob?.cancel()
        observeJob = appScope.scope.launch {
            combine(
                observePeople(),
                observeActiveContextDetails(),
            ) { snapshot, details ->
                snapshot to (details.campaign?.levelingMode ?: LevelingMode.Milestone)
            }
                .catch { error ->
                    if (openedKey != null || showingUnavailable) {
                        _state.value = CharacterSheetViewState.Error(
                            message = error.message ?: "Could not load the character sheet",
                            canRetry = true,
                        )
                    }
                }
                .collect { (snapshot, levelingMode) ->
                    latestPeople = snapshot
                    latestLevelingMode = levelingMode
                    publish()
                }
        }
    }

    private fun publish() {
        if (showingUnavailable && openedKey == null) {
            return
        }
        val key = openedKey
        if (key == null) {
            _state.value = CharacterSheetViewState.Hidden
            return
        }
        val content = contentFrom(key) ?: run {
            _state.value = CharacterSheetViewState.Unavailable(
                "This person is no longer in the active world or campaign.",
            )
            return
        }
        _state.value = content
    }

    private fun contentFrom(
        key: CharacterSheetViewState.PersonKey,
    ): CharacterSheetViewState.Content? {
        return when (key.membership) {
            PersonMembership.WorldLibrary -> {
                val person = latestPeople.worldPeople.firstOrNull { it.id == key.id } ?: return null
                contentFromWorld(key, person)
            }
            PersonMembership.ThisCampaign -> {
                val person = latestPeople.campaignPeople.firstOrNull { it.id == key.id } ?: return null
                contentFromCampaign(key, person)
            }
        }
    }

    private fun contentFromWorld(
        key: CharacterSheetViewState.PersonKey,
        person: WorldPerson,
    ): CharacterSheetViewState.Content {
        return content(
            key = key,
            name = person.name,
            kind = person.kind,
            sheet = person.sheet,
            hitPoints = person.sheet.hitPoints,
            usesOverlayHitPoints = false,
            displayedDeathSaves = (person.sheet as? FifthEditionSheet)?.deathSaves,
            avatarPath = avatarFileStore.pathIfPresent(PersonRef.World(person.id)),
            deathSavesWritable = person.sheet is FifthEditionSheet,
            pathfinderWritable = person.sheet is Pathfinder2ESheet,
        )
    }

    private fun contentFromCampaign(
        key: CharacterSheetViewState.PersonKey,
        person: CampaignPerson,
    ): CharacterSheetViewState.Content {
        val resolved = resolvedSheet(person)
        val overlayHp = person.overlayHitPoints
        val displayedDeathSaves = when {
            person.sheet is FifthEditionSheet -> person.sheet.deathSaves
            resolved is FifthEditionSheet -> resolved.deathSaves
            else -> null
        }
        val displayedSheet = when {
            resolved is FifthEditionSheet && displayedDeathSaves != null -> {
                resolved.copy(deathSaves = displayedDeathSaves)
            }
            else -> resolved
        }
        return content(
            key = key,
            name = person.name,
            kind = person.kind,
            sheet = displayedSheet,
            hitPoints = overlayHp ?: displayedSheet.hitPoints,
            usesOverlayHitPoints = overlayHp != null,
            displayedDeathSaves = displayedDeathSaves,
            avatarPath = avatarPathForCampaign(person),
            deathSavesWritable = displayedSheet is FifthEditionSheet,
            pathfinderWritable = displayedSheet is Pathfinder2ESheet,
        )
    }

    private fun content(
        key: CharacterSheetViewState.PersonKey,
        name: String,
        kind: PersonKind,
        sheet: PersonSheet,
        hitPoints: Int,
        usesOverlayHitPoints: Boolean,
        displayedDeathSaves: DeathSaves?,
        avatarPath: String?,
        deathSavesWritable: Boolean,
        pathfinderWritable: Boolean,
    ): CharacterSheetViewState.Content {
        val scores = sheet.abilityScores
        return CharacterSheetViewState.Content(
            key = key,
            name = name,
            kind = kind,
            identityLine = identityLine(kind, sheet),
            experiencePoints = if (latestLevelingMode == LevelingMode.Experience) {
                sheet.currentXp
            } else {
                null
            },
            systemBadge = sheet.gameSystem().displayName,
            avatarPath = avatarPath,
            abilityScores = abilityTiles(scores),
            proficiencyBonus = proficiencyBonus(sheet),
            initiativeBonus = scores.modifierFor(scores.dexterity),
            vitals = CharacterSheetViewState.Vitals(
                hitPoints = hitPoints,
                maxHitPoints = sheet.maxHitPoints,
                temporaryHitPoints = sheet.temporaryHitPoints,
                armorClass = sheet.armorClass,
                speed = sheet.movementSpeed(),
                usesOverlayHitPoints = usesOverlayHitPoints,
                fifthEdition = (sheet as? FifthEditionSheet)?.let { fifth ->
                    CharacterSheetViewState.FifthEditionVitals(
                        deathSaves = displayedDeathSaves ?: fifth.deathSaves,
                        writable = deathSavesWritable,
                    )
                },
                pathfinder = (sheet as? Pathfinder2ESheet)?.let { pf ->
                    CharacterSheetViewState.PathfinderVitals(
                        perception = pf.perception,
                        dying = pf.dying,
                        wounded = pf.wounded,
                        writable = pathfinderWritable,
                    )
                },
            ),
            body = bodyFrom(sheet, scores),
        )
    }

    private fun bodyFrom(
        sheet: PersonSheet,
        scores: AbilityScores,
    ): CharacterSheetViewState.SheetBody {
        return when (sheet) {
            is FifthEditionSheet -> CharacterSheetViewState.SheetBody.FifthEdition(
                skills = fifthEditionSkills(sheet, scores),
                skillsCaption = if (sheet.skills.any { it.proficient }) {
                    "Proficiency bonus applied when marked"
                } else {
                    "Mark skill proficiency on the character editor"
                },
                concentratingSpell = sheet.concentratingSpell,
                spellSlots = sheet.spellSlots
                    .filter { it.maximum > 0 }
                    .sortedBy { it.level }
                    .map { slot ->
                        CharacterSheetViewState.SpellSlotRow(
                            level = slot.level,
                            remaining = slot.remaining(),
                            maximum = slot.maximum,
                        )
                    },
                features = sheet.features.map { feature ->
                    CharacterSheetViewState.NamedText(
                        name = feature.name,
                        description = feature.description,
                    )
                },
                spells = groupFifthEditionSpells(sheet),
                items = sheet.items.map { item ->
                    CharacterSheetViewState.ItemRow(
                        name = item.name,
                        quantity = item.quantity,
                        notes = item.notes,
                    )
                },
                notes = sheet.notes,
            )
            is Pathfinder2ESheet -> CharacterSheetViewState.SheetBody.Pathfinder(
                skills = sheet.skills.map { skill ->
                    CharacterSheetViewState.PathfinderSkillRow(
                        name = skill.name,
                        rank = skill.rank.name,
                    )
                },
                feats = sheet.feats.map { feat ->
                    val label = if (feat.type.isBlank()) feat.name else "${feat.name} [${feat.type}]"
                    CharacterSheetViewState.NamedText(
                        name = label,
                        description = feat.description,
                    )
                },
                spells = groupPathfinderSpells(sheet),
                notes = sheet.notes,
            )
        }
    }

    private fun writeDeathSaves(transform: (DeathSaves) -> DeathSaves) {
        val key = openedKey ?: return
        val current = currentDeathSaves() ?: return
        val next = transform(current)
        appScope.scope.launch {
            when (key.membership) {
                PersonMembership.WorldLibrary -> writeWorldDeathSaves(key.id, next)
                PersonMembership.ThisCampaign -> {
                    val person = latestPeople.campaignPeople.firstOrNull { it.id == key.id }
                    updateDeathSaves(key.id, next)
                    val worldId = person?.worldPersonId
                    if (worldId != null) {
                        writeWorldDeathSaves(worldId, next)
                    }
                }
            }
        }
    }

    private suspend fun writeWorldDeathSaves(personId: String, deathSaves: DeathSaves) {
        val person = latestPeople.worldPeople.firstOrNull { it.id == personId } ?: return
        val sheet = person.sheet as? FifthEditionSheet ?: return
        updateWorldPerson(
            personId,
            WorldPersonDraft(
                kind = person.kind,
                name = person.name,
                description = person.description,
                sheet = sheet.copy(deathSaves = deathSaves),
            ),
        )
    }

    private fun writePathfinderCondition(
        dying: Int?,
        wounded: Int?,
    ) {
        val key = openedKey ?: return
        appScope.scope.launch {
            when (key.membership) {
                PersonMembership.WorldLibrary -> {
                    val person = latestPeople.worldPeople.firstOrNull { it.id == key.id } ?: return@launch
                    val sheet = person.sheet as? Pathfinder2ESheet ?: return@launch
                    updateWorldPerson(
                        key.id,
                        WorldPersonDraft(
                            kind = person.kind,
                            name = person.name,
                            description = person.description,
                            sheet = sheet.copy(
                                dying = dying ?: sheet.dying,
                                wounded = wounded ?: sheet.wounded,
                            ),
                        ),
                    )
                }
                PersonMembership.ThisCampaign -> {
                    val person = latestPeople.campaignPeople.firstOrNull { it.id == key.id }
                        ?: return@launch
                    val worldId = person.worldPersonId
                    if (worldId != null) {
                        val worldPerson = latestPeople.worldPeople.firstOrNull { it.id == worldId }
                            ?: return@launch
                        val sheet = worldPerson.sheet as? Pathfinder2ESheet ?: return@launch
                        updateWorldPerson(
                            worldId,
                            WorldPersonDraft(
                                kind = worldPerson.kind,
                                name = worldPerson.name,
                                description = worldPerson.description,
                                sheet = sheet.copy(
                                    dying = dying ?: sheet.dying,
                                    wounded = wounded ?: sheet.wounded,
                                ),
                            ),
                        )
                    } else {
                        val sheet = person.sheet as? Pathfinder2ESheet ?: return@launch
                        updateCampaignPerson(
                            key.id,
                            CampaignPersonDraft(
                                kind = person.kind,
                                name = person.name,
                                description = person.description,
                                sheet = sheet.copy(
                                    dying = dying ?: sheet.dying,
                                    wounded = wounded ?: sheet.wounded,
                                ),
                                overlayHitPoints = person.overlayHitPoints,
                                overlayNotes = person.overlayNotes,
                            ),
                        )
                    }
                }
            }
        }
    }

    private fun currentDeathSaves(): DeathSaves? {
        val content = _state.value as? CharacterSheetViewState.Content ?: return null
        return content.vitals.fifthEdition?.deathSaves
    }

    private fun resolvedSheet(person: CampaignPerson): PersonSheet {
        val worldId = person.worldPersonId ?: return person.sheet
        return latestPeople.worldPeople.firstOrNull { it.id == worldId }?.sheet ?: person.sheet
    }

    private fun avatarPathForCampaign(person: CampaignPerson): String? {
        return avatarFileStore.pathIfPresent(PersonRef.Campaign(person.id))
            ?: person.worldPersonId?.let { worldId ->
                avatarFileStore.pathIfPresent(PersonRef.World(worldId))
            }
    }

    private fun identityLine(kind: PersonKind, sheet: PersonSheet): String {
        val identity = when (sheet) {
            is FifthEditionSheet -> {
                val race = sheet.race.ifBlank { "No race" }
                val classes = if (sheet.classLevels.isEmpty()) {
                    "No classes"
                } else {
                    sheet.classLevels.joinToString(", ") { level ->
                        val subclass = if (level.subclass.isBlank()) "" else " (${level.subclass})"
                        "${level.className}$subclass ${level.level}"
                    }
                }
                "$race · $classes · Level ${sheet.totalLevel()}"
            }
            is Pathfinder2ESheet -> {
                val ancestry = sheet.ancestry.ifBlank { "No ancestry" }
                val extras = listOfNotNull(
                    sheet.heritage.takeIf { it.isNotBlank() },
                    sheet.background.takeIf { it.isNotBlank() },
                )
                val classLabel = if (sheet.className.isBlank()) {
                    "No class"
                } else {
                    val path = if (sheet.subclass.isBlank()) "" else " (${sheet.subclass})"
                    "${sheet.className}$path ${sheet.level}"
                }
                (listOf(ancestry) + extras + classLabel).joinToString(" · ")
            }
        }
        return "${kind.displayName} · $identity"
    }

    private fun abilityTiles(scores: AbilityScores): List<CharacterSheetViewState.AbilityScoreTile> {
        return listOf(
            tile("STR", scores.strength, scores),
            tile("DEX", scores.dexterity, scores),
            tile("CON", scores.constitution, scores),
            tile("INT", scores.intelligence, scores),
            tile("WIS", scores.wisdom, scores),
            tile("CHA", scores.charisma, scores),
        )
    }

    private fun tile(
        label: String,
        score: Int,
        scores: AbilityScores,
    ): CharacterSheetViewState.AbilityScoreTile {
        return CharacterSheetViewState.AbilityScoreTile(
            label = label,
            score = score,
            modifier = scores.modifierFor(score),
        )
    }

    private fun proficiencyBonus(sheet: PersonSheet): Int? {
        return when (sheet) {
            is FifthEditionSheet -> ((sheet.totalLevel() - 1) / 4) + 2
            is Pathfinder2ESheet -> null
        }
    }

    private fun groupFifthEditionSpells(
        sheet: FifthEditionSheet,
    ): List<CharacterSheetViewState.SpellGroup> {
        return sheet.spells
            .groupBy { it.level }
            .toSortedMap()
            .map { (level, spells) ->
                val heading = if (level == 0) "Cantrips" else "Level $level"
                CharacterSheetViewState.SpellGroup(
                    heading = heading,
                    spells = spells.map { spell ->
                        CharacterSheetViewState.SpellRow(
                            name = spell.name,
                            prepared = spell.prepared,
                        )
                    },
                )
            }
    }

    private fun groupPathfinderSpells(
        sheet: Pathfinder2ESheet,
    ): List<CharacterSheetViewState.SpellGroup> {
        return sheet.spells
            .groupBy { it.rank }
            .toSortedMap()
            .map { (rank, spells) ->
                val heading = if (rank == 0) "Cantrips" else "Rank $rank"
                CharacterSheetViewState.SpellGroup(
                    heading = heading,
                    spells = spells.map { spell ->
                        CharacterSheetViewState.SpellRow(
                            name = spell.name,
                            prepared = spell.prepared,
                        )
                    },
                )
            }
    }

    private fun fifthEditionSkills(
        sheet: FifthEditionSheet,
        scores: AbilityScores,
    ): List<CharacterSheetViewState.SkillRow> {
        val proficiency = proficiencyBonus(sheet) ?: 0
        return FifthEditionSkillCatalog.skills.map { catalogSkill ->
            val stored = sheet.skills.firstOrNull { skill ->
                skill.name.equals(catalogSkill.name, ignoreCase = true)
            }
            val proficient = stored?.proficient == true
            val score = when (catalogSkill.ability) {
                "STR" -> scores.strength
                "DEX" -> scores.dexterity
                "CON" -> scores.constitution
                "INT" -> scores.intelligence
                "WIS" -> scores.wisdom
                else -> scores.charisma
            }
            val modifier = scores.modifierFor(score) + if (proficient) proficiency else 0
            CharacterSheetViewState.SkillRow(
                name = catalogSkill.name,
                ability = catalogSkill.ability,
                modifier = modifier,
                proficient = proficient,
            )
        }
    }
}
