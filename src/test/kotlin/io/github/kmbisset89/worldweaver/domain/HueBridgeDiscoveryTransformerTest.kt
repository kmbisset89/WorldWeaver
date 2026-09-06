package io.github.kmbisset89.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals

internal class HueBridgeDiscoveryTransformerTest {
    private val transformer = HueBridgeDiscoveryTransformer()

    @Test
    fun mapsDiscoveryEntries() {
        val bridges = transformer.transform(
            """[{"id":"001788fffe","internalipaddress":"192.168.1.40"}]""",
        )
        assertEquals(listOf(HueBridge(bridgeId = "001788fffe", ipAddress = "192.168.1.40")), bridges)
    }

    @Test
    fun skipsEntriesWithoutIp() {
        assertEquals(emptyList(), transformer.transform("""[{"id":"only-id"}]"""))
        assertEquals(emptyList(), transformer.transform("not-json"))
    }
}
