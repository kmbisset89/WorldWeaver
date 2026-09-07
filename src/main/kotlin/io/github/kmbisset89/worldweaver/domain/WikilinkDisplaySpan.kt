package io.github.kmbisset89.worldweaver.domain

internal data class WikilinkDisplaySpan(
    val text: String,
    val target: WikilinkTarget?,
    val unresolved: Boolean = false,
)
