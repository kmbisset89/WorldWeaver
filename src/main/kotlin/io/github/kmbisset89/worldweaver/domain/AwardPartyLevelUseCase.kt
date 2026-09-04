package io.github.kmbisset89.worldweaver.domain

import java.time.Instant

internal class AwardPartyLevelUseCase(
    private val campaignPersonRepository: CampaignPersonRepository,
    private val sessionRepository: SessionRepository,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data class Awarded(val partySize: Int, val partyLevel: Int) : Result
        data object NoPlayerCharacters : Result
    }

    suspend operator fun invoke(
        campaignId: String,
        sessionId: String? = null,
    ): Result {
        val party = campaignPersonRepository.getByCampaign(campaignId)
            .filter { it.kind == PersonKind.PlayerCharacter }
        if (party.isEmpty()) {
            return Result.NoPlayerCharacters
        }
        val now = instantProvider.now()
        val updated = party.mapNotNull { person ->
            val nextSheet = incrementedSheet(person.sheet) ?: return@mapNotNull null
            person.copy(sheet = nextSheet, updatedAt = now)
        }
        if (updated.isEmpty()) {
            return Result.NoPlayerCharacters
        }
        updated.forEach { person ->
            campaignPersonRepository.update(person)
        }
        val partyLevel = updated.maxOf { it.sheet.totalLevel() }
        appendRecap(sessionId, "Party reached level $partyLevel", now)
        return Result.Awarded(partySize = updated.size, partyLevel = partyLevel)
    }

    private fun incrementedSheet(sheet: PersonSheet): PersonSheet? {
        return when (sheet) {
            is FifthEditionSheet -> {
                if (sheet.classLevels.isEmpty()) {
                    null
                } else {
                    val lastIndex = sheet.classLevels.lastIndex
                    sheet.copy(
                        classLevels = sheet.classLevels.mapIndexed { index, level ->
                            if (index == lastIndex) {
                                level.copy(level = level.level + 1)
                            } else {
                                level
                            }
                        },
                    )
                }
            }
            is Pathfinder2ESheet -> sheet.copy(level = sheet.level + 1)
        }
    }

    private suspend fun appendRecap(
        sessionId: String?,
        line: String,
        now: Instant,
    ) {
        val session = sessionId?.let { id -> sessionRepository.getById(id) } ?: return
        val recap = if (session.recap.isBlank()) {
            line
        } else {
            "${session.recap}\n$line"
        }
        sessionRepository.update(session.copy(recap = recap, updatedAt = now))
    }
}
