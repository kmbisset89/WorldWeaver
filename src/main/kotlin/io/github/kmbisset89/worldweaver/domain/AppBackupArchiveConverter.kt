package io.github.kmbisset89.worldweaver.domain

import kotlinx.serialization.json.Json
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

internal class AppBackupArchiveConverter {
    sealed interface ReadResult {
        data class Ready(
            val manifest: AppBackupManifest,
            val prefs: AppBackupPrefs,
            val extractedDataDir: File,
        ) : ReadResult

        data object UnsupportedVersion : ReadResult
        data object InvalidArchive : ReadResult
    }

    fun write(
        destFile: File,
        manifest: AppBackupManifest,
        prefs: AppBackupPrefs,
        databaseFile: File,
        avatarsDir: File,
        mapsDir: File,
        worldMapsDir: File,
        voicesDir: File,
        srdDir: File,
    ) {
        destFile.parentFile?.mkdirs()
        ZipOutputStream(destFile.outputStream().buffered()).use { zip ->
            zip.writeJson(MANIFEST_ENTRY, json.encodeToString(AppBackupManifest.serializer(), manifest))
            zip.writeJson(PREFS_ENTRY, json.encodeToString(AppBackupPrefs.serializer(), prefs))
            if (databaseFile.isFile) {
                zip.writeFile(DATABASE_ENTRY, databaseFile)
            }
            zip.writeTree(AVATARS_PREFIX, avatarsDir)
            zip.writeTree(MAPS_PREFIX, mapsDir)
            zip.writeTree(WORLD_MAPS_PREFIX, worldMapsDir)
            zip.writeTree(VOICES_PREFIX, voicesDir)
            zip.writeTree(SRD_PREFIX, srdDir)
        }
    }

    fun read(sourceFile: File, extractTo: File): ReadResult {
        if (!sourceFile.isFile) {
            return ReadResult.InvalidArchive
        }
        return try {
            ZipFile(sourceFile).use { zip ->
                val manifestBytes = zip.readEntry(MANIFEST_ENTRY) ?: return ReadResult.InvalidArchive
                val prefsBytes = zip.readEntry(PREFS_ENTRY) ?: return ReadResult.InvalidArchive
                val manifest = json.decodeFromString(
                    AppBackupManifest.serializer(),
                    manifestBytes.decodeToString(),
                )
                if (manifest.formatVersion > AppBackupManifest.FORMAT_VERSION) {
                    return ReadResult.UnsupportedVersion
                }
                if (manifest.formatVersion != AppBackupManifest.FORMAT_VERSION) {
                    return ReadResult.InvalidArchive
                }
                if (manifest.dbSchemaVersion > AppBackupManifest.DB_SCHEMA_VERSION) {
                    return ReadResult.UnsupportedVersion
                }
                val prefs = json.decodeFromString(
                    AppBackupPrefs.serializer(),
                    prefsBytes.decodeToString(),
                )
                extractTo.mkdirs()
                var hasDatabase = false
                zip.entries().asSequence().forEach { entry ->
                    if (entry.isDirectory) {
                        return@forEach
                    }
                    when {
                        entry.name == MANIFEST_ENTRY || entry.name == PREFS_ENTRY -> Unit
                        entry.name == DATABASE_ENTRY -> {
                            val dest = File(extractTo, WorldWeaverDataDirectory.DATABASE_FILE_NAME)
                            writeExtractedFile(extractTo, dest, zip.readBytes(entry))
                            hasDatabase = true
                        }
                        entry.name.startsWith(AVATARS_PREFIX) ||
                            entry.name.startsWith(MAPS_PREFIX) ||
                            entry.name.startsWith(WORLD_MAPS_PREFIX) ||
                            entry.name.startsWith(VOICES_PREFIX) ||
                            entry.name.startsWith(SRD_PREFIX) -> {
                            val relative = entry.name.removePrefix(DATA_PREFIX)
                            val dest = File(extractTo, relative)
                            writeExtractedFile(extractTo, dest, zip.readBytes(entry))
                        }
                        else -> Unit
                    }
                }
                if (!hasDatabase) {
                    return ReadResult.InvalidArchive
                }
                ReadResult.Ready(
                    manifest = manifest,
                    prefs = prefs,
                    extractedDataDir = extractTo,
                )
            }
        } catch (_: Exception) {
            ReadResult.InvalidArchive
        }
    }

    private fun writeExtractedFile(extractTo: File, dest: File, bytes: ByteArray) {
        val root = extractTo.canonicalFile
        val canonical = dest.canonicalFile
        if (!canonical.path.startsWith(root.path + File.separator) && canonical != root) {
            error("Zip entry escapes extract directory")
        }
        dest.parentFile?.mkdirs()
        dest.writeBytes(bytes)
    }

    private fun ZipOutputStream.writeJson(name: String, jsonText: String) {
        putNextEntry(ZipEntry(name))
        write(jsonText.encodeToByteArray())
        closeEntry()
    }

    private fun ZipOutputStream.writeFile(name: String, file: File) {
        putNextEntry(ZipEntry(name))
        file.inputStream().buffered().use { input ->
            input.copyTo(this)
        }
        closeEntry()
    }

    private fun ZipOutputStream.writeTree(prefix: String, dir: File) {
        if (!dir.isDirectory) {
            return
        }
        dir.walkTopDown()
            .filter { it.isFile }
            .forEach { file ->
                val relative = file.relativeTo(dir).invariantSeparatorsPath
                writeFile("$prefix$relative", file)
            }
    }

    private fun ZipFile.readEntry(name: String): ByteArray? {
        val entry = getEntry(name) ?: return null
        return readBytes(entry)
    }

    private fun ZipFile.readBytes(entry: ZipEntry): ByteArray {
        return getInputStream(entry).use { it.readBytes() }
    }

    private companion object {
        const val MANIFEST_ENTRY = "manifest.json"
        const val PREFS_ENTRY = "prefs.json"
        const val DATA_PREFIX = "data/"
        const val DATABASE_ENTRY = "${DATA_PREFIX}ww.db"
        const val AVATARS_PREFIX = "${DATA_PREFIX}avatars/"
        const val MAPS_PREFIX = "${DATA_PREFIX}maps/"
        const val WORLD_MAPS_PREFIX = "${DATA_PREFIX}world_maps/"
        const val VOICES_PREFIX = "${DATA_PREFIX}voices/"
        const val SRD_PREFIX = "${DATA_PREFIX}srd/"

        val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }
    }
}
