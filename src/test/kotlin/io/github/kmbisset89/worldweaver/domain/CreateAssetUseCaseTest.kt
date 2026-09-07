package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class CreateAssetUseCaseTest {
    @Test
    fun createRequiresActiveWorld() = runTest {
        val harness = Harness()
        val source = Files.createTempFile("maybe", ".png").toFile()
        source.writeBytes(byteArrayOf(1, 2, 3))

        val result = harness.createAsset(source)

        assertIs<CreateAssetUseCase.Result.NoActiveWorld>(result)
        assertTrue(harness.assets.all().isEmpty())
    }

    @Test
    fun createRejectsEmptyFile() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")
        val source = Files.createTempFile("empty", ".txt").toFile()

        val result = harness.createAsset(source)

        assertIs<CreateAssetUseCase.Result.EmptyFile>(result)
    }

    @Test
    fun createStoresWorldOwnedFile() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")
        val dir = Files.createTempDirectory("maybe-src").toFile()
        val source = java.io.File(dir, "harbor-map.png")
        source.writeBytes(byteArrayOf(9, 8, 7, 6))

        val result = harness.createAsset(source)

        val created = assertIs<CreateAssetUseCase.Result.Created>(result)
        assertEquals("world-1", created.asset.worldId)
        assertEquals("harbor-map", created.asset.displayName)
        assertEquals(source.name, created.asset.originalFileName)
        assertEquals(4, created.asset.byteSize)
        assertEquals(byteArrayOf(9, 8, 7, 6).toList(), harness.fileStore.read(created.asset.id, created.asset.originalFileName)?.toList())
    }

    private class Harness {
        val assets = FakeAssetRepository()
        val context = FakeActiveContextRepository()
        val fileStore = AssetFileStore(Files.createTempDirectory("ww-assets").toFile())
        private val instant = InstantProvider { Instant.parse("2026-09-06T12:00:00Z") }
        private var nextId = 0
        private val ids = EntityIdFactory { "asset-${++nextId}" }
        val createAsset = CreateAssetUseCase(assets, fileStore, context, ids, instant)
    }
}
