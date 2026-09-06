package io.github.kmbisset89.worldweaver.domain

internal class DeleteAtmosphereMoodUseCase(
    private val store: AtmosphereSettingsStore,
) {
    sealed interface Result {
        data object Deleted : Result
        data object NotFound : Result
    }

    operator fun invoke(moodId: String): Result {
        val existing = store.settings.value.moods
        if (existing.none { it.id == moodId }) {
            return Result.NotFound
        }
        store.setMoods(existing.filterNot { it.id == moodId })
        return Result.Deleted
    }
}
