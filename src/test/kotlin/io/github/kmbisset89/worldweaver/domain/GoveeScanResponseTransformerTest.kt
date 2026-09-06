package io.github.kmbisset89.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class GoveeScanResponseTransformerTest {
    private val transformer = GoveeScanResponseTransformer()

    @Test
    fun mapsScanDatagram() {
        val device = transformer.transform(
            """{"msg":{"cmd":"scan","data":{"ip":"192.168.1.50","device":"AA:BB:CC","sku":"H6072"}}}""",
        )
        assertEquals(GoveeDevice(deviceId = "AA:BB:CC", ipAddress = "192.168.1.50", sku = "H6072"), device)
    }

    @Test
    fun rejectsIncompletePayloads() {
        assertNull(transformer.transform("""{"msg":{"cmd":"scan","data":{"ip":"192.168.1.50"}}}"""))
        assertNull(transformer.transform("not-json"))
    }
}
