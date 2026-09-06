package io.github.kmbisset89.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class HomeAssistantSceneListTransformerTest {
    private val transformer = HomeAssistantSceneListTransformer()

    @Test
    fun keepsSceneEntitiesAndFriendlyNames() {
        val json = """
            [
              {"entity_id":"light.kitchen","attributes":{"friendly_name":"Kitchen"}},
              {"entity_id":"scene.combat","attributes":{"friendly_name":"Combat"}},
              {"entity_id":"scene.tavern","attributes":{"friendly_name":"Tavern"}}
            ]
        """.trimIndent()

        val scenes = transformer.transform(json)

        assertEquals(
            listOf(
                HomeAssistantScene("scene.combat", "Combat"),
                HomeAssistantScene("scene.tavern", "Tavern"),
            ),
            scenes,
        )
    }

    @Test
    fun fallsBackToEntityObjectIdWhenFriendlyNameMissing() {
        val json = """[{"entity_id":"scene.boss_fight","attributes":{}}]"""

        val scenes = transformer.transform(json)

        assertEquals(listOf(HomeAssistantScene("scene.boss_fight", "boss fight")), scenes)
    }

    @Test
    fun returnsEmptyListForNonArrayPayload() {
        assertTrue(transformer.transform("""{"message":"API running."}""").isEmpty())
    }
}
