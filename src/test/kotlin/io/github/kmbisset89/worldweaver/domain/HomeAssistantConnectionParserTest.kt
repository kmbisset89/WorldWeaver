package io.github.kmbisset89.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class HomeAssistantConnectionParserTest {
    private val parser = HomeAssistantConnectionParser()

    @Test
    fun trimsAndStripsTrailingSlash() {
        val result = parser.parse(
            baseUrl = " http://homeassistant.local:8123/ ",
            token = " token-1 ",
        )
        val valid = assertIs<HomeAssistantConnectionParser.Result.Valid>(result)
        assertEquals("http://homeassistant.local:8123", valid.connection.baseUrl)
        assertEquals("token-1", valid.connection.token)
    }

    @Test
    fun acceptsHttps() {
        val result = parser.parse("https://ha.example:8123", "abc")
        val valid = assertIs<HomeAssistantConnectionParser.Result.Valid>(result)
        assertEquals("https://ha.example:8123", valid.connection.baseUrl)
    }

    @Test
    fun rejectsBlankUrl() {
        assertIs<HomeAssistantConnectionParser.Result.BlankUrl>(parser.parse("  ", "token"))
    }

    @Test
    fun rejectsBlankToken() {
        assertIs<HomeAssistantConnectionParser.Result.BlankToken>(
            parser.parse("http://homeassistant.local:8123", "  "),
        )
    }

    @Test
    fun rejectsMissingScheme() {
        assertIs<HomeAssistantConnectionParser.Result.InvalidUrl>(
            parser.parse("homeassistant.local:8123", "token"),
        )
    }

    @Test
    fun rejectsNonHttpScheme() {
        assertIs<HomeAssistantConnectionParser.Result.InvalidUrl>(
            parser.parse("ftp://homeassistant.local:8123", "token"),
        )
    }

    @Test
    fun rejectsHostlessUrl() {
        assertIs<HomeAssistantConnectionParser.Result.InvalidUrl>(parser.parse("http://", "token"))
    }
}
