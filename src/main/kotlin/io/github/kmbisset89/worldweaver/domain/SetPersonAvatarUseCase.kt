package io.github.kmbisset89.worldweaver.domain

import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

internal class SetPersonAvatarUseCase(
    private val avatarFileStore: PersonAvatarFileStore,
    private val worldPersonRepository: WorldPersonRepository,
    private val campaignPersonRepository: CampaignPersonRepository,
    private val instantProvider: InstantProvider,
) {
    sealed interface Result {
        data object Saved : Result
        data object NotFound : Result
        data object InvalidImage : Result
    }

    suspend operator fun invoke(ref: PersonRef, imageBytes: ByteArray): Result {
        val readable = try {
            ImageIO.read(ByteArrayInputStream(imageBytes))
        } catch (_: Exception) {
            null
        }
        if (readable == null || readable.width <= 0 || readable.height <= 0) {
            return Result.InvalidImage
        }
        val now = instantProvider.now()
        when (ref) {
            is PersonRef.World -> {
                val person = worldPersonRepository.getById(ref.id) ?: return Result.NotFound
                avatarFileStore.write(ref, imageBytes)
                worldPersonRepository.update(person.copy(updatedAt = now))
            }
            is PersonRef.Campaign -> {
                val person = campaignPersonRepository.getById(ref.id) ?: return Result.NotFound
                avatarFileStore.write(ref, imageBytes)
                campaignPersonRepository.update(person.copy(updatedAt = now))
            }
        }
        return Result.Saved
    }
}
