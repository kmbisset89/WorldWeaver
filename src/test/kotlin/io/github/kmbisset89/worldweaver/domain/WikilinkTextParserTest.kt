package io.github.kmbisset89.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class WikilinkTextParserTest {
    private val parser = WikilinkTextParser()

    @Test
    fun parseSplitsPlainTextAndLinks() {
        val segments = parser.parse("Visit [[Blackbriar Inn]] tonight.")
        assertEquals(3, segments.size)
        assertIs<WikilinkSegment.Text>(segments[0])
        assertEquals("Visit ", (segments[0] as WikilinkSegment.Text).value)
        val link = assertIs<WikilinkSegment.Link>(segments[1])
        assertEquals("Blackbriar Inn", link.lookup)
        assertNull(link.alias)
        assertEquals(" tonight.", (segments[2] as WikilinkSegment.Text).value)
    }

    @Test
    fun parseReadsKindIdAndAlias() {
        val link = assertIs<WikilinkSegment.Link>(
            parser.parse("[[lore:abc-1|The Sundering]]").single(),
        )
        assertEquals("lore", link.kindPrefix)
        assertEquals("abc-1", link.targetId)
        assertEquals("The Sundering", link.alias)
        assertEquals("The Sundering", link.displayText)
    }

    @Test
    fun incompleteQueryReturnsTextAfterLastOpenToken() {
        assertEquals("Bri", parser.incompleteQuery("Talk to [[Bri"))
        assertNull(parser.incompleteQuery("Talk to [[Briar]] then"))
        assertNull(parser.incompleteQuery("No links here"))
    }
}
