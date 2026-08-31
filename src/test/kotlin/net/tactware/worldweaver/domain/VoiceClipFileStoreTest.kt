package net.tactware.worldweaver.domain

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class VoiceClipFileStoreTest {
    @Test
    fun writeReadAndDeleteRoundTrip() {
        val store = VoiceClipFileStore(Files.createTempDirectory("ww-voices").toFile())
        val ref = VoiceClipRef.WorldPerson("person-1")
        val bytes = VoiceClipWavFormat.wrapPcm(ByteArray(40))

        store.write(ref, bytes)

        assertContentEquals(bytes, store.read(ref))
        assertTrue(store.pathIfPresent(ref)!!.endsWith("world-person/person-1.wav"))

        store.delete(ref)

        assertNull(store.read(ref))
        assertNull(store.pathIfPresent(ref))
    }

    @Test
    fun copyWritesACampaignClip() {
        val store = VoiceClipFileStore(Files.createTempDirectory("ww-voices").toFile())
        val world = VoiceClipRef.WorldPerson("world-1")
        val campaign = VoiceClipRef.CampaignPerson("campaign-1")
        val bytes = VoiceClipWavFormat.wrapPcm(ByteArray(16))
        store.write(world, bytes)

        store.copy(world, campaign)

        assertContentEquals(bytes, store.read(campaign))
        assertTrue(store.pathIfPresent(campaign)!!.contains("/campaign-person/"))
    }
}
