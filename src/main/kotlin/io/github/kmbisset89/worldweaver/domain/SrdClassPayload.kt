package io.github.kmbisset89.worldweaver.domain

import kotlinx.serialization.Serializable

@Serializable
internal data class SrdClassPayload(
    val name: String,
    val subclasses: List<String> = emptyList(),
)
