package io.github.kmbisset89.worldweaver.domain

internal class DeleteSessionRecordingUseCase(
    private val sessionRecordingFileStore: SessionRecordingFileStore,
) {
    sealed interface Result {
        data object Deleted : Result
        data object NotFound : Result
    }

    operator fun invoke(sessionId: String, recordingId: String): Result {
        if (!sessionRecordingFileStore.delete(sessionId, recordingId)) {
            return Result.NotFound
        }
        return Result.Deleted
    }
}
