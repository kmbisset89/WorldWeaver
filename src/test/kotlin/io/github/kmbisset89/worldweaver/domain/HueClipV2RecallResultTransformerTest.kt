package io.github.kmbisset89.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class HueClipV2RecallResultTransformerTest {
    private val transformer = HueClipV2RecallResultTransformer()

    @Test
    fun readsSuccessfulRecall() {
        val result = transformer.transform(
            statusCode = 200,
            jsonText = """{"errors":[],"data":[{"rid":"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa","rtype":"scene"}]}""",
        )
        assertIs<HueClipV2RecallResultTransformer.Result.Success>(result)
    }

    @Test
    fun readsUnauthorizedStatus() {
        val result = transformer.transform(
            statusCode = 403,
            jsonText = """{"errors":[{"description":"unauthorized"}],"data":[]}""",
        )
        assertIs<HueClipV2RecallResultTransformer.Result.Unauthorized>(result)
    }

    @Test
    fun readsFailedRecall() {
        val result = transformer.transform(
            statusCode = 400,
            jsonText = """{"errors":[{"description":"scene cannot be recalled"}],"data":[]}""",
        )
        val failed = assertIs<HueClipV2RecallResultTransformer.Result.Failed>(result)
        assertEquals("scene cannot be recalled", failed.message)
    }
}
