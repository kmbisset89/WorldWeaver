package io.github.kmbisset89.worldweaver.domain

internal class CloseSessionUseCase(
    private val sessionRepository: SessionRepository,
    private val encounterRepository: EncounterRepository,
    private val questRepository: QuestRepository,
    private val locationOverlayRepository: LocationOverlayRepository,
    private val locationRepository: LocationRepository,
    private val worldCalendarRepository: WorldCalendarRepository,
    private val campaignRepository: CampaignRepository,
    private val instantProvider: InstantProvider,
    private val dateFormatter: WorldDateFormatter = WorldDateFormatter(),
) {
    sealed interface Result {
        data class Closed(val recap: String) : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(
        sessionId: String,
        whyItMatters: String = "",
    ): Result {
        val session = sessionRepository.getById(sessionId) ?: return Result.NotFound
        val recap = buildRecap(session, whyItMatters.trim())
        val now = instantProvider.now()
        sessionRepository.update(
            session.copy(
                recap = recap,
                updatedAt = now,
            )
        )
        return Result.Closed(recap)
    }

    private suspend fun buildRecap(session: Session, whyItMatters: String): String {
        val lines = mutableListOf<String>()
        val campaign = campaignRepository.getById(session.campaignId)
        val calendar = campaign?.let { worldCalendarRepository.getByWorld(it.worldId) }
        val stamped = session.inWorldDate?.let { date ->
            calendar?.let { dateFormatter.format(it, date) }
        }
        if (!stamped.isNullOrBlank()) {
            lines += "Date: $stamped"
        }
        encounterRepository.getByCampaign(session.campaignId)
            .filter { it.status == EncounterStatus.Ended && it.outcomeNote.isNotBlank() }
            .forEach { encounter ->
                lines += "Encounter ${encounter.name}: ${encounter.outcomeNote}"
            }
        questRepository.getByCampaign(session.campaignId)
            .filter { it.status == QuestStatus.Active }
            .forEach { quest ->
                val open = quest.objectives.count { it.status == QuestObjectiveStatus.Open }
                val complete = quest.objectives.count { it.status == QuestObjectiveStatus.Complete }
                lines += "Quest ${quest.title}: $complete complete, $open open"
            }
        val overlays = locationOverlayRepository.getByCampaign(session.campaignId)
            .filter { it.hasPartyPresence }
        overlays.forEach { overlay ->
            val location = locationRepository.getById(overlay.locationId)
            val name = location?.name ?: overlay.locationId
            lines += "Party at $name"
        }
        if (whyItMatters.isNotEmpty()) {
            lines += "Why it matters next week: $whyItMatters"
        }
        return lines.joinToString("\n")
    }
}
