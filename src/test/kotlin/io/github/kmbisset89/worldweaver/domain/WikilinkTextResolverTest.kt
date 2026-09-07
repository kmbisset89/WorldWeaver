package io.github.kmbisset89.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class WikilinkTextResolverTest {
    private val resolver = WikilinkTextResolver()
    private val inn = WikilinkTarget(
        kind = WikilinkTargetKind.Location,
        id = "loc-1",
        title = "Blackbriar Inn",
        worldId = "world-1",
        campaignId = null,
    )
    private val catalog = WikilinkCatalog(listOf(inn))

    @Test
    fun resolveShowsAliasForKnownTargets() {
        val spans = resolver.resolve("See [[location:loc-1|the inn]].", catalog)
        assertEquals("See ", spans[0].text)
        assertEquals("the inn", spans[1].text)
        assertEquals(inn, spans[1].target)
        assertFalse(spans[1].unresolved)
        assertEquals(".", spans[2].text)
    }

    @Test
    fun resolveKeepsUnknownTokensVisible() {
        val spans = resolver.resolve("See [[Ghost Town]].", catalog)
        assertTrue(spans[1].unresolved)
        assertEquals("[[Ghost Town]]", spans[1].text)
    }
}
