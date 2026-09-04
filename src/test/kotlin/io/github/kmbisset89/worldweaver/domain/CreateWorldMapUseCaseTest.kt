package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class CreateWorldMapUseCaseTest {
    @Test
    fun createRequiresActiveWorld() = runTest {
        val harness = Harness()

        val result = harness.createWorldMap(harness.draft())

        assertIs<CreateWorldMapUseCase.Result.NoActiveWorld>(result)
        assertTrue(harness.worldMaps.all().isEmpty())
    }

    @Test
    fun createRejectsUnreadableImage() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")

        val result = harness.createWorldMap(harness.draft(imagePng = byteArrayOf(1, 2, 3)))

        assertIs<CreateWorldMapUseCase.Result.InvalidImage>(result)
    }

    @Test
    fun createRejectsUnknownLocation() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")

        val result = harness.createWorldMap(harness.draft(locationId = "missing"))

        assertIs<CreateWorldMapUseCase.Result.LocationNotFound>(result)
    }

    @Test
    fun createPersistsWorldRootMapAndTiles() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")

        val result = harness.createWorldMap(
            harness.draft(imagePng = BattleMapPngFixture.pngBytes(512, 512)),
        )

        val created = assertIs<CreateWorldMapUseCase.Result.Created>(result)
        assertEquals("world-1", created.worldMap.worldId)
        assertEquals(null, created.worldMap.locationId)
        assertEquals(512, created.worldMap.originalWidth)
        assertEquals(1, harness.worldMaps.all().size)
        val tileDir = harness.mapsRoot.resolve(created.worldMap.id).resolve("tiles/0")
        assertTrue(tileDir.toFile().isDirectory)
        assertTrue(tileDir.toFile().listFiles()?.isNotEmpty() == true)
    }

    @Test
    fun replaceOverwritesExistingLocationMap() = runTest {
        val harness = Harness()
        harness.context.setActiveWorldId("world-1")
        val location = harness.insertContinent()
        val first = harness.createWorldMap(
            harness.draft(
                locationId = location.id,
                imagePng = BattleMapPngFixture.pngBytes(256, 256),
            )
        )
        val created = assertIs<CreateWorldMapUseCase.Result.Created>(first)

        val second = harness.createWorldMap(
            harness.draft(
                locationId = location.id,
                imagePng = BattleMapPngFixture.pngBytes(512, 512),
            )
        )
        val replaced = assertIs<CreateWorldMapUseCase.Result.Created>(second)
        assertEquals(created.worldMap.id, replaced.worldMap.id)
        assertEquals(512, replaced.worldMap.originalWidth)
        assertEquals(1, harness.worldMaps.all().size)
    }

    private class Harness {
        val mapsRoot = Files.createTempDirectory("ww-world-maps")
        val worldMaps = FakeWorldMapRepository()
        val locations = FakeLocationRepository()
        val fileStore = WorldMapFileStore(mapsRoot.toFile())
        val context = FakeActiveContextRepository()
        private val instant = InstantProvider { Instant.parse("2026-09-04T12:00:00Z") }
        private val ids = EntityIdFactory { "world-map-1" }
        val createWorldMap = CreateWorldMapUseCase(
            worldMaps,
            locations,
            fileStore,
            MapTilePyramidFactory(),
            context,
            ids,
            instant,
        )

        suspend fun insertContinent(): Location {
            val now = Instant.parse("2026-09-04T12:00:00Z")
            val location = Location(
                id = "loc-1",
                worldId = "world-1",
                type = LocationType.Continent,
                parentLocationId = null,
                name = "Faerun",
                description = "",
                climate = "",
                terrain = "",
                government = "",
                landmarks = emptyList(),
                history = "",
                notes = "",
                createdAt = now,
                updatedAt = now,
            )
            locations.insert(location)
            return location
        }

        fun draft(
            locationId: String? = null,
            imagePng: ByteArray = BattleMapPngFixture.pngBytes(64, 64),
        ): WorldMapDraft {
            return WorldMapDraft(locationId = locationId, imagePng = imagePng)
        }
    }
}
