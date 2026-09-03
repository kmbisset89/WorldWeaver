package net.tactware.worldweaver.domain

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class ImportSrdCatalogUseCaseTest {
    @Test
    fun importBundledWritesCatalog() = runTest {
        val harness = Harness()

        val result = harness.importSrd(ImportSrdCatalogUseCase.Source.Bundled)

        val imported = assertIs<ImportSrdCatalogUseCase.Result.Imported>(result)
        assertEquals("5E SRD 5.1", imported.catalog.sourceLabel)
        assertTrue(imported.catalog.races.isNotEmpty())
        assertTrue(imported.catalog.classes.isNotEmpty())
        assertTrue(imported.catalog.spells.isNotEmpty())
        assertTrue(imported.catalog.monsters.isNotEmpty())
        assertEquals(imported.catalog, harness.catalogs.get())
    }

    @Test
    fun importFileWritesSanitizedCatalog() = runTest {
        val harness = Harness()
        val file = Files.createTempFile("ww-srd", ".json").toFile()
        try {
            file.writeText(
                """
                {
                  "formatVersion": 1,
                  "sourceLabel": " Custom SRD ",
                  "races": ["  Human  ", "human", ""],
                  "classes": [{ "name": " Wizard ", "subclasses": [" School of Evocation ", ""] }],
                  "spells": [{ "name": " Fireball ", "level": 3 }],
                  "monsters": [{
                    "name": " Goblin ",
                    "creatureType": " humanoid ",
                    "challengeRating": "1/4",
                    "hitPoints": 7,
                    "armorClass": 15,
                    "walkSpeed": 30
                  }]
                }
                """.trimIndent(),
            )

            val result = harness.importSrd(ImportSrdCatalogUseCase.Source.File(file))

            val imported = assertIs<ImportSrdCatalogUseCase.Result.Imported>(result)
            assertEquals("Custom SRD", imported.catalog.sourceLabel)
            assertEquals(listOf("Human"), imported.catalog.races)
            assertEquals("Wizard", imported.catalog.classes.single().name)
            assertEquals(listOf("School of Evocation"), imported.catalog.classes.single().subclasses)
            assertEquals("Fireball", imported.catalog.spells.single().name)
            assertEquals("Goblin", imported.catalog.monsters.single().name)
        } finally {
            file.delete()
        }
    }

    @Test
    fun importRejectsInvalidJson() = runTest {
        val harness = Harness()
        val file = Files.createTempFile("ww-srd-bad", ".json").toFile()
        try {
            file.writeText("{ not-json")

            val result = harness.importSrd(ImportSrdCatalogUseCase.Source.File(file))

            assertIs<ImportSrdCatalogUseCase.Result.InvalidFile>(result)
            assertEquals(null, harness.catalogs.get())
        } finally {
            file.delete()
        }
    }

    @Test
    fun importRejectsWrongFormatVersion() = runTest {
        val harness = Harness()
        val file = Files.createTempFile("ww-srd-ver", ".json").toFile()
        try {
            file.writeText(
                """{ "formatVersion": 2, "sourceLabel": "Future", "races": ["Human"] }""",
            )

            val result = harness.importSrd(ImportSrdCatalogUseCase.Source.File(file))

            assertIs<ImportSrdCatalogUseCase.Result.InvalidFile>(result)
        } finally {
            file.delete()
        }
    }

    private class Harness {
        val catalogs = FakeSrdCatalogRepository()
        private val converter = SrdCatalogJsonConverter()
        val importSrd = ImportSrdCatalogUseCase(
            catalogRepository = catalogs,
            bundledLoader = BundledSrdCatalogLoader(converter),
            converter = converter,
            instantProvider = InstantProvider { Instant.parse("2026-08-31T12:00:00Z") },
        )
    }
}
