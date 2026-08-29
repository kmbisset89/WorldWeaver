package net.tactware.worldweaver.domain

internal enum class GameSystem(
    val displayName: String,
) {
    FifthEdition("5E"),
    ;

    companion object {
        fun fromStorage(value: String): GameSystem {
            return entries.firstOrNull { it.name == value } ?: FifthEdition
        }
    }
}
