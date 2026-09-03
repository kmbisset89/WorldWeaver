package io.github.kmbisset89.worldweaver.domain

internal data class QuestDraft(
    val title: String,
    val summary: String,
    val status: QuestStatus,
    val locationId: String?,
    val objectives: List<QuestObjective>,
    val links: List<QuestLink>,
)
