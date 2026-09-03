package io.github.kmbisset89.worldweaver.domain

internal class ClearPersonAvatarUseCase(
    private val avatarFileStore: PersonAvatarFileStore,
    private val worldPersonRepository: WorldPersonRepository,
    private val campaignPersonRepository: CampaignPersonRepository,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data object Cleared : Result
        data object NotFound : Result
    }

    suspend operator fun invoke(ref: PersonRef): Result {
        val now = instantProvider.now()
        when (ref) {
            is PersonRef.World -> {
                val person = worldPersonRepository.getById(ref.id) ?: return Result.NotFound
                avatarFileStore.delete(ref)
                worldPersonRepository.update(person.copy(updatedAt = now))
            }
            is PersonRef.Campaign -> {
                val person = campaignPersonRepository.getById(ref.id) ?: return Result.NotFound
                avatarFileStore.delete(ref)
                campaignPersonRepository.update(person.copy(updatedAt = now))
            }
        }
        return Result.Cleared
    }
}
