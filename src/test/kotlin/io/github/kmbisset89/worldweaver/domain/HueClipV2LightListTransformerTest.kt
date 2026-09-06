package io.github.kmbisset89.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals

internal class HueClipV2LightListTransformerTest {
    private val transformer = HueClipV2LightListTransformer()

    @Test
    fun mapsNamedLightsAndSorts() {
        val lights = transformer.transform(
            """
            {
              "errors": [],
              "data": [
                {
                  "id": "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
                  "metadata": {"name": "Table lamp"}
                },
                {
                  "id": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                  "metadata": {"name": "Kitchen"}
                }
              ]
            }
            """.trimIndent(),
        )
        assertEquals(
            listOf(
                HueLight(id = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", name = "Kitchen"),
                HueLight(id = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb", name = "Table lamp"),
            ),
            lights,
        )
    }
}
