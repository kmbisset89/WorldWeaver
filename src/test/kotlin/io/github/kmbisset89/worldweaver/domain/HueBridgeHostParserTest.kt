package io.github.kmbisset89.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class HueBridgeHostParserTest {
    private val parser = HueBridgeHostParser()

    @Test
    fun acceptsLanIp() {
        val valid = assertIs<HueBridgeHostParser.Result.Valid>(parser.parse("192.168.1.40"))
        assertEquals("192.168.1.40", valid.host)
    }

    @Test
    fun stripsSchemePortAndPath() {
        val valid = assertIs<HueBridgeHostParser.Result.Valid>(
            parser.parse(" http://192.168.1.40:80/api "),
        )
        assertEquals("192.168.1.40", valid.host)
    }

    @Test
    fun rejectsBlank() {
        assertIs<HueBridgeHostParser.Result.Blank>(parser.parse("  "))
    }

    @Test
    fun rejectsSpaces() {
        assertIs<HueBridgeHostParser.Result.Invalid>(parser.parse("192.168.1. 40"))
    }
}
