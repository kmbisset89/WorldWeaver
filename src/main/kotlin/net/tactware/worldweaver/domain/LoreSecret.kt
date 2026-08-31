package net.tactware.worldweaver.domain

internal data class LoreSecret(
    val id: String,
    val title: String,
    val secret: String,
    val hints: List<LoreHint>,
)
