package io.github.kmbisset89.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class HueJsonArrayResultTransformerTest {
    private val transformer = HueJsonArrayResultTransformer()

    @Test
    fun readsPairedUsername() {
        val result = transformer.transformPair("""[{"success":{"username":"hue-key"}}]""")
        val username = assertIs<HueJsonArrayResultTransformer.Result.Username>(result)
        assertEquals("hue-key", username.username)
    }

    @Test
    fun readsLinkButtonError() {
        val result = transformer.transformPair(
            """[{"error":{"type":101,"address":"","description":"link button not pressed"}}]""",
        )
        assertIs<HueJsonArrayResultTransformer.Result.LinkButtonNotPressed>(result)
    }

    @Test
    fun readsUnauthorizedAction() {
        val result = transformer.transformAction(
            """[{"error":{"type":1,"address":"/groups/0/action","description":"unauthorized user"}}]""",
        )
        assertIs<HueJsonArrayResultTransformer.Result.Unauthorized>(result)
        assertTrue(transformer.isErrorArray("""[{"error":{"type":1}}]"""))
    }

    @Test
    fun readsSuccessfulAction() {
        val result = transformer.transformAction("""[{"success":{"/groups/0/action/scene":"abc"}}]""")
        assertIs<HueJsonArrayResultTransformer.Result.Success>(result)
    }
}
