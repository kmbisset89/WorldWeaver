package net.tactware.worldweaver.domain

internal class ClearVoiceClipUseCase(
    private val voiceClipFileStore: VoiceClipFileStore,
    private val worldPersonRepository: WorldPersonRepository,
    private val campaignPersonRepository: CampaignPersonRepository,
    private val locationRepository: LocationRepository,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data object Cleared : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(ref: VoiceClipRef): Result {
        val now = instantProvider.now()
        when (ref) {
            is VoiceClipRef.WorldPerson -> {
                val person = worldPersonRepository.getById(ref.id) ?: return Result.NotFound
                voiceClipFileStore.delete(ref)
                worldPersonRepository.update(person.copy(updatedAt = now))
            }
            is VoiceClipRef.CampaignPerson -> {
                val person = campaignPersonRepository.getById(ref.id) ?: return Result.NotFound
                voiceClipFileStore.delete(ref)
                campaignPersonRepository.update(person.copy(updatedAt = now))
            }
            is VoiceClipRef.Location -> {
                val location = locationRepository.getById(ref.id) ?: return Result.NotFound
                voiceClipFileStore.delete(ref)
                locationRepository.update(location.copy(updatedAt = now))
            }
        }
        return Result.Cleared
    }
}
