package io.github.kmbisset89.worldweaver.domain

internal data class LoreSecret(
    val id: String,
    val title: String,
    val secret: String,
    val hints: List<LoreHint>,
)
