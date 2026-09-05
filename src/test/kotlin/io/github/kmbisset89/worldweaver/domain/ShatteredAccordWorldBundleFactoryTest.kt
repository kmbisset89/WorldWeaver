package io.github.kmbisset89.worldweaver.domain

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class ShatteredAccordWorldBundleFactoryTest {
    @Test
    fun shatteredAccordBundleMapsTheBibleAndWritesTheFixture() {
        val source = ShatteredAccordWorldBundleFactory().create()
        val dest = File("fixtures", "shattered-accord.wwbundle")
        WorldBundleArchiveConverter().write(source, dest)

        val read = assertIs<WorldBundleArchiveConverter.ReadResult.Ready>(
            WorldBundleArchiveConverter().read(dest),
        )
        val bundle = read.bundle

        assertEquals("The Shattered Accord", bundle.world.name)
        assertEquals(GameSystem.FifthEdition, bundle.world.defaultGameSystem)
        assertEquals("AA", bundle.calendar?.eraSuffix)
        assertEquals(12, bundle.calendar?.months?.size)
        assertEquals(listOf("Aontachd Vigil", "Day the Harmony Broke"), bundle.observances.map { it.name })
        assertEquals(listOf("The Shattered Accord"), bundle.campaigns.map { it.name })
        assertEquals(5, bundle.locations.count { it.type == LocationType.Area })
        assertEquals(8, bundle.factions.size)
        assertTrue(bundle.factions.any { it.name == "Veil of Thorns" })
        assertEquals(
            listOf(
                "Caelum Ironfist",
                "Aeliana Shadowglen",
                "Fianna Blazeheart",
                "Seraphina Songweaver",
                "Kaelan Windwalker",
                "Liora Shadowgleam",
                "Eirlys Ironfist",
            ),
            bundle.campaignPeople.filter { it.kind == PersonKind.PlayerCharacter }.map { it.name },
        )
        assertEquals(6, bundle.companions.size)
        assertTrue(bundle.companions.all { it.kind == CompanionKind.AnimalCompanion })
        val veil = bundle.loreEntries.first { it.title == "The Veil of Thorns" }
        assertTrue(veil.secrets.any { it.title.contains("GM secret", ignoreCase = true) })
        assertEquals(
            listOf(
                "Session 1: Caelum's Stolen Flame",
                "Session 2: The Ambush Road",
                "Session 3: Wildflowers in the Garden",
                "Session 4: The Heart Breaks",
                "Session 5: The Gathering Feels It",
            ),
            bundle.sessions.map { it.name },
        )
        assertTrue(bundle.sessions.all { it.inWorldDate != null })
        assertTrue(bundle.avatarFiles.isEmpty())
        assertTrue(bundle.mapFiles.isEmpty())
        assertTrue(bundle.encounters.isEmpty())
        assertTrue(bundle.battleMaps.isEmpty())
        assertTrue(dest.isFile)
        assertTrue(dest.length() > 1_000)
    }
}
