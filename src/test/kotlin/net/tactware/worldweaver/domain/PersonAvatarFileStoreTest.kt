package net.tactware.worldweaver.domain

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class PersonAvatarFileStoreTest {
    @Test
    fun writeReadAndDeleteRoundTrip() {
        val store = PersonAvatarFileStore(Files.createTempDirectory("ww-avatars").toFile())
        val ref = PersonRef.World("person-1")
        val bytes = byteArrayOf(1, 2, 3, 4)

        store.write(ref, bytes)

        assertContentEquals(bytes, store.read(ref))
        assertTrue(store.pathIfPresent(ref)!!.endsWith("world/person-1.png"))

        store.delete(ref)

        assertNull(store.read(ref))
        assertNull(store.pathIfPresent(ref))
    }

    @Test
    fun copyWritesACampaignAvatar() {
        val store = PersonAvatarFileStore(Files.createTempDirectory("ww-avatars").toFile())
        val world = PersonRef.World("world-1")
        val campaign = PersonRef.Campaign("campaign-1")
        store.write(world, byteArrayOf(9, 8, 7))

        store.copy(world, campaign)

        assertContentEquals(byteArrayOf(9, 8, 7), store.read(campaign))
        assertTrue(store.pathIfPresent(campaign)!!.contains("/campaign/"))
    }
}
