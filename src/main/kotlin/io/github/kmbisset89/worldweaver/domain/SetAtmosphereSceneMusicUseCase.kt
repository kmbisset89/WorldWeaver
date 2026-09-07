package io.github.kmbisset89.worldweaver.domain

internal class SetAtmosphereSceneMusicUseCase(
    private val store: AtmosphereSettingsStore,
) {
    sealed interface Result {
        data object Updated : Result
        data object SceneNotFound : Result
        data object TrackNotFound : Result
    }

    operator fun invoke(sceneId: String, trackId: String?): Result {
        val settings = store.settings.value
        val scene = settings.scenes.firstOrNull { it.id == sceneId } ?: return Result.SceneNotFound
        val trimmed = trackId?.trim().orEmpty()
        if (trimmed.isNotEmpty() && settings.musicTracks.none { it.id == trimmed }) {
            return Result.TrackNotFound
        }
        store.setScenes(
            settings.scenes.map { current ->
                if (current.id == sceneId) {
                    scene.copy(musicTrackId = trimmed)
                } else {
                    current
                }
            },
        )
        return Result.Updated
    }
}
