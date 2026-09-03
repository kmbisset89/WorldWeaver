package io.github.kmbisset89.worldweaver.domain

internal data class WorldPersonDraft(
    val kind: PersonKind,
    val name: String,
    val description: String,
    val sheet: PersonSheet,
)
