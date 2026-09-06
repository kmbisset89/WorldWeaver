package io.github.kmbisset89.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class HueSceneListTransformerTest {
    private val transformer = HueSceneListTransformer()

    @Test
    fun mapsNamedScenesAndSorts() {
        val scenes = transformer.transform(
            """
            {
              "zzz": {"name": "Combat", "group": "1"},
              "aaa": {"name": "Tavern", "group": "2"}
            }
            """.trimIndent(),
        )
        assertEquals(
            listOf(
                HueScene(id = "zzz", name = "Combat", groupId = "1"),
                HueScene(id = "aaa", name = "Tavern", groupId = "2"),
            ),
            scenes,
        )
    }

    @Test
    fun treatsErrorArrayAsUnauthorizedPayload() {
        assertNull(transformer.transform("""[{"error":{"type":1,"description":"unauthorized"}}]"""))
    }
}
