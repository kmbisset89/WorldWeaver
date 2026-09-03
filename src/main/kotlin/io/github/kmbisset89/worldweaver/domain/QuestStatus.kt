package io.github.kmbisset89.worldweaver.domain

internal enum class QuestStatus(
    val displayName: String,
) {
    Active("Active"),
    Completed("Completed"),
    ;

    companion object {
        fun fromStorage(value: String): QuestStatus {
            return entries.firstOrNull { it.name == value } ?: Active
        }
    }
}
