package net.tactware.worldweaver.domain

internal class DeleteWorldUseCase(
    private val worldRepository: WorldRepository,
    private val campaignRepository: CampaignRepository,
    private val activeContextRepository: ActiveContextRepository,
) {
    sealed interface Result {
        data object Deleted : Result
        data class Blocked(val campaignCount: Int) : Result
    }

    suspend operator fun invoke(worldId: String): Result {
        val campaignCount = campaignRepository.countByWorld(worldId)
        if (campaignCount > 0) {
            return Result.Blocked(campaignCount)
        }
        worldRepository.delete(worldId)
        val context = activeContextRepository.get()
        if (context.activeWorldId == worldId) {
            activeContextRepository.setActiveWorldId(null)
            activeContextRepository.setActiveCampaignId(null)
        }
        return Result.Deleted
    }
}
