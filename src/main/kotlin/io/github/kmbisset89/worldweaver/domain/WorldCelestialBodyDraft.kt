package io.github.kmbisset89.worldweaver.domain

internal data class WorldCelestialBodyDraft(
    val name: String,
    val notes: String,
    val kind: CelestialBodyKind,
    val periodDays: Int,
    val epochOffsetDays: Int,
    val sortIndex: Int? = null,
)
