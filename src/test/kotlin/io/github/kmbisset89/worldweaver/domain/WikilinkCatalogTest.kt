package io.github.kmbisset89.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class WikilinkCatalogTest {
    private val inn = WikilinkTarget(
        kind = WikilinkTargetKind.Location,
        id = "loc-1",
        title = "Blackbriar Inn",
        worldId = "world-1",
        campaignId = null,
    )
    private val lore = WikilinkTarget(
        kind = WikilinkTargetKind.Lore,
        id = "lore-1",
        title = "The Sundering",
        worldId = "world-1",
        campaignId = null,
    )
    private val catalog = WikilinkCatalog(listOf(inn, lore))
    private val parser = WikilinkTextParser()

    @Test
    fun resolvePrefersKindAndId() {
        val link = parser.parse("[[lore:lore-1|Old name]]").single() as WikilinkSegment.Link
        assertEquals(lore, catalog.resolve(link))
    }

    @Test
    fun resolveMatchesUniqueTitle() {
        val link = parser.parse("[[Blackbriar Inn]]").single() as WikilinkSegment.Link
        assertEquals(inn, catalog.resolve(link))
    }

    @Test
    fun resolveReturnsNullWhenUnknown() {
        val link = parser.parse("[[Missing]]").single() as WikilinkSegment.Link
        assertNull(catalog.resolve(link))
    }

    @Test
    fun suggestRanksPrefixMatchesFirst() {
        val suggestions = catalog.suggest("sund")
        assertEquals(listOf(lore), suggestions)
    }
}
