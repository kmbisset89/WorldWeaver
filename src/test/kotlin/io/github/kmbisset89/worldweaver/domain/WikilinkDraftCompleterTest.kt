package io.github.kmbisset89.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals

internal class WikilinkDraftCompleterTest {
    private val completer = WikilinkDraftCompleter()
    private val target = WikilinkTarget(
        kind = WikilinkTargetKind.Location,
        id = "loc-1",
        title = "Blackbriar Inn",
        worldId = "world-1",
        campaignId = null,
    )

    @Test
    fun completeReplacesTheOpenToken() {
        val completed = completer.complete("Meet at [[Bla", target)
        assertEquals("Meet at [[location:loc-1|Blackbriar Inn]]", completed)
    }

    @Test
    fun completeAppendsWhenNoOpenTokenExists() {
        val completed = completer.complete("Meet at ", target)
        assertEquals("Meet at [[location:loc-1|Blackbriar Inn]]", completed)
    }
}
