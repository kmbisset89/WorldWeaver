package io.github.kmbisset89.worldweaver.domain

internal class DeleteAtmosphereSceneUseCase(
    private val store: AtmosphereSettingsStore,
) {
    sealed interface Result {
        data object Deleted : Result
        data object NotFound : Result
    }

    operator fun invoke(sceneId: String): Result {
        val existing = store.settings.value.scenes
        if (existing.none { it.id == sceneId }) {
            return Result.NotFound
        }
        store.setScenes(existing.filterNot { it.id == sceneId })
        return Result.Deleted
    }
}
