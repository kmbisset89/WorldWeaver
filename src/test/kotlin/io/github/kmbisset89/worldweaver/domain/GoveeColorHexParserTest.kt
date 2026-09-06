package io.github.kmbisset89.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class GoveeColorHexParserTest {
    private val parser = GoveeColorHexParser()

    @Test
    fun parsesHashPrefixedHex() {
        val rgb = parser.parse(" #C41E3A ")
        assertEquals(GoveeColorHexParser.Rgb(196, 30, 58), rgb)
    }

    @Test
    fun parsesBareHex() {
        val rgb = parser.parse("E39B5A")
        assertEquals(GoveeColorHexParser.Rgb(227, 155, 90), rgb)
    }

    @Test
    fun rejectsShortOrInvalidValues() {
        assertNull(parser.parse("#FFF"))
        assertNull(parser.parse("nothex"))
        assertNull(parser.parse(""))
    }
}
