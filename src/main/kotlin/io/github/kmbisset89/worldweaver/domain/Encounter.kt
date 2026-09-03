package io.github.kmbisset89.worldweaver.domain

import java.time.Instant

internal data class Encounter(
    val id: String,
    val campaignId: String,
    val name: String,
    val locationId: String?,
    val battleMapId: String? = null,
    val difficulty: EncounterDifficulty,
    val notes: String,
    val outcomeNote: String,
    val status: EncounterStatus,
    val currentRound: Int,
    val currentTurnIndex: Int,
    val participants: List<EncounterParticipant>,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    fun initiativeOrder(): List<EncounterParticipant> {
        return participants.sortedWith(
            compareByDescending<EncounterParticipant> { it.initiativeTotal() ?: Int.MIN_VALUE }
                .thenBy { it.name.lowercase() }
                .thenBy { it.id },
        )
    }
}
