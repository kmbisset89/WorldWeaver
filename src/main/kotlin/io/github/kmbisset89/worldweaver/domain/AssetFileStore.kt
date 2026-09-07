package io.github.kmbisset89.worldweaver.domain

import java.io.File

internal class AssetFileStore(
    private val assetsRoot: File,
) {
    fun write(assetId: String, originalFileName: String, bytes: ByteArray) {
        val dir = assetDirectory(assetId)
        if (dir.exists()) {
            dir.deleteRecursively()
        }
        dir.mkdirs()
        File(dir, storedFileName(originalFileName)).writeBytes(bytes)
    }

    fun read(assetId: String, originalFileName: String): ByteArray? {
        val file = storedFile(assetId, originalFileName)
        if (!file.isFile) {
            return null
        }
        return file.readBytes()
    }

    fun pathIfPresent(assetId: String, originalFileName: String): String? {
        val file = storedFile(assetId, originalFileName)
        if (!file.isFile) {
            return null
        }
        return file.absolutePath
    }

    fun delete(assetId: String) {
        val dir = assetDirectory(assetId)
        if (dir.exists()) {
            dir.deleteRecursively()
        }
    }

    private fun storedFile(assetId: String, originalFileName: String): File {
        return File(assetDirectory(assetId), storedFileName(originalFileName))
    }

    private fun assetDirectory(assetId: String): File {
        return File(assetsRoot, assetId)
    }

    private fun storedFileName(originalFileName: String): String {
        val name = File(originalFileName).name
        if (name.isBlank() || name == "." || name == "..") {
            return "file"
        }
        return name
    }
}
