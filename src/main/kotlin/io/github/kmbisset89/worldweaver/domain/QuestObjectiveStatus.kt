package io.github.kmbisset89.worldweaver.domain

internal enum class QuestObjectiveStatus(
    val displayName: String,
) {
    Open("Open"),
    Complete("Complete"),
    Failed("Failed"),
    ;

    companion object {
        fun fromStorage(value: String): QuestObjectiveStatus {
            return entries.firstOrNull { it.name == value } ?: Open
        }
    }
}
