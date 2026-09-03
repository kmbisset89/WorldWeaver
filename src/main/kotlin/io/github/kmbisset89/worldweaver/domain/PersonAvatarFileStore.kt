package io.github.kmbisset89.worldweaver.domain

import java.io.File

internal class PersonAvatarFileStore(
    private val avatarsRoot: File,
) {
    fun write(ref: PersonRef, png: ByteArray) {
        val file = fileFor(ref)
        file.parentFile.mkdirs()
        file.writeBytes(png)
    }

    fun read(ref: PersonRef): ByteArray? {
        val file = fileFor(ref)
        if (!file.isFile) {
            return null
        }
        return file.readBytes()
    }

    fun pathIfPresent(ref: PersonRef): String? {
        val file = fileFor(ref)
        return if (file.isFile) file.absolutePath else null
    }

    fun copy(from: PersonRef, to: PersonRef) {
        val bytes = read(from) ?: return
        write(to, bytes)
    }

    fun delete(ref: PersonRef) {
        val file = fileFor(ref)
        if (file.isFile) {
            file.delete()
        }
    }

    private fun fileFor(ref: PersonRef): File {
        val folder = when (ref) {
            is PersonRef.World -> "world"
            is PersonRef.Campaign -> "campaign"
        }
        return File(File(avatarsRoot, folder), "${ref.id}.png")
    }
}
