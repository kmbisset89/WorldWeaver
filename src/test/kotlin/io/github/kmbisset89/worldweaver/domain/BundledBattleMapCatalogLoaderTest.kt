package io.github.kmbisset89.worldweaver.domain

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class BundledBattleMapCatalogLoaderTest {
    @Test
    fun isAvailableWhenACatalogPngExists() {
        val root = Files.createTempDirectory("ww-bundled-maps")
        writePng(root, "06_greenwood_clearing.png")
        val loader = BundledBattleMapCatalogLoader(roots = listOf(root.toFile()))

        assertTrue(loader.isAvailable())
        assertEquals(root.toFile().canonicalFile, loader.resolvedRoot()?.canonicalFile)
    }

    @Test
    fun isUnavailableWhenFolderIsMissing() {
        val missing = Files.createTempDirectory("ww-bundled-maps").resolve("missing").toFile()
        val loader = BundledBattleMapCatalogLoader(roots = listOf(missing))

        assertFalse(loader.isAvailable())
        assertNull(loader.resolvedRoot())
    }

    @Test
    fun loadPngReturnsBytesForAKnownFile() {
        val root = Files.createTempDirectory("ww-bundled-maps")
        val expected = BattleMapPngFixture.pngBytes(32, 32)
        Files.write(root.resolve("06_greenwood_clearing.png"), expected)
        val loader = BundledBattleMapCatalogLoader(roots = listOf(root.toFile()))

        assertTrue(expected.contentEquals(loader.loadPng("06_greenwood_clearing.png")))
        assertNull(loader.loadPng("missing.png"))
    }

    @Test
    fun isAvailableWhenAMediumCatalogPngExists() {
        val root = Files.createTempDirectory("ww-bundled-medium-maps")
        writePng(root, "06_riverford_village.png")
        val loader = BundledBattleMapCatalogLoader(roots = listOf(root.toFile()))

        assertTrue(loader.isAvailable())
        assertTrue(BattleMapPngFixture.pngBytes(16, 16).contentEquals(loader.loadPng("06_riverford_village.png")))
    }

    private fun writePng(root: Path, fileName: String) {
        Files.write(root.resolve(fileName), BattleMapPngFixture.pngBytes(16, 16))
    }
}
