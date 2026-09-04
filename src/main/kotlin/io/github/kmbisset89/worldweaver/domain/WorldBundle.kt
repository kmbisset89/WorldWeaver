package io.github.kmbisset89.worldweaver.domain

import kotlinx.serialization.Serializable
import java.time.Instant

internal data class WorldBundle(
    val formatVersion: Int,
    val exportedAt: Instant,
    val world: World,
    val calendar: WorldCalendar? = null,
    val campaigns: List<Campaign>,
    val locations: List<Location>,
    val loreEntries: List<Lore>,
    val factions: List<Faction> = emptyList(),
    val memberships: List<FactionMembership> = emptyList(),
    val worldPeople: List<WorldPerson>,
    val campaignPeople: List<CampaignPerson>,
    val locationOverlays: List<LocationOverlay>,
    val quests: List<Quest>,
    val sessions: List<Session>,
    val plotThreads: List<PlotThread>,
    val referenceDocs: List<ReferenceDoc>,
    val battleMaps: List<BattleMap>,
    val battleMapSituations: List<BattleMapSituation>,
    val worldMaps: List<WorldMap> = emptyList(),
    val encounters: List<Encounter>,
    val relationships: List<PersonRelationship>,
    val companions: List<PersonCompanion>,
    val avatarFiles: List<AvatarFile>,
    val mapFiles: List<MapFile>,
    val worldMapFiles: List<WorldMapFile> = emptyList(),
    val voiceFiles: List<VoiceFile> = emptyList(),
) {
    data class AvatarFile(
        val ref: PersonRef,
        val png: ByteArray,
    )

    data class MapFile(
        val battleMapId: String,
        val relativePath: String,
        val bytes: ByteArray,
    )

    data class WorldMapFile(
        val worldMapId: String,
        val relativePath: String,
        val bytes: ByteArray,
    )

    data class VoiceFile(
        val ref: VoiceClipRef,
        val wav: ByteArray,
    )

    fun toManifest(): Manifest {
        return Manifest(
            formatVersion = formatVersion,
            exportedAtEpochMillis = exportedAt.toEpochMilli(),
            originalWorldName = world.name,
        )
    }

    fun toPayload(): Payload {
        return Payload(
            world = WorldRecord.from(world),
            calendar = calendar?.let(WorldCalendarRecord::from),
            campaigns = campaigns.map(CampaignRecord::from),
            locations = locations.map(LocationRecord::from),
            loreEntries = loreEntries.map(LoreRecord::from),
            factions = factions.map(FactionRecord::from),
            memberships = memberships.map(FactionMembershipRecord::from),
            worldPeople = worldPeople.map(WorldPersonRecord::from),
            campaignPeople = campaignPeople.map(CampaignPersonRecord::from),
            locationOverlays = locationOverlays.map(LocationOverlayRecord::from),
            quests = quests.map(QuestRecord::from),
            sessions = sessions.map(SessionRecord::from),
            plotThreads = plotThreads.map(PlotThreadRecord::from),
            referenceDocs = referenceDocs.map(ReferenceDocRecord::from),
            battleMaps = battleMaps.map(BattleMapRecord::from),
            battleMapSituations = battleMapSituations.map(BattleMapSituationRecord::from),
            worldMaps = worldMaps.map(WorldMapRecord::from),
            encounters = encounters.map(EncounterRecord::from),
            relationships = relationships.map(PersonRelationshipRecord::from),
            companions = companions.map(PersonCompanionRecord::from),
        )
    }

    companion object {
        const val FORMAT_VERSION = 1

        fun fromRecords(
            manifest: Manifest,
            payload: Payload,
            avatarFiles: List<AvatarFile>,
            mapFiles: List<MapFile>,
            worldMapFiles: List<WorldMapFile> = emptyList(),
            voiceFiles: List<VoiceFile> = emptyList(),
        ): WorldBundle {
            val (factions, relationships) = resolveLegacyFactions(
                payload = payload,
                exportedAt = Instant.ofEpochMilli(manifest.exportedAtEpochMillis),
            )
            return WorldBundle(
                formatVersion = manifest.formatVersion,
                exportedAt = Instant.ofEpochMilli(manifest.exportedAtEpochMillis),
                world = payload.world.toDomain(),
                calendar = payload.calendar?.toDomain(),
                campaigns = payload.campaigns.map { it.toDomain() },
                locations = payload.locations.map { it.toDomain() },
                loreEntries = payload.loreEntries.map { it.toDomain() },
                factions = factions,
                memberships = payload.memberships.map { it.toDomain() },
                worldPeople = payload.worldPeople.map { it.toDomain() },
                campaignPeople = payload.campaignPeople.map { it.toDomain() },
                locationOverlays = payload.locationOverlays.map { it.toDomain() },
                quests = payload.quests.map { it.toDomain() },
                sessions = payload.sessions.map { it.toDomain() },
                plotThreads = payload.plotThreads.map { it.toDomain() },
                referenceDocs = payload.referenceDocs.map { it.toDomain() },
                battleMaps = payload.battleMaps.map { it.toDomain() },
                battleMapSituations = payload.battleMapSituations.map { it.toDomain() },
                worldMaps = payload.worldMaps.map { it.toDomain() },
                encounters = payload.encounters.map { it.toDomain() },
                relationships = relationships,
                companions = payload.companions.map { it.toDomain() },
                avatarFiles = avatarFiles,
                mapFiles = mapFiles,
                worldMapFiles = worldMapFiles,
                voiceFiles = voiceFiles,
            )
        }

        private fun resolveLegacyFactions(
            payload: Payload,
            exportedAt: Instant,
        ): Pair<List<Faction>, List<PersonRelationship>> {
            val factions = payload.factions.map { it.toDomain() }.toMutableList()
            val worldByPerson = mutableMapOf<Pair<String, String>, String>()
            payload.worldPeople.forEach { person ->
                worldByPerson["World" to person.id] = person.worldId
            }
            val campaignWorlds = payload.campaigns.associate { it.id to it.worldId }
            payload.campaignPeople.forEach { person ->
                campaignWorlds[person.campaignId]?.let { worldId ->
                    worldByPerson["Campaign" to person.id] = worldId
                }
            }
            val relationships = payload.relationships.map { record ->
                val existingId = record.factionId?.takeIf { it.isNotBlank() }
                if (existingId != null || record.factionLean.isBlank()) {
                    return@map record.toDomain()
                }
                val lean = record.factionLean.trim()
                val worldId = worldByPerson[record.from.kind to record.from.id]
                    ?: worldByPerson[record.to.kind to record.to.id]
                    ?: payload.world.id
                val faction = factions.firstOrNull { existing ->
                    existing.worldId == worldId && existing.name.equals(lean, ignoreCase = true)
                } ?: Faction(
                    id = "legacy-fac-$worldId-${lean.lowercase()}",
                    worldId = worldId,
                    name = lean,
                    description = "",
                    goals = "",
                    notes = "",
                    createdAt = exportedAt,
                    updatedAt = exportedAt,
                ).also { factions += it }
                record.toDomain().copy(factionId = faction.id)
            }
            return factions to relationships
        }
    }

    @Serializable
    data class Manifest(
        val formatVersion: Int,
        val exportedAtEpochMillis: Long,
        val originalWorldName: String,
    )

    @Serializable
    data class Payload(
        val world: WorldRecord,
        val calendar: WorldCalendarRecord? = null,
        val campaigns: List<CampaignRecord>,
        val locations: List<LocationRecord>,
        val loreEntries: List<LoreRecord>,
        val factions: List<FactionRecord> = emptyList(),
        val memberships: List<FactionMembershipRecord> = emptyList(),
        val worldPeople: List<WorldPersonRecord>,
        val campaignPeople: List<CampaignPersonRecord>,
        val locationOverlays: List<LocationOverlayRecord>,
        val quests: List<QuestRecord>,
        val sessions: List<SessionRecord>,
        val plotThreads: List<PlotThreadRecord>,
        val referenceDocs: List<ReferenceDocRecord>,
        val battleMaps: List<BattleMapRecord>,
        val battleMapSituations: List<BattleMapSituationRecord>,
        val worldMaps: List<WorldMapRecord> = emptyList(),
        val encounters: List<EncounterRecord>,
        val relationships: List<PersonRelationshipRecord>,
        val companions: List<PersonCompanionRecord>,
    )

    @Serializable
    data class WorldRecord(
        val id: String,
        val name: String,
        val description: String,
        val defaultGameSystem: String,
        val createdAtEpochMillis: Long,
        val updatedAtEpochMillis: Long,
    ) {
        fun toDomain(): World {
            return World(
                id = id,
                name = name,
                description = description,
                defaultGameSystem = GameSystem.fromStorage(defaultGameSystem),
                createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
                updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
            )
        }

        companion object {
            fun from(world: World): WorldRecord {
                return WorldRecord(
                    id = world.id,
                    name = world.name,
                    description = world.description,
                    defaultGameSystem = world.defaultGameSystem.name,
                    createdAtEpochMillis = world.createdAt.toEpochMilli(),
                    updatedAtEpochMillis = world.updatedAt.toEpochMilli(),
                )
            }
        }
    }

    @Serializable
    data class WorldCalendarRecord(
        val id: String,
        val worldId: String,
        val eraSuffix: String,
        val months: List<WorldCalendarMonthRecord>,
        val weekdays: List<WorldCalendarWeekdayRecord>,
        val currentYear: Int? = null,
        val currentMonthId: String? = null,
        val currentDay: Int? = null,
        val createdAtEpochMillis: Long,
        val updatedAtEpochMillis: Long,
    ) {
        fun toDomain(): WorldCalendar {
            return WorldCalendar(
                id = id,
                worldId = worldId,
                eraSuffix = eraSuffix,
                months = months.map { it.toDomain() },
                weekdays = weekdays.map { it.toDomain() },
                currentDate = toCurrentDate(),
                createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
                updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
            )
        }

        private fun toCurrentDate(): WorldDate? {
            val year = currentYear ?: return null
            val monthId = currentMonthId ?: return null
            val day = currentDay ?: return null
            return WorldDate(year = year, monthId = monthId, day = day)
        }

        companion object {
            fun from(calendar: WorldCalendar): WorldCalendarRecord {
                return WorldCalendarRecord(
                    id = calendar.id,
                    worldId = calendar.worldId,
                    eraSuffix = calendar.eraSuffix,
                    months = calendar.months.map(WorldCalendarMonthRecord::from),
                    weekdays = calendar.weekdays.map(WorldCalendarWeekdayRecord::from),
                    currentYear = calendar.currentDate?.year,
                    currentMonthId = calendar.currentDate?.monthId,
                    currentDay = calendar.currentDate?.day,
                    createdAtEpochMillis = calendar.createdAt.toEpochMilli(),
                    updatedAtEpochMillis = calendar.updatedAt.toEpochMilli(),
                )
            }
        }
    }

    @Serializable
    data class WorldCalendarMonthRecord(
        val id: String,
        val name: String,
        val days: Int,
    ) {
        fun toDomain(): WorldCalendarMonth {
            return WorldCalendarMonth(id = id, name = name, days = days)
        }

        companion object {
            fun from(month: WorldCalendarMonth): WorldCalendarMonthRecord {
                return WorldCalendarMonthRecord(id = month.id, name = month.name, days = month.days)
            }
        }
    }

    @Serializable
    data class WorldCalendarWeekdayRecord(
        val id: String,
        val name: String,
    ) {
        fun toDomain(): WorldCalendarWeekday {
            return WorldCalendarWeekday(id = id, name = name)
        }

        companion object {
            fun from(weekday: WorldCalendarWeekday): WorldCalendarWeekdayRecord {
                return WorldCalendarWeekdayRecord(id = weekday.id, name = weekday.name)
            }
        }
    }

    @Serializable
    data class CampaignRecord(
        val id: String,
        val worldId: String,
        val name: String,
        val description: String,
        val notes: String,
        val gameSystem: String?,
        val status: String,
        val createdAtEpochMillis: Long,
        val updatedAtEpochMillis: Long,
    ) {
        fun toDomain(): Campaign {
            return Campaign(
                id = id,
                worldId = worldId,
                name = name,
                description = description,
                notes = notes,
                gameSystem = gameSystem?.let(GameSystem::fromStorage),
                status = CampaignStatus.fromStorage(status),
                createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
                updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
            )
        }

        companion object {
            fun from(campaign: Campaign): CampaignRecord {
                return CampaignRecord(
                    id = campaign.id,
                    worldId = campaign.worldId,
                    name = campaign.name,
                    description = campaign.description,
                    notes = campaign.notes,
                    gameSystem = campaign.gameSystem?.name,
                    status = campaign.status.name,
                    createdAtEpochMillis = campaign.createdAt.toEpochMilli(),
                    updatedAtEpochMillis = campaign.updatedAt.toEpochMilli(),
                )
            }
        }
    }

    @Serializable
    data class LocationRecord(
        val id: String,
        val worldId: String,
        val type: String,
        val parentLocationId: String?,
        val name: String,
        val description: String,
        val climate: String,
        val terrain: String,
        val government: String,
        val landmarks: List<String>,
        val history: String,
        val notes: String,
        val mapAnchorX: Double? = null,
        val mapAnchorY: Double? = null,
        val createdAtEpochMillis: Long,
        val updatedAtEpochMillis: Long,
    ) {
        fun toDomain(): Location {
            return Location(
                id = id,
                worldId = worldId,
                type = LocationType.fromStorage(type),
                parentLocationId = parentLocationId,
                name = name,
                description = description,
                climate = climate,
                terrain = terrain,
                government = government,
                landmarks = landmarks,
                history = history,
                notes = notes,
                mapAnchorX = mapAnchorX,
                mapAnchorY = mapAnchorY,
                createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
                updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
            )
        }

        companion object {
            fun from(location: Location): LocationRecord {
                return LocationRecord(
                    id = location.id,
                    worldId = location.worldId,
                    type = location.type.name,
                    parentLocationId = location.parentLocationId,
                    name = location.name,
                    description = location.description,
                    climate = location.climate,
                    terrain = location.terrain,
                    government = location.government,
                    landmarks = location.landmarks,
                    history = location.history,
                    notes = location.notes,
                    mapAnchorX = location.mapAnchorX,
                    mapAnchorY = location.mapAnchorY,
                    createdAtEpochMillis = location.createdAt.toEpochMilli(),
                    updatedAtEpochMillis = location.updatedAt.toEpochMilli(),
                )
            }
        }
    }

    @Serializable
    data class LoreRecord(
        val id: String,
        val worldId: String,
        val title: String,
        val content: String,
        val category: String,
        val tags: List<String>,
        val relatedEntryIds: List<String>,
        val secrets: List<LoreSecretRecord>,
        val locationId: String?,
        val characterId: String?,
        val createdAtEpochMillis: Long,
        val updatedAtEpochMillis: Long,
    ) {
        fun toDomain(): Lore {
            return Lore(
                id = id,
                worldId = worldId,
                title = title,
                content = content,
                category = LoreCategory.fromStorage(category),
                tags = tags,
                relatedEntryIds = relatedEntryIds,
                secrets = secrets.map { it.toDomain() },
                locationId = locationId,
                characterId = characterId,
                createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
                updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
            )
        }

        companion object {
            fun from(lore: Lore): LoreRecord {
                return LoreRecord(
                    id = lore.id,
                    worldId = lore.worldId,
                    title = lore.title,
                    content = lore.content,
                    category = lore.category.name,
                    tags = lore.tags,
                    relatedEntryIds = lore.relatedEntryIds,
                    secrets = lore.secrets.map(LoreSecretRecord::from),
                    locationId = lore.locationId,
                    characterId = lore.characterId,
                    createdAtEpochMillis = lore.createdAt.toEpochMilli(),
                    updatedAtEpochMillis = lore.updatedAt.toEpochMilli(),
                )
            }
        }
    }

    @Serializable
    data class LoreSecretRecord(
        val id: String,
        val title: String,
        val secret: String,
        val hints: List<LoreHintRecord>,
    ) {
        fun toDomain(): LoreSecret {
            return LoreSecret(
                id = id,
                title = title,
                secret = secret,
                hints = hints.map { it.toDomain() },
            )
        }

        companion object {
            fun from(secret: LoreSecret): LoreSecretRecord {
                return LoreSecretRecord(
                    id = secret.id,
                    title = secret.title,
                    secret = secret.secret,
                    hints = secret.hints.map(LoreHintRecord::from),
                )
            }
        }
    }

    @Serializable
    data class LoreHintRecord(
        val id: String,
        val text: String,
        val revealed: Boolean,
    ) {
        fun toDomain(): LoreHint {
            return LoreHint(id = id, text = text, revealed = revealed)
        }

        companion object {
            fun from(hint: LoreHint): LoreHintRecord {
                return LoreHintRecord(id = hint.id, text = hint.text, revealed = hint.revealed)
            }
        }
    }

    @Serializable
    data class WorldPersonRecord(
        val id: String,
        val worldId: String,
        val kind: String,
        val name: String,
        val description: String,
        val sheet: FifthEditionSheetRecord,
        val sheetSystem: String = GameSystem.FifthEdition.name,
        val pf2eSheet: Pathfinder2ESheetRecord? = null,
        val createdAtEpochMillis: Long,
        val updatedAtEpochMillis: Long,
    ) {
        fun toDomain(): WorldPerson {
            return WorldPerson(
                id = id,
                worldId = worldId,
                kind = PersonKind.fromStorage(kind),
                name = name,
                description = description,
                sheet = PersonSheetRecord.decode(sheetSystem, sheet, pf2eSheet),
                createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
                updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
            )
        }

        companion object {
            fun from(person: WorldPerson): WorldPersonRecord {
                val encoded = PersonSheetRecord.encode(person.sheet)
                return WorldPersonRecord(
                    id = person.id,
                    worldId = person.worldId,
                    kind = person.kind.name,
                    name = person.name,
                    description = person.description,
                    sheet = encoded.fifthEdition,
                    sheetSystem = encoded.sheetSystem,
                    pf2eSheet = encoded.pf2eSheet,
                    createdAtEpochMillis = person.createdAt.toEpochMilli(),
                    updatedAtEpochMillis = person.updatedAt.toEpochMilli(),
                )
            }
        }
    }

    @Serializable
    data class CampaignPersonRecord(
        val id: String,
        val campaignId: String,
        val worldPersonId: String?,
        val kind: String,
        val name: String,
        val description: String,
        val sheet: FifthEditionSheetRecord,
        val sheetSystem: String = GameSystem.FifthEdition.name,
        val pf2eSheet: Pathfinder2ESheetRecord? = null,
        val overlayHitPoints: Int?,
        val overlayNotes: String,
        val createdAtEpochMillis: Long,
        val updatedAtEpochMillis: Long,
    ) {
        fun toDomain(): CampaignPerson {
            return CampaignPerson(
                id = id,
                campaignId = campaignId,
                worldPersonId = worldPersonId,
                kind = PersonKind.fromStorage(kind),
                name = name,
                description = description,
                sheet = PersonSheetRecord.decode(sheetSystem, sheet, pf2eSheet),
                overlayHitPoints = overlayHitPoints,
                overlayNotes = overlayNotes,
                createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
                updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
            )
        }

        companion object {
            fun from(person: CampaignPerson): CampaignPersonRecord {
                val encoded = PersonSheetRecord.encode(person.sheet)
                return CampaignPersonRecord(
                    id = person.id,
                    campaignId = person.campaignId,
                    worldPersonId = person.worldPersonId,
                    kind = person.kind.name,
                    name = person.name,
                    description = person.description,
                    sheet = encoded.fifthEdition,
                    sheetSystem = encoded.sheetSystem,
                    pf2eSheet = encoded.pf2eSheet,
                    overlayHitPoints = person.overlayHitPoints,
                    overlayNotes = person.overlayNotes,
                    createdAtEpochMillis = person.createdAt.toEpochMilli(),
                    updatedAtEpochMillis = person.updatedAt.toEpochMilli(),
                )
            }
        }
    }

    @Serializable
    data class FifthEditionSheetRecord(
        val race: String,
        val classLevels: List<ClassLevelRecord>,
        val abilityScores: AbilityScoresRecord,
        val hitPoints: Int,
        val maxHitPoints: Int,
        val temporaryHitPoints: Int,
        val armorClass: Int,
        val walkSpeed: Int,
        val deathSaves: DeathSavesRecord,
        val items: List<InventoryItemRecord>,
        val features: List<PersonFeatureRecord>,
        val spells: List<PersonSpellRecord>,
        val notes: String,
        val skills: List<FifthEditionSkillRecord> = emptyList(),
        val spellSlots: List<FifthEditionSpellSlotRecord> = emptyList(),
        val concentratingSpell: String = "",
        val creatureSize: String = "Medium",
    ) {
        fun toDomain(): FifthEditionSheet {
            return FifthEditionSheet(
                race = race,
                classLevels = classLevels.map { it.toDomain() },
                abilityScores = abilityScores.toDomain(),
                hitPoints = hitPoints,
                maxHitPoints = maxHitPoints,
                temporaryHitPoints = temporaryHitPoints,
                armorClass = armorClass,
                walkSpeed = walkSpeed,
                deathSaves = deathSaves.toDomain(),
                items = items.map { it.toDomain() },
                features = features.map { it.toDomain() },
                spells = spells.map { it.toDomain() },
                notes = notes,
                skills = skills.map { it.toDomain() },
                spellSlots = spellSlots.map { it.toDomain() },
                concentratingSpell = concentratingSpell,
                creatureSize = CreatureSize.fromStorage(creatureSize),
            )
        }

        companion object {
            fun from(sheet: FifthEditionSheet): FifthEditionSheetRecord {
                return FifthEditionSheetRecord(
                    race = sheet.race,
                    classLevels = sheet.classLevels.map(ClassLevelRecord::from),
                    abilityScores = AbilityScoresRecord.from(sheet.abilityScores),
                    hitPoints = sheet.hitPoints,
                    maxHitPoints = sheet.maxHitPoints,
                    temporaryHitPoints = sheet.temporaryHitPoints,
                    armorClass = sheet.armorClass,
                    walkSpeed = sheet.walkSpeed,
                    deathSaves = DeathSavesRecord.from(sheet.deathSaves),
                    items = sheet.items.map(InventoryItemRecord::from),
                    features = sheet.features.map(PersonFeatureRecord::from),
                    spells = sheet.spells.map(PersonSpellRecord::from),
                    notes = sheet.notes,
                    skills = sheet.skills.map(FifthEditionSkillRecord::from),
                    spellSlots = sheet.spellSlots.map(FifthEditionSpellSlotRecord::from),
                    concentratingSpell = sheet.concentratingSpell,
                    creatureSize = sheet.creatureSize.name,
                )
            }
        }
    }

    data class EncodedPersonSheetRecord(
        val sheetSystem: String,
        val fifthEdition: FifthEditionSheetRecord,
        val pf2eSheet: Pathfinder2ESheetRecord?,
    )

    object PersonSheetRecord {
        fun decode(
            sheetSystem: String,
            fifthEdition: FifthEditionSheetRecord,
            pf2eSheet: Pathfinder2ESheetRecord?,
        ): PersonSheet {
            return when (GameSystem.fromStorage(sheetSystem)) {
                GameSystem.Pathfinder2E -> pf2eSheet?.toDomain() ?: Pathfinder2ESheet.empty()
                GameSystem.FifthEdition -> fifthEdition.toDomain()
            }
        }

        fun encode(sheet: PersonSheet): EncodedPersonSheetRecord {
            return when (sheet) {
                is FifthEditionSheet -> EncodedPersonSheetRecord(
                    sheetSystem = GameSystem.FifthEdition.name,
                    fifthEdition = FifthEditionSheetRecord.from(sheet),
                    pf2eSheet = null,
                )
                is Pathfinder2ESheet -> EncodedPersonSheetRecord(
                    sheetSystem = GameSystem.Pathfinder2E.name,
                    fifthEdition = FifthEditionSheetRecord.from(FifthEditionSheet.empty()),
                    pf2eSheet = Pathfinder2ESheetRecord.from(sheet),
                )
            }
        }
    }

    @Serializable
    data class Pathfinder2ESheetRecord(
        val ancestry: String,
        val heritage: String,
        val background: String,
        val className: String,
        val subclass: String,
        val level: Int,
        val abilityScores: AbilityScoresRecord,
        val hitPoints: Int,
        val maxHitPoints: Int,
        val temporaryHitPoints: Int,
        val armorClass: Int,
        val perception: Int,
        val landSpeed: Int,
        val skills: List<Pathfinder2ESkillRecord> = emptyList(),
        val feats: List<Pathfinder2EFeatRecord> = emptyList(),
        val spells: List<Pathfinder2ESpellRecord> = emptyList(),
        val notes: String,
        val dying: Int = 0,
        val wounded: Int = 0,
        val creatureSize: String = "Medium",
    ) {
        fun toDomain(): Pathfinder2ESheet {
            return Pathfinder2ESheet(
                ancestry = ancestry,
                heritage = heritage,
                background = background,
                className = className,
                subclass = subclass,
                level = level,
                abilityScores = abilityScores.toDomain(),
                hitPoints = hitPoints,
                maxHitPoints = maxHitPoints,
                temporaryHitPoints = temporaryHitPoints,
                armorClass = armorClass,
                perception = perception,
                landSpeed = landSpeed,
                skills = skills.map { it.toDomain() },
                feats = feats.map { it.toDomain() },
                spells = spells.map { it.toDomain() },
                notes = notes,
                dying = dying,
                wounded = wounded,
                creatureSize = CreatureSize.fromStorage(creatureSize),
            )
        }

        companion object {
            fun from(sheet: Pathfinder2ESheet): Pathfinder2ESheetRecord {
                return Pathfinder2ESheetRecord(
                    ancestry = sheet.ancestry,
                    heritage = sheet.heritage,
                    background = sheet.background,
                    className = sheet.className,
                    subclass = sheet.subclass,
                    level = sheet.level,
                    abilityScores = AbilityScoresRecord.from(sheet.abilityScores),
                    hitPoints = sheet.hitPoints,
                    maxHitPoints = sheet.maxHitPoints,
                    temporaryHitPoints = sheet.temporaryHitPoints,
                    armorClass = sheet.armorClass,
                    perception = sheet.perception,
                    landSpeed = sheet.landSpeed,
                    skills = sheet.skills.map(Pathfinder2ESkillRecord::from),
                    feats = sheet.feats.map(Pathfinder2EFeatRecord::from),
                    spells = sheet.spells.map(Pathfinder2ESpellRecord::from),
                    notes = sheet.notes,
                    dying = sheet.dying,
                    wounded = sheet.wounded,
                    creatureSize = sheet.creatureSize.name,
                )
            }
        }
    }

    @Serializable
    data class Pathfinder2ESkillRecord(
        val name: String,
        val rank: String,
    ) {
        fun toDomain(): Pathfinder2ESkill {
            return Pathfinder2ESkill(
                name = name,
                rank = Pathfinder2ESkillRank.fromStorage(rank),
            )
        }

        companion object {
            fun from(skill: Pathfinder2ESkill): Pathfinder2ESkillRecord {
                return Pathfinder2ESkillRecord(name = skill.name, rank = skill.rank.name)
            }
        }
    }

    @Serializable
    data class Pathfinder2EFeatRecord(
        val name: String,
        val type: String,
        val description: String,
    ) {
        fun toDomain(): Pathfinder2EFeat {
            return Pathfinder2EFeat(name = name, type = type, description = description)
        }

        companion object {
            fun from(feat: Pathfinder2EFeat): Pathfinder2EFeatRecord {
                return Pathfinder2EFeatRecord(
                    name = feat.name,
                    type = feat.type,
                    description = feat.description,
                )
            }
        }
    }

    @Serializable
    data class Pathfinder2ESpellRecord(
        val name: String,
        val rank: Int,
        val prepared: Boolean,
    ) {
        fun toDomain(): Pathfinder2ESpell {
            return Pathfinder2ESpell(name = name, rank = rank, prepared = prepared)
        }

        companion object {
            fun from(spell: Pathfinder2ESpell): Pathfinder2ESpellRecord {
                return Pathfinder2ESpellRecord(
                    name = spell.name,
                    rank = spell.rank,
                    prepared = spell.prepared,
                )
            }
        }
    }

    @Serializable
    data class ClassLevelRecord(
        val className: String,
        val subclass: String,
        val level: Int,
    ) {
        fun toDomain(): ClassLevel {
            return ClassLevel(className = className, subclass = subclass, level = level)
        }

        companion object {
            fun from(level: ClassLevel): ClassLevelRecord {
                return ClassLevelRecord(
                    className = level.className,
                    subclass = level.subclass,
                    level = level.level,
                )
            }
        }
    }

    @Serializable
    data class AbilityScoresRecord(
        val strength: Int,
        val dexterity: Int,
        val constitution: Int,
        val intelligence: Int,
        val wisdom: Int,
        val charisma: Int,
    ) {
        fun toDomain(): AbilityScores {
            return AbilityScores(
                strength = strength,
                dexterity = dexterity,
                constitution = constitution,
                intelligence = intelligence,
                wisdom = wisdom,
                charisma = charisma,
            )
        }

        companion object {
            fun from(scores: AbilityScores): AbilityScoresRecord {
                return AbilityScoresRecord(
                    strength = scores.strength,
                    dexterity = scores.dexterity,
                    constitution = scores.constitution,
                    intelligence = scores.intelligence,
                    wisdom = scores.wisdom,
                    charisma = scores.charisma,
                )
            }
        }
    }

    @Serializable
    data class DeathSavesRecord(
        val successes: Int,
        val failures: Int,
    ) {
        fun toDomain(): DeathSaves {
            return DeathSaves(successes = successes, failures = failures)
        }

        companion object {
            fun from(saves: DeathSaves): DeathSavesRecord {
                return DeathSavesRecord(successes = saves.successes, failures = saves.failures)
            }
        }
    }

    @Serializable
    data class InventoryItemRecord(
        val name: String,
        val quantity: Int,
        val notes: String,
    ) {
        fun toDomain(): InventoryItem {
            return InventoryItem(name = name, quantity = quantity, notes = notes)
        }

        companion object {
            fun from(item: InventoryItem): InventoryItemRecord {
                return InventoryItemRecord(name = item.name, quantity = item.quantity, notes = item.notes)
            }
        }
    }

    @Serializable
    data class PersonFeatureRecord(
        val name: String,
        val description: String,
    ) {
        fun toDomain(): PersonFeature {
            return PersonFeature(name = name, description = description)
        }

        companion object {
            fun from(feature: PersonFeature): PersonFeatureRecord {
                return PersonFeatureRecord(name = feature.name, description = feature.description)
            }
        }
    }

    @Serializable
    data class PersonSpellRecord(
        val name: String,
        val level: Int,
        val prepared: Boolean,
    ) {
        fun toDomain(): PersonSpell {
            return PersonSpell(name = name, level = level, prepared = prepared)
        }

        companion object {
            fun from(spell: PersonSpell): PersonSpellRecord {
                return PersonSpellRecord(name = spell.name, level = spell.level, prepared = spell.prepared)
            }
        }
    }

    @Serializable
    data class FifthEditionSkillRecord(
        val name: String,
        val ability: String,
        val proficient: Boolean,
    ) {
        fun toDomain(): FifthEditionSkill {
            return FifthEditionSkill(name = name, ability = ability, proficient = proficient)
        }

        companion object {
            fun from(skill: FifthEditionSkill): FifthEditionSkillRecord {
                return FifthEditionSkillRecord(
                    name = skill.name,
                    ability = skill.ability,
                    proficient = skill.proficient,
                )
            }
        }
    }

    @Serializable
    data class FifthEditionSpellSlotRecord(
        val level: Int,
        val maximum: Int,
        val used: Int,
    ) {
        fun toDomain(): FifthEditionSpellSlot {
            return FifthEditionSpellSlot(level = level, maximum = maximum, used = used)
        }

        companion object {
            fun from(slot: FifthEditionSpellSlot): FifthEditionSpellSlotRecord {
                return FifthEditionSpellSlotRecord(
                    level = slot.level,
                    maximum = slot.maximum,
                    used = slot.used,
                )
            }
        }
    }

    @Serializable
    data class LocationOverlayRecord(
        val campaignId: String,
        val locationId: String,
        val hasPartyPresence: Boolean,
        val notes: String,
        val updatedAtEpochMillis: Long,
    ) {
        fun toDomain(): LocationOverlay {
            return LocationOverlay(
                campaignId = campaignId,
                locationId = locationId,
                hasPartyPresence = hasPartyPresence,
                notes = notes,
                updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
            )
        }

        companion object {
            fun from(overlay: LocationOverlay): LocationOverlayRecord {
                return LocationOverlayRecord(
                    campaignId = overlay.campaignId,
                    locationId = overlay.locationId,
                    hasPartyPresence = overlay.hasPartyPresence,
                    notes = overlay.notes,
                    updatedAtEpochMillis = overlay.updatedAt.toEpochMilli(),
                )
            }
        }
    }

    @Serializable
    data class QuestRecord(
        val id: String,
        val campaignId: String,
        val title: String,
        val summary: String,
        val status: String,
        val locationId: String?,
        val objectives: List<QuestObjectiveRecord>,
        val links: List<QuestLinkRecord>,
        val createdAtEpochMillis: Long,
        val updatedAtEpochMillis: Long,
    ) {
        fun toDomain(): Quest {
            return Quest(
                id = id,
                campaignId = campaignId,
                title = title,
                summary = summary,
                status = QuestStatus.fromStorage(status),
                locationId = locationId,
                objectives = objectives.map { it.toDomain() },
                links = links.map { it.toDomain() },
                createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
                updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
            )
        }

        companion object {
            fun from(quest: Quest): QuestRecord {
                return QuestRecord(
                    id = quest.id,
                    campaignId = quest.campaignId,
                    title = quest.title,
                    summary = quest.summary,
                    status = quest.status.name,
                    locationId = quest.locationId,
                    objectives = quest.objectives.map(QuestObjectiveRecord::from),
                    links = quest.links.map(QuestLinkRecord::from),
                    createdAtEpochMillis = quest.createdAt.toEpochMilli(),
                    updatedAtEpochMillis = quest.updatedAt.toEpochMilli(),
                )
            }
        }
    }

    @Serializable
    data class QuestObjectiveRecord(
        val id: String,
        val title: String,
        val status: String,
    ) {
        fun toDomain(): QuestObjective {
            return QuestObjective(
                id = id,
                title = title,
                status = QuestObjectiveStatus.fromStorage(status),
            )
        }

        companion object {
            fun from(objective: QuestObjective): QuestObjectiveRecord {
                return QuestObjectiveRecord(
                    id = objective.id,
                    title = objective.title,
                    status = objective.status.name,
                )
            }
        }
    }

    @Serializable
    data class QuestLinkRecord(
        val id: String,
        val kind: String,
        val targetId: String,
    ) {
        fun toDomain(): QuestLink {
            return QuestLink(
                id = id,
                kind = QuestLinkKind.fromStorage(kind),
                targetId = targetId,
            )
        }

        companion object {
            fun from(link: QuestLink): QuestLinkRecord {
                return QuestLinkRecord(id = link.id, kind = link.kind.name, targetId = link.targetId)
            }
        }
    }

    @Serializable
    data class SessionRecord(
        val id: String,
        val campaignId: String,
        val name: String,
        val notes: String,
        val inWorldYear: Int? = null,
        val inWorldMonthId: String? = null,
        val inWorldDay: Int? = null,
        val scenes: List<SessionSceneRecord>,
        val marchOrder: List<MarchOrderEntryRecord>,
        val recap: String = "",
        val createdAtEpochMillis: Long,
        val updatedAtEpochMillis: Long,
    ) {
        fun toDomain(): Session {
            return Session(
                id = id,
                campaignId = campaignId,
                name = name,
                notes = notes,
                inWorldDate = toInWorldDate(),
                scenes = scenes.map { it.toDomain() },
                marchOrder = marchOrder.map { it.toDomain() },
                recap = recap,
                createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
                updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
            )
        }

        companion object {
            fun from(session: Session): SessionRecord {
                return SessionRecord(
                    id = session.id,
                    campaignId = session.campaignId,
                    name = session.name,
                    notes = session.notes,
                    inWorldYear = session.inWorldDate?.year,
                    inWorldMonthId = session.inWorldDate?.monthId,
                    inWorldDay = session.inWorldDate?.day,
                    scenes = session.scenes.map(SessionSceneRecord::from),
                    marchOrder = session.marchOrder.map(MarchOrderEntryRecord::from),
                    recap = session.recap,
                    createdAtEpochMillis = session.createdAt.toEpochMilli(),
                    updatedAtEpochMillis = session.updatedAt.toEpochMilli(),
                )
            }
        }

        private fun toInWorldDate(): WorldDate? {
            val year = inWorldYear ?: return null
            val monthId = inWorldMonthId ?: return null
            val day = inWorldDay ?: return null
            return WorldDate(year = year, monthId = monthId, day = day)
        }
    }

    @Serializable
    data class SessionSceneRecord(
        val id: String,
        val title: String,
        val notes: String,
    ) {
        fun toDomain(): SessionScene {
            return SessionScene(id = id, title = title, notes = notes)
        }

        companion object {
            fun from(scene: SessionScene): SessionSceneRecord {
                return SessionSceneRecord(id = scene.id, title = scene.title, notes = scene.notes)
            }
        }
    }

    @Serializable
    data class MarchOrderEntryRecord(
        val id: String,
        val person: PersonRefRecord,
        val displayName: String,
    ) {
        fun toDomain(): MarchOrderEntry {
            return MarchOrderEntry(id = id, person = person.toDomain(), displayName = displayName)
        }

        companion object {
            fun from(entry: MarchOrderEntry): MarchOrderEntryRecord {
                return MarchOrderEntryRecord(
                    id = entry.id,
                    person = PersonRefRecord.from(entry.person),
                    displayName = entry.displayName,
                )
            }
        }
    }

    @Serializable
    data class PlotThreadRecord(
        val id: String,
        val campaignId: String,
        val sessionId: String?,
        val title: String,
        val details: String,
        val status: String,
        val priority: String,
        val createdAtEpochMillis: Long,
        val updatedAtEpochMillis: Long,
    ) {
        fun toDomain(): PlotThread {
            return PlotThread(
                id = id,
                campaignId = campaignId,
                sessionId = sessionId,
                title = title,
                details = details,
                status = PlotThreadStatus.fromStorage(status),
                priority = PlotThreadPriority.fromStorage(priority),
                createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
                updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
            )
        }

        companion object {
            fun from(thread: PlotThread): PlotThreadRecord {
                return PlotThreadRecord(
                    id = thread.id,
                    campaignId = thread.campaignId,
                    sessionId = thread.sessionId,
                    title = thread.title,
                    details = thread.details,
                    status = thread.status.name,
                    priority = thread.priority.name,
                    createdAtEpochMillis = thread.createdAt.toEpochMilli(),
                    updatedAtEpochMillis = thread.updatedAt.toEpochMilli(),
                )
            }
        }
    }

    @Serializable
    data class ReferenceDocRecord(
        val id: String,
        val campaignId: String,
        val sessionId: String?,
        val title: String,
        val pathOrUrl: String,
        val createdAtEpochMillis: Long,
        val updatedAtEpochMillis: Long,
    ) {
        fun toDomain(): ReferenceDoc {
            return ReferenceDoc(
                id = id,
                campaignId = campaignId,
                sessionId = sessionId,
                title = title,
                pathOrUrl = pathOrUrl,
                createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
                updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
            )
        }

        companion object {
            fun from(doc: ReferenceDoc): ReferenceDocRecord {
                return ReferenceDocRecord(
                    id = doc.id,
                    campaignId = doc.campaignId,
                    sessionId = doc.sessionId,
                    title = doc.title,
                    pathOrUrl = doc.pathOrUrl,
                    createdAtEpochMillis = doc.createdAt.toEpochMilli(),
                    updatedAtEpochMillis = doc.updatedAt.toEpochMilli(),
                )
            }
        }
    }

    @Serializable
    data class BattleMapRecord(
        val id: String,
        val campaignId: String,
        val name: String,
        val originalWidth: Int,
        val originalHeight: Int,
        val tileSizePx: Int,
        val minZoom: Int,
        val maxZoom: Int,
        val columns: Int,
        val rows: Int,
        val unitName: String,
        val unitsPerTile: Double,
        val fogEnabled: Boolean = false,
        val revealedCells: List<String> = emptyList(),
        val blockedCells: List<String> = emptyList(),
        val difficultCells: List<String> = emptyList(),
        val items: List<BattleMapItemRecord> = emptyList(),
        val createdAtEpochMillis: Long,
        val updatedAtEpochMillis: Long,
    ) {
        fun toDomain(): BattleMap {
            return BattleMap(
                id = id,
                campaignId = campaignId,
                name = name,
                originalWidth = originalWidth,
                originalHeight = originalHeight,
                tileSizePx = tileSizePx,
                minZoom = minZoom,
                maxZoom = maxZoom,
                columns = columns,
                rows = rows,
                unitName = unitName,
                unitsPerTile = unitsPerTile,
                fogEnabled = fogEnabled,
                revealedCells = revealedCells.mapNotNull(::cellFromToken).toSet(),
                blockedCells = blockedCells.mapNotNull(::cellFromToken).toSet(),
                difficultCells = difficultCells.mapNotNull(::cellFromToken).toSet(),
                items = items.map { it.toDomain() },
                createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
                updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
            )
        }

        companion object {
            fun from(map: BattleMap): BattleMapRecord {
                return BattleMapRecord(
                    id = map.id,
                    campaignId = map.campaignId,
                    name = map.name,
                    originalWidth = map.originalWidth,
                    originalHeight = map.originalHeight,
                    tileSizePx = map.tileSizePx,
                    minZoom = map.minZoom,
                    maxZoom = map.maxZoom,
                    columns = map.columns,
                    rows = map.rows,
                    unitName = map.unitName,
                    unitsPerTile = map.unitsPerTile,
                    fogEnabled = map.fogEnabled,
                    revealedCells = map.revealedCells
                        .sortedWith(compareBy({ it.column }, { it.row }))
                        .map { cell -> "${cell.column},${cell.row}" },
                    blockedCells = map.blockedCells
                        .sortedWith(compareBy({ it.column }, { it.row }))
                        .map { cell -> "${cell.column},${cell.row}" },
                    difficultCells = map.difficultCells
                        .sortedWith(compareBy({ it.column }, { it.row }))
                        .map { cell -> "${cell.column},${cell.row}" },
                    items = map.items.map(BattleMapItemRecord::from),
                    createdAtEpochMillis = map.createdAt.toEpochMilli(),
                    updatedAtEpochMillis = map.updatedAt.toEpochMilli(),
                )
            }

            private fun cellFromToken(token: String): GridCell? {
                val bits = token.split(',')
                if (bits.size != 2) {
                    return null
                }
                val column = bits[0].toIntOrNull() ?: return null
                val row = bits[1].toIntOrNull() ?: return null
                return GridCell(column = column, row = row)
            }
        }
    }

    @Serializable
    data class BattleMapItemRecord(
        val id: String,
        val name: String,
        val column: Int,
        val row: Int,
    ) {
        fun toDomain(): BattleMapItem {
            return BattleMapItem(
                id = id,
                name = name,
                cell = GridCell(column = column, row = row),
            )
        }

        companion object {
            fun from(item: BattleMapItem): BattleMapItemRecord {
                return BattleMapItemRecord(
                    id = item.id,
                    name = item.name,
                    column = item.cell.column,
                    row = item.cell.row,
                )
            }
        }
    }

    @Serializable
    data class BattleMapSituationRecord(
        val id: String,
        val battleMapId: String,
        val name: String,
        val visible: Boolean,
        val sortIndex: Int,
        val createdAtEpochMillis: Long,
        val updatedAtEpochMillis: Long,
    ) {
        fun toDomain(): BattleMapSituation {
            return BattleMapSituation(
                id = id,
                battleMapId = battleMapId,
                name = name,
                visible = visible,
                sortIndex = sortIndex,
                createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
                updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
            )
        }

        companion object {
            fun from(situation: BattleMapSituation): BattleMapSituationRecord {
                return BattleMapSituationRecord(
                    id = situation.id,
                    battleMapId = situation.battleMapId,
                    name = situation.name,
                    visible = situation.visible,
                    sortIndex = situation.sortIndex,
                    createdAtEpochMillis = situation.createdAt.toEpochMilli(),
                    updatedAtEpochMillis = situation.updatedAt.toEpochMilli(),
                )
            }
        }
    }

    @Serializable
    data class WorldMapRecord(
        val id: String,
        val worldId: String,
        val locationId: String?,
        val originalWidth: Int,
        val originalHeight: Int,
        val tileSizePx: Int,
        val minZoom: Int,
        val maxZoom: Int,
        val createdAtEpochMillis: Long,
        val updatedAtEpochMillis: Long,
    ) {
        fun toDomain(): WorldMap {
            return WorldMap(
                id = id,
                worldId = worldId,
                locationId = locationId,
                originalWidth = originalWidth,
                originalHeight = originalHeight,
                tileSizePx = tileSizePx,
                minZoom = minZoom,
                maxZoom = maxZoom,
                createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
                updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
            )
        }

        companion object {
            fun from(worldMap: WorldMap): WorldMapRecord {
                return WorldMapRecord(
                    id = worldMap.id,
                    worldId = worldMap.worldId,
                    locationId = worldMap.locationId,
                    originalWidth = worldMap.originalWidth,
                    originalHeight = worldMap.originalHeight,
                    tileSizePx = worldMap.tileSizePx,
                    minZoom = worldMap.minZoom,
                    maxZoom = worldMap.maxZoom,
                    createdAtEpochMillis = worldMap.createdAt.toEpochMilli(),
                    updatedAtEpochMillis = worldMap.updatedAt.toEpochMilli(),
                )
            }
        }
    }

    @Serializable
    data class EncounterRecord(
        val id: String,
        val campaignId: String,
        val name: String,
        val locationId: String?,
        val battleMapId: String?,
        val difficulty: String,
        val notes: String,
        val outcomeNote: String,
        val status: String,
        val currentRound: Int,
        val currentTurnIndex: Int,
        val participants: List<EncounterParticipantRecord>,
        val createdAtEpochMillis: Long,
        val updatedAtEpochMillis: Long,
    ) {
        fun toDomain(): Encounter {
            return Encounter(
                id = id,
                campaignId = campaignId,
                name = name,
                locationId = locationId,
                battleMapId = battleMapId,
                difficulty = EncounterDifficulty.fromStorage(difficulty),
                notes = notes,
                outcomeNote = outcomeNote,
                status = EncounterStatus.fromStorage(status),
                currentRound = currentRound,
                currentTurnIndex = currentTurnIndex,
                participants = participants.map { it.toDomain() },
                createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
                updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
            )
        }

        companion object {
            fun from(encounter: Encounter): EncounterRecord {
                return EncounterRecord(
                    id = encounter.id,
                    campaignId = encounter.campaignId,
                    name = encounter.name,
                    locationId = encounter.locationId,
                    battleMapId = encounter.battleMapId,
                    difficulty = encounter.difficulty.name,
                    notes = encounter.notes,
                    outcomeNote = encounter.outcomeNote,
                    status = encounter.status.name,
                    currentRound = encounter.currentRound,
                    currentTurnIndex = encounter.currentTurnIndex,
                    participants = encounter.participants.map(EncounterParticipantRecord::from),
                    createdAtEpochMillis = encounter.createdAt.toEpochMilli(),
                    updatedAtEpochMillis = encounter.updatedAt.toEpochMilli(),
                )
            }
        }
    }

    @Serializable
    data class EncounterParticipantRecord(
        val id: String,
        val name: String,
        val source: String,
        val sourceId: String?,
        val initiativeRoll: Int?,
        val initiativeBonus: Int,
        val armorClass: Int,
        val hitPoints: Int,
        val maxHitPoints: Int,
        val temporaryHitPoints: Int,
        val conditions: List<String>,
        val groupCount: Int,
        val combatState: String,
        val gridColumn: Int?,
        val gridRow: Int?,
        val visibleToPlayers: Boolean = true,
        val attacksAllowed: Int = EncounterParticipant.MIN_ATTACKS_ALLOWED,
        val attacksUsed: Int = 0,
        val bonusActionUsed: Boolean = false,
        val reactionUsed: Boolean = false,
    ) {
        fun toDomain(): EncounterParticipant {
            return EncounterParticipant(
                id = id,
                name = name,
                source = EncounterParticipantSource.fromStorage(source),
                sourceId = sourceId,
                initiativeRoll = initiativeRoll,
                initiativeBonus = initiativeBonus,
                armorClass = armorClass,
                hitPoints = hitPoints,
                maxHitPoints = maxHitPoints,
                temporaryHitPoints = temporaryHitPoints,
                conditions = conditions,
                groupCount = groupCount,
                combatState = CombatState.fromStorage(combatState),
                gridColumn = gridColumn,
                gridRow = gridRow,
                visibleToPlayers = visibleToPlayers,
                attacksAllowed = attacksAllowed,
                attacksUsed = attacksUsed,
                bonusActionUsed = bonusActionUsed,
                reactionUsed = reactionUsed,
            )
        }

        companion object {
            fun from(participant: EncounterParticipant): EncounterParticipantRecord {
                return EncounterParticipantRecord(
                    id = participant.id,
                    name = participant.name,
                    source = participant.source.name,
                    sourceId = participant.sourceId,
                    initiativeRoll = participant.initiativeRoll,
                    initiativeBonus = participant.initiativeBonus,
                    armorClass = participant.armorClass,
                    hitPoints = participant.hitPoints,
                    maxHitPoints = participant.maxHitPoints,
                    temporaryHitPoints = participant.temporaryHitPoints,
                    conditions = participant.conditions,
                    groupCount = participant.groupCount,
                    combatState = participant.combatState.name,
                    gridColumn = participant.gridColumn,
                    gridRow = participant.gridRow,
                    visibleToPlayers = participant.visibleToPlayers,
                    attacksAllowed = participant.attacksAllowed,
                    attacksUsed = participant.attacksUsed,
                    bonusActionUsed = participant.bonusActionUsed,
                    reactionUsed = participant.reactionUsed,
                )
            }
        }
    }

    @Serializable
    data class FactionRecord(
        val id: String,
        val worldId: String,
        val name: String,
        val description: String,
        val goals: String,
        val notes: String,
        val createdAtEpochMillis: Long,
        val updatedAtEpochMillis: Long,
    ) {
        fun toDomain(): Faction {
            return Faction(
                id = id,
                worldId = worldId,
                name = name,
                description = description,
                goals = goals,
                notes = notes,
                createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
                updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
            )
        }

        companion object {
            fun from(faction: Faction): FactionRecord {
                return FactionRecord(
                    id = faction.id,
                    worldId = faction.worldId,
                    name = faction.name,
                    description = faction.description,
                    goals = faction.goals,
                    notes = faction.notes,
                    createdAtEpochMillis = faction.createdAt.toEpochMilli(),
                    updatedAtEpochMillis = faction.updatedAt.toEpochMilli(),
                )
            }
        }
    }

    @Serializable
    data class FactionMembershipRecord(
        val id: String,
        val person: PersonRefRecord,
        val factionId: String,
        val role: String,
        val notes: String,
        val createdAtEpochMillis: Long,
    ) {
        fun toDomain(): FactionMembership {
            return FactionMembership(
                id = id,
                person = person.toDomain(),
                factionId = factionId,
                role = role,
                notes = notes,
                createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
            )
        }

        companion object {
            fun from(membership: FactionMembership): FactionMembershipRecord {
                return FactionMembershipRecord(
                    id = membership.id,
                    person = PersonRefRecord.from(membership.person),
                    factionId = membership.factionId,
                    role = membership.role,
                    notes = membership.notes,
                    createdAtEpochMillis = membership.createdAt.toEpochMilli(),
                )
            }
        }
    }

    @Serializable
    data class PersonRelationshipRecord(
        val id: String,
        val from: PersonRefRecord,
        val to: PersonRefRecord,
        val type: String,
        val description: String,
        val factionId: String? = null,
        val factionLean: String = "",
    ) {
        fun toDomain(): PersonRelationship {
            return PersonRelationship(
                id = id,
                from = from.toDomain(),
                to = to.toDomain(),
                type = RelationshipType.fromStorage(type),
                description = description,
                factionId = factionId?.takeIf { it.isNotBlank() },
            )
        }

        companion object {
            fun from(relationship: PersonRelationship): PersonRelationshipRecord {
                return PersonRelationshipRecord(
                    id = relationship.id,
                    from = PersonRefRecord.from(relationship.from),
                    to = PersonRefRecord.from(relationship.to),
                    type = relationship.type.name,
                    description = relationship.description,
                    factionId = relationship.factionId,
                    factionLean = "",
                )
            }
        }
    }

    @Serializable
    data class PersonCompanionRecord(
        val id: String,
        val owner: PersonRefRecord,
        val companion: PersonRefRecord,
        val kind: String,
    ) {
        fun toDomain(): PersonCompanion {
            return PersonCompanion(
                id = id,
                owner = owner.toDomain(),
                companion = companion.toDomain(),
                kind = CompanionKind.fromStorage(kind),
            )
        }

        companion object {
            fun from(companion: PersonCompanion): PersonCompanionRecord {
                return PersonCompanionRecord(
                    id = companion.id,
                    owner = PersonRefRecord.from(companion.owner),
                    companion = PersonRefRecord.from(companion.companion),
                    kind = companion.kind.name,
                )
            }
        }
    }

    @Serializable
    data class PersonRefRecord(
        val kind: String,
        val id: String,
    ) {
        fun toDomain(): PersonRef {
            return when (kind) {
                "Campaign" -> PersonRef.Campaign(id)
                else -> PersonRef.World(id)
            }
        }

        companion object {
            fun from(ref: PersonRef): PersonRefRecord {
                val kind = when (ref) {
                    is PersonRef.World -> "World"
                    is PersonRef.Campaign -> "Campaign"
                }
                return PersonRefRecord(kind = kind, id = ref.id)
            }
        }
    }
}
