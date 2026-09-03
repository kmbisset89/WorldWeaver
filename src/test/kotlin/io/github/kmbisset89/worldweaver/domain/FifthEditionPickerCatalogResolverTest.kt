package io.github.kmbisset89.worldweaver.domain

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class FifthEditionPickerCatalogResolverTest {
    private val resolver = FifthEditionPickerCatalogResolver()

    @Test
    fun bundledFallbackWhenNothingImported() {
        val catalog = resolver.resolve(null)

        assertEquals(FifthEditionReference.races, catalog.races)
        assertEquals(FifthEditionReference.classes, catalog.classes)
        assertEquals(FifthEditionReference.spells, catalog.spells)
        assertTrue(catalog.monsters.isEmpty())
        assertEquals(
            FifthEditionReference.subclassesFor("Wizard"),
            catalog.subclassesFor("Wizard"),
        )
    }

    @Test
    fun importedNamesComeFirstAndUnionBundled() {
        val catalog = resolver.resolve(
            SrdCatalog(
                formatVersion = 1,
                sourceLabel = "Custom",
                importedAt = Instant.parse("2026-08-31T12:00:00Z"),
                races = listOf("Aasimar", "Human"),
                classes = listOf(
                    SrdClassEntry("Artificer", listOf("Alchemist")),
                    SrdClassEntry("Wizard", listOf("School of Necromancy")),
                ),
                spells = listOf(SrdSpellEntry("Eldritch Blast", 0)),
                monsters = listOf(
                    SrdMonsterEntry("Goblin", "humanoid", "1/4", 7, 15, 30),
                ),
            ),
        )

        assertEquals("Aasimar", catalog.races.first())
        assertTrue(catalog.races.contains("Human"))
        assertTrue(catalog.races.contains("Tiefling"))
        assertEquals("Artificer", catalog.classes.first())
        assertTrue(catalog.classes.contains("Wizard"))
        assertEquals(
            listOf("School of Necromancy") + FifthEditionReference.subclassesFor("Wizard"),
            catalog.subclassesFor("Wizard"),
        )
        assertEquals(listOf("Alchemist"), catalog.subclassesFor("Artificer"))
        assertEquals("Eldritch Blast", catalog.spells.first())
        assertTrue(catalog.spells.contains("Fireball"))
        assertEquals(0, catalog.spellLevelFor("Eldritch Blast"))
        assertEquals("Goblin", catalog.monsters.single().name)
    }
}
