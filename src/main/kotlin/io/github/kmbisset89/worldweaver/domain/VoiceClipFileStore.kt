package io.github.kmbisset89.worldweaver.domain

import java.io.File

internal class VoiceClipFileStore(
    private val voicesRoot: File,
) {
    fun write(ref: VoiceClipRef, wav: ByteArray) {
        val file = fileFor(ref)
        file.parentFile.mkdirs()
        file.writeBytes(wav)
    }

    fun read(ref: VoiceClipRef): ByteArray? {
        val file = fileFor(ref)
        if (!file.isFile) {
            return null
        }
        return file.readBytes()
    }

    fun pathIfPresent(ref: VoiceClipRef): String? {
        val file = fileFor(ref)
        return if (file.isFile) file.absolutePath else null
    }

    fun copy(from: VoiceClipRef, to: VoiceClipRef) {
        val bytes = read(from) ?: return
        write(to, bytes)
    }

    fun delete(ref: VoiceClipRef) {
        val file = fileFor(ref)
        if (file.isFile) {
            file.delete()
        }
    }

    private fun fileFor(ref: VoiceClipRef): File {
        val folder = when (ref) {
            is VoiceClipRef.WorldPerson -> "world-person"
            is VoiceClipRef.CampaignPerson -> "campaign-person"
            is VoiceClipRef.Location -> "location"
        }
        return File(File(voicesRoot, folder), "${ref.id}.wav")
    }
}
