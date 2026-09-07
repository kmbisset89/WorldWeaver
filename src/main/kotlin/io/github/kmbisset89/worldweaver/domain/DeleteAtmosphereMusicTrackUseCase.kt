package io.github.kmbisset89.worldweaver.domain

internal class DeleteAtmosphereMusicTrackUseCase(
    private val store: AtmosphereSettingsStore,
) {
    sealed interface Result {
        data object Deleted : Result
        data object NotFound : Result
    }

    operator fun invoke(trackId: String): Result {
        val settings = store.settings.value
        if (settings.musicTracks.none { it.id == trackId }) {
            return Result.NotFound
        }
        store.setMusicTracks(settings.musicTracks.filterNot { it.id == trackId })
        val cleared = settings.scenes.map { scene ->
            if (scene.musicTrackId == trackId) {
                scene.copy(musicTrackId = "")
            } else {
                scene
            }
        }
        if (cleared != settings.scenes) {
            store.setScenes(cleared)
        }
        return Result.Deleted
    }
}
