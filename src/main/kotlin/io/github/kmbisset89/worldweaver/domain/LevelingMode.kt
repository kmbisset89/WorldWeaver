package io.github.kmbisset89.worldweaver.domain

internal enum class LevelingMode(
    val displayName: String,
) {
    Milestone("Milestone"),
    Experience("XP"),
    ;

    companion object {
        fun fromStorage(value: String): LevelingMode {
            return entries.firstOrNull { it.name == value } ?: Milestone
        }
    }
}
