package io.github.kmbisset89.worldweaver.domain

import java.time.Instant

internal data class Asset(
    val id: String,
    val worldId: String,
    val displayName: String,
    val originalFileName: String,
    val notes: String,
    val byteSize: Long,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    val isImage: Boolean
        get() = IMAGE_EXTENSIONS.contains(fileExtension().lowercase())

    fun fileExtension(): String {
        val dot = originalFileName.lastIndexOf('.')
        if (dot < 0 || dot == originalFileName.lastIndex) {
            return ""
        }
        return originalFileName.substring(dot + 1)
    }

    private companion object {
        val IMAGE_EXTENSIONS = setOf("png", "jpg", "jpeg", "gif", "webp", "bmp")
    }
}
