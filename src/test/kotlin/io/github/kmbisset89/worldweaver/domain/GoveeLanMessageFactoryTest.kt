package io.github.kmbisset89.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals

internal class GoveeLanMessageFactoryTest {
    private val factory = GoveeLanMessageFactory()

    @Test
    fun buildsScanTurnBrightnessAndColorPayloads() {
        assertEquals(
            """{"msg":{"cmd":"scan","data":{"account_topic":"reserve"}}}""",
            factory.scan(),
        )
        assertEquals("""{"msg":{"cmd":"turn","data":{"value":1}}}""", factory.turn(true))
        assertEquals("""{"msg":{"cmd":"turn","data":{"value":0}}}""", factory.turn(false))
        assertEquals("""{"msg":{"cmd":"brightness","data":{"value":80}}}""", factory.brightness(80))
        assertEquals(
            """{"msg":{"cmd":"colorwc","data":{"color":{"r":196,"g":30,"b":58},"colorTemInKelvin":0}}}""",
            factory.color(196, 30, 58),
        )
    }
}
