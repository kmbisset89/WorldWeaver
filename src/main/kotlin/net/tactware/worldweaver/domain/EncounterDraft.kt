package net.tactware.worldweaver.domain

internal data class EncounterDraft(
    val name: String,
    val locationId: String?,
    val battleMapId: String? = null,
    val difficulty: EncounterDifficulty,
    val notes: String,
    val outcomeNote: String,
    val participants: List<EncounterParticipant>,
)
