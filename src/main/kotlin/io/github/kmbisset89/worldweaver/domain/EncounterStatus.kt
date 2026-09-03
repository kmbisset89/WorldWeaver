package io.github.kmbisset89.worldweaver.domain

internal enum class EncounterStatus(
    val displayName: String,
) {
    Planned("Planned"),
    Active("Active"),
    Ended("Ended"),
    ;

    companion object {
        fun fromStorage(value: String): EncounterStatus {
            return entries.firstOrNull { it.name == value } ?: Planned
        }
    }
}
