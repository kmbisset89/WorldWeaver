package io.github.kmbisset89.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class HueClipV2SceneListTransformerTest {
    private val transformer = HueClipV2SceneListTransformer()

    @Test
    fun mapsNamedScenesAndSorts() {
        val scenes = transformer.transform(
            """
            {
              "errors": [],
              "data": [
                {
                  "id": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
                  "id_v1": "/scenes/zzz",
                  "metadata": {"name": "Combat"},
                  "group": {"rid": "room-1", "rtype": "room"}
                },
                {
                  "id": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                  "id_v1": "/scenes/aaa",
                  "metadata": {"name": "Tavern"},
                  "group": {"rid": "room-2", "rtype": "room"}
                }
              ]
            }
            """.trimIndent(),
        )
        assertEquals(
            listOf(
                HueScene(
                    id = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
                    name = "Combat",
                    groupId = "room-1",
                ),
                HueScene(
                    id = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                    name = "Tavern",
                    groupId = "room-2",
                ),
            ),
            scenes,
        )
    }

    @Test
    fun resolvesLegacyV1SceneId() {
        val json = """
            {
              "data": [
                {
                  "id": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                  "id_v1": "/scenes/hue-combat",
                  "metadata": {"name": "Combat"}
                }
              ]
            }
        """.trimIndent()
        assertEquals(
            "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
            transformer.resolveId(json, "hue-combat"),
        )
        assertEquals(
            "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
            transformer.resolveId(json, "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
        )
        assertNull(transformer.resolveId(json, "missing"))
    }
}
