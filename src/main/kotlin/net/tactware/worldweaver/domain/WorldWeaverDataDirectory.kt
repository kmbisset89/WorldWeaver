package net.tactware.worldweaver.domain

import java.io.File

internal class WorldWeaverDataDirectory(
    val root: File = File(System.getProperty("user.home"), DIR_NAME),
) {
    val databaseFile: File
        get() = File(root, DATABASE_FILE_NAME)

    val avatarsDir: File
        get() = File(root, AVATARS_DIR_NAME)

    val mapsDir: File
        get() = File(root, MAPS_DIR_NAME)

    val voicesDir: File
        get() = File(root, VOICES_DIR_NAME)

    val srdDir: File
        get() = File(root, SRD_DIR_NAME)

    fun ensureExists() {
        root.mkdirs()
    }

    companion object {
        const val DIR_NAME = ".worldweaver"
        const val DATABASE_FILE_NAME = "ww.db"
        const val AVATARS_DIR_NAME = "avatars"
        const val MAPS_DIR_NAME = "maps"
        const val VOICES_DIR_NAME = "voices"
        const val SRD_DIR_NAME = "srd"
    }
}
