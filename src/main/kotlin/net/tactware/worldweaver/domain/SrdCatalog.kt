package net.tactware.worldweaver.domain

import java.time.Instant

internal data class SrdCatalog(
    val formatVersion: Int,
    val sourceLabel: String,
    val importedAt: Instant,
    val races: List<String>,
    val classes: List<SrdClassEntry>,
    val spells: List<SrdSpellEntry>,
    val monsters: List<SrdMonsterEntry>,
) {
    companion object {
        const val FORMAT_VERSION = 1
    }
}
