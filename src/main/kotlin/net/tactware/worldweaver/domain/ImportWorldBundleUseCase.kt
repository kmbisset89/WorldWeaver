package net.tactware.worldweaver.domain

import java.io.File

internal class ImportWorldBundleUseCase(
    private val archiveConverter: WorldBundleArchiveConverter,
    private val idRemapper: WorldBundleIdRemapper,
    private val transactionRunner: TransactionRunner,
    private val worldRepository: WorldRepository,
    private val worldCalendarRepository: WorldCalendarRepository,
    private val defaultCalendarFactory: DefaultWorldCalendarFactory,
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
    private val setActiveWorld: SetActiveWorldUseCase,
) {
    sealed interface Result {
        data class Imported(val world: World) : Result
        data object UnsupportedVersion : Result
        data object InvalidArchive : Result
        data class Failed(val message: String) : Result
    }

    suspend operator fun invoke(sourceFile: File): Result {
        val read = archiveConverter.read(sourceFile)
        val source = when (read) {
            is WorldBundleArchiveConverter.ReadResult.Ready -> read.bundle
            WorldBundleArchiveConverter.ReadResult.UnsupportedVersion -> return Result.UnsupportedVersion
            WorldBundleArchiveConverter.ReadResult.InvalidArchive -> return Result.InvalidArchive
        }
        return try {
            val remapped = idRemapper.remap(source)
            persist(remapped)
            setActiveWorld(remapped.world.id)
            Result.Imported(remapped.world)
        } catch (error: Exception) {
            Result.Failed(error.message ?: "Could not import the world backup")
        }
    }

    private suspend fun persist(bundle: WorldBundle) {
        transactionRunner.run {
            worldRepository.insert(bundle.world)
            val calendar = bundle.calendar?.copy(worldId = bundle.world.id)
                ?: defaultCalendarFactory.create(bundle.world.id, bundle.exportedAt)
            worldCalendarRepository.insert(calendar)
            locationsInParentOrder(bundle.locations).forEach { location ->
                locationRepository.insert(location)
            }
            bundle.worldPeople.forEach { worldPersonRepository.insert(it) }
            bundle.loreEntries.forEach { loreRepository.insert(it) }
            bundle.factions.forEach { factionRepository.insert(it) }
            bundle.campaigns.forEach { campaignRepository.insert(it) }
            bundle.campaignPeople.forEach { campaignPersonRepository.insert(it) }
            bundle.locationOverlays.forEach { locationOverlayRepository.upsert(it) }
            bundle.sessions.forEach { sessionRepository.insert(it) }
            bundle.plotThreads.forEach { plotThreadRepository.insert(it) }
            bundle.referenceDocs.forEach { referenceDocRepository.insert(it) }
            bundle.quests.forEach { questRepository.insert(it) }
            bundle.battleMaps.forEach { battleMapRepository.insert(it) }
            bundle.battleMapSituations.forEach { battleMapSituationRepository.insert(it) }
            bundle.encounters.forEach { encounterRepository.insert(it) }
            bundle.memberships.forEach { factionMembershipRepository.insert(it) }
            bundle.relationships.forEach { personRelationshipRepository.insert(it) }
            bundle.companions.forEach { personCompanionRepository.insert(it) }
        }
        bundle.avatarFiles.forEach { file ->
            avatarFileStore.write(file.ref, file.png)
        }
        bundle.mapFiles
            .groupBy { it.battleMapId }
            .forEach { (battleMapId, files) ->
                battleMapFileStore.writeRelativeFiles(
                    battleMapId = battleMapId,
                    files = files.map { it.relativePath to it.bytes },
                )
            }
        bundle.voiceFiles.forEach { file ->
            voiceClipFileStore.write(file.ref, file.wav)
        }
    }

    private fun locationsInParentOrder(locations: List<Location>): List<Location> {
        val remaining = locations.toMutableList()
        val ordered = mutableListOf<Location>()
        val placed = mutableSetOf<String>()
        while (remaining.isNotEmpty()) {
            val ready = remaining.filter { location ->
                val parentId = location.parentLocationId
                parentId == null || parentId in placed || remaining.none { it.id == parentId }
            }
            if (ready.isEmpty()) {
                ordered.addAll(remaining)
                break
            }
            ordered.addAll(ready)
            placed.addAll(ready.map { it.id })
            remaining.removeAll(ready)
        }
        return ordered
    }
}
