package io.github.kmbisset89.worldweaver.domain

import kotlinx.serialization.Serializable

@Serializable
internal data class SrdCatalogPayload(
    val formatVersion: Int,
    val sourceLabel: String,
    val importedAtEpochMillis: Long = 0,
    val races: List<String> = emptyList(),
    val classes: List<SrdClassPayload> = emptyList(),
    val spells: List<SrdSpellPayload> = emptyList(),
    val monsters: List<SrdMonsterPayload> = emptyList(),
)
