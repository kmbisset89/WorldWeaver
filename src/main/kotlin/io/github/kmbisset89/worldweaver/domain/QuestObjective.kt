package io.github.kmbisset89.worldweaver.domain

internal data class QuestObjective(
    val id: String,
    val title: String,
    val status: QuestObjectiveStatus,
)
