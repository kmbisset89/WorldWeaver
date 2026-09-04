package io.github.kmbisset89.worldweaver.domain

import java.time.Instant

internal class AwardPartyExperienceUseCase(
    private val campaignPersonRepository: CampaignPersonRepository,
    private val sessionRepository: SessionRepository,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data class Awarded(val partySize: Int, val amount: Int) : Result
        data object NoPlayerCharacters : Result
        data object InvalidAmount : Result
    }

    suspend operator fun invoke(
        campaignId: String,
        amount: Int,
        sessionId: String? = null,
    ): Result {
        if (amount <= 0) {
            return Result.InvalidAmount
        }
        val party = campaignPersonRepository.getByCampaign(campaignId)
            .filter { it.kind == PersonKind.PlayerCharacter }
        if (party.isEmpty()) {
            return Result.NoPlayerCharacters
        }
        val now = instantProvider.now()
        party.forEach { person ->
            campaignPersonRepository.update(
                person.copy(
                    sheet = awardedSheet(person.sheet, amount),
                    updatedAt = now,
                )
            )
        }
        appendRecap(sessionId, "Awarded $amount XP", now)
        return Result.Awarded(partySize = party.size, amount = amount)
    }

    private fun awardedSheet(sheet: PersonSheet, amount: Int): PersonSheet {
        return when (sheet) {
            is FifthEditionSheet -> sheet.copy(currentXp = sheet.currentXp + amount)
            is Pathfinder2ESheet -> sheet.copy(currentXp = sheet.currentXp + amount)
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
