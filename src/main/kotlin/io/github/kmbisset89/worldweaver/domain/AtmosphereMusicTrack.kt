package io.github.kmbisset89.worldweaver.domain

import kotlinx.serialization.Serializable
import java.io.File

/**
 * A machine-local music file linked for table ambience.
 *
 * [path] is an absolute path on this computer. The file is not copied into World Weaver data.
 */
@Serializable
internal data class AtmosphereMusicTrack(
    val id: String,
    val displayName: String,
    val path: String,
    val sortOrder: Int,
) {
    fun fileIsPresent(): Boolean = File(path).isFile

    companion object {
        val SUPPORTED_EXTENSIONS = setOf(
            "mp3",
            "wav",
            "ogg",
            "flac",
            "m4a",
            "aac",
            "aiff",
            "aif",
        )

        fun extensionOf(path: String): String {
            val name = File(path).name
            val dot = name.lastIndexOf('.')
            if (dot < 0 || dot == name.lastIndex) {
                return ""
            }
            return name.substring(dot + 1).lowercase()
        }

        fun isSupported(path: String): Boolean {
            return SUPPORTED_EXTENSIONS.contains(extensionOf(path))
        }

        fun displayNameFrom(path: String): String {
            val name = File(path).name
            val dot = name.lastIndexOf('.')
            val stem = if (dot > 0) name.substring(0, dot) else name
            return stem.ifBlank { name }
        }
    }
}
