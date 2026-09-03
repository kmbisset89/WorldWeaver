package io.github.kmbisset89.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class SrdCatalogFileStoreTest {
    @Test
    fun writeObserveAndClearRoundTrip() = runTest {
        val temp = Files.createTempDirectory("ww-srd-store").toFile()
        try {
            val store = SrdCatalogFileStore(temp, SrdCatalogJsonConverter())
            val catalog = SrdCatalog(
                formatVersion = 1,
                sourceLabel = "5E SRD 5.1",
                importedAt = Instant.parse("2026-08-31T12:00:00Z"),
                races = listOf("Human"),
                classes = listOf(SrdClassEntry("Wizard", listOf("School of Evocation"))),
                spells = listOf(SrdSpellEntry("Fireball", 3)),
                monsters = listOf(SrdMonsterEntry("Goblin", "humanoid", "1/4", 7, 15, 30)),
            )

            store.write(catalog)

            assertEquals(catalog, store.get())
            assertTrue(java.io.File(temp, "5e.json").isFile)

            val reloaded = SrdCatalogFileStore(temp, SrdCatalogJsonConverter())
            assertEquals(catalog, reloaded.get())

            store.clear()
            assertNull(store.get())
            assertTrue(!java.io.File(temp, "5e.json").exists())
        } finally {
            temp.deleteRecursively()
        }
    }
}
