package io.github.kmbisset89.worldweaver.domain

import java.io.File

internal class CreateAtmosphereMusicTrackUseCase(
    private val store: AtmosphereSettingsStore,
    private val entityIdFactory: EntityIdFactory,
) {
    sealed interface Result {
        data class Created(val track: AtmosphereMusicTrack) : Result
        data object InvalidPath : Result
        data object UnsupportedFormat : Result
        data object DuplicatePath : Result
    }

    operator fun invoke(path: String): Result {
        val trimmed = path.trim()
        if (trimmed.isEmpty()) {
            return Result.InvalidPath
        }
        val file = File(trimmed)
        if (!file.isFile) {
            return Result.InvalidPath
        }
        if (!AtmosphereMusicTrack.isSupported(file.path)) {
            return Result.UnsupportedFormat
        }
        val canonical = try {
            file.canonicalFile.absolutePath
        } catch (_: Exception) {
            return Result.InvalidPath
        }
        val existing = store.settings.value.musicTracks
        val duplicate = existing.any { track ->
            try {
                File(track.path).canonicalFile.absolutePath == canonical
            } catch (_: Exception) {
                track.path == canonical
            }
        }
        if (duplicate) {
            return Result.DuplicatePath
        }
        val track = AtmosphereMusicTrack(
            id = entityIdFactory.create(),
            displayName = AtmosphereMusicTrack.displayNameFrom(file.name),
            path = canonical,
            sortOrder = (existing.maxOfOrNull { it.sortOrder } ?: -1) + 1,
        )
        store.setMusicTracks(existing + track)
        return Result.Created(track)
    }
}
