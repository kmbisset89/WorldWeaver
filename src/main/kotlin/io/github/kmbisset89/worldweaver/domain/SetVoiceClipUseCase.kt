package io.github.kmbisset89.worldweaver.domain

internal class SetVoiceClipUseCase(
    private val voiceClipFileStore: VoiceClipFileStore,
    private val worldPersonRepository: WorldPersonRepository,
    private val campaignPersonRepository: CampaignPersonRepository,
    private val locationRepository: LocationRepository,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data object Saved : Result
        data object NotFound : Result
        data object InvalidAudio : Result
    }

    suspend operator fun invoke(ref: VoiceClipRef, wavBytes: ByteArray): Result {
        if (!VoiceClipWavFormat.isValid(wavBytes)) {
            return Result.InvalidAudio
        }
        val now = instantProvider.now()
        when (ref) {
            is VoiceClipRef.WorldPerson -> {
                val person = worldPersonRepository.getById(ref.id) ?: return Result.NotFound
                voiceClipFileStore.write(ref, wavBytes)
                worldPersonRepository.update(person.copy(updatedAt = now))
            }
            is VoiceClipRef.CampaignPerson -> {
                val person = campaignPersonRepository.getById(ref.id) ?: return Result.NotFound
                voiceClipFileStore.write(ref, wavBytes)
                campaignPersonRepository.update(person.copy(updatedAt = now))
            }
            is VoiceClipRef.Location -> {
                val location = locationRepository.getById(ref.id) ?: return Result.NotFound
                voiceClipFileStore.write(ref, wavBytes)
                locationRepository.update(location.copy(updatedAt = now))
            }
        }
        return Result.Saved
    }
}
