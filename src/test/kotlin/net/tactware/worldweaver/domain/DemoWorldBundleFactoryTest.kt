package net.tactware.worldweaver.domain

import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class DemoWorldBundleFactoryTest {
    @Test
    fun demoBundleTellsALooseStoryAndWritesTheFixture() {
        val source = DemoWorldBundleFactory().create()
        val dest = File("fixtures", "demo-campaign.wwbundle")
        WorldBundleArchiveConverter().write(source, dest)

        val read = assertIs<WorldBundleArchiveConverter.ReadResult.Ready>(
            WorldBundleArchiveConverter().read(dest),
        )
        val bundle = read.bundle

        assertEquals("The Shattered Expanse", bundle.world.name)
        assertEquals("TR", bundle.calendar?.eraSuffix)
        assertEquals(12, bundle.calendar?.months?.size)
        assertTrue(bundle.sessions.all { it.inWorldDate != null })
        assertEquals(listOf("Salt and Silence"), bundle.campaigns.map { it.name })
        assertEquals(
            listOf(
                "Session 1: Fog on the Landing",
                "Session 2: Lanterns After Midnight",
                "Session 3: What the Tide Keeps",
                "Session 4: The Vault Door",
                "Session 5: A Name in the Manor",
            ),
            bundle.sessions.map { it.name },
        )
        assertTrue(bundle.sessions.all { session -> session.notes.contains("if", ignoreCase = true) })
        assertEquals(1, bundle.quests.count { it.status == QuestStatus.Completed })
        assertTrue(bundle.quests.any { it.title == "The Courier's Satchel" })
        assertEquals(
            listOf("Harbor Landing", "The Salted Lantern", "Tide Shrine Crypt", "Old Salt Vaults"),
            bundle.battleMaps.map { it.name },
        )
        assertEquals(listOf("Flooded channels"), bundle.battleMapSituations.map { it.name })
        assertEquals(bundle.worldPeople.size, bundle.avatarFiles.count { it.ref is PersonRef.World })
        assertEquals(bundle.campaignPeople.size, bundle.avatarFiles.count { it.ref is PersonRef.Campaign })
        assertTrue(bundle.avatarFiles.all { file -> isPngAtLeast(file.png, 64) })
        assertTrue(bundle.mapFiles.any { it.relativePath == "original.png" && isPngAtLeast(it.bytes, 256) })
        assertTrue(bundle.mapFiles.any { it.relativePath.startsWith("situations/") })
        assertTrue(bundle.encounters.all { encounter -> encounter.participants.any { it.gridColumn != null } })
        assertTrue(dest.isFile)
        assertTrue(dest.length() > 100_000)
    }

    private fun isPngAtLeast(bytes: ByteArray, minEdge: Int): Boolean {
        val image = ImageIO.read(bytes.inputStream()) ?: return false
        return image.width >= minEdge && image.height >= minEdge
    }
}
