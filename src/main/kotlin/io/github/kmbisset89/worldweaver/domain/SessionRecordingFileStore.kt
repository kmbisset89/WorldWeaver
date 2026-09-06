package io.github.kmbisset89.worldweaver.domain

import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

internal class SessionRecordingFileStore(
    private val recordingsRoot: File,
) {
    fun createFile(sessionId: String, kind: SessionRecordingKind, startedAt: Instant): File {
        val folder = folderFor(sessionId)
        folder.mkdirs()
        val stamp = TIMESTAMP.format(startedAt)
        val suffix = fileSuffix(kind)
        var file = File(folder, "${stamp}_$suffix")
        var attempt = 1
        while (file.exists()) {
            file = File(folder, "${stamp}_${attempt}_$suffix")
            attempt += 1
        }
        return file
    }

    fun list(sessionId: String): List<SessionRecording> {
        val folder = folderFor(sessionId)
        val files = folder.listFiles() ?: return emptyList()
        return files.mapNotNull { file -> recordingFrom(sessionId, file) }
            .sortedByDescending { recording -> recording.startedAt }
    }

    fun delete(sessionId: String, recordingId: String): Boolean {
        val file = File(folderFor(sessionId), recordingId)
        if (!file.isFile) {
            return false
        }
        return file.delete()
    }

    fun deleteAll(sessionId: String) {
        val folder = folderFor(sessionId)
        if (folder.isDirectory) {
            folder.deleteRecursively()
        }
    }

    private fun recordingFrom(sessionId: String, file: File): SessionRecording? {
        if (!file.isFile) {
            return null
        }
        val match = FILE_NAME.matchEntire(file.name) ?: return null
        val startedAt = runCatching {
            Instant.from(TIMESTAMP.parse(match.groupValues[1]))
        }.getOrNull() ?: return null
        val kind = when (match.groupValues[3]) {
            "audio" -> SessionRecordingKind.Audio
            "video" -> SessionRecordingKind.Video
            else -> return null
        }
        return SessionRecording(
            id = file.name,
            sessionId = sessionId,
            kind = kind,
            path = file.absolutePath,
            startedAt = startedAt,
            byteSize = file.length(),
        )
    }

    private fun folderFor(sessionId: String): File {
        return File(recordingsRoot, sessionId)
    }

    private fun fileSuffix(kind: SessionRecordingKind): String {
        return when (kind) {
            SessionRecordingKind.Audio -> "audio.wav"
            SessionRecordingKind.Video -> "video.mp4"
        }
    }

    private companion object {
        val TIMESTAMP: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC)
        val FILE_NAME = Regex("""^(\d{8}T\d{6}Z)(?:_(\d+))?_(audio|video)\.(wav|mp4)$""")
    }
}
