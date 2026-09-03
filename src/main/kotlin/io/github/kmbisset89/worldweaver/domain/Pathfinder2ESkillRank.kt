package io.github.kmbisset89.worldweaver.domain

internal enum class Pathfinder2ESkillRank {
    Untrained,
    Trained,
    Expert,
    Master,
    Legendary,
    ;

    companion object {
        fun fromStorage(value: String): Pathfinder2ESkillRank {
            return entries.firstOrNull { it.name == value } ?: Untrained
        }
    }
}
