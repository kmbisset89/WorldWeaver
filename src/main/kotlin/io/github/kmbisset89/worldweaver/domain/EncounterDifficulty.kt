package io.github.kmbisset89.worldweaver.domain

internal enum class EncounterDifficulty(
    val displayName: String,
) {
    Easy("Easy"),
    Medium("Medium"),
    Hard("Hard"),
    Deadly("Deadly"),
    Other("Other"),
    ;

    companion object {
        fun fromStorage(value: String): EncounterDifficulty {
            return entries.firstOrNull { it.name == value } ?: Medium
        }
    }
}
