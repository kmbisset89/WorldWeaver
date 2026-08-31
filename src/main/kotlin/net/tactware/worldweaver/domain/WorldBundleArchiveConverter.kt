package net.tactware.worldweaver.domain

import kotlinx.serialization.json.Json
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

internal class WorldBundleArchiveConverter {
    sealed interface ReadResult {
        data class Ready(val bundle: WorldBundle) : ReadResult
        data object UnsupportedVersion : ReadResult
        data object InvalidArchive : ReadResult
    }

    fun write(bundle: WorldBundle, destFile: File) {
        destFile.parentFile?.mkdirs()
        ZipOutputStream(destFile.outputStream().buffered()).use { zip ->
            zip.putNextEntry(ZipEntry(MANIFEST_ENTRY))
            zip.write(json.encodeToString(WorldBundle.Manifest.serializer(), bundle.toManifest()).encodeToByteArray())
            zip.closeEntry()
            zip.putNextEntry(ZipEntry(PAYLOAD_ENTRY))
            zip.write(json.encodeToString(WorldBundle.Payload.serializer(), bundle.toPayload()).encodeToByteArray())
            zip.closeEntry()
            bundle.avatarFiles.forEach { file ->
                zip.putNextEntry(ZipEntry(avatarEntryPath(file.ref)))
                zip.write(file.png)
                zip.closeEntry()
            }
            bundle.mapFiles.forEach { file ->
                zip.putNextEntry(ZipEntry(mapEntryPath(file.battleMapId, file.relativePath)))
                zip.write(file.bytes)
                zip.closeEntry()
            }
            bundle.voiceFiles.forEach { file ->
                zip.putNextEntry(ZipEntry(voiceEntryPath(file.ref)))
                zip.write(file.wav)
                zip.closeEntry()
            }
        }
    }

    fun read(sourceFile: File): ReadResult {
        if (!sourceFile.isFile) {
            return ReadResult.InvalidArchive
        }
        return try {
            ZipFile(sourceFile).use { zip ->
                val manifestBytes = zip.readEntry(MANIFEST_ENTRY) ?: return ReadResult.InvalidArchive
                val payloadBytes = zip.readEntry(PAYLOAD_ENTRY) ?: return ReadResult.InvalidArchive
                val manifest = json.decodeFromString(WorldBundle.Manifest.serializer(), manifestBytes.decodeToString())
                if (manifest.formatVersion != WorldBundle.FORMAT_VERSION) {
                    return ReadResult.UnsupportedVersion
                }
                val payload = json.decodeFromString(WorldBundle.Payload.serializer(), payloadBytes.decodeToString())
                val avatarFiles = mutableListOf<WorldBundle.AvatarFile>()
                val mapFiles = mutableListOf<WorldBundle.MapFile>()
                val voiceFiles = mutableListOf<WorldBundle.VoiceFile>()
                zip.entries().asSequence().forEach { entry ->
                    if (entry.isDirectory) {
                        return@forEach
                    }
                    when {
                        entry.name.startsWith(AVATARS_PREFIX) -> {
                            val ref = personRefFromAvatarPath(entry.name) ?: return@forEach
                            avatarFiles += WorldBundle.AvatarFile(ref = ref, png = zip.readBytes(entry))
                        }
                        entry.name.startsWith(MAPS_PREFIX) -> {
                            val remainder = entry.name.removePrefix(MAPS_PREFIX)
                            val slash = remainder.indexOf('/')
                            if (slash <= 0) {
                                return@forEach
                            }
                            mapFiles += WorldBundle.MapFile(
                                battleMapId = remainder.substring(0, slash),
                                relativePath = remainder.substring(slash + 1),
                                bytes = zip.readBytes(entry),
                            )
                        }
                        entry.name.startsWith(VOICES_PREFIX) -> {
                            val ref = voiceRefFromPath(entry.name) ?: return@forEach
                            voiceFiles += WorldBundle.VoiceFile(ref = ref, wav = zip.readBytes(entry))
                        }
                    }
                }
                ReadResult.Ready(
                    WorldBundle.fromRecords(
                        manifest = manifest,
                        payload = payload,
                        avatarFiles = avatarFiles,
                        mapFiles = mapFiles,
                        voiceFiles = voiceFiles,
                    )
                )
            }
        } catch (_: Exception) {
            ReadResult.InvalidArchive
        }
    }

    private fun ZipFile.readEntry(name: String): ByteArray? {
        val entry = getEntry(name) ?: return null
        return readBytes(entry)
    }

    private fun ZipFile.readBytes(entry: ZipEntry): ByteArray {
        return getInputStream(entry).use { it.readBytes() }
    }

    private fun avatarEntryPath(ref: PersonRef): String {
        val folder = when (ref) {
            is PersonRef.World -> "world"
            is PersonRef.Campaign -> "campaign"
        }
        return "$AVATARS_PREFIX$folder/${ref.id}.png"
    }

    private fun mapEntryPath(battleMapId: String, relativePath: String): String {
        return "$MAPS_PREFIX$battleMapId/$relativePath"
    }

    private fun voiceEntryPath(ref: VoiceClipRef): String {
        val folder = when (ref) {
            is VoiceClipRef.WorldPerson -> "world-person"
            is VoiceClipRef.CampaignPerson -> "campaign-person"
            is VoiceClipRef.Location -> "location"
        }
        return "$VOICES_PREFIX$folder/${ref.id}.wav"
    }

    private fun voiceRefFromPath(path: String): VoiceClipRef? {
        val remainder = path.removePrefix(VOICES_PREFIX)
        val parts = remainder.split('/')
        if (parts.size != 2 || !parts[1].endsWith(".wav")) {
            return null
        }
        val id = parts[1].removeSuffix(".wav")
        return when (parts[0]) {
            "world-person" -> VoiceClipRef.WorldPerson(id)
            "campaign-person" -> VoiceClipRef.CampaignPerson(id)
            "location" -> VoiceClipRef.Location(id)
            else -> null
        }
    }

    private fun personRefFromAvatarPath(path: String): PersonRef? {
        val remainder = path.removePrefix(AVATARS_PREFIX)
        val parts = remainder.split('/')
        if (parts.size != 2 || !parts[1].endsWith(".png")) {
            return null
        }
        val id = parts[1].removeSuffix(".png")
        return when (parts[0]) {
            "world" -> PersonRef.World(id)
            "campaign" -> PersonRef.Campaign(id)
            else -> null
        }
    }

    private companion object {
        const val MANIFEST_ENTRY = "manifest.json"
        const val PAYLOAD_ENTRY = "bundle.json"
        const val AVATARS_PREFIX = "avatars/"
        const val MAPS_PREFIX = "maps/"
        const val VOICES_PREFIX = "voices/"

        val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
        }
    }
}
