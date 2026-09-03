package io.github.kmbisset89.worldweaver.domain

import kotlinx.serialization.Serializable

@Serializable
internal data class SrdSpellPayload(
    val name: String,
    val level: Int,
)
