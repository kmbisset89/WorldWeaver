package net.tactware.worldweaver.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class BattleMapImagePromptFactoryTest {
    private val factory = BattleMapImagePromptFactory()

    @Test
    fun createIncludesNameGridAndScenery() {
        val prompt = factory.create(
            name = "Salt Vaults",
            columns = 20,
            rows = 20,
            unitName = "ft",
            unitsPerTile = 5.0,
            scenery = "flooded limestone crypt, collapsed columns, shallow water, raised walkways",
        )

        assertEquals(
            "Create a top-down tabletop RPG battle map for \"Salt Vaults\". " +
                "Square grid, 20 by 20 tiles, 5 ft per square. " +
                "Orthographic overhead view, even lighting, no characters, tokens, compass, UI, or text. " +
                "Scene: flooded limestone crypt, collapsed columns, shallow water, raised walkways. " +
                "Distinct floors, walls, and obstacles that read clearly at tabletop scale.",
            prompt,
        )
    }

    @Test
    fun createOmitsNameWhenBlank() {
        val prompt = factory.create(
            name = "  ",
            columns = 16,
            rows = 12,
            unitName = "ft",
            unitsPerTile = 5.0,
            scenery = "harbor docks at dusk",
        )

        assertTrue(prompt.startsWith("Create a top-down tabletop RPG battle map. "))
        assertFalse(prompt.contains(" for \""))
        assertTrue(prompt.contains("Square grid, 16 by 12 tiles, 5 ft per square."))
        assertTrue(prompt.contains("Scene: harbor docks at dusk."))
    }

    @Test
    fun createUsesGenericSceneryWhenBlank() {
        val prompt = factory.create(
            name = "Harbor Landing",
            columns = 20,
            rows = 20,
            unitName = "ft",
            unitsPerTile = 5.0,
            scenery = "   ",
        )

        assertTrue(
            prompt.contains(
                "Scene: varied terrain with clear walkable space, cover, and landmarks.",
            ),
        )
    }

    @Test
    fun createFallsBackToSquareGridWhenDimensionsAreMissing() {
        val prompt = factory.create(
            name = "Cave",
            columns = null,
            rows = 20,
            unitName = "ft",
            unitsPerTile = 5.0,
            scenery = "dripping cavern",
        )

        assertTrue(prompt.contains("Square grid."))
        assertFalse(prompt.contains("tiles"))
        assertTrue(prompt.contains("Scene: dripping cavern."))
    }

    @Test
    fun createFormatsFractionalUnitsAndOmitsBlankUnitName() {
        val prompt = factory.create(
            name = "Mire",
            columns = 10,
            rows = 8,
            unitName = "",
            unitsPerTile = 2.5,
            scenery = "sunken boardwalk",
        )

        assertTrue(prompt.contains("Square grid, 10 by 8 tiles, 2.5 units per square."))
    }
}
