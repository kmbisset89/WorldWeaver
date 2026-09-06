package io.github.kmbisset89.worldweaver.domain

internal enum class CelestialBodyKind(
    val displayName: String,
) {
    Sun("Sun"),
    Moon("Moon"),
    Planet("Planet"),
    ;

    companion object {
        fun fromStorage(value: String): CelestialBodyKind {
            return entries.firstOrNull { it.name == value } ?: Planet
        }
    }
}
