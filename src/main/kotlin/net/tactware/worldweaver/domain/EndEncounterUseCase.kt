package net.tactware.worldweaver.domain

internal class EndEncounterUseCase(
    private val encounterRepository: EncounterRepository,
    private val sessionRepository: SessionRepository,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data object Ended : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(
        encounterId: String,
        outcomeNote: String,
    ): Result {
        val encounter = encounterRepository.getById(encounterId) ?: return Result.NotFound
        val note = outcomeNote.trim()
        val now = instantProvider.now()
        encounterRepository.update(
            encounter.copy(
                status = EncounterStatus.Ended,
                outcomeNote = note,
                updatedAt = now,
            )
        )
        val currentSession = sessionRepository.getByCampaign(encounter.campaignId)
            .maxWithOrNull(compareBy<Session> { it.updatedAt }.thenBy { it.createdAt })
        if (currentSession != null && note.isNotEmpty()) {
            val appended = appendOutcome(currentSession.notes, encounter.name, note)
            sessionRepository.update(
                currentSession.copy(
                    notes = appended,
                    updatedAt = now,
                )
            )
        }
        return Result.Ended
    }

    private fun appendOutcome(
        existingNotes: String,
        encounterName: String,
        outcome: String,
    ): String {
        val block = "Encounter: $encounterName\n$outcome"
        return if (existingNotes.isBlank()) {
            block
        } else {
            "${existingNotes.trimEnd()}\n\n$block"
        }
    }
}
