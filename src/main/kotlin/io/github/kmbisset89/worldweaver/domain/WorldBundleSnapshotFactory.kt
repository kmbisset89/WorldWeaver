package io.github.kmbisset89.worldweaver.domain

internal class WorldBundleSnapshotFactory(
    private val worldRepository: WorldRepository,
    private val worldCalendarRepository: WorldCalendarRepository,
    private val campaignRepository: CampaignRepository,
    private val locationRepository: LocationRepository,
    private val loreRepository: LoreRepository,
    private val factionRepository: FactionRepository,
    private val factionMembershipRepository: FactionMembershipRepository,
    private val worldPersonRepository: WorldPersonRepository,
    private val campaignPersonRepository: CampaignPersonRepository,
    private val locationOverlayRepository: LocationOverlayRepository,
    private val questRepository: QuestRepository,
    private val sessionRepository: SessionRepository,
    private val plotThreadRepository: PlotThreadRepository,
    private val referenceDocRepository: ReferenceDocRepository,
    private val battleMapRepository: BattleMapRepository,
    private val battleMapSituationRepository: BattleMapSituationRepository,
    private val encounterRepository: EncounterRepository,
    private val personRelationshipRepository: PersonRelationshipRepository,
    private val personCompanionRepository: PersonCompanionRepository,
    private val avatarFileStore: PersonAvatarFileStore,
    private val battleMapFileStore: BattleMapFileStore,
    private val voiceClipFileStore: VoiceClipFileStore,
    private val instantProvider: InstantProvider,
) {
    suspend fun create(worldId: String): WorldBundle? {
        val world = worldRepository.getById(worldId) ?: return null
        val campaigns = campaignRepository.getByWorld(worldId)
        val campaignIds = campaigns.map { it.id }.toSet()
        val worldPeople = worldPersonRepository.getByWorld(worldId)
        val campaignPeople = campaignIds.flatMap { campaignPersonRepository.getByCampaign(it) }
        val personIds = worldPeople.map { it.id }.toSet() + campaignPeople.map { it.id }.toSet()
        val battleMaps = campaignIds.flatMap { battleMapRepository.getByCampaign(it) }
        val locations = locationRepository.getByWorld(worldId)
        return WorldBundle(
            formatVersion = WorldBundle.FORMAT_VERSION,
            exportedAt = instantProvider.now(),
            world = world,
            calendar = worldCalendarRepository.getByWorld(worldId),
            campaigns = campaigns,
            locations = locations,
            loreEntries = loreRepository.getByWorld(worldId),
            factions = factionRepository.getByWorld(worldId),
            memberships = factionMembershipRepository.getAll().filter { membership ->
                containsPerson(membership.person, personIds)
            },
            worldPeople = worldPeople,
            campaignPeople = campaignPeople,
            locationOverlays = campaignIds.flatMap { locationOverlayRepository.getByCampaign(it) },
            quests = campaignIds.flatMap { questRepository.getByCampaign(it) },
            sessions = campaignIds.flatMap { sessionRepository.getByCampaign(it) },
            plotThreads = campaignIds.flatMap { plotThreadRepository.getByCampaign(it) },
            referenceDocs = campaignIds.flatMap { referenceDocRepository.getByCampaign(it) },
            battleMaps = battleMaps,
            battleMapSituations = battleMaps.flatMap { battleMapSituationRepository.getByBattleMap(it.id) },
            encounters = campaignIds.flatMap { encounterRepository.getByCampaign(it) },
            relationships = personRelationshipRepository.getAll().filter { relationship ->
                containsPerson(relationship.from, personIds) && containsPerson(relationship.to, personIds)
            },
            companions = personCompanionRepository.getAll().filter { companion ->
                containsPerson(companion.owner, personIds) && containsPerson(companion.companion, personIds)
            },
            avatarFiles = collectAvatars(worldPeople, campaignPeople),
            mapFiles = collectMapFiles(battleMaps),
            voiceFiles = collectVoiceFiles(locations, worldPeople, campaignPeople),
        )
    }

    private fun collectAvatars(
        worldPeople: List<WorldPerson>,
        campaignPeople: List<CampaignPerson>,
    ): List<WorldBundle.AvatarFile> {
        val files = mutableListOf<WorldBundle.AvatarFile>()
        worldPeople.forEach { person ->
            val ref = PersonRef.World(person.id)
            val png = avatarFileStore.read(ref) ?: return@forEach
            files += WorldBundle.AvatarFile(ref = ref, png = png)
        }
        campaignPeople.forEach { person ->
            val ref = PersonRef.Campaign(person.id)
            val png = avatarFileStore.read(ref) ?: return@forEach
            files += WorldBundle.AvatarFile(ref = ref, png = png)
        }
        return files
    }

    private fun collectMapFiles(battleMaps: List<BattleMap>): List<WorldBundle.MapFile> {
        return battleMaps.flatMap { map ->
            battleMapFileStore.listRelativeFiles(map.id).map { (relativePath, bytes) ->
                WorldBundle.MapFile(
                    battleMapId = map.id,
                    relativePath = relativePath,
                    bytes = bytes,
                )
            }
        }
    }

    private fun collectVoiceFiles(
        locations: List<Location>,
        worldPeople: List<WorldPerson>,
        campaignPeople: List<CampaignPerson>,
    ): List<WorldBundle.VoiceFile> {
        val files = mutableListOf<WorldBundle.VoiceFile>()
        locations.forEach { location ->
            val ref = VoiceClipRef.Location(location.id)
            val wav = voiceClipFileStore.read(ref) ?: return@forEach
            files += WorldBundle.VoiceFile(ref = ref, wav = wav)
        }
        worldPeople.forEach { person ->
            val ref = VoiceClipRef.WorldPerson(person.id)
            val wav = voiceClipFileStore.read(ref) ?: return@forEach
            files += WorldBundle.VoiceFile(ref = ref, wav = wav)
        }
        campaignPeople.forEach { person ->
            val ref = VoiceClipRef.CampaignPerson(person.id)
            val wav = voiceClipFileStore.read(ref) ?: return@forEach
            files += WorldBundle.VoiceFile(ref = ref, wav = wav)
        }
        return files
    }

    private fun containsPerson(ref: PersonRef, personIds: Set<String>): Boolean {
        return ref.id in personIds
    }
}
