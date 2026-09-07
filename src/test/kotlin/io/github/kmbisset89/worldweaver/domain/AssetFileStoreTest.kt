package io.github.kmbisset89.worldweaver.domain

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class AssetFileStoreTest {
    @Test
    fun writeReadAndDeleteRoundTrip() {
        val store = AssetFileStore(Files.createTempDirectory("ww-assets").toFile())
        val bytes = byteArrayOf(1, 2, 3, 4)

        store.write("asset-1", "sketch.png", bytes)

        assertContentEquals(bytes, store.read("asset-1", "sketch.png"))
        assertTrue(store.pathIfPresent("asset-1", "sketch.png")!!.endsWith("asset-1/sketch.png"))

        store.delete("asset-1")

        assertNull(store.read("asset-1", "sketch.png"))
        assertNull(store.pathIfPresent("asset-1", "sketch.png"))
    }
}
