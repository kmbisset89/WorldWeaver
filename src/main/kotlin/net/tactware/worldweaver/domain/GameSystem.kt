package net.tactware.worldweaver.domain

internal enum class GameSystem(
    val displayName: String,
) {
    FifthEdition("5E"),
    Pathfinder2E("PF2E"),
    ;

    companion object {
        fun fromStorage(value: String): GameSystem {
            return entries.firstOrNull { it.name == value } ?: FifthEdition
        }

        fun resolve(stored: GameSystem?, worldDefault: GameSystem): GameSystem {
            return stored ?: worldDefault
        }
    }
}
