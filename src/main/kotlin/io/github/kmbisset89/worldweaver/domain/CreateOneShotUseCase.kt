package io.github.kmbisset89.worldweaver.domain

internal class CreateOneShotUseCase(
    private val createWorld: CreateWorldUseCase,
    private val createCampaign: CreateCampaignUseCase,
    private val createLocation: CreateLocationUseCase,
    private val createWorldPerson: CreateWorldPersonUseCase,
    private val createFaction: CreateFactionUseCase,
    private val createLore: CreateLoreUseCase,
    private val createQuest: CreateQuestUseCase,
    private val createSession: CreateSessionUseCase,
    private val createEncounter: CreateEncounterUseCase,
    private val sheetFactory: PersonSheetFactory = PersonSheetFactory(),
) {
    sealed interface Result {
        data class Created(
            val world: World,
            val campaign: Campaign,
            val session: Session,
        ) : Result

        data class Failed(
            val step: Step,
            val message: String,
        ) : Result
    }

    enum class Step {
        World,
        Campaign,
        Places,
        People,
        Faction,
        Lore,
        Quest,
        Session,
        Encounter,
    }

    suspend operator fun invoke(draft: OneShotDraft): Result {
        if (draft.worldName.isBlank()) {
            return Result.Failed(Step.World, "Name is required")
        }
        if (draft.campaignName.isBlank()) {
            return Result.Failed(Step.Campaign, "Name is required")
        }
        if (draft.sites.isEmpty()) {
            return Result.Failed(Step.Places, "At least one adventure site is required")
        }
        if (draft.questTitle.isBlank()) {
            return Result.Failed(Step.Quest, "Title is required")
        }
        val world = when (val created = createWorld(
            draft.worldName,
            draft.worldDescription,
            draft.gameSystem,
        )) {
            is CreateWorldUseCase.Result.Created -> created.world
            CreateWorldUseCase.Result.InvalidName -> {
                return Result.Failed(Step.World, "Name is required")
            }
        }
        val campaign = when (val created = createCampaign(
            draft.campaignName,
            draft.campaignDescription,
            draft.campaignNotes,
            draft.gameSystem,
        )) {
            is CreateCampaignUseCase.Result.Created -> created.campaign
            CreateCampaignUseCase.Result.InvalidName -> {
                return Result.Failed(Step.Campaign, "Name is required")
            }
            CreateCampaignUseCase.Result.NoActiveWorld -> {
                return Result.Failed(Step.Campaign, "No active world")
            }
        }
        val places = insertPlaces(draft) ?: return Result.Failed(
            Step.Places,
            "Could not create the location tree",
        )
        val people = insertPeople(draft) ?: return Result.Failed(
            Step.People,
            "Could not create one of the people",
        )
        val factionFailure = insertFaction(draft)
        if (factionFailure != null) {
            return factionFailure
        }
        val lore = insertPremiseLore(draft, places)
        if (lore is LoreResult.Failed) {
            return lore.failure
        }
        val questFailure = insertQuest(draft, places, people, lore.entry)
        if (questFailure != null) {
            return questFailure
        }
        val session = when (val created = createSession(
            SessionDraft(
                name = draft.sessionName,
                notes = draft.sessionNotes,
                scenes = draft.scenes.map { scene ->
                    SessionScene(id = "", title = scene.title, notes = scene.notes)
                },
                marchOrder = emptyList(),
            ),
        )) {
            is CreateSessionUseCase.Result.Created -> created.session
            CreateSessionUseCase.Result.InvalidName -> {
                return Result.Failed(Step.Session, "Name is required")
            }
            CreateSessionUseCase.Result.InvalidDate -> {
                return Result.Failed(Step.Session, "The in-world date is invalid")
            }
            CreateSessionUseCase.Result.NoActiveCampaign -> {
                return Result.Failed(Step.Session, "No active campaign")
            }
        }
        val encounterFailure = insertEncounter(draft, places, people)
        if (encounterFailure != null) {
            return encounterFailure
        }
        return Result.Created(
            world = world,
            campaign = campaign,
            session = session,
        )
    }

    private suspend fun insertPlaces(draft: OneShotDraft): CreatedPlaces? {
        val continent = unwrapLocation(
            createLocation(
                locationDraft(
                    type = LocationType.Continent,
                    parentLocationId = null,
                    name = draft.realmName,
                    description = draft.realmDescription,
                ),
            ),
        ) ?: return null
        val region = unwrapLocation(
            createLocation(
                locationDraft(
                    type = LocationType.Area,
                    parentLocationId = continent.id,
                    name = draft.regionName,
                    description = "",
                    climate = draft.regionClimate,
                    terrain = draft.regionTerrain,
                ),
            ),
        ) ?: return null
        val settlement = unwrapLocation(
            createLocation(
                locationDraft(
                    type = LocationType.City,
                    parentLocationId = region.id,
                    name = draft.settlementName,
                    description = draft.settlementDescription,
                    climate = draft.regionClimate,
                    terrain = draft.regionTerrain,
                ),
            ),
        ) ?: return null
        val sites = draft.sites.map { site ->
            unwrapLocation(
                createLocation(
                    locationDraft(
                        type = LocationType.Place,
                        parentLocationId = settlement.id,
                        name = site.name,
                        description = site.description,
                    ),
                ),
            ) ?: return null
        }
        return CreatedPlaces(
            continent = continent,
            region = region,
            settlement = settlement,
            sites = draft.sites.zip(sites),
        )
    }

    private suspend fun insertPeople(draft: OneShotDraft): List<CreatedPerson>? {
        return draft.people.map { person ->
            when (val created = createWorldPerson(
                WorldPersonDraft(
                    kind = person.kind,
                    name = person.name,
                    description = person.description,
                    sheet = sheetFactory.empty(draft.gameSystem),
                ),
            )) {
                is CreateWorldPersonUseCase.Result.Created -> CreatedPerson(
                    role = person.role,
                    person = created.person,
                )
                CreateWorldPersonUseCase.Result.InvalidName,
                CreateWorldPersonUseCase.Result.InvalidKind,
                CreateWorldPersonUseCase.Result.NoActiveWorld,
                -> return null
            }
        }
    }

    private suspend fun insertFaction(draft: OneShotDraft): Result.Failed? {
        val faction = draft.faction ?: return null
        return when (createFaction(
            FactionDraft(
                name = faction.name,
                description = faction.description,
                goals = faction.goals,
                notes = "",
            ),
        )) {
            is CreateFactionUseCase.Result.Created -> null
            CreateFactionUseCase.Result.InvalidName -> {
                Result.Failed(Step.Faction, "Name is required")
            }
            CreateFactionUseCase.Result.DuplicateName -> {
                Result.Failed(Step.Faction, "A faction with that name already exists")
            }
            CreateFactionUseCase.Result.NoActiveWorld -> {
                Result.Failed(Step.Faction, "No active world")
            }
        }
    }

    private suspend fun insertPremiseLore(
        draft: OneShotDraft,
        places: CreatedPlaces,
    ): LoreResult {
        val content = draft.loreContent.trim()
        if (content.isEmpty()) {
            return LoreResult.Skipped
        }
        val secrets = if (draft.loreSecret.isNullOrBlank()) {
            emptyList()
        } else {
            listOf(
                LoreSecret(
                    id = "",
                    title = draft.loreSecretTitle?.trim().orEmpty().ifBlank { "The twist" },
                    secret = draft.loreSecret.trim(),
                    hints = emptyList(),
                ),
            )
        }
        val openingId = places.siteId(OneShotDraft.Site.Role.Opening) ?: places.sites.first().second.id
        return when (val created = createLore(
            LoreDraft(
                title = draft.loreTitle.ifBlank { "The premise" },
                content = content,
                category = LoreCategory.Other,
                tags = listOf("one-shot", "premise"),
                relatedEntryIds = emptyList(),
                secrets = secrets,
                locationId = openingId,
                characterId = null,
            ),
        )) {
            is CreateLoreUseCase.Result.Created -> LoreResult.Created(created.lore)
            CreateLoreUseCase.Result.InvalidTitle -> {
                LoreResult.Failed(Result.Failed(Step.Lore, "Title is required"))
            }
            CreateLoreUseCase.Result.InvalidContent -> {
                LoreResult.Failed(Result.Failed(Step.Lore, "Content is required"))
            }
            CreateLoreUseCase.Result.NoActiveWorld -> {
                LoreResult.Failed(Result.Failed(Step.Lore, "No active world"))
            }
            CreateLoreUseCase.Result.InvalidLocation -> {
                LoreResult.Failed(Result.Failed(Step.Lore, "The lore location is invalid"))
            }
        }
    }

    private suspend fun insertQuest(
        draft: OneShotDraft,
        places: CreatedPlaces,
        people: List<CreatedPerson>,
        lore: Lore?,
    ): Result.Failed? {
        val locationId = places.siteId(OneShotDraft.Site.Role.Climax)
            ?: places.sites.lastOrNull()?.second?.id
        val links = buildList {
            lore?.let { entry ->
                add(QuestLink(id = "", kind = QuestLinkKind.LORE, targetId = entry.id))
            }
            people.forEach { created ->
                add(
                    QuestLink(
                        id = "",
                        kind = QuestLinkKind.WORLD_PERSON,
                        targetId = created.person.id,
                    ),
                )
            }
        }
        return when (createQuest(
            QuestDraft(
                title = draft.questTitle,
                summary = draft.questSummary,
                status = QuestStatus.Active,
                locationId = locationId,
                objectives = draft.questObjectives.map { title ->
                    QuestObjective(
                        id = "",
                        title = title,
                        status = QuestObjectiveStatus.Open,
                    )
                },
                links = links,
            ),
        )) {
            is CreateQuestUseCase.Result.Created -> null
            CreateQuestUseCase.Result.InvalidTitle -> {
                Result.Failed(Step.Quest, "Title is required")
            }
            CreateQuestUseCase.Result.InvalidLocation -> {
                Result.Failed(Step.Quest, "The quest location is invalid")
            }
            CreateQuestUseCase.Result.NoActiveCampaign -> {
                Result.Failed(Step.Quest, "No active campaign")
            }
        }
    }

    private suspend fun insertEncounter(
        draft: OneShotDraft,
        places: CreatedPlaces,
        people: List<CreatedPerson>,
    ): Result.Failed? {
        val name = draft.encounterName?.trim().orEmpty()
        if (name.isEmpty()) {
            return null
        }
        val villain = people.firstOrNull { it.role == OneShotDraft.Person.Role.Villain }?.person
        val participants = if (villain == null) {
            emptyList()
        } else {
            listOf(
                EncounterParticipant(
                    id = "",
                    name = villain.name,
                    source = EncounterParticipantSource.WorldPerson,
                    sourceId = villain.id,
                    initiativeRoll = null,
                    initiativeBonus = 0,
                    armorClass = villain.sheet.armorClass,
                    hitPoints = villain.sheet.hitPoints,
                    maxHitPoints = villain.sheet.maxHitPoints,
                    temporaryHitPoints = villain.sheet.temporaryHitPoints,
                    conditions = emptyList(),
                    groupCount = 1,
                    combatState = CombatState.Conscious,
                ),
            )
        }
        val locationId = places.siteId(OneShotDraft.Site.Role.Climax)
            ?: places.sites.lastOrNull()?.second?.id
        return when (createEncounter(
            EncounterDraft(
                name = name,
                locationId = locationId,
                battleMapId = null,
                difficulty = draft.encounterDifficulty ?: EncounterDifficulty.Medium,
                notes = "",
                outcomeNote = "",
                participants = participants,
            ),
        )) {
            is CreateEncounterUseCase.Result.Created -> null
            CreateEncounterUseCase.Result.InvalidName -> {
                Result.Failed(Step.Encounter, "Name is required")
            }
            CreateEncounterUseCase.Result.InvalidLocation -> {
                Result.Failed(Step.Encounter, "The encounter location is invalid")
            }
            CreateEncounterUseCase.Result.InvalidBattleMap -> {
                Result.Failed(Step.Encounter, "The battle map is invalid")
            }
            CreateEncounterUseCase.Result.NoActiveCampaign -> {
                Result.Failed(Step.Encounter, "No active campaign")
            }
        }
    }

    private fun unwrapLocation(result: CreateLocationUseCase.Result): Location? {
        return (result as? CreateLocationUseCase.Result.Created)?.location
    }

    private fun locationDraft(
        type: LocationType,
        parentLocationId: String?,
        name: String,
        description: String,
        climate: String = "",
        terrain: String = "",
    ): LocationDraft {
        return LocationDraft(
            type = type,
            parentLocationId = parentLocationId,
            name = name,
            description = description,
            climate = climate,
            terrain = terrain,
            government = "",
            landmarks = emptyList(),
            history = "",
            notes = "",
        )
    }

    private data class CreatedPlaces(
        val continent: Location,
        val region: Location,
        val settlement: Location,
        val sites: List<Pair<OneShotDraft.Site, Location>>,
    ) {
        fun siteId(role: OneShotDraft.Site.Role): String? {
            return sites.firstOrNull { it.first.role == role }?.second?.id
        }
    }

    private data class CreatedPerson(
        val role: OneShotDraft.Person.Role,
        val person: WorldPerson,
    )

    private sealed interface LoreResult {
        val entry: Lore?

        data class Created(val lore: Lore) : LoreResult {
            override val entry: Lore = lore
        }

        data object Skipped : LoreResult {
            override val entry: Lore? = null
        }

        data class Failed(val failure: Result.Failed) : LoreResult {
            override val entry: Lore? = null
        }
    }
}
